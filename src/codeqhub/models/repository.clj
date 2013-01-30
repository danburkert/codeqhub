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

(defn repo
  "Return the repository in the database identified by the user and repo name."
  [db user name]
  (->> (util/user-repo->uri user name)
       (util/index-get-id db :repo/uri)
       (d/entity db)))

(defn refs
  "Return set of all refs in repository with repo metadata."
  [repo] (map #(with-meta % {:repo repo}) (:repo/refs repo)))

(defn ref
  "Return the ref with label in repository with repo metadata."
  [repo label] (first (filter (comp (partial = label) :ref/label) (refs repo))))

(defn branches
  "Return the set of branches in the repository with repo metadata."
  [repo] (filter (comp (partial = :branch) :git/type) (:repo/refs repo)))

(defn branch
  "Return the branch in the repository with label, or nil if it does not exist.
   The branch includes repo metadata."
  [repo label]
  (first (filter (comp (partial = label) ref/label) (branches repo))))

(defn tags
  "Return the set of tags in the repository with repo metadata."
  [repo] (filter (comp (partial = :tag) :git/type) (:repo/refs repo)))

(defn tag
  "Return the tag in the repository with label, or nil if it does not exist.
   The tag includes repo metadata."
  [repo label] (first (filter (comp (partial = label) ref/label) (tags repo))))

(defn default-branch
  "Return the default branch of the repository with repo metadata."
  [repo] (with-meta (:repo/defaultBranch repo) {:repo repo}))

(defn commits
  "Return sequence of all commits in repository with repo metadata."
  [repo] (map #(with-meta % {:repo repo}) (:repo/commits repo)))

(defn commit
  "Return commit in repository identified by sha with repo metadata.  sha is
   case insensitive, and may match a prefix of the actual sha, as long as it is
   a unique prefix."
  [repo sha]
  (let [matches
        (filter (comp (partial re-matches (re-pattern (str "(?i)^" sha ".*$")))
                      :git/sha)
                (commits repo))]
    (if (> (count matches) 1)
      nil
      (first matches))))

(defn stars
  "Return number of stars in repository."
  [repo] (:repo/stars repo))

(defn forks
  "Return number of forks in repository."
  [repo] (:repo/forks repo))

(defn parent
  "Return the parent repository of repo, i.e. the repository that repo is
   forked from."
  [repo] (:repo/parent repo))

(defn name
  "Return the name of the repository."
  [repo] (util/uri->repo (:repo/uri repo)))

(defn user
  "Return the repository owner's Github username."
  [repo] (util/uri->user (:repo/uri repo)))

(defn uri
  "Return the URI of the repository."
  [repo] (:repo/uri repo))
