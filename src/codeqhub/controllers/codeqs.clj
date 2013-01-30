(ns codeqhub.controllers.codeqs
  (:require [compojure.core :refer [defroutes GET]]
            [codeqhub.database :refer [get-db]]
            [codeqhub.models.repository :as repo]
            [codeqhub.models.commit :as commit]
            [codeqhub.models.ref :as ref]
            [codeqhub.views.html.layout :refer [common]]
            [codeqhub.views.html.codeq :as codeq.view]
            [codeqhub.views.html.namespace :as namespace.view]))

(defn show-codeq [user name commit namespace codename]
  "Show codeq."
  (let [db (get-db)
        repo (repo/repo db user name)]
    (if-let [ref (repo/ref repo commit)]
      (let [commit (ref/commit ref)
            codeq (commit/codeq commit (str namespace "/" codename))]
        (common "codeqhub" (codeq.view/codeq repo commit ref codeq)))
      (if-let [commit (repo/commit repo commit)]
        (let [codeq (commit/codeq commit (str namespace "/" codename))]
          (common "codeqhub" (codeq.view/codeq repo commit nil codeq)))))))

(defroutes routes
  (GET "/:user/:name/codeq/:commit/:namespace/:codename"
       [user name commit namespace codename]
       (show-codeq user name commit namespace codename)))
