(ns codeqhub.handler
  (:require [compojure.handler :as handler]
            [compojure.core :refer [routes]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [codeqhub.controllers.codeqs :as codeqs.cont]
            [codeqhub.controllers.namespaces :as namespaces.cont]
            [codeqhub.controllers.repositories :as repository.cont]))

(def app
  (handler/site (-> (routes codeqs.cont/routes
                            namespaces.cont/routes
                            repository.cont/routes)
                    wrap-reload
                    wrap-stacktrace)))
