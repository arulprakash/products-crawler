(ns amazon.connectors
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as str]
            [clj-http.client :as client]
            [clj-http.conn-mgr :as conn-mgr])
  (:import org.apache.commons.validator.routines.UrlValidator))



