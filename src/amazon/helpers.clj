(ns amazon.helpers
  (:require [net.cgrand.enlive-html :as html]
            [clojure.zip :as z]
            [clojure.string :as str]
            )
  (:import (org.apache.commons.validator.routines UrlValidator)))

(defn find-key [k coll]
  (let [coll-zip (z/zipper coll? seq nil coll)]
    (loop [x coll-zip]
      (when-not (z/end? x)
        (if-let [v (-> x z/node k)]
          v
          (recur (z/next x)))))))

(defn is-url-valid?
  [url]
  (println url)
  (let [validator (UrlValidator. (into-array ["http" "https"]))]
    (.isValid validator url)))
