(ns file-checker.core
  (:require [clojure.java.io :as io]
            [digest]
            [iota]
            [file-checker.spark-context :as sc]
            [flambo.api :as f]
            [flambo.tuple :as ft]
            [clojure.core.reducers :as r]
            [clojure.pprint :refer [pprint]])
  (:gen-class)
  (:import [org.apache.spark.api.java JavaRDD]))

(declare file->map dir->files parallel duplicates duplicated-files)

(defn -main
  [& args]
  (println "Looking for duplicate files ..")
  (->> args first dir->files parallel duplicated-files)
  ;;(->> args first dir->files duplicates)
  )

(defn- file->map
  "Returns hash map of a key and the file path.
  hash algorithm can be MD5 or SHA.
  Please refer to https://github.com/tebeka/clj-digest"
  [f & {:keys [algorithm] :or {algorithm digest/sha}}]
  (hash-map (algorithm f) (.getPath f)))

(f/defsparkfn f->m
  [f]
  (ft/tuple (digest/sha f) (.getPath f)))

(defn dir->files
  "Returns a tree made out of the given directory"
  [dir]
  (->> dir
       io/file
       file-seq
       (filter #(.isFile %))))

(defn- duplicated?
  [rdd]
  (when (<= 2 (count (._2 rdd))) true))

(defn- parallel
  "expecting dir->map's output as a input"
  [fs]
  (let [sc (if (= org.apache.spark.api.java.JavaSparkContext (class @sc/sc))
             @sc/sc
             (throw (Exception. "Set up Spark context first. Call (set-sc)")))]
    (-> (f/parallelize @sc/sc fs)
        (f/map-to-pair f->m)
        f/group-by-key
        (f/filter duplicated?)
        (f/map (fn [x] (._2 x)))
        f/collect
        )))

(defn- duplicated-files
  "Returns duplicated files from a given Spark's result"
  [result]
  (->> result
       (map #(take 2 %))
       (into [])))

(defn- trx
  "Returns a tree of the key and the file path pairs."
  [ms]
  (r/fold ;; Parallel reducing
   ;; combining step. {K [Vs ..]} is a input not {k v}
   (fn cc
     ([] {})
     ([ret x]
      (let [k (-> x keys first)]
        (assoc ret k (conj (get ret k []) x)))))
   ;; reducing step. create hash map of {k [vs..]}
   (fn rr
     ([] {})
     ([ret x]
      (let [kv (file->map x)
            k (-> kv keys first)]
        (assoc ret k (conj (get ret k []) kv)))))
   ms))

(defn- duplicates
  "Returns duplicated files from a given folding result"
  [m]
  (->> m
       (pmap file->map)
       trx
       (filter (fn ff [[k v]] (<= 2 (count v))))
       (map (fn [[k v]] (mapcat vals v)))
       (into [])))
