(ns codeqhub.database
  (:require [datomic.api :as d]))

(def conn
  (let [uri (get (System/getenv) "DATOMIC_URI" "datomic:free://localhost:4334/codeq")]
    (d/connect uri)))

(defn get-db []
  (d/db conn))
