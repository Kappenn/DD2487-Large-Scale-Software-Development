(ns kth.tutorial-4
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]))

; Predicates (using set as a predicate)
(int? 9)
(string? "thing")

; sets can be used as "predicates"
(#{1 5 6} 5)
(contains? #{1 5 6} 6)
(filter #{1 5 6} [1 5 6 8])

; Namespaces (namespaced keywords)
:kth.tutorial-4/attack
::attack

; nothing funky going on here, just :: syntax
(= ::attack :kth.tutorial-4/attack)

; Registry (s/def, s/registry, doc)
(s/def :minion/attack int?)

(->> (s/registry)
     (keys)
     (filter (fn [kw]
               (= "minion" (namespace kw)))))

; Checking stuff (s/valid?, s/conform, s/explain)
(s/valid? :minion/attack 3)
(s/valid? :minion/attack {})

; explain what possibly went wrong
(s/explain :minion/attack 3)
(s/explain :minion/attack {:a 2})

; Composing predicates (s/and, s/or)
(s/def :minion/health (s/and int? pos?))

(s/valid? :minion/health 5)
(s/valid? :minion/health -1)

(s/def :minion/attack-buff (s/or :buff int? :nil nil?))
(s/valid? :minion/attack-buff 4)
(s/valid? :minion/attack-buff nil)

; Maps (s/keys)
(s/def ::minion (s/keys :req-un [:minion/attack
                                 :minion/health]
                        :opt-un [:minion/attack-buff]))

; only check what's required
(s/valid? ::minion {:health 3
                    :attack 4
                    :garbage "aksdkdm"
                    :attack-buff 1})

; notice how we only spec the attributes (keys) and not their values

; Collections (s/coll-of, s/map-of, s/tuple)
(s/def :minion/attack-buffs (s/coll-of :minion/attack-buff
                                       :count 3
                                       :kind vector?))

(s/valid? :minion/attack-buffs [1 2 3])
(s/valid? :minion/attack-buffs '(1 2 3))
(s/valid? :minion/attack-buffs #{7 8 9})

; :count = 3
(s/valid? :minion/attack-buffs [1 2 3 4])

; spec'ing functions using :pre and :post
(defn get-attack
  [minion]
  {:pre [(s/valid? ::minion minion)]}
  (:attack minion))

(get-attack {:attack 4 :health 4})

; spec'ing functions using fdef (s/cat, s/alt, :args, :ret, :fn)
(s/fdef get-attack
        :args (s/cat :minion ::minion)
        :ret int?)

; turns on function spec checking
(comment
  (stest/instrument))

; Instrumentation (stest/instrument)

; Generators (s/gen, gen/generate, gen/sample)
(gen/generate (s/gen :minion/attack))
(gen/generate (s/gen :minion/health))
(gen/sample (s/gen :minion/health))
(gen/generate (s/gen ::minion))

; Exercise (s/exercise)
(s/exercise-fn `get-attack)
