(ns codeqhub.views.html.commit
  (:require [codeqhub.models.repository :as repo]
            [codeqhub.models.commit :as commit]
            [codeqhub.models.codeq :as codeq]))

(defn- namespace-url [ns & [{:keys [user name label sha]}]]
  {:pre [(string? ns) (string? user) (string? name)
         (or (string? label) (string? sha))]}
  (str "/" user
       "/" name
       "/namespace"
       "/" (or label sha)
       "/" ns))

(defn- codeq-url [codeq ns & [{:keys [user name label sha]}]]
  {:pre [ (string? ns) (string? user) (string? name)
         (or (string? label) (string? sha))]}
  (str "/" user
       "/" name
       "/namespace"
       "/" (or label sha)
       "/" ns
       "/" codeq))

(defn namespace-list
  [namespaces & [url]]
  [:ul
   (for [[ns codeqs] namespaces]
     [:li [:a {:href (namespace-url ns url)} ns]
      [:ol
       (for [codeq codeqs]
         (let [identifier (codeq/identifier codeq)]
         [:li
          [:a {:href (codeq-url identifier ns url)}
           identifier]]))]])])

(defn commit
  [commit & [url]]
  [:h2 "namespaces"
   (namespace-list (commit/namespaces commit) url)])
