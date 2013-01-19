(ns codeqhub.handler
  (:require [compojure.handler :as handler]
            [compojure.core :refer [routes]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [codeqhub.controllers.home :refer [home-routes]]
            [codeqhub.controllers.repositories :refer [repo-routes]]))

(def ^:dynamic *url*)

(def app
  (handler/site (-> (routes home-routes
                            repo-routes)
                  wrap-reload
                  wrap-stacktrace)))
