(ns codeqhub.views.html.commit
  (:require [codeqhub.helpers.url :as url]
            [codeqhub.models.commit :as commit]
            [codeqhub.models.repository :as repo]
            [codeqhub.views.html.namespace :as namespace.view]
            [codeqhub.views.html.ref :as ref.view]))

(defn commits [repo commits]
  "Return HTML view of commit list."
  [:ul
   (for [commit commits]
     [:li
      [:span [:a {:href (url/namespaces repo commit nil)}
              (take 10 (commit/sha commit))]]
      [:span (take 50 (commit/message commit))]])])

(defn commit [repo commit branch namespaces]
  "Return HTML view of the commit."
  (list
    [:h3 "Namespaces"]
    (namespace.view/namespaces repo commit branch namespaces)
    [:h3 "Previous Commits"]
    (commits repo (take 10 (commit/ancestors commit)))
    [:h3 "Branches"]
    (ref.view/branches repo (repo/branches repo))
    [:h3 "Tags"]
    (ref.view/branches repo (repo/tags repo))))
