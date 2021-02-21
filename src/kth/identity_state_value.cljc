(ns kth.identity-state-value
  (:require [ysera.test :refer [error? is is=]]
            [ysera.error :refer [error]]))

{:talk   "Simple made easy"
 :author "Rich Hickey"
 :why    [{:reason
           "Understanding complexity"
           :take-aways
           ["We should do simple stuff - not the easy stuff"
            "Simple: objective, one fold, not intertwined"
            "Easy: Close to my capabilities"
            "Humans are limited in understanding complex things"
            "Complexity is different from beginners problem - should fix complexity, not beginners problem"
            "Changing software requires understanding and reason about our code (challenging)"
            "Fixing bugs means understanding our code (challenging)"
            "It's a problem arising when the software gets large"]}
          {:reason
           "Information is simple - don't ruin it"
           :take-aways
           ["Hiding it behind a micro language (the class)"
            "Represent data as data"]}
          {:reason
           "Simplicity is a choice"
           :take-aways
           ["Develop sensibility around entanglement"]}]}

{:talk   "Persistent Data Structures and Managed References"
 :author "Rich Hickey"
 :why    [{:reason
           "Defining pure function"
           :take-aways
           ["Depends only on its arguments"
            "Given the same arguments, always returns the same value (memoization!?)"
            "Has no effect on the world"
            "Has no notion of time"]}

          {:reason
           "Describing functional programming"
           :take-aways
           ["Emphasize functions"
            "tremendous benefits [not a fact]"
            "programming with values is critical [not a fact]"]}

          {:reason
           "I wanted you to think about the following"
           :take-aways
           ["local and distributed systems/processes"
            "time in a system, processes might answer differently depending on when"
            "Place oriented programming (PLOP)"
            "Co-located entities can observe each other without cooperation"
            {:title "Race walker foul"
             :texts ["get left foot - off the ground"
                     "get right foot - off the ground"
                     "must be a foul, right?"]}
            "can we copy an object"
            "locking - no user locks in clojure code"
            "can an aggregated thing be a value"
            "'Change' is a function that takes a value and returns another 'changed' value"
            "can you set a month an a date and the date transforms into something new"]}

          {:reason
           "I wanted you to get the answers about"
           :take-aways
           ["memory and efficiency regarding persistent data structures"]}]}


; Create a reference to a state {:counter 0}
(def state-atom (atom {:counter 0}))

; access its state (taking a snapshot)
(def state-1 (deref state-atom))

state-1

(deref state-atom)

; change refs - not the values
; reset the ref to a given value (independent of the old value, like assoc)
(reset! state-atom {:counter 4})


; An updating pure function
(defn inc-counter
  {:test (fn []
           (is= (inc-counter {:counter 10})
                {:counter 11}))}
  [state]
  (update state :counter inc))

; swap the value with a pure function of the old value (like update)
(swap! state-atom inc-counter)


; swapping with a function that ends up in an error do not change the state-atom
(defn broken-inc-counter
  {:test (fn []
           (error? (broken-inc-counter {:counter 10})))}
  [state]
  (error "I love the number I have."))

(comment
  (swap! state-atom broken-inc-counter)
  )








; Spin loop updates (from 2nd video)

(def counter-atom (atom 0))

; Version 1 - mutable objects without any protection

(dotimes [_ 10]
  (future (dotimes [_ 10000]
            (let [v (deref counter-atom)]
              (reset! counter-atom (inc v))))))

(deref counter-atom)


; Version 2 - compare and swap/set

(dotimes [_ 10]
  (future (dotimes [_ 10000]
            (let [v (deref counter-atom)]
              (compare-and-set! counter-atom v (inc v))))))

(deref counter-atom)


; Version 3 - compare and swap recursively - done!

(dotimes [_ 10]
  (future (dotimes [_ 10000]
            (loop [current-value (deref counter-atom)]
              (when-not (compare-and-set! counter-atom current-value (inc current-value))
                (recur (deref counter-atom)))))))

(deref counter-atom)


; Version 4 - swap! does it for us

(reset! counter-atom 0)

(dotimes [_ 10]
  (future (dotimes [_ 10000]
            (swap! counter-atom inc))))

(deref counter-atom)












; Identity - state - value
; The identity
(def entity-atom (atom {:name   "Imp"
                        :attack 1
                        :health 1
                        :id     "i"}))

; The state of the identity ...

(def entity-1 (deref entity-atom))

; ... being a value

entity-1

; Change the identity with a pure function of the past

(swap! entity-atom (fn [entity] (update entity :attack inc)))
(swap! entity-atom update :attack inc)

(deref entity-atom)

; The state has now become a new value

(def entity-2 (deref entity-atom))

; They are not equal
(= entity-1 entity-2)



; Example of (harmless) encapsulated state. [memoization]

(defn remember [f]
  (let [memory-atom (atom {})]
    (fn [& args]
      (if-let [result (get (deref memory-atom) args)]
        result
        (let [result (apply f args)]
          (swap! memory-atom assoc args result)
          result)))))


(defn fibonacci
  {:test (fn []
           (is= (fibonacci 0) 1)
           (is= (fibonacci 1) 1)
           (is= (fibonacci 2) 2)
           (is= (fibonacci 4) 5)
           (is= (fibonacci 8) 34))}
  [n]
  (if (<= n 1)
    1
    (+ (fibonacci (dec n))
       (fibonacci (dec (dec n))))))

(time (fibonacci 40))

(def fibonacci (remember fibonacci))

(time (fibonacci 40))

(= [0 0] [0 0])

(= remember memoize)

; Validators

(comment
  (set-validator! state-atom
                  (fn [state]
                    (not= (:counter state) 6)))

  (swap! state-atom update :counter inc)
  )


; Watchers
(comment
  (add-watch state-atom
             :name-1
             (fn [_ _ old-value new-value]
               (println old-value)
               (println new-value)
               (println "YEAH")))

  (reset! state-atom {:counter 7})

  (remove-watch state-atom :name-1)
  )



