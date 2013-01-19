(ns codeqhub.views.html.repository
  (:require [codeqhub.models.repository :as repo]))

(defn repos [repos]
  "Return HTML view of repo list."
  [:ul
   (for [repo repos]
     [:li
      [:span
       [:a {:href repo} "github"]]
      [:span
       [:a {:href repo} repo]
       "/"
       [:a {:href repo} (repo/name repo)]]])])

(defn repo [repo]
  "Return HTML view of repo."
  nil)
