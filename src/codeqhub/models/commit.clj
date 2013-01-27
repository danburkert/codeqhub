(ns codeqhub.models.commit
  (:require [datomic.api :as d]
            [codeqhub.database :as db]
            [codeqhub.models.repository :as repo]
            [codeqhub.models.codeq :as codeq]
            [codeqhub.models.util :as util])
  (:refer-clojure :exclude [ancestors]))

(defn sha [commit]
  "Return the commit sha."
  (:git/sha commit))

(defn message [commit]
  "Return the commit message."
  (:commit/message commit))

(defn committer [commit]
  "Return the email address of the committer."
  (:email/address (:commit/committer commit)))

(defn commit-date [commit]
  "return the commit date."
  (:commit/committedat commit))

(defn author [commit]
  "Return the email address of the author."
  (:email/address (:commit/author commit)))

(defn authored-date [commit]
  "return the authored date."
  (:commit/authoredAt commit))

(defn codeqs
  "Return the codeqs contained in the commit and namespace (if provided)."
  ([commit]
   (let [db (d/entity-db commit)
         commit-id (:db/id commit)]
     (util/qes '[:find ?cq
                 :in $ % ?cm
                 :where (commit-codeqs ?cm ?cq)]
               db util/rules commit-id)))
  ([commit namespace]
   (let [codeqs (codeqs commit)]
     (filter #(= (codeq/namespace %) namespace) codeqs))))

(defn codeq [commit codename]
  "Return the codeq named codeqname in the commit."
  (let [db (d/entity-db commit)
        commit-id (:db/id commit)]
    (first
      (util/qes '[:find ?cq
                  :in $ % ?cm ?codename
                  :where
                  [?cq :clj/def ?def]
                  [?def :code/name ?codename]
                  (commit-codeqs ?cm ?cq)]
                db util/rules commit-id codename))))

(defn namespaces [commit]
  "Return a map of namespaces to codeqs in the commit."
  (dissoc (group-by codeq/namespace (sort-by codeq/location (codeqs commit)))
          nil))

(defn ancestors [commit]
  "Return a sequence of the ancestors of this commit in reverse commit order."
  (let  [db (d/entity-db commit)
         rules '[[(commit-ancestors ?c ?a) [?c :commit/parents ?a]]
                 [(commit-ancestors ?c ?a) [?c :commit/parents ?p]
                                            (commit-ancestors ?p ?a)]]]
    (sort-by :commit/committedAt #(compare %2 %1)
             (util/qes '[:find ?ancestors
                         :in $ % ?commit
                         :where
                         (commit-ancestors ?commit ?ancestors)]
                       db rules (:db/id commit)))))
