(ns codeqhub.controllers.namespaces
  (:require [compojure.core :refer [defroutes GET]]))

(defn show-namespace [user name commit namespace]
 "Show namespace."
  nil)

(defn show-namespaces
  "Show repository namespaces.  Commit is either an identifying ref label or
   sha.  If commit is not specified then the default branch is used."
  ([user name] nil)
  ([user name commit] nil))

(defroutes routes
  (GET "/:user/:name/namespaces" [user name]
       (show-namespaces user name))
  (GET "/:user/:name/namespaces/:commit" [user name commit]
       (show-namespaces user name))
  (GET "/:user/:name/namespace/:commit/:namespace" [user name commit namespace]
       (show-namespace user name commit namespace)))
