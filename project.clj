(defproject file-checker "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [spyscope "0.1.6"]
                 [yieldbot/flambo "0.8.2"]
                 [iota "1.1.3"]
                 [digest "1.4.6"]]
  :main ^:skip-aot file-checker.core
  :target-path "target/%s"
  :profiles {:provided
             {:dependencies
              [[org.apache.spark/spark-core_2.11 "2.2.0"]]}
             :dev {:aot [file-checker.core]}
             :uberjar {:aot :all}})
