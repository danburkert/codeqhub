(ns codeqhub.views.html.repository
  (:require [codeqhub.models.repository :as repo]
            [codeqhub.models.commit :as commit]
            [codeqhub.helpers.url :as url]
            [codeqhub.views.html.namespace :as namespace.view]
            [codeqhub.views.html.commit :as commit.view]
            [codeqhub.views.html.ref :as ref.view]))

(defn repo [repo & body]
  "Return the common repository layout."
  [[:h2 (str (repo/user repo) "/" (repo/name repo))]
   body])


(defn repos [repos]
  "Return HTML view of repo list."
  [:ul
   (for [repo repos]
     [:li
      [:span
       [:a {:href (url/github repo)} "github"]]
      [:span
       [:a {:href (url/user repo)} (repo/user repo)]
       "/"
       [:a {:href (url/repo repo)} (repo/name repo)]]])])
