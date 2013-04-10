(ns codeqhub.test.generators
  (:require [clojure.test :as t]
            [clojure.data.generators :as gen]
            [datomic.api :as d]
            [codeqhub.models.repository :as repo]
            [codeqhub.models.util :as util])
  (:refer-clojure :exclude [ref]))

(defn alphanumeric-char
  "Returns a random alpha numeric ascii character."
  []
  (char
    (gen/weighted {(gen/uniform 65 (+ 65 26)) 26
                   (gen/uniform 97 (+ 97 26)) 26
                   (gen/uniform 48 (+ 48 10)) 10})))

(defn hex-char
  "Returns a random hex character."
  []
  (char
    (gen/weighted {(gen/uniform 48 (+ 48 10)) 10
                   (gen/uniform 97 (+ 97 6)) 6})))

(defn alphanumeric-string []
  (let [s (gen/string alphanumeric-char)]
    (if (> (count s) 0)
      s
      (recur))))

(defn sha []
  (gen/string hex-char (constantly 40)))

(defn date []
  (java.util.Date. (gen/geometric (/ 1 1359832244000))))

(defn tempid []
  (d/tempid :db.part/user))

(defn location []
  (let [start-line (gen/uniform 0 1000)]
    (str start-line " "
         (gen/geometric 0.5) " "
         (+ start-line (gen/geometric 1/10)) " "
         (gen/geometric 0.1))))

(defn with-ref [parent-id attr gen-child]
  (let [child-id (tempid)]
    (vec (concat
           [[:db/add parent-id attr child-id]]
           (gen-child child-id)))))

(defn defop []
  (rand-nth ["def" "defn" "defprotocol" "defmacro"
             "definline" "defmethod" "defmulti" "defn-"
             "defonce" "defstruct" "deftype"]))

(defn codeq
  ([] (codeq (tempid)))
  ([id & {:keys [blob-id type] :or {blob-id (tempid)
                                    type :clj/def}}]
   (let [code-id (tempid)
         codename-id (tempid)]
     (vec
       (concat [[:db/add id :codeq/file blob-id]
                [:db/add id :codeq/loc (location)]
                [:db/add id type code-id]
                [:db/add code-id :code/sha (sha)]
                [:db/add code-id :code/text (gen/string)]
                [:db/add code-id :code/highlight (gen/string)]
                [:db/add id :codeq/code codename-id]
                [:db/add codename-id :code/name (alphanumeric-string)]]
               (if (= type :clj/def)
                 [[:db/add id :clj/defop (defop)]]
                 []))))))

(declare node)

(defn file
  ([] (file (tempid)))
  ([id & {:keys [blob-id filename-id path-id]
          :or {blob-id (tempid)
               filename-id (tempid)
               path-id (tempid)}}]
   (vec (apply concat
               [[:db/add id :node/filename filename-id]
                [:db/add filename-id :file/name (gen/string)]
                [:db/add id :node/paths path-id]
                [:db/add path-id :file/name (gen/string)]
                [:db/add id :node/object blob-id]
                [:db/add blob-id :git/sha (sha)]
                [:db/add blob-id :git/type :blob]]
               (codeq (tempid) :blob-id blob-id :type :clj/ns)
               (gen/reps (gen/geometric 1/10)
                         #(codeq (tempid) :blob-id blob-id))))))

(defn tree
  ([] (tree (tempid)))
  ([id]
   (vec (apply concat
               [[:db/add id :git/type :tree]
                [:db/add id :git/sha (sha)]]
               (gen/reps (gen/geometric 1/4)
                         #(with-ref id :tree/nodes node))))))

(defn node [id]
  (gen/weighted {#(tree id) 1
                 #(file id) 5}))

(defn commit
  [id & {:keys [repo-id author-id committer-id]
         :or {author-id (tempid)
              committer-id (tempid)}}]
  (vec
    (apply concat
           [[:db/add id :git/sha (sha)]
            [:db/add id :commit/message (gen/string)]
            [:db/add id :commit/committedAt (date)]
            [:db/add id :commit/authoredAt (date)]
            [:db/add id :commit/committer committer-id]
            [:db/add committer-id :email/address (gen/string)]
            [:db/add id :commit/author author-id]
            [:db/add author-id :email/address (gen/string)]]
           ;(with-ref id :commit/tree tree)
           (when repo-id [[:db/add repo-id :repo/commits id]])
           (gen/reps #(let [n (gen/geometric (/ 3 5))] (if (< n 3) n 0))
                     (fn [] (let [parent-id (tempid)]
                              (conj
                                (commit parent-id :repo-id repo-id
                                        :author-id author-id
                                        :committer-id committer-id)
                                [[:db/add id :commit/parents parent-id]])))))))

(defn ref
  [id & {:keys [repo-id type commit-id label]
         :or {type (rand-nth [:tag :branch])
              label (gen/string)}}]
  (vec (concat
         [[:db/add id :git/type type]
          [:db/add id :ref/label label]
          [:db/add id :ref/commit commit-id]]
         (if commit-id
           [[:db/add id :ref/commit commit-id]]
           (let [commit-id (tempid)]
             (concat
               [[:db/add id :ref/commit commit-id]]
               (commit commit-id :repo-id repo-id))))
         (if repo-id
           [[:db/add repo-id :repo/refs id]]))))

(defn repo [& {:keys [user name] :or {user (alphanumeric-string)
                                      name (alphanumeric-string)}}]
  (let [repo-id (tempid)]
    (vec (apply concat
                [[:db/add repo-id :repo/uri (util/user-repo->uri user name)]
                 [:db/add repo-id :repo/stars (rand-int 1000)]
                 [:db/add repo-id :repo/forks (rand-int 50)]]
                (gen/reps (gen/geometric 1/10)
                          (fn [] (ref (tempid))))))))

(repo)

(defn gen-db-conn
  "Generate an in-memory database to use for tests, and return the connection."
  []
  (let [uri (str "datomic:mem://" (alphanumeric-string))]
    (do (d/create-database uri)
        (d/connect uri))))

(declare schema)

(defn db-with
  "Create a database and transact an instance of the generator.  Returns the
   database."
  [tx-data]
  (let [conn (gen-db-conn)]
    (d/transact conn schema)
    (:db-after @(d/transact conn tx-data))))

(defn db-add
  "Add transaction data to database and return new database."
  [db tx-data]
  (:db-after (d/with db tx-data)))

(def schema
     [
      ;;tx attrs
      {:db/id #db/id[:db.part/db]
       :db/ident :tx/commit
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Associate tx with this git commit"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :tx/file
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Associate tx with this git blob"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :tx/analyzer
       :db/valueType :db.type/keyword
       :db/cardinality :db.cardinality/one
       :db/index true
       :db/doc "Associate tx with this analyzer"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :tx/analyzerRev
       :db/valueType :db.type/long
       :db/cardinality :db.cardinality/one
       :db/doc "Associate tx with this analyzer revision"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :tx/op
       :db/valueType :db.type/keyword
       :db/index true
       :db/cardinality :db.cardinality/one
       :db/doc "Associate tx with this operation - one of :import, :analyze"
       :db.install/_attribute :db.part/db}

      ;;git stuff
      {:db/id #db/id[:db.part/db]
       :db/ident :git/type
       :db/valueType :db.type/keyword
       :db/cardinality :db.cardinality/one
       :db/index true
       :db/doc "Type enum for git objects - one of :commit, :tree, :blob, :tag, :branch"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :git/sha
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "A git sha, should be in repo"
       :db/unique :db.unique/identity
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :repo/commits
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/many
       :db/doc "Associate repo with these git commits"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :repo/refs
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/many
       :db/doc "Associate repo with these git refs"
       :db/unique :db.unique/value
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :repo/defaultBranch
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Default branch of repository."
       :db/unique :db.unique/value
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :repo/uri
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "A git repo uri"
       :db/unique :db.unique/identity
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :repo/stars
       :db/valueType :db.type/long
       :db/cardinality :db.cardinality/one
       :db/doc "Number of repository stars."
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :repo/forks
       :db/valueType :db.type/long
       :db/cardinality :db.cardinality/one
       :db/doc "Number of repository forks."
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :repo/homepage
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "Repository homepage."
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :repo/parent
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Parent repository."
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :ref/commit
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Commit pointed to by git ref"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :ref/label
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "Git ref label"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :commit/parents
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/many
       :db/doc "Parents of a commit"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :commit/tree
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Root node of a commit"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :commit/message
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "A commit message"
       :db/fulltext true
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :commit/author
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Person who authored a commit"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :commit/authoredAt
       :db/valueType :db.type/instant
       :db/cardinality :db.cardinality/one
       :db/doc "Timestamp of authorship of commit"
       :db/index true
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :commit/committer
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Person who committed a commit"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :commit/committedAt
       :db/valueType :db.type/instant
       :db/cardinality :db.cardinality/one
       :db/doc "Timestamp of commit"
       :db/index true
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :tree/nodes
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/many
       :db/doc "Nodes of a git tree"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :node/filename
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "filename of a tree node"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :node/paths
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/many
       :db/doc "paths of a tree node"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :node/object
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Git object (tree/blob) in a tree node"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :git/prior
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Node containing prior value of a git object"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :email/address
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "An email address"
       :db/unique :db.unique/identity
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :file/name
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "A filename"
       :db/fulltext true
       :db/unique :db.unique/identity
       :db.install/_attribute :db.part/db}

      ;;codeq stuff
      {:db/id #db/id[:db.part/db]
       :db/ident :codeq/file
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Git file containing codeq"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :codeq/loc
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "Location of codeq in file. A location string in format \"line col endline endcol\", one-based"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :codeq/parent
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Parent (containing) codeq of codeq (if one)"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :codeq/code
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "Code entity of codeq"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :code/sha
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "SHA of whitespace-minified code segment text: consecutive ws becomes a single space, then trim. ws-sensitive langs don't minify."
       :db/unique :db.unique/identity
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :code/text
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "The source code for a code segment"
       ;;:db/fulltext true
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :code/highlight
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "The highlighted HTML source code for a code segment"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :code/name
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "A globally-namespaced programming language identifier"
       :db/fulltext true
       :db/unique :db.unique/identity
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :clj/ns
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "codename of ns defined by expression"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :clj/def
       :db/valueType :db.type/ref
       :db/cardinality :db.cardinality/one
       :db/doc "codename defined by expression"
       :db.install/_attribute :db.part/db}

      {:db/id #db/id[:db.part/db]
       :db/ident :clj/defop
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one
       :db/doc "the def form (defn, defmacro etc) used to create this definition"
       :db.install/_attribute :db.part/db}])
