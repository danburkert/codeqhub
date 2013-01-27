(ns codeqhub.views.html.namespace
  (:require [codeqhub.models.codeq :as codeq]
            [codeqhub.helpers.url :as url]
            [codeqhub.views.html.codeq :as codeq.view])
  (:refer-clojure :exclude [namespace]))

(defn namespace [repo commit ref namespace codeqs]
  "Return HTML view of namespace."
  (list
    [:h3 namespace]
    (codeq.view/codeqs repo commit ref codeqs)))

(defn namespaces
  "Return HTML view of namespaces list from repo."
  [repo commit ref namespaces]
  [:ul
   (for [[ns codeqs] namespaces]
     [:li [:a {:href (url/namespace repo commit ref ns)} ns]
      (codeq.view/codeqs repo commit ref codeqs)])])
