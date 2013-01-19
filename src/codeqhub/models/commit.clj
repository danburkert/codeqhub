(ns codeqhub.models.commit
  (:require [datomic.api :as d]
            [codeqhub.models.repository :as repo]
            [codeqhub.models.codeq :as codeq]
            [codeqhub.models.util :as util]))

(defn sha
  "Return the commit sha."
  [commit]
  (:git/sha commit))

(defn message
  "Return the commit message."
  [commit]
  (:commit/message commit))

(defn committer
  "Return the email address of the committer."
  [commit]
  (:email/address (:commit/committer commit)))

(defn commit-date
  "return the commit date."
  [commit]
  (:commit/committedat commit))

(defn author
  "Return the email address of the author."
  [commit]
  (:email/address (:commit/author commit)))

(defn authored-date
  "return the authored date."
  [commit]
  (:commit/authoredAt commit))

(defn codeqs
  "Return the codeqs contained in the commit."
  [commit]
  (let [db (d/entity-db commit)
        commit-id (:db/id commit)]
    (util/qes '[:find ?cq-id
                :in $ % ?cm-id
                :where (commit-codeqs ?cm-id ?cq-id)]
              db util/rules commit-id)))

(defn namespaces
  "Return a map of namespaces to codeqs in the commit."
  [commit]
  (dissoc (group-by codeq/namespace (sort-by codeq/location (codeqs commit)))
          nil))
