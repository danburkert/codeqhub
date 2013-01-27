(defproject codeqhub "0.1.0-SNAPSHOT"
  :description "an online codeq hub"
  :url "http://codeqhub.com"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.datomic/datomic-free "0.8.3692"]
                 [compojure "1.1.3"]
                 [ring "1.1.7"]
                 [hiccup "1.0.2"]]
  :plugins [[lein-ring "0.8.0"]]
  :ring {:handler codeqhub.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
