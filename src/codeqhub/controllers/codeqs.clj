(ns codeqhub.controllers.codeqs
  (:require [compojure.core :refer [defroutes GET]]))

(defn show-codeq [user name commit namespace codename]
  "Show codeq."
  nil)

(defroutes routes
  (GET "/:user/:name/:commit/:namespace/:codename"
       [user name commit namespace codename]
       (show-codeq user name commit namespace codename)))
