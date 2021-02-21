(ns kth.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]))

; Working with basic types

; Using double :: in a keyword it will be a namespaced keyword

(= ::something :kth.spec/something)
; => true

; You define a spec with def in the spec library (imported as s)

(s/def ::attack int?)

; You can not use s/valid? to check if a value is valid according to the spec.

(s/valid? ::attack 5)
; => true
(s/valid? ::attack [13 35])
; => false


(s/def ::sleepy boolean?)
(s/valid? ::sleepy true)
; => true


(s/def ::id string?)
(s/valid? ::id "m-12")
; => true


; Belonging to a set of values
(s/def ::rarity #{:common :rare :epic :legendary})

(s/valid? ::rarity :epic)
; => true
(s/valid? ::rarity :custom)
; => false

; If you do not understand why a value is not valid you can ask clojure to explain it

(s/explain ::rarity :custom)
; => :custom - failed: #{:common :epic :legendary :rare} spec: :kth.spec/rarity

; This can be useful when the spec is complicated


; Union types - extending ::attack to also include nil
(s/def ::maybe-attack (s/or :a-value ::attack
                            :nothing nil?))
(s/valid? ::maybe-attack 5)
; => true
(s/valid? ::maybe-attack nil)
; => true
(s/valid? ::maybe-attack "5")
; => false

; The reason why we had to put branch names in the s/or definition is to have a way of
; talking about the branches.

(s/explain ::maybe-attack "5")
; => "5" - failed: int? at: [:a-value] spec: :kth.spec/attack
;    "5" - failed: nil? at: [:nothing] spec: :kth.spec/maybe-attack

; You can also check if a value conforms to a spec with s/conform.
; If it doesn't conform it will say that the value is invalid
(s/conform ::maybe-attack "5")
; => :clojure.spec.alpha/invalid
; However if it conforms it will return the value and the branch it took to get there
(s/conform ::maybe-attack 5)
; => [:a-value 5]


; Creating a custom spec as a one variable function
(s/def ::position (s/and int?
                         (fn [x] (<= 0 x 6))))

(s/explain ::position "10")
; => "10" - failed: int? spec: :kth.spec/position
(s/explain ::position 10)
; => 10 - failed: (fn [x] (<= 0 x 6)) spec: :kth.spec/position

(s/valid? ::position 4)
; => true



; Working with objects/maps
(s/def ::minion (s/keys :req-un [::attack
                                 ::id
                                 ::position]
                        :opt-un [::rarity]))

(s/valid? ::minion {:id        "m-12"
                    :attack    3
                    :position  5
                    :destroyed false})
; => true

; It will always accept more key-values. The spec only describes what has to be present.
; The optional key means that, if this key is present then it has to fulfill the given spec.

(s/valid? ::minion {:id        "m-12"
                    :attack    3
                    :position  5
                    :destroyed false
                    :rarity    3})
; => false

(s/explain ::minion {:id        "m-12"
                     :attack    3
                     :position  5
                     :destroyed false
                     :rarity    3})
; => 3 - failed: #{:common :epic :legendary :rare} in: [:rarity] at: [:rarity] spec: :kth.spec/rarity

(s/explain ::minion {:id "m-12" :position 5 :rarity :epic})
; => {:id "m-12", :rarity :epic} - failed: (contains? % :attack) spec: :kth.spec/minion

(s/explain-str ::minion {:id "m-12" :position 5 :rarity :epic})
; Will result in a string representation of the error

(s/explain-data ::minion {:id "m-12" :position 5 :rarity :epic})
; Will result in a data representation of the error
; => #:clojure.spec.alpha{:problems ({:path [],
;                                     :pred (clojure.core/fn [%] (clojure.core/contains? % :attack)),
;                                     :val {:id "m-12", :rarity :epic},
;                                     :via [:kth.spec/minion],
;                                     :in []}),
;                         :spec :kth.spec/minion,
;                         :value {:id "m-12", :rarity :epic}}


(clojure.repl/doc ::minion)
; -------------------------
; :kth.spec/minion
; Spec
;  (keys :req-un [:kth.spec/id :kth.spec/attack] :opt-un [:kth.spec/rarity])

; The clojure core code is also implementing spec, hence
(clojure.repl/doc fn)
; => clojure.core/fn ...
; will give you the full spec of how a function is defined.



; Working with collections
(s/def ::minions (s/and (s/coll-of ::minion)
                        (fn [minions]
                          (= (sort-by :position minions)
                             minions))))

(s/valid? ::minions [{:id "m1" :attack 1 :position 1}
                     {:id "m2" :attack 1 :position 0}])
; => false

(s/explain ::minions [{:id "m1" :attack 1 :position 1}
                      {:id "m2" :attack 1 :position 0}])
; => [{:id "m1", :attack 1, :position 1} {:id "m2", :attack 1, :position 0}]
;    - failed: (fn [minions] (= (sort-by :position minions) minions)) spec: :kth.spec/minions

(s/valid? ::minions [{:id "m1" :attack 1 :position 0}
                     {:id "m2" :attack 1 :position 1}])
; => true

(s/explain ::minions [{:id "m1" :attack 1 :position 1}
                      {:id "m2" :attack 1 :position 10}])
; => 10 - failed: (fn [x] (<= 0 x 6)) in: [1 :position] at: [:position] spec: :kth.spec/position


; How to use clojure in the code base
(defn adding-ints
  [& args]
  {:pre [(s/valid? (s/coll-of int?) args)]}
  (apply + args))

(adding-ints 43 53 32)
; => 128

(comment
  (adding-ints "a" 3 "b")
  ; => Assert failed: (s/valid? (s/coll-of int?) args)
  )


; Spec'ing functions

(s/fdef adding-ints
        :args (s/coll-of pos-int?)
        :ret (s/and int?
                    (fn [x] (>= x 0)))
        :fn (fn [{args :args ret :ret}]
              (= (apply + args)
                 ret)))

(adding-ints 10 -10)
; => 0

(comment
  (stest/instrument 'kth.spec/adding-ints)
  )

; After loading instrument we get errors if we call adding-ints incorrect

(adding-ints 10 -10)
; Syntax error (ExceptionInfo) compiling at (clojure_notes_05_spec_1.cljc:195:1).
;Call to #'kth.spec/adding-ints did not conform to spec.