(ns file-checker.spark-context
  (:require [flambo.conf :as conf]
            [flambo.api :as f]))

(def sc (atom []))

(def c
  (-> (conf/spark-conf)
      (conf/master "local[*]")
      (conf/app-name "file-checker")))

(defn set-sc
  []
  (reset! sc (f/spark-context c)))
