(ns amazon.getters
  (:require [net.cgrand.enlive-html :as html]
            [clojure.zip :as z]
            [clojure.string :as str]
            [amazon.helpers :refer :all]
            [clj-http.client :as client]
            [clj-http.conn-mgr :as conn-mgr]
            [clojure.pprint :as pprint :refer [pprint print-table]])
  (:import [java.net UnknownHostException
            MalformedURLException
            URISyntaxException]))

(def bsr1 [:li#SalesRank])
(def bsr2 [:span.zg_hrsr_rank])
(def pr1 [:span#priceblock_ourprice])
(def pr2 [:span#priceblock_dealprice])
(def pr3 [:span.a-color-price])
(def ts1 :table#productDetails_detailBullets_sections1)
(def ts2 :table.a-keyvalue)
(def as1 [:td.bucket :li])
(def review [:span#acrCustomerReviewText])
(def np [:a.pagnNext])
(def user-agent {"http.useragent" "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.20 (KHTML, like Gecko) Chrome/11.0.672.2 Safari/534.20"})

(def proxies
  [;list proxies here
   ])
(def direct-proxy {:proxy-host (rand-nth proxies) :proxy-port 3128})
(def socks-proxy {:connection-manager
                  (conn-mgr/make-socks-proxied-conn-manager (rand-nth proxies) 3128)})
(def local {:proxy-host "127.0.0.1" :proxy-port 80})

(def choose-proxy [direct-proxy, socks-proxy, local])
(defn make-request
  [url]
  (client/get url direct-proxy))

(defn get-table-data
  [doc table field]
  (let [th [table :th]
        td [table :td]
        hdrs (map #(str/trim (html/text %)) (html/select doc th))
        data (map #(str/trim (html/text %)) (html/select doc td))
        table (zipmap hdrs data)]
    (get table field)))

(defn fetch-url
  [url]
  (try
    (let [req (make-request url)
          body (:body req)]
      (html/html-snippet body))
    (catch UnknownHostException e (first nil))
    (catch MalformedURLException e (first nil))
    (catch Exception e (do
                         (spit "resources/exceptions.txt" (str "URL: " url "\nException:" e) :append true)
                         (fetch-url url)))))

(defn get-asin
  [doc]
  (let [s1 (get-table-data doc ts1 "ASIN")
        s2 (str/replace (html/text (first (filter #(str/includes? (html/text %) "ASIN:") (html/select doc as1)))) "ASIN: " "")]
    (or s1 s2)))

(defn get-bsr
  [doc]
  (let [s1 (get-table-data doc ts1 "Best Sellers Rank")
        s2 (html/text (first (html/select doc bsr1)))
        s3 (html/text (first (html/select doc bsr2)))]
    (if (str/blank? (or s1 s2 s3))
      0
      (Integer. (str/replace (first (re-seq #"#\d+" (or s1 s2 s3))) #"#" "")))))

(defn get-price
  [doc]
  (let [s1 (map #(re-seq #"\d+" (html/text (first (html/select doc %)))) [pr1 pr2 pr3])
        s2 (filter #(not (nil? %)) s1)]
    (try
      (if (empty? s2)
        0
        (Float/parseFloat (str/join "." (take 2 (first s2)))))
      (catch Exception e (spit "resources/exceptions.txt" (str "\nPrice: " (pr-str s1)) :append true)))))

(defn get-reviews
  [doc]
  (let [reviews (first (find-key :content (html/select doc review)))]
    (if (str/blank? reviews)
      0
      (Integer. (reduce str (re-seq #"\d+" reviews))))))

(defn get-title
  [doc]
  (->> (html/select doc [:h1#title])
       (first)
       (html/text)
       (str/trim)
       (take 50)
       (reduce str)))

(defn get-weight
  [doc]
  (let [s1 (get-table-data doc ts1 "Shipping Weight")
        s2 (get-table-data doc ts2 "Shipping Weight")
        s3 (str/replace (html/text (first (filter #(str/includes? (html/text %) "Shipping Weight:") (html/select doc as1)))) "Shipping Weight: " "")
        w1 (or s1 s2 s3)]
    (if (str/blank? w1)
      0
      (let [w2 (Float/parseFloat (str/join "." (re-seq #"\d+" w1)))]
        (if  (str/includes? w1 "pounds")
          (* w2 16)
          w2)))))
