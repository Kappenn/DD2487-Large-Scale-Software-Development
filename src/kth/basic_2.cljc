(ns kth.basic-2
  (:require [ysera.test :refer [is= is is-not]]))

; clojure sheet cheat

; abbreviations should be avoided at all cost - except

; defn, metadata, tests, pre and (post)

(defn f
  "A doc string"
  {:private true
   :test    (fn []
              (is= (f 3 4) 13))
   :release "1.0"}
  [x y]
  {:pre [(integer? x) (> y 2)]}
  (+ (* x y) 1))

(comment
  (f 3 1))


; function overloading

(defn g
  ([x]
   x)
  ([x y]
   (println "x")
   (+ x y)))

(g 5 4)

; pure functions

; do

(do (println "x")
    5)

; let

(let [x 5]
  (println x)
  (inc x))

; destructuring

(let [x {:a {:b 1
             :c 4}
         :d "42"}
      {{c :c} :a} x
      [f s t & r] (range 10)]
  (println f t)
  (println r))

; do and implicit do

; vals, keys

; map and map-indexed (create-empty-state)

(->> (range 10)
     (map-indexed (fn [index x] (+ index x)))
     (take 4))

; or, and, truthy, falsy

(or nil false 0 1)
(or nil false nil)

(and true 0 nil 34)
(and "42" true {} [])

(if 1 2 3)

(if-not 1 2 3)

; ->, ->>, as->

; is, is-not, is= (Ysera !?)

; if, if-not, when, when-not, macro

; ns :refer

; some
(map (fn [x]
        (when (= x 6)
          7))
      (range 10))

(when (= 1 1)
  3
  2)

(if (= 1 1)
  (do 3
      2))

; laziness

; concat (concat push javascript)

; polymorphism

(defmulti h (fn [state] (:a state)))

(defmethod h "KALLE"
  [state] "K")

(h {:a "KALLE"})

(defmethod h :kalle
  [state] 5)

(h {:a :kalle})

(def v ["KALLE" "PELLE" "STINA" "ANNA" {:a 42}])

(get v 2)

(assoc v 1 "PELE")

(get-in v [4 :a])









