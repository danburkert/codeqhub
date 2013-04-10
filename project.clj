(defproject codeqhub "0.1.0-SNAPSHOT"
  :description "an online codeq hub"
  :url "http://codeqhub.com"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.datomic/datomic-free "0.8.3862"]
                 [compojure "1.1.5"]
                 [ring "1.1.8"]
                 [hiccup "1.0.3"]]
  :plugins [[lein-ring "0.8.0"]]
  :ring {:handler codeqhub.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]
                        [org.clojure/data.generators "0.1.0"]]}})
