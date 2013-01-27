(ns codeqhub.views.html.codeq
  (:require [codeqhub.models.codeq :as codeq]
            [codeqhub.helpers.url :as url]))

(defn codeq [codeq commit ref codeq]
  "Return HTML view of codeq."
  (:code/highlight (:codeq/code codeq)))

(defn codeqs [repo commit ref codeqs]
  "Return HTML view of codeq list."
  [:ol
   (for [codeq codeqs]
     (if-let [id (codeq/identifier codeq)]
       [:li
        [:a {:href (url/codeq repo commit ref codeq)}
         (codeq/identifier codeq)]]))])
