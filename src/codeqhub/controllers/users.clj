(ns codeqhub.controllers.users
  (:require [codeqhub.models.repository :as repo]
            [codeqhub.database :refer [get-db]]
            [codeqhub.views.html.layout :refer [common]]
            [codeqhub.views.html.repository :as repo-view]
            [compojure.core :refer [defroutes GET]]))

(defn home []
  (let [db (get-db)
        repos (repo/repos db)]
    (common "codeqhub" (repo-view/repo-list repos))))

(defroutes home-routes
  (GET "/" [] (home)))
