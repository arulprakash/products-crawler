(ns amazon.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]
            [clojure.zip :as z]
            [clojure.string :as str]
            [amazon.helpers :refer :all]
            [amazon.db :as db]
            [amazon.queue :refer :all]
            [amazon.getters :refer :all]
            [mount.core :as mount]
            [clojure.core.async.impl.protocols :as impl]
            [clojure.core.async.impl.concurrent :as conc]
            [clojure.core.async.impl.exec.threadpool :as tp]
            [clojure.core.async :as async]
            [clojure.pprint :as pprint :refer [pprint print-table]])
  (:import [org.apache.commons.validator.routines.UrlValidator]
           [java.util.concurrent Executors Executor]))

(defn thread-pool-executor
  []
  (let [executor-svc (Executors/newFixedThreadPool
                      200
                      (conc/counted-thread-factory "async-dispatch-%d" true))]
    (reify impl/Executor
      (impl/exec [this r]
        (.execute executor-svc ^Runnable r)))))

(alter-var-root #'clojure.core.async.impl.dispatch/executor
                (constantly (delay (thread-pool-executor))))

(defn parse-items
  [url]
  (async/go
    (let [doc (fetch-url url)
          p {:url url
             :title (get-title doc)
             :reviews (get-reviews doc)
             :asin (get-asin doc)
             :bsr (get-bsr doc)
             :price (get-price doc)
             :weight (get-weight doc)}]
      (if (and (> (:reviews p) 25)
               (< (:bsr p) 1000)
               (> (:price p) 10.00)
               (< (:price p) 150.00)
               (not-any? nil? (:asin p))
               (= 0 (:count (db/get-asin {:asin (:asin p)}))))
        (db/create-record! p)))))

(defn get-list
  [url]
  (as-> (fetch-url url) p
    (html/select p [:li.s-result-item])
    (doall (map #(find-key :href %) p))
    (filter #(is-url-valid? %) p)
    (doall (map #(parse-items %) p))
    (spit "resources/updatelog.txt" (str (:url url) "\n") :append true)))

(defn start-seed
  [url]
  (let [url (str/replace url "%5B%5D-" "")
        doc (fetch-url url)
        next-url (str/join ["https://amazon.com" (find-key :href (html/select doc np))])]
    (println (str "URL:" url))
    (println (str "Next URL:" next-url))
    (if (and url (= 0 (:count (db/does-url-exist? {:url url}))))
      (db/populate-link! {:url url}))
    (async/go (start-seed next-url))))

(defn start-parse
  []
  (while true
    (if-let [url (db/get-first-url)]
      (do
        (db/update-url-status! url)
        (async/go (get-list (:url url)))))))

(defn start-machine
  []
  (let [urlfile (slurp "resources/urls.txt")
        initials (str/split urlfile #"\n")]
    (mount/start)
    (future (start-parse))
    (doall (map #(async/go (start-seed %)) initials))))
