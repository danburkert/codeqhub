(ns codeqhub.views.html.commit
  (:require [codeqhub.helpers.url :as url]
            [codeqhub.models.commit :as commit]))

(defn commit [repo commit branch namespaces]
  "Return HTML view of the commit."
  (list
    [:h2 "Namespaces"]
    (namespace.view/namespaces repo commit branch namespaces)
    [:h2 "Previous Commits"]
    (commit.view/commits repo (take 10 (commit/ancestors commit)))
    [:h2 "Branches"]
    (ref.view/branches repo (repo/branches repo))
    [:h2 "Tags"]
    (ref.view/branches repo (repo/tags repo))))

(defn commits [repo commits]
  "Return HTML view of commit list."
  [:ul
   (for [commit commits]
     [:li
      [:span [:a {:href (url/namespaces repo commit nil)}
              (take 10 (commit/sha commit))]]
      [:span (take 50 (commit/message commit))]])])
