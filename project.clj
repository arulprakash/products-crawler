(defproject amazon "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [enlive "1.1.6"]
                 [commons-validator/commons-validator "1.5.1"]
                 [migratus "0.9.3"]
                 [com.layerware/hugsql "0.4.7"]
                 [org.postgresql/postgresql "42.1.1"]
                 [conman "0.6.3"]
                 [mount "0.1.11"]
                 [cheshire "5.7.1"]
                 [com.taoensso/carmine "2.16.0"]
                 [clj-http "3.5.0"]
                 [org.clojure/core.async "0.3.442"]]
  :plugins [[lein-exec "0.3.6"]]
  :main amazon.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
