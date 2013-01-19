(ns codeqhub.controllers.repositories
  (:require [codeqhub.models.repository :as repo]
            [codeqhub.database :refer [get-db]]
            [compojure.core :refer [defroutes GET]]))

(defn show-repository []
  "Show repository."
  nil)

(defn show-repositories
  "Show all repositories, or if user is supplied, all repositories of the user."
  ([] nil)
  ([user] nil))

(defroutes routes
  (GET "/" [] (show-repositories))
  (GET "/:user" [user] (show-repositories user))
  (GET "/:user/:name" [user name] (show-repository user name)))
