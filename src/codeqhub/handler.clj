(ns codeqhub.handler
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer [routes defroutes]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [hiccup.page :as page]
            [codeqhub.controllers.codeqs :as codeqs.cont]
            [codeqhub.controllers.namespaces :as namespaces.cont]
            [codeqhub.controllers.repositories :as repository.cont]
            [codeqhub.views.html.layout :as layout]))

(defroutes resources
  (route/resources "/"))

(defroutes four-oh-four
  (route/not-found (layout/four-oh-four)))

(def app
  (handler/site (-> (routes resources
                            codeqs.cont/routes
                            namespaces.cont/routes
                            repository.cont/routes
                            four-oh-four)
                    wrap-reload
                    wrap-stacktrace)))
