(ns kth.feedback-9
  (:require [ysera.random :refer [get-random-int]]))

{:feedback "After sprint 2"
 :general-problems
           [{:problem :have-you-missed-class?}
            {:problem :no-cards-in-the-engine} ; in tests of course
            {:problem :randomness}
            {:problem :where-card-functionality-should-be-implemented} ; how do we handle new requirements
            {:problem :flow-vs-data} ; OO is close to the flow - FP is based on facts. Look at example=edwin
            {:problem :facts-vs-decisions}
            {:problem :wrong-abstraction}
            {:problem :not-optimizing-code-in-cards}]}

; Wanted to add frozen because the buff remains more than 1 turn




(->> (range 10)
     (map (fn [_] (rand-int 10))))

(let [state {:seed 1}]
  (->> (range 10)
       (reduce (fn [[state ints] _]
                 (let [[new-seed random-int] (get-random-int (:seed state) 10)]
                   [(assoc state :seed new-seed) (conj ints random-int)]))
               [state []])))


{"name"
 (let [on-heal-fn (fn [])]
   {:attack 5
    :on-character-heal on-heal-fn
    :on-minion-heal on-heal-fn})}




