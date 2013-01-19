(ns codeqhub.controllers.repositories
  (:require [codeqhub.views.html.layout :refer [common]]
            [codeqhub.views.html.repository :as repo.view]
            [codeqhub.views.html.commit :as commit.view]
            [codeqhub.controllers.commits :as commits.controller]
            [codeqhub.database :refer [get-db]]
            [codeqhub.models.repository :as repo]
            [codeqhub.models.ref :as ref]
            [codeqhub.models.commit :as commit]
            [compojure.core :refer [defroutes GET]]))

(defn show-repo [user name]
  (let [db (get-db)
        repo (repo/repo db user name)
        branch (repo/default-branch repo)
        commit (ref/commit branch)]
    (common "codeqhub"
            (commit.view/commit commit {:user user
                                        :name name
                                        :sha (commit/sha commit)
                                        :label (ref/label branch)}))))

(defn show-user-repos [user]
  (let [db (get-db)
        repos (repo/repos db user)]
    (common "codeqhub" (repo.view/repo-list repos))))

(defroutes repo-routes
  (GET "/:user" [user] (show-user-repos user))
  (GET "/:user/:name" [user name]
       (show-repo user name)))
