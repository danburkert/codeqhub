(ns codeqhub.views.edn.repository
  (:require [codeqhub.models.repository :as repo]
            [codeqhub.models.ref :as ref]))

(defn repo
  "Format repo into map suitable for sending to client as edn."
  [repo]
  (merge
    {:name (repo/name repo)
     :user (repo/name repo)
     :stars (repo/stars repo)
     :forks (repo/forks repo)
     :uri (repo/github-url repo)
     :default-branch (ref/label (repo/default-branch repo))
     :branches (mapv
                 (comp (partial zipmap [:label :commit])
                       (juxt :ref/label (comp :git/sha :ref/commit)))
                 (repo/branches repo))}
    (if-let [parent (repo/parent repo)]
      {:parent parent})))
