(ns kth.tutorial-3
  (:require [ysera.test :refer [error? is is=]]
            [ysera.error :refer [error]]
            [ysera.collections :refer [seq-contains?]]
            [ysera.random :refer [get-random-int]]
            [firestone.construct :refer :all]
            [firestone.core :refer :all]
            [firestone.definitions :refer [get-definition]]))

; Good style source: https://github.com/bbatsov/clojure-style-guide

(defn sum
  [x y]
  (+ x y))

(-> {:a 1 :b 2 :c 3 :d 4 :e 5}
    (assoc :f 6))

(seq-contains? #{1 2 3} 4)

(= (set [1 2 3 3]) #{1 2 3})

(= (list 1 2 3) '(1 2 3))

(defn my-sum
  [x y]
  {:pre [(number? x) (number? y) (> x 4)]}
  (+ x y))

(defn get-thing
  []
  nil)

(defn minion-exists?
  [name]
  (nil? name))

(if (get-thing)
  "it was truthy"
  "it was falsey")

(nil? 3)
(= 3 nil)

(some? 3)

(let [x 6]
  (cond
    (> x 5) "It's greater than 5"
    (< x 2) "It's smaller than 2"
    :else "I don't know"))

(let [target-type :minion]
  (condp = target-type
    :minion "It's a minion"
    :hero "It's a hero!"))

(if-let [minion false]
  minion
  "It's not here :/")


(defn get-minion-attack
  [{attack :attack name :name}]
  attack)

(get-minion-attack {:attack 3 :name "Gustave"})

(get-random-int 3 100)
