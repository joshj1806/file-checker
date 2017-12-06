(ns file-checker.core
  (:require [clojure.java.io :as io]
            [digest]
            [iota]
            [clojure.core.reducers :as r]
            [clojure.pprint :refer [pprint]])
  (:gen-class))

(declare file->map dir->map duplicates)

(defn -main
  [& args]
  (println "Looking for duplicate files ..")
  (->> args first dir->map duplicates))

(defn- file->map
  "Returns hash map of a key and the file path.
  hash agorithm can be MD5 or SHA.
  Please refer to https://github.com/tebeka/clj-digest"
  [f & {:keys [algorithm] :or {algorithm digest/sha}}]
  (hash-map (algorithm f) (.getPath f)))

(defn dir->map
  "Returns a tree made out of the given directory"
  [dir]
  (->> dir
       io/file
       file-seq
       (r/filter #(.isFile %))))

(defn- tr
  "Returns a tree of the key and the file path pairs."
  [ms]
  (r/fold ;; Parallel reducing
   (fn dd
     ([] {})
     ([ret x]
      (let [kv (file->map x)
            k (-> kv keys first)]
        (assoc ret k (conj (get ret k []) kv)))))
   ms))

(defn- duplicates
  "Returns duplicate files"
  [m]
  (->> m
       tr
       (filter (fn ff [[k v]] (<= 2 (count v))))
       (into [])))


;; files -> tree
;; travers all tree elements to produce {cksum, path} (parrel map)
;;    skip symbolic links
;; groupby cksum
;; filter if more than 2 values
;; print the outputs
