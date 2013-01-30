(ns codeqhub.models.ref
  (:require [datomic.api :as d]
            [codeqhub.models.util :as util])
  (:refer-clojure :exclude [ref type]))

(defn label
  "Return the label of the reference"
  [ref]
  (:ref/label ref))

(defn commit
  "Return the commit pointed to by the reference."
  [ref]
  (:ref/commit ref {:ref ref}))

(defn type
  "Return the type of the reference."
  [ref]
  (:git/type ref))
