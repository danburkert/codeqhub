(ns codeqhub.models.commit
  (:require [datomic.api :as d]
            [codeqhub.database :as db]
            [codeqhub.models.repository :as repo]
            [codeqhub.models.codeq :as codeq]
            [codeqhub.models.util :as util])
  (:refer-clojure :exclude [ancestors]))

(defn sha
  "Return the commit sha."
  [commit] (:git/sha commit))

(defn message
  "Return the commit message."
  [commit] (:commit/message commit))

(defn committer
  "Return the email address of the committer."
  [commit] (:email/address (:commit/committer commit)))

(defn commit-date
  "return the commit date."
  [commit] (:commit/committedat commit))

(defn author
  "Return the email address of the author."
  [commit] (:email/address (:commit/author commit)))

(defn authored-date
  "return the authored date."
  [commit] (:commit/authoredAt commit))

(defn codeqs
  "Return sequence of the codeqs contained in the commit and namespace
   (if provided).  Each returned codeq contains commit metadata."
  ([commit]
   (let [db (d/entity-db commit)
         commit-id (:db/id commit)]
     (-> (util/qes '[:find ?cq
                     :in $ % ?cm
                     :where (commit-codeqs ?cm ?cq)]
                   db util/rules commit-id)
         (with-meta {:commit commit}))))
  ([commit namespace]
   (let [codeqs (codeqs commit)]
     (filter #(= (codeq/namespace %) namespace) codeqs))))

(defn codeq
  "Return the codeq with matching codename in the specified commit.  The
   returned codeq has commit metadata."
  [commit codename]
  (let [db (d/entity-db commit)
        commit-id (:db/id commit)]
    (-> (util/qes '[:find ?cq
                    :in $ % ?cm ?codename
                    :where
                    [?cq :clj/def ?def]
                    [?def :code/name ?codename]
                    (commit-codeqs ?cm ?cq)]
                  db util/rules commit-id codename)
        first
        (with-meta {:commit commit}))))

(defn namespaces
  "Return a map of namespaces to codeqs in the commit, the returned codeqs have
   commit metadata."
  [commit]
  (dissoc (group-by codeq/namespace (sort-by codeq/location (codeqs commit)))
          nil))

(defn ancestors
  "Return a sequence of the ancestors of this commit in reverse commit order."
  [commit]
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
