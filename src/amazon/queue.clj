(ns amazon.queue
  (:require [amazon.helpers :refer :all]
            [taoensso.carmine :as car :refer (wcar)]))

(def server1-conn {:pool {}
                   :spec {:host "127.0.0.1"
                          :port 6379
                          :db 0}})

(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn enqueue
  [url]
  (if (is-url-valid? url)
    (wcar*
     (car/sadd "urlqueue" url))))

(defn dequeue
  []
  (wcar*
   (car/spop "urlqueue")))

(defn queue-len
  []
  (wcar*
   (car/scard "urlqueue")))

(defn queue-list
  []
  (wcar*
   (car/smembers "urlqueue")))
