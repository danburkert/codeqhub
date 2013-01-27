(ns codeqhub.views.html.commit
  (:require [codeqhub.helpers.url :as url]
            [codeqhub.models.commit :as commit]))

(defn commits [repo commits]
  "Return HTML view of commit list."
  [:ul
   (for [commit commits]
     [:li
      [:span [:a {:href (url/namespaces repo commit nil)}
              (take 10 (commit/sha commit))]]
      [:span (take 50 (commit/message commit))]])])
