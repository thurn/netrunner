(ns game.rng
  "Per-game seedable randomness and deterministic ids for headless AI play.
  When `*rng*` / `*ids*` are unbound, every function keeps upstream behaviour."
  (:refer-clojure :exclude [shuffle rand-int rand-nth flatten])
  #?(:clj (:require [clj-uuid :as uuid])))

#?(:clj
   (do
     (def ^:dynamic ^java.util.Random *rng*
       "A java.util.Random used for every game-logic random draw, or nil."
       nil)

     (def ^:dynamic *headless*
       "When true, skip work that only feeds the web UI (ability cost labels)."
       false)

     (def ^:dynamic *ids*
       "An atom holding a long counter used for cids, uuids and timestamps, or nil."
       nil)

     (defn shuffle
       ([coll] (shuffle coll nil))
       ([coll ^java.util.Random fallback]
        (if-let [r (or *rng* fallback)]
          (let [al (java.util.ArrayList. ^java.util.Collection (or (seq coll) []))]
            (java.util.Collections/shuffle al r)
            (clojure.lang.RT/vector (.toArray al)))
          (clojure.core/shuffle coll))))

     (defn rand-int [n]
       (if *rng* (.nextInt *rng* (int n)) (clojure.core/rand-int n)))

     (defn rand-nth [coll]
       (if *rng* (nth coll (rand-int (count coll))) (clojure.core/rand-nth coll)))

     (defn- next-id ^long [] (swap! *ids* inc))

     (defn cid []
       (if *ids* (str "c" (next-id)) (str (random-uuid))))

     (defn uuid-v4 []
       (if *ids* (java.util.UUID. 0x4d4f4e4f (next-id)) (uuid/v4)))

     (defn uuid-v1 []
       (if *ids* (java.util.UUID. 0x4d4f4e50 (next-id)) (uuid/v1)))

     (defn flatten
       "Eager equivalent of clojure.core/flatten for hot cost paths."
       [x]
       (if (sequential? x)
         (persistent! ((fn step [acc y] (if (sequential? y) (reduce step acc y) (conj! acc y))) (transient []) x))
         ()))

     (defn timestamp []
       (if *ids* (java.time.Instant/ofEpochSecond 0 (next-id)) (java.time.Instant/now))))

   :cljs
   (do
     (def shuffle clojure.core/shuffle)
     (def rand-int clojure.core/rand-int)
     (def rand-nth clojure.core/rand-nth)
     (defn cid [] (str (random-uuid)))))
