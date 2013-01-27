(ns codeqhub.controllers.namespaces
  (:require [compojure.core :refer [defroutes GET]]
            [codeqhub.database :refer [get-db]]
            [codeqhub.models.repository :as repo]
            [codeqhub.models.commit :as commit]
            [codeqhub.models.ref :as ref]
            [codeqhub.views.html.layout :refer [common]]
            [codeqhub.views.html.namespace :as namespace.view]))

(defn show-namespace [user name commit namespace]
  "Show namespace."
  (let [db (get-db)
        repo (repo/repo db user name)]
    (if-let [ref (repo/ref repo commit)]
      (let [commit (ref/commit ref)
            codeqs (commit/codeqs commit namespace)]
        (common "codeqhub" (namespace.view/namespace repo commit ref namespace codeqs)))
      (if-let [commit (repo/commit repo commit)]
        (let [codeqs (commit/codeqs commit namespace)]
          (common "codeqhub" (namespace.view/namespace repo commit nil namespace codeqs)))))))

(defn show-namespaces
  "Show repository namespaces.  Commit is either an identifying ref label or
   sha.  If commit is not specified then the default branch is used."
  ([user name]
   (let [db (get-db)
         repo (repo/repo db user name)
         branch (repo/default-branch repo)
         commit (ref/commit branch)
         namespaces (commit/namespaces commit)]
     (common "codeqhub" (namespace.view/namespaces repo commit branch namespaces))))
  ([user name commit]
   (let [db (get-db)
         repo (repo/repo db user name)]
     (if-let [ref (repo/ref repo commit)]
       (let [commit (ref/commit ref)
             namespaces (commit/namespaces commit)]
         (common "codeqhub" (namespace.view/namespaces repo commit ref namespaces)))
       (if-let [commit (repo/commit repo commit)]
         (let [namespaces (commit/namespaces commit)]
           (common "codeqhub" (namespace.view/namespaces repo commit nil namespaces))))))))

(defroutes routes
  (GET "/:user/:name/namespaces" [user name]
       (show-namespaces user name))
  (GET "/:user/:name/namespaces/:commit" [user name commit]
       (show-namespaces user name commit))
  (GET "/:user/:name/namespace/:commit/:namespace" [user name commit namespace]
       (show-namespace user name commit namespace)))
