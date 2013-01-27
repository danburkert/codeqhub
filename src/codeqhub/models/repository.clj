(ns codeqhub.models.repository
  (:require [datomic.api :as d]
            [codeqhub.models.ref :as ref]
            [codeqhub.models.util :as util])
  (:refer-clojure :exclude [name ref]))

(defn repos
  "Return sequence of all repositories belonging to user, or all repositories
   if user is not specified."
  ([db] (util/find-all-by db :repo/uri))
  ([db user]
   (->> (d/index-range db :repo/uri (util/user->uri user) nil)
        (take-while (comp (partial = user)
                          (comp util/uri->user :v)))
        (map (comp (partial d/entity db) :e)))))

(defn repo [db user name]
  (->> (util/user-repo->uri user name)
       (util/index-get-id db :repo/uri)
       (d/entity db)))

(defn refs [repo]
  "Return set of all refs in repository."
  (:repo/refs repo))

(defn ref [repo label]
  "Return the ref with label in repository."
  (first (filter (comp (partial = label) :ref/label) (refs repo))))

(defn branches [repo]
  "Return the set of branches in the repository."
  (filter (comp (partial = :branch) :git/type) (:repo/refs repo)))

(defn branch [repo label]
  "Return the branch in the repository with label, or nil if it does not exist."
  (first (filter (comp (partial = label) ref/label) (branches repo))))

(defn tags [repo]
  "Return the set of tags in the repository."
  (filter (comp (partial = :tag) :git/type) (:repo/refs repo)))

(defn tag [repo label]
  "Return the tag in the repository with label, or nil if it does not exist."
  (first (filter (comp (partial = label) ref/label) (tags repo))))

(defn default-branch [repo]
  "Return the default branch of the repository."
  (:repo/defaultBranch repo))

(defn commits [repo]
  "Return set of all commits in repository."
  (:repo/commits repo))

(defn commit [repo sha]
  "Return commit in repository identified by sha.  sha is case insensitive,
   and may match a prefix of the actual sha, as long as it is a unique prefix."
  (let [matches
        (filter (comp (partial re-matches (re-pattern (str "(?i)^" sha ".*$"))) :git/sha)
                (commits repo))]
    (if (> (count matches) 1)
      nil
      (first matches))))

(defn stars [repo]
  "Return number of stars in repository."
  (:repo/stars repo))

(defn forks [repo]
  "Return number of forks in repository."
  (:repo/forks repo))

(defn parent [repo]
  "Return the parent repository of repo, i.e. the repository that repo is
   forked from."
  (:repo/parent repo))

(defn name [repo]
  "Return the name of the repository."
  (util/uri->repo (:repo/uri repo)))

(defn user [repo]
  "Return the repository owner's Github username."
  (util/uri->user (:repo/uri repo)))

(defn uri [repo]
  "Return the URI of the repository."
  (:repo/uri repo))
