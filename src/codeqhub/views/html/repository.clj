(ns codeqhub.views.html.repository
  (:require [codeqhub.models.repository :as repo]))

(defn- user-url [repo]
  (str "/" (repo/user repo)))

(defn- repo-url [repo]
  (str "/" (repo/user repo)
       "/" (repo/name repo)))

(defn repo-list
  [repos]
  [:ul
   (for [repo repos]
     [:li
      [:span
       [:a {:href (repo/github-url repo)} "github"]]
      [:span
       [:a {:href (user-url repo)} (repo/user repo)]
       "/"
       [:a {:href (repo-url repo)} (repo/name repo)]]])])
