(ns codeqhub.views.html.layout
  (:require [hiccup.core :refer [html]]
            [hiccup.page :as hp]))

(defn common [title & body]
  (hp/html5
    [:head
     [:title title]
     (hp/include-css "/css/pygments.css")]
    [:body
     [:div {:id "header"}
      [:h1  "codeqhub"]]
     [:div body]]))

(defn four-oh-four []
  (common "Page Not Found"
          [:div {:id "four-oh-four"}
           "The page you requested could not be found"]))
