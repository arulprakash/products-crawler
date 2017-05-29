(ns amazon.seed
  (:require [amazon.db :as db]
            [mount.core :as mount]
            [clojure.string :as str]))

(defn prepare-links
  [url]
  (let [r (range 1 401 1)]
    (doall (map #(as-> url u
                   (str/replace u "page=2" (str "page=" %))
                   (str/replace u "sr_pg_2" (str "sr_pg_" %))
                   {:url u}
                   (if (= 0 (:count (db/does-url-exist? u)))
                     (db/populate-link! u)))
                r))))


(defn populate-links
  []
  (let [urlfile (slurp "resources/urls.txt")
        initials (str/split urlfile #"\n")]
    (mount/start)
    (doall (map #(prepare-links %) initials))))
