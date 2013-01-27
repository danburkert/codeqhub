(ns codeqhub.helpers.url
  (:require [codeqhub.models.repository :as repo]
            [codeqhub.models.commit :as commit]
            [codeqhub.models.ref :as ref]
            [codeqhub.models.codeq :as codeq])
  (:refer-clojure :exclude [namespace]))

(defn namespace [repo commit ref ns]
  "Return the relative codeqhub URL of the namespace."
  (str "/" (repo/user repo)
       "/" (repo/name repo)
       "/namespace"
       "/" (or (ref/label ref) (commit/sha commit))
       "/" ns))

(defn namespaces [repo commit ref]
  "Return the relative codeqhub URL of the namespaces at commit or ref"
  (str "/" (repo/user repo)
       "/" (repo/name repo)
       "/namespaces"
       "/" (or (ref/label ref) (commit/sha commit))))

(defn codeq [repo commit ref codeq]
  "Return the relative codeqhub URL of the codeq."
  (str "/" (repo/user repo)
       "/" (repo/name repo)
       "/codeq"
       "/" (or (ref/label ref) (commit/sha commit))
       "/" (codeq/name codeq)))

(defn repo [repo]
  "Return the relative codeqhub URL of the repository."
  (str "/" (repo/user repo) "/" (repo/name repo)))

(defn user [repo]
  "Return the relative codeqhub URL of the repository's owner."
  (str "/" (repo/user repo)))

(defn github [repo]
  "Return the repository's github URL."
  (str "https://" (repo/uri repo)))
