(ns codeqhub.models.util
  (:require [datomic.api :as d]))

(defn uri->user
  "return github user name of repository uri"
  [^String uri]
  (let [repo-idx (.lastIndexOf uri "/")
        user-idx (.lastIndexOf uri "/" (dec repo-idx))]
    (subs uri (inc user-idx) repo-idx)))

(defn uri->repo
  "return github repository name of repository uri"
  [^String uri]
  (let [repo-idx (.lastIndexOf uri "/")]
    (subs uri (inc repo-idx))))

(defn uri->user-repo
  "return github repository name & user name of repository uri"
  [^String uri]
  (let [repo-idx (.lastIndexOf uri "/")
        user-idx (.lastIndexOf uri "/" (dec repo-idx))]
    [(subs uri (inc user-idx) repo-idx)
     (subs uri (inc repo-idx))]))

(defn user-repo->uri
  "construct github uri from user name and repo name"
  [user repo]
  (str "github.com/" user "/" repo))

(defn user->uri
  "construct github user page uri from user name."
  [user]
  (str "github.com/" user))

(defn qes
  "Returns the entities returned by a query, assuming that
   all :find results are entity ids."
  [query db & args]
  (->> (apply d/q query db args)
       (mapcat (fn [items]
              (map (partial d/entity db) items)))))

(defn tqes
  "Returns the touched entities returned by a query, assuming
   that all :find results are entity ids."
  [query db & args]
  (->> (apply d/q query db args)
       (mapv (fn [items]
               (mapv (comp d/touch (partial d/entity db)) items)))))

(defn qfs
  "Returns the first of each query result."
  [query db & args]
  (->> (apply d/q query db args)
       (map first)))

(defn qmap
  "Returns the results of a query in map form with specified keys"
  [query keys db & args]
  (->> (apply d/q query db args)
       (map (partial zipmap keys))))

(defn index-get-id
  "Get id of attribute attr in database db of value v"
  [db attr v]
  (let [d (first (d/index-range db attr v nil))]
    (when (and d (= (:v d) v))
      (:e d))))

(defn find-all-by
  "Returns all entities possessing attr."
  ;; TODO: figure out if this would be faster with an index-range scan
  [db attr]
  (qes '[:find ?e
         :in $ ?attr
         :where [?e ?attr]]
       db attr))

(def rules
  '[[(node-files ?n ?f)   [?n :node/object ?f] [?f :git/type :blob]]
    [(node-files ?n ?f)   [?n :node/object ?t] [?t :git/type :tree]
                          [?t :tree/nodes ?n2] (node-files ?n2 ?f)]
    [(object-nodes ?o ?n) [?n :node/object ?o]]
    [(object-nodes ?o ?n) [?n2 :node/object ?o] [?t :tree/nodes ?n2] (object-nodes ?t ?n)]
    [(commit-files ?c ?f) [?c :commit/tree ?root] (node-files ?root ?f)]
    [(commit-codeqs ?c ?cq) (commit-files ?c ?f) [?cq :codeq/file ?f]]
    [(file-commits ?f ?c) (object-nodes ?f ?n) [?c :commit/tree ?n]]
    [(codeq-commits ?cq ?c) [?cq :codeq/file ?f] (file-commits ?f ?c)]
    [(codeq-ns ?cq ?ns) [?cq :clj/ns ?ns]] ;; TODO: actually test this
    [(codeq-ns ?cq ?ns) [?cq :codeq/file ?f] [?cqs :codeq/file ?f]
                        [?cqs :clj/ns ?ns]]])
