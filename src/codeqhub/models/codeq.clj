(ns codeqhub.models.codeq
  (:require [datomic.api :as d]
            [clojure.string :as s]
            [codeqhub.models.util :as util])
  (:refer-clojure :exclude [name namespace]))

(defn parent
  "Return the parent of the codeq."
  [codeq]
  (:codeq/parent codeq))

(defn text
  "Return the text of the codeq."
  [codeq]
  (:code/text (:codeq/code codeq)))

(defn location
  "Return the coordinates of the codeq with its file."
  [codeq]
  (mapv #(Integer/parseInt %)
        (-> (:codeq/loc codeq)
            (s/split #"\s"))))

(defn name
  "Return the fully namespaced name of the codeq."
  [codeq]
  (or (:code/name (:clj/def codeq))
      (:code/name (:clj/ns codeq))))

(defn namespace
  "Return the namespace of the codeq."
  [codeq]
  (if-let [^String name (name codeq)]
    (let [idx (.lastIndexOf name "/")]
      (if (> idx 0)
        (.substring name 0 idx)
        name))))

(defn identifier
  "Return the identitifer of the codeq, i.e. the name without the namespace."
  [codeq]
  (if-let [^String name (name codeq)]
    (let [idx (.lastIndexOf name "/")]
      (when (> idx 0)
        (.substring name (inc idx))))))

(defn highlight
  "Return the highlighted text of the codeq."
  [codeq]
  (-> codeq
      :codeq/code
      :code/highlight))
