(ns codeqhub.controllers.repositories
  (:require [compojure.core :refer [defroutes GET]]
            [codeqhub.database :refer [get-db]]
            [codeqhub.models.repository :as repo]
            [codeqhub.models.commit :as commit]
            [codeqhub.models.ref :as ref]
            [codeqhub.views.html.layout :refer [common]]
            [codeqhub.views.html.commit :as commit.view]
            [codeqhub.views.html.repository :as repo.view]))

(defn show-repository [user name]
  "Show repository."
  (let [db (get-db)
        repo (repo/repo db user name)
        branch (repo/default-branch repo)
        commit (ref/commit branch)
        namespaces (commit/namespaces commit)
        ]
    (common (str user "/" name " | codeqhub")
            (repo.view/repo repo
                            (commit.view/commit repo commit branch namespaces)))))

(defn show-repositories
  "Show all repositories, or if user is supplied, all repositories of the user."
  ([]
   (let [db (get-db)
         repos (repo/repos db)]
     (common "codeqhub" (repo.view/repos repos))))
  ([user]
   (let [db (get-db)
         repos (repo/repos db user)]
     (common user (repo.view/repos repos)))))

(defroutes routes
  (GET "/" [] (show-repositories))
  (GET "/:user" [user] (show-repositories user))
  (GET "/:user/:name" [user name] (show-repository user name)))
