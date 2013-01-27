(ns codeqhub.views.html.ref
  (:require [codeqhub.helpers.url :as url]
            [codeqhub.models.ref :as ref]))

(defn branches [repo branches]
  "Return HTML view of branch list."
  [:ul
   (for [branch branches]
     [:li
      [:span [:a {:href (url/namespaces repo nil branch)}
              (ref/label branch)]]])])

(defn tags [repo tags]
  "Return HTML view of tag list."
  [:ul
   (for [tag tags]
     [:li
      [:span [:a {:href (url/namespaces repo nil tag)}
              (ref/label tag)]]])])
