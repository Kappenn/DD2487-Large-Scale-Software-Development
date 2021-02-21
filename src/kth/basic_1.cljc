(ns kth.basic-1)

; string
"abc"

; numbers
123

; booleans
true
false

; maps/entities/objects
{}

; lists/vector
[]
(list)

; sets
#{}

; maps
{:a 1
 :b "abc"}

(def m {:a 1
        :b "abc"
        :d {:e "kth"}})

m

; updating things in a map
(update m :a inc)

(update m :a (fn [old-value]
               (+ 3 old-value)))

(update-in m [:d :e] (fn [old-value]
                       (count old-value)))


; create new key-values
(assoc m :c {})

(assoc m :a {})

(assoc m :a 1 :b 2 :c 4 :d {:e "something"})

(assoc-in m [:c :d :e] {:f 42})


; read values
(get m :a)

(:d m)

; maps are functions
(m :d)

(get-in m [:a :e :d])

; delete
(dissoc m :d)
(assoc m :d nil)

; Threading macros ->

(dissoc (update (assoc m :c 42) :a dec) :b)

(-> m
    (assoc :c 42)
    (update :a dec)
    (dissoc :b))

(as-> m $
      (assoc $ :c 42)
      (update $ :a dec)
      (dissoc $ :b))

; iterations over maps

(def m1 {:a 1
         :b 1
         :c 2
         :d 3
         :e 5
         :f 8})

(vals m1)
(keys m1)

(reduce-kv (fn [a k v]
             (+ a v))                                       ; function describing each iteration
           0                                                ; initial value
           m1)                                              ; map to reduce over

(reduce-kv (fn [a k v]
             (if (odd? v)
               (update a k inc)
               a))
           m1
           m1)

; lists and vectors

; list
(def l (list 1 1 2 3 5 8 13 21))

(conj l 0)

; vector
(def v [1 1 2 3 5 8 13 21])

(conj v 34)

(reduce (fn [a v] (+ a v)) 0 (filter even? (map (fn [x] (+ x 3)) l)))

(->> l
     (map (fn [x] (+ x 3)))
     (filter even?)
     (reduce (fn [a v]
               (+ a v))
             0))

(->> l
     (map (fn [x] (+ x 3)))
     (filter even?)
     (reduce +))

(apply + l)

(->> l
     (map (fn [x] (+ x 3)))
     (filter even?)
     (apply +))


; Laziness
(->> (range)
     (filter even?)
     (map (fn [x]
            (println x)
            (inc x)))
     (take 5)
     (apply +))

; vectors becomes lists
(time (->> v
           (map inc)))

(time (->> v
           (mapv inc)))

(let [a 1
      b 34
      c (+ a b)
      a 4]
  (let [a 8]
    (println a b c)
    (+ a b c))
  (println a b c)
  (+ a b c))







































































