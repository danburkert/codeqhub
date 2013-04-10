(ns codeqhub.test.models.repository
  (:require [clojure.test :as t]
            [codeqhub.models.repository :as repo]
            [codeqhub.test.generators :as gen]))

(defn- repo? [repo-ent]
  (and
    (contains? repo-ent :repo/uri)
    (contains? repo-ent :repo/stars)
    (contains? repo-ent :repo/forks)))

(t/deftest test-repo-user-constraints
  (t/are [user] (thrown? AssertionError (repo/repo nil user nil))
         "-"
         "1 2 3"
         ""
         "foo!"
         "foo$"
         "foo/bar"))

(t/deftest test-repo-name-constraints
  (t/are [name] (thrown? AssertionError (repo/repo nil "foo" name))
         "1 2 3"
         ""
         "foo!"
         "foo$"
         "foo/bar"))

(t/deftest test-repo
  (let [db (gen/db-with (gen/repo :user "test-user" :name "test-repo"))
        repo (repo/repo db "test-user" "test-repo")]
    (t/is (= (repo/user repo) "test-user"))
    (t/is (repo? repo))))

(t/deftest test-repos
  (let [db (-> (gen/db-with (gen/repo))
               (gen/db-add (gen/repo))
               (gen/db-add (gen/repo :user "user1"))
               (gen/db-add (gen/repo :user "user1"))
               (gen/db-add (gen/repo :user "user1")))]
    (t/is (= 5 (count (repo/repos db))))
    (t/is (= 3 (count (repo/repos db "user1"))))
    (t/is (every? repo? (repo/repos db)))))

(t/deftest test-refs

  )

(t/run-tests)
