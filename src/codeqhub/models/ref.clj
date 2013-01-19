(ns codeqhub.models.ref
  (:require [datomic.api :as d]
            [codeqhub.models.util :as util])
  (:refer-clojure :exclude [ref type]))

(defn label
  [ref]
  "Return the label of the reference"
  (:ref/label ref))

(defn commit
  [ref]
  "Return the commit pointed to by the reference."
  (:ref/commit ref))

(defn type
  [ref]
  "Return the type of the reference."
  (:git/type ref))
