(ns codeqhub.views.html.repository
  (:require [codeqhub.models.repository :as repo]
            [codeqhub.models.commit :as commit]
            [codeqhub.helpers.url :as url]
            [codeqhub.views.html.namespace :as namespace.view]
            [codeqhub.views.html.commit :as commit.view]
            [codeqhub.views.html.ref :as ref.view]))

(defn repo [repo commit branch namespaces]
  "Return HTML view of repo at specific commit or branch."
  (list
    [:h2 "Namespaces"]
    (namespace.view/namespaces repo commit branch namespaces)
    [:h2 "Previous Commits"]
    (commit.view/commits repo (take 10 (commit/ancestors commit)))
    [:h2 "Branches"]
    (ref.view/branches repo (repo/branches repo))
    [:h2 "Tags"]
    (ref.view/branches repo (repo/tags repo))))

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
