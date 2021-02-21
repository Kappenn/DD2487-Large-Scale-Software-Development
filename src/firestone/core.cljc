(ns firestone.core
  (:require
    [ysera.test :refer [is is-not is= error?]]
    [ysera.error :refer [error]]
    [ysera.collections :refer [seq-contains?]]
    [ysera.random :refer [random-nth get-random-int]]
    [firestone.definitions :refer [get-definition
                                   get-definitions]]
    [firestone.construct :refer [add-cards-to-deck
                                 add-cards-to-hand
                                 add-card-to-deck
                                 add-card-to-hand
                                 add-minions-to-board
                                 add-minions-to-graveyard
                                 add-minion-to-board
                                 add-minion-to-graveyard
                                 create-card
                                 create-empty-state
                                 create-game
                                 create-hero
                                 create-minion
                                 get-cards
                                 get-deck
                                 get-hero
                                 get-heroes
                                 get-hero-class
                                 get-minion
                                 get-minion-value-by-position-index
                                 get-minions
                                 get-next-player-id-in-turn
                                 get-player
                                 generate-id
                                 remove-minion
                                 update-minion
                                 ]]))


(declare get-card-from-hand, player-take-card-from-deck, move-top-card-from-deck-to-hand)

(defn get-seed
  "Gets the seed from the state"
  {:test (fn []
           (is= (-> (create-game)
                    (get-seed))
                1))}
  [state]
  (get-in state [:seed])
  )
;TODO update seed to a "true" random number when creating game
(defn update-seed
  "Returns a new state with updated seed"
  {:test (fn []
           (is= (-> (create-game)
                    (update-seed 4)
                    (get-seed))
                4))}
  [state seed]
  (assoc-in state [:seed] seed)
  )
(defn get-minion-ids-on-board
  "Return a list of all minion-ids on the board"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi" :abilities ["not-taunt" "yet-another-ability"])]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo2" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi2" :abilities ["not-taunt" "yet-another-ability"])]}])
                    (get-minion-ids-on-board)

                    )
                '("bo" "fi" "bo2" "fi2"))
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi" :abilities ["not-taunt" "yet-another-ability"])]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo2" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi2" :abilities ["not-taunt" "yet-another-ability"])
                                             (create-minion "Boulderfist Ogre" :id "bo3")
                                             (create-minion "Flame Imp" :id "fi3")]}])
                    (get-minion-ids-on-board)
                    )
                '("bo" "fi" "bo2" "fi2" "bo3" "fi3"))
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi" :abilities ["not-taunt" "yet-another-ability"])]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo2" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi2" :abilities ["not-taunt" "yet-another-ability"])
                                             (create-minion "Boulderfist Ogre" :id "bo3")
                                             (create-minion "Flame Imp" :id "fi3")]}])
                    (get-minion-ids-on-board "p1")
                    )
                '("bo" "fi"))
           )}
  ([state]
   (let [player-ids ["p1" "p2"]]
     (-> (reduce (fn [acc player-id]
                   (conj acc (-> (get-minions state player-id)
                                 (->> (into [] (map :id)))
                                 )))

                 [] player-ids)
         (flatten)
         (vec)))
   )
  ([state player-id]
   (-> (conj [] (-> (get-minions state player-id)
                    (->> (into [] (map :id)))
                    ))
       (flatten)
       (vec))
   )
  )


(defn get-character-or-card
  "Returns the character with the given id from the state."
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1")}])
                    (get-character-or-card "h1")
                    (:name))
                "Anduin Wrynn")
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-character-or-card "bo")
                    (:name))
                "Boulderfist Ogre")
           (is= (-> (create-game [{:hand [(create-card "Fireball" :id "fb")] :minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-character-or-card "fb")
                    (:name))
                "Fireball"))}
  [state id]
  (or (some (fn [m] (when (= (:id m) id) m))
            (get-minions state))
      (some (fn [h] (when (= (:id h) id) h))
            (get-heroes state))
      (some (fn [c] (when (= (:id c) id) c))
            (get-cards state))))

(defn get-minion-value
  "Returns value in minion given minion-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-minion-value "bo" :entity-type))
                :minion))}
  [state minion-id key]
  (get-in (get-minion state minion-id) [key]))

(defn get-minion-buff
  "get minions buffs by minion-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion "bo" :buffs :attack inc)
                    (get-minion-buff "bo" :attack))
                1))}
  [state id buff]
  (buff (get-minion-value state id :buffs)))

(defn get-minion-buffs
  "get minions buffs by minion-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion "bo" :buffs :attack inc)
                    (get-minion-buffs "bo"))
                {:attack 1, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion "bo" :buffs :tmp-attack inc)
                    (get-minion-buffs "bo"))
                {:attack 0, :health 0, :tmp-attack 1, :tmp-health 0 :aura-attack 0}))}
  [state id]
  (get-minion-value state id :buffs))

(defn update-minion-buff-health
  "updated a minions attack buff value by minion-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion-buff-health "bo" inc)
                    (get-minion-buffs "bo"))
                {:attack 0, :health 1, :tmp-attack 0, :tmp-health 0 :aura-attack 0}))}
  [state minion-id function-or-value]
  (update-minion state minion-id :buffs :health function-or-value))

(defn update-minion-buff-attack
  "updated a minions attack buff value by minion-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion-buff-attack "bo" inc)
                    (get-minion-buffs "bo"))
                {:attack 1, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0}))}
  [state minion-id function-or-value]
  (update-minion state minion-id :buffs :attack function-or-value))


(defn update-minion-buff-aura-attack
  "updated a minions aura-attack buff value by minion-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion-buff-aura-attack "bo" inc)
                    (get-minion-buffs "bo"))
                {:attack 0, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 1}))}
  [state minion-id function-or-value]
  (update-minion state minion-id :buffs :aura-attack function-or-value))

(defn update-minions-buff-aura-attack
  "update-all-minions-buff-attack by value"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Faceless Manipulator" :id "fm")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo" :can-attack false)
                                             (create-minion "Unstable Ghoul" :id "ug1")
                                             (create-minion "Unstable Ghoul" :id "ug2")]}])
                    (update-minions-buff-aura-attack ["fm" "bo" "ug1"] inc)
                    (get-minion-buff "bo" :aura-attack))
                1))}
  [state ids value]
  (reduce (fn [state id]
            (update-minion-buff-aura-attack state id value))
          state
          ids))



(defn get-player-value
  {:test (fn []
           (is= (-> (create-game)
                    (get-player-value "p1" :mana-capacity))
                1)
           (is= (as-> (create-game) $
                      (:name (get-player-value $ "p1" :hero)))
                "Uther Lightbringer"))}
  [state player-id key]
  (get-in state [:players player-id key]))


(defn update-minion-can-attack
  "updated a minions can-attack value by minion-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion-can-attack "bo" false)
                    (get-minion-value "bo" :can-attack))
                false))}

  [state minion-id function-or-value]
  (update-minion state minion-id :can-attack function-or-value))


(defn update-minions-can-attack
  "update-all-minions-can-attack by value"
  {:test (fn []
           ;ids "bo" "go" and "vo" should be sleepy and put in minion-ids-summoned-this-turn
           (is= (-> (create-game [{:minions [(create-minion "Faceless Manipulator" :id "fm")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo" :can-attack false)
                                             (create-minion "Unstable Ghoul" :id "ug1")
                                             (create-minion "Unstable Ghoul" :id "ug2")]}])
                    (update-minions-can-attack ["fm" "bo" "ug1"] true)
                    (get-minion-value "bo" :can-attack))
                true))}
  [state ids value]
  (reduce (fn [state ids]
            (update-minion-can-attack state ids value))
          state
          ids))


(defn update-minion-can-attack-by-minion-definition
  "updated a minions can-attack value by minion-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Argent Watchman" :id "aw")]}])
                    (update-minion-can-attack "aw" false)
                    (update-minion-can-attack-by-minion-definition "aw")
                    (get-minion-value "aw" :can-attack))
                false)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion-can-attack "bo" false)
                    (update-minion-can-attack-by-minion-definition "bo")
                    (get-minion-value "bo" :can-attack))
                true))}

  [state minion-id]
  (let [minion (get-minion state minion-id)
        minion-definition (get-definition (:name minion))]
    (update-minion state minion-id :can-attack (:can-attack minion-definition))))

(defn update-minions-can-attack-by-minion-definition
  "update-all-minions-can-attack by value"
  {:test (fn []
           ;ids "bo" "go" and "vo" should be sleepy and put in minion-ids-summoned-this-turn
           (is= (-> (create-game [{:minions [(create-minion "Faceless Manipulator" :id "fm")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo" :can-attack false)
                                             (create-minion "Unstable Ghoul" :id "ug1")
                                             (create-minion "Unstable Ghoul" :id "ug2")]}])
                    (update-minions-can-attack-by-minion-definition ["fm" "bo" "ug1"])
                    (get-minion-value "bo" :can-attack))
                true))}
  [state ids]
  (reduce (fn [state ids]
            (update-minion-can-attack-by-minion-definition state ids))
          state
          ids))

(defn get-player-mana
  {:test (fn []
           (is= (-> (create-game)
                    (get-player-mana "p1"))
                1))}
  [state player-id]
  (get-in state [:players player-id :mana]))

(defn get-player-mana-capacity
  {:test (fn []
           (is= (-> (create-game)
                    (get-player-mana-capacity "p1"))
                1))}
  [state player-id]
  (get-in state [:players player-id :mana-capacity]))


(defn get-player-fatigue-counter
  {:test (fn []
           (is= (-> (create-game)
                    (get-player-fatigue-counter "p1"))
                0))}
  [state player-id]
  (get-in state [:players player-id :fatigue-counter]))


(defn get-player-hero-by-player-id
  "gets players hero by player id"
  {:test (fn []
           (is= (-> (create-game)
                    (get-player-hero-by-player-id "p1")
                    (get :name))
                "Uther Lightbringer"))}
  [state player-id]
  (get-in state [:players player-id :hero]))


(defn get-player-graveyard
  "gets players graveyard by player-id"
  {:test (fn []
           ;should be two minions in graveyard
           (is= (as-> (create-game [{:graveyard [(create-minion "Boulderfist Ogre")
                                                 "Injured Blademaster"]}]) $
                      (get-player-graveyard $ "p1")
                      (map :name $))
                ["Boulderfist Ogre" "Injured Blademaster"])
           ;graveyard is empty
           (is= (-> (create-game)
                    (get-player-graveyard "p1"))
                []))}
  [state player-id]
  (get-in state [:players player-id :graveyard]))



(defn get-players-graveyard
  "gets both players graveyard"
  {:test (fn []
           ;should be 3 minions in graveyard
           (is= (as-> (create-game [{:graveyard [(create-minion "Boulderfist Ogre")
                                                 "Injured Blademaster"]}
                                    {:graveyard [(create-minion "Unstable Ghoul")]}]) $
                      (get-players-graveyard $)
                      (map :name $))
                ["Boulderfist Ogre" "Injured Blademaster" "Unstable Ghoul"])
           ;graveyard is empty
           (is= (-> (create-game)
                    (get-players-graveyard))
                []))}
  [state]
  (let [p1-graveyard (get-player-graveyard state "p1")
        p2-graveyard (get-player-graveyard state "p2")]
    (concat p1-graveyard p2-graveyard)))


(defn get-player-graveyard-ids
  "returns minion-ids from graveyard given player id"
  {:test (fn []
           ;should be 2 minions in graveyard
           (is= (-> (create-game [{:graveyard [(create-minion "Boulderfist Ogre" :id "bo")]}
                                  {:graveyard [(create-minion "Unstable Ghoul" :id "ug")]}])
                    (get-player-graveyard-ids "p1"))
                ["bo"])
           ;graveyard is empty
           (is= (-> (create-game)
                    (get-player-graveyard-ids "p1"))
                []))}
  [state player-id]
  (let [minions-on-players-graveyard (get-player-graveyard state player-id)]
    (map :id minions-on-players-graveyard)))



(defn get-players-graveyard-ids
  "returns ids of minions in both players graveyard"
  {:test (fn []
           ;should be 2 minions in graveyard
           (is= (as-> (create-game [{:graveyard [(create-minion "Boulderfist Ogre" :id "bo")]}
                                    {:graveyard [(create-minion "Unstable Ghoul" :id "ug")]}]) $
                      (get-players-graveyard-ids $))
                ["bo" "ug"])
           ;graveyard is empty
           (is= (-> (create-game)
                    (get-players-graveyard-ids))
                []))}
  [state]
  (let [player-ids ["p1" "p2"]]
    (-> (reduce (fn [acc player-id]
                  (conj acc (-> (get-player-graveyard state player-id)
                                (->> (into [] (map :id)))
                                )))
                [] player-ids)
        (flatten)
        (vec))))



(defn get-minion-from-graveyard-given-id
  "Returns the minion with the given id from graveyard."
  {:test (fn []
           ;should be 2 minions in graveyard
           (is= (as-> (create-game [{:graveyard [(create-minion "Boulderfist Ogre" :id "bo")]}
                                    {:graveyard [(create-minion "Unstable Ghoul" :id "ug")]}]) $
                      (:id (get-minion-from-graveyard-given-id $ "bo")))
                "bo"))}
  [state id]
  (->> (get-players-graveyard state)
       (filter (fn [m] (= (:id m) id)))
       (first)))



(defn is-minion-in-graveyard?
  "check if a minion is on any of the players graveyard"
  {:test (fn []
           ;ug is in graveyard
           (is= (-> (create-game [{:graveyard [(create-minion "Boulderfist Ogre" :id "bo")]}
                                  {:graveyard [(create-minion "Unstable Ghoul" :id "ug")]}])
                    (is-minion-in-graveyard? "ug"))
                true)
           ;gg is not on graveyard
           (is= (-> (create-game [{:graveyard [(create-minion "Boulderfist Ogre" :id "bo")]}
                                  {:graveyard [(create-minion "Unstable Ghoul" :id "ug")]}])
                    (is-minion-in-graveyard? "gg"))
                false))}
  [state minion-id]
  (let [all-minion-ids-in-graveyard (get-players-graveyard-ids state)]

    (if (some #(= minion-id %) all-minion-ids-in-graveyard)
      true
      false)))

(defn remove-minion-from-graveyard
  "Removes a minion with the given id from graveyard."
  {:test (fn []
           (is= (-> (create-game [{:graveyard [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (remove-minion-from-graveyard "p1" "bo")
                    (is-minion-in-graveyard? "bo"))
                false))}
  [state player-id id]
  (update-in state
             [:players player-id :graveyard]
             (fn [minions]
               (remove (fn [m] (= (:id m) id)) minions))))


(defn should-update-mana?
  {:test (fn []
           (is= (-> (create-game)
                    (should-update-mana? "p1"))
                true))}

  [state player-id]
  (< (get-player-value state player-id :mana) 10))

(defn should-update-mana-capacity?
  {:test (fn []
           (is= (-> (create-game)
                    (should-update-mana-capacity? "p1"))
                true))}

  [state player-id]
  (< (get-player-value state player-id :mana-capacity) 10))


(defn update-player-value
  "Returns state given player-id"
  {:test (fn []
           (is= (-> (create-game)
                    (update-player-value "p1" :mana inc)
                    (get-player-value "p1" :mana))
                2)
           (is= (-> (create-game)
                    (update-player-value "p1" :mana 10)
                    (get-player-value "p1" :mana))
                10)
           (is= (-> (create-game)
                    (update-player-value "p1" :mana-capacity 10)
                    (get-player-value "p1" :mana-capacity))
                10)
           (is= (-> (create-game)
                    (update-player-value "p1" :mana-capacity 5)
                    (update-player-value "p1" :mana get-player-value :mana-capacity)
                    (get-player-value "p1" :mana))
                5))}

  ([state player-id key function-or-value]
   (if (fn? function-or-value)
     (update-in state [:players player-id key] function-or-value)
     (assoc-in state [:players player-id key] function-or-value)))

  ([state player-id key function function-key]
   (assoc-in state [:players player-id key] (function state player-id function-key))))

(defn update-player-mana
  "update player mana with value or function"
  {:test (fn []
           (is= (-> (create-game)
                    (update-player-mana "p1" inc)
                    (get-player-mana "p1"))
                2))}
  [state player-id function-or-value]
  (update-player-value state player-id :mana function-or-value))

(defn update-player-mana-capacity
  "update player mana-capacity with value or function"
  {:test (fn []
           (is= (-> (create-game)
                    (update-player-mana-capacity "p1" inc)
                    (get-player-mana-capacity "p1"))
                2))}
  [state player-id function-or-value]
  (update-player-value state player-id :mana-capacity function-or-value))

(defn update-player-fatigue-counter
  "update player fatigue with value or function"
  {:test (fn []
           (is= (-> (create-game)
                    (update-player-fatigue-counter "p1" inc)
                    (get-player-fatigue-counter "p1"))
                1))}
  [state player-id function-or-value]
  (update-player-value state player-id :fatigue-counter function-or-value))




(defn get-health
  "Returns the health of the character."
  {:test (fn []
           ;minion have buff: 2 health
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :buffs {:health 2 :attack 0})]}])
                    (get-health "bo"))
                9)
           ;minion have buff: 5 health and damage taken 3 => 7 + 5 - 3 = 9
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :damage-taken 3 :id "bo" :buffs {:health 5 :attack 0})]}])
                    (get-health "bo"))
                9)
           ; Uninjured minion
           (is= (-> (create-minion "Flame Imp")
                    (get-health))
                2)
           ; Injured minion
           (is= (-> (create-minion "Flame Imp" :damage-taken 1)
                    (get-health))
                1)
           ; Minion in a state
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-health "bo"))
                7)
           ; Uninjured hero
           (is= (-> (create-hero "Anduin Wrynn")
                    (get-health))
                30)
           ; Injured hero
           (is= (-> (create-hero "Anduin Wrynn" :damage-taken 2)
                    (get-health))
                28)
           ; Hero in a state
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1")}])
                    (get-health "h1"))
                30))}
  ([character]
   {:pre [(map? character) (contains? character :damage-taken)]}
   (let [definition (get-definition character)]
     (- (:health definition) (:damage-taken character))))
  ([state id]
   (let [character (get-character-or-card state id)
         entity-type (:entity-type character)
         definition (get-definition character)]
     (if (= entity-type :minion)
       (get-health state id (:buffs character))
       (get-health (get-character-or-card state id)))))
  ([state minion-id buffs]
   (let [character (get-character-or-card state minion-id)
         definition (get-definition character)
         total-health (+ (:health definition) (:health buffs))]
     (- total-health (:damage-taken character)))))


(defn update-mana-after-hero-power
  "Reduces player mana given hero-power"
  {:test (fn []
           (is= (-> (create-game)
                    (update-player-mana "p1" 10)
                    (update-mana-after-hero-power "p1" "Reinforce")
                    (get-player-mana "p1"))
                8))}
  [state player-id hero-power-name]
  (let [mana-cost (:mana-cost (get-definition hero-power-name))]
    (update-player-mana state player-id (- (get-player-mana state player-id) mana-cost))))


(defn update-mana-after-played-card
  "Reduces player mana given card-id"
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Faceless Manipulator" :id "fm")]}])
                    (update-player-mana "p1" 10)
                    (update-mana-after-played-card "p1" "fm")
                    (get-player-mana "p1"))
                5))}
  [state player-id card-id]
  (let [card (get-card-from-hand state player-id card-id)
        mana-cost (:mana-cost (get-definition (:name card)))]
    (update-player-mana state player-id (- (get-player-mana state player-id) mana-cost))))


(defn get-hero-value-by-hero-id
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "aw")}])
                    (get-hero-value-by-hero-id "aw" :entity-type))
                :hero)
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "aw")}])
                    (get-hero-value-by-hero-id "awawdawd" :entity-type))
                nil))}
  [state hero-id key]
  (get-in (get-hero state hero-id) [key]))

(defn get-hero-power-used-by-hero-id
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "aw" :hero-power-used false)}])
                    (get-hero-power-used-by-hero-id "aw"))
                false))}
  [state hero-id]
  (get-hero-value-by-hero-id state hero-id :hero-power-used))



(defn get-player-id-by-character-id
  {:test (fn []
           (is= (-> (create-game)
                    (get-player-id-by-character-id "h1"))
                "p1")
           (is= (-> (create-game)
                    (get-player-id-by-character-id "h2"))
                "p2")
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-player-id-by-character-id "bo"))
                "p1"))}
  [state hero-or-minion-id]
  (let [player1-hero ((get-player-value state "p1" :hero) :id)
        entity-type-is-hero (get-hero-value-by-hero-id state hero-or-minion-id :entity-type)]
    (if entity-type-is-hero
      (if (= player1-hero hero-or-minion-id)
        "p1"
        "p2")
      (get-minion-value state hero-or-minion-id :owner-id))))


(defn get-hero-value
  {:test (fn []
           (is= (-> (create-game)
                    (get-hero-value "p1" :damage-taken))
                0))}

  [state player-id key]
  (get-in state [:players player-id :hero key]))



(defn update-hero-value
  "Returns state with updated hero given player-id"
  {:test (fn []
           (is= (-> (create-game)
                    (update-hero-value "p1" :damage-taken 10)
                    (get-hero-value "p1" :damage-taken))
                10)
           (is= (-> (create-game)
                    (update-player-value "p1" :fatigue-counter inc)
                    (update-hero-value "p1" :damage-taken get-player-value :fatigue-counter)
                    (get-hero-value "p1" :damage-taken))
                1))}

  ([state player-id key function-or-value]
   (if (fn? function-or-value)
     (update-in state [:players player-id :hero key] function-or-value)
     (assoc-in state [:players player-id :hero key] function-or-value)))

  ([state player-id key function function-key]
   (assoc-in state [:players player-id :hero key] (function state player-id function-key))))

(defn update-hero-value-by-hero-id
  "Updates hero given hero-id. NOTE could be generalized to character?"
  {:test (fn []
           (is= (-> (create-game)
                    (update-hero-value-by-hero-id "h1" :damage-taken 10)
                    (get-hero-value-by-hero-id "h1" :damage-taken))
                10)
           (is= (-> (create-game)
                    (update-hero-value-by-hero-id "h1" :hero-power-used 1)
                    (get-hero-value-by-hero-id "h1" :hero-power-used))
                1)
           (is= (-> (create-game)
                    (update-hero-value-by-hero-id "h2" :damage-taken 10)
                    (get-hero-value-by-hero-id "h2" :damage-taken))
                10))}

  ([state hero-id key function-or-value]
   (let [player-id (get-player-id-by-character-id state hero-id)]
     (update-hero-value state player-id key function-or-value))))

(defn update-hero-power-used-by-hero-id
  "Returns a state with updateded hero-power-used. input is hero-id"
  {:test (fn []
           (is= (as-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "aw")}]) $
                      (update-hero-power-used-by-hero-id $ "aw" false)
                      (get-hero-power-used-by-hero-id $ "aw"))
                false)
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "aw")}])
                    (update-hero-power-used-by-hero-id "aw" true)
                    (get-hero-power-used-by-hero-id "aw"))
                true))}
  [state hero-id function-or-value]
  (update-hero-value-by-hero-id state hero-id :hero-power-used function-or-value))


(defn get-damage-taken
  "Returns the damage taken of the character. "
  {:test (fn []
           ; Minion in a state
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :damage-taken 2)]}])
                    (get-damage-taken "bo"))
                2)
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1" :damage-taken 2)}])
                    (get-damage-taken "h1"))
                2))}

  ([state character-id]
   (:damage-taken (get-character-or-card state character-id))))

(defn get-hero-value-by-player-id
  "get heroes value given player-id"
  {:test (fn []
           (is= (-> (create-game)
                    (get-hero-value-by-player-id "p1" :damage-taken))
                0))}

  [state player-id key]
  (get-in state [:players player-id :hero key]))



(defn get-hero-damage-taken-by-player-id
  "get heroes damage-taken given player-id"
  {:test (fn []
           (is= (-> (create-game)
                    (get-hero-damage-taken-by-player-id "p1"))
                0))}
  [state player-id]
  (get-hero-value-by-player-id state player-id :damage-taken))

(defn get-player-minion-ids
  "return ids for players minions on board"
  {:test (fn []
           ;should return ids of player "p1" minions
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib") (create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-player-minion-ids "p1"))
                ["ib" "bo"]))}

  [state player-id]
  (map :id (get-minions state player-id)))



(defn get-player-deck-ids
  "returns card-ids from deck given player id"
  {:test (fn []

           (is= (-> (create-game [{:deck [(create-card "Boulderfist Ogre" :id "bo")
                                          (create-card "Boulderfist Ogre" :id "bo1")]}])
                    (get-player-deck-ids "p1"))
                ["bo" "bo1"])
           (is= (-> (create-game)
                    (get-player-deck-ids "p1"))
                []))}
  [state player-id]
  (let [players-deck (get-deck state player-id)]
    (map :id players-deck)))

(defn get-abilities-from-minions
  "Get all the abilities of the players minions as a list"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi" :abilities ["not-taunt" "yet-another-ability"])]}])
                    (get-abilities-from-minions "p1")
                    (count))
                4)
           )}
  [state player-id]

  (-> (get-minions state player-id)
      (->> (map :abilities)
           (apply concat))))

(defn check-for-ability-in-player
  "Check if a specific player has any minion with the provided ability"
  {:test (fn []
           (is (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                            (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                   (check-for-ability-in-player "p1", :taunt)))
           (is-not (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])]}])
                       (check-for-ability-in-player "p1", :taunt2)))
           )}
  [state player-id ability]
  (as-> (get-abilities-from-minions state player-id) $
        (some #(= ability %) $)))



(defn get-abilities-from-minion
  "Get a minions abilities"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                             (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                    (get-abilities-from-minion "bo")
                    (count))
                2)
           )}
  [state minion-id]
  (-> (get-minion-value state minion-id :abilities)))

(defn update-ability
  "Adds the ability to the minion abilities"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Unstable Ghoul" :id "ug" :attacks-performed-this-turn 1
                                                            :abilities [:taunt :deathrattle])]}])
                    (update-ability "ug" :frozen)
                    (get-minion "ug")
                    (:abilities))
                [:taunt :deathrattle :frozen])
           (is= (-> (create-game [{:minions [(create-minion "Unstable Ghoul" :id "ug" :attacks-performed-this-turn 1
                                                            :abilities [:taunt :deathrattle])]}])
                    (update-ability "ug" :rush)
                    (get-minion "ug")
                    (:abilities))
                [:taunt :deathrattle :rush])
           )}
  [state minion-id ability]
  (let [active-abilities (get-abilities-from-minion state minion-id)
        updated-abilities (into [] (concat active-abilities [ability]))]
    (update-minion state minion-id :abilities updated-abilities)))

(defn remove-ability
  "Removes an ability from a minion given minion-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Unstable Ghoul" :id "ug" :attacks-performed-this-turn 1
                                                            :abilities [:taunt :deathrattle :frozen])]}])
                    (remove-ability "ug" :frozen)
                    (get-minion "ug")
                    (:abilities))
                [:taunt :deathrattle])
           )}
  [state minion-id ability]
  (let [abilities (get-abilities-from-minion state minion-id)
        updated-abilities (remove #(= ability %) abilities)]
    (update-minion state minion-id :abilities updated-abilities)
    ))


(defn update-abilities
  "Adds the ability to the minions abilities given a list of minion-ids"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Unstable Ghoul" :id "ug" :attacks-performed-this-turn 1
                                                            :abilities [:taunt :deathrattle])]}])
                    (update-abilities ["ug", "ib"] :frozen)
                    (get-minion "ug")
                    (:abilities))
                [:taunt :deathrattle :frozen])
           )}
  [state minion-ids ability]
  (reduce (fn [state minion-ids]
            (update-ability state minion-ids ability))
          state
          minion-ids))

(defn check-for-ability-in-minion
  "Check if a specific minion has the ability true/false"
  {:test (fn []
           (is (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                            (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                   (check-for-ability-in-minion "bo", :taunt)))
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                             (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                    (check-for-ability-in-minion "bo", :taunt))
                true)
           (is-not (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])]}])
                       (check-for-ability-in-minion "bo", :taunt2)
                       ))
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])]}])
                    (check-for-ability-in-minion "bo", :taunt2)
                    )
                false))}
  [state minion-id ability]
  (as-> (get-abilities-from-minion state minion-id) $
        (if (some #(= ability %) $)
          true
          false
          )))

(defn has-aura?
  "returns true if the minion has aura-effect "
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Tinkmaster Overspark" :id "to" :abilities [:aura])
                                             (create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Unstable Ghoul" :id "ug")]}])
                    (has-aura? "to"))
                true)
           (is= (-> (create-game [{:minions [(create-minion "Tinkmaster Overspark" :id "to" :abilities [:aura])
                                             (create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Unstable Ghoul" :id "ug")]}])
                    (has-aura? "bo"))
                false)
           )}
  [state minion-id]
  (if (check-for-ability-in-minion state minion-id :aura)
    true
    false))

(defn get-minions-with-ability
  "Returns a list with all the minions with a specific ability for a given player"
  {:test (fn []
           ;One minion has the ability should return one minion
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                             (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                    (get-minions-with-ability "p1", :taunt))
                ["bo"])
           ;multiple minion has the ability should return multiple minion
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                             (create-minion "Flame Imp" :id "fi" :abilities [:taunt :yet-another-ability])
                                             (create-minion "Flame Imp" :id "fi2" :abilities [:taunt :yet-another-ability])]}])
                    (get-minions-with-ability "p1", :taunt))
                ["bo", "fi", "fi2"])
           ;Opponent also has the same, should still only return the current players minions
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                             (create-minion "Flame Imp" :id "fi" :abilities [:taunt :yet-another-ability])
                                             (create-minion "Flame Imp" :id "fi2" :abilities [:taunt :yet-another-ability])]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo4" :abilities [:taunt :another-ability])
                                             (create-minion "Flame Imp" :id "fi4" :abilities [:taunt :yet-another-ability])
                                             (create-minion "Flame Imp" :id "fi5" :abilities [:taunt :yet-another-ability])]}])
                    (get-minions-with-ability "p1", :taunt))
                ["bo", "fi", "fi2"])
           ;Player2s minions with taunt should be returned
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                             (create-minion "Flame Imp" :id "fi" :abilities [:taunt :yet-another-ability])
                                             (create-minion "Flame Imp" :id "fi2" :abilities [:taunt :yet-another-ability])]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo4" :abilities [:taunt :another-ability])
                                             (create-minion "Flame Imp" :id "fi4" :abilities [:taunt :yet-another-ability])
                                             (create-minion "Flame Imp" :id "fi5" :abilities [:taunt :yet-another-ability])]}])
                    (get-minions-with-ability "p2", :taunt))
                ["bo4", "fi4", "fi5"])
           )}
  [state player-id ability]
  (let [minion-ids (get-player-minion-ids state player-id)]
    (reduce (fn [acc minion-id]
              (if (check-for-ability-in-minion state minion-id ability)
                (conj acc minion-id)
                acc))
            [] minion-ids)))

(defn get-active-abilities-from-minion
  "Get a minions abilities"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :active-abilities [:taunt :another-ability])
                                             (create-minion "Flame Imp" :id "fi" :active-abilities [:not-taunt :yet-another-ability])]}])
                    (get-active-abilities-from-minion "bo")
                    (count))
                2)
           )}
  [state minion-id]
  (-> (get-minion-value state minion-id :active-abilities)))

(defn update-active-abilities
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Unstable Ghoul" :id "ug" :attacks-performed-this-turn 1
                                                            :active-abilities [:taunt :deathrattle])]}])
                    (update-active-abilities "ug" :frozen)
                    (get-minion "ug")
                    (:active-abilities))
                [:taunt :deathrattle :frozen])
           )}
  [state minion-id ability]
  (let [active-abilities (get-active-abilities-from-minion state minion-id)
        updated-abilities (into [] (concat active-abilities [ability]))]
    (update-minion state minion-id :active-abilities updated-abilities)
    ))

(defn remove-active-ability
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Unstable Ghoul" :id "ug" :attacks-performed-this-turn 1
                                                            :active-abilities [:taunt :deathrattle])]}])
                    (remove-active-ability "ug" :frozen)
                    (get-minion "ug")
                    (:active-abilities))
                [:taunt :deathrattle])
           )}
  [state minion-id ability]
  (let [active-abilities (get-active-abilities-from-minion state minion-id)
        updated-abilities (remove #(= ability %) active-abilities)]
    (update-minion state minion-id :active-abilities updated-abilities)
    ))


(defn run-event
  "Run a minions event"
  {:test (fn []
           ;Should randomly pick an enemy character and deal 8 damage. p2 only has hero so its hero will get 8 damage-taken
           (is= (-> (create-game [{:minions [(create-minion "Ragnaros the Firelord" :id "rtf")]}]
                                 :seed 2)
                    (run-event "rtf" :end-turn-event)
                    (get-hero-damage-taken-by-player-id "p2"))
                8)
           (is= (-> (create-game [{:minions [(create-minion "Ragnaros, Lightlord" :id "rll" :damage-taken 2)
                                             (create-minion "Ragnaros, Lightlord" :id "r22" :damage-taken 10)
                                             (create-minion "Nat, the Darkfisher" :id "nat" :damage-taken 10)]}
                                  {:minions [(create-minion "Nat, the Darkfisher" :id "bo" :damage-taken 8)]}]
                                 :seed 2)
                    (run-event "rll" :end-turn-event)
                    (get-damage-taken "r22"))
                2)
           (is= (-> (create-game [{:minions [(create-minion "Argent Watchman" :id "aw" :can-attack true)]}])
                    (run-event "aw" :end-turn-event)
                    (get-minion-value "aw" :can-attack))
                false)
           ;Should randomly pick an enemy character and deal 8 damage.
           (is= (-> (create-game [{:minions [(create-minion "Ragnaros the Firelord" :id "rtf")]}
                                  {:minions [(create-minion "Ragnaros the Firelord" :id "rtf2")
                                             (create-minion "Ragnaros the Firelord" :id "rtf3")]}]
                                 :seed 2)
                    (update-minion-buff-health "rtf3" 10)
                    (run-event "rtf" :end-turn-event)
                    (get-damage-taken "rtf3")
                    )
                8)
           ;Should get a card
           (is= (-> (create-game [{:minions [(create-minion "Nat, the Darkfisher" :id "nat")]}
                                  {:deck ["Fireball" "Injured Blademaster" "Injured Blademaster"]}]
                                 :seed 1)
                    (run-event "nat" :end-turn-event)
                    (get-cards "p2")
                    (count)
                    )
                1)
           )}
  ([state minion-id event]
   (let [minion-name (get-minion-value state minion-id :name)
         event-function (event (get-definition minion-name))]
     (event-function state minion-id)))
  ([state minion-id event character-id-that-fired-this-event]
   (let [minion-name (get-minion-value state minion-id :name)
         event-function (event (get-definition minion-name))]
     (event-function state minion-id character-id-that-fired-this-event))))



(defn run-events
  "Runs an event on all of the minions that has this event of a player"
  {:test (fn []
           ;One Ragnaros the Firelord should deal 8 dmg to the enemy hero
           (is= (-> (create-game [{:minions [(create-minion "Ragnaros the Firelord" :id "rtf")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}]
                                 :seed 2)
                    (run-events "p1" :end-turn-event)
                    (get-hero-damage-taken-by-player-id "p2"))
                8)
           ;Two Ragnaros the Firelord should deal 16 dmg to the enemy hero
           (is= (-> (create-game [{:minions [(create-minion "Ragnaros the Firelord" :id "rtf")
                                             (create-minion "Ragnaros the Firelord" :id "rtf2")]}]
                                 :seed 2)
                    (run-events "p1" :end-turn-event)
                    (get-hero-damage-taken-by-player-id "p2"))
                16))}
  ([state player-id event]
   (let [minions-with-event (get-minions-with-ability state player-id event)]

     (->> minions-with-event
          (reduce-kv (fn [state _ minion]
                       (run-event state
                                  minion
                                  event))
                     state))))
  ([state player-id event character-id-that-fired-this-event]
   (let [minions-with-event (get-minions-with-ability state player-id event)]

     (->> minions-with-event
          (reduce-kv (fn [state _ minion]
                       (run-event state
                                  minion
                                  event
                                  character-id-that-fired-this-event))
                     state)))))

(defn run-aura-function
  "Runs the minions aura-function on the target"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Flame Imp" :id "ai")
                                             (create-minion "Dire Wolf Alpha" :id "dwa")
                                             (create-minion "Boulderfist Ogre" :id "bo")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo" :buffs {:health 2 :attack 0 :tmp-health 1 :aura-attack 5})]}]
                                 )
                    (run-aura-function "dwa" "bo")
                    (get-minion-buff "bo" :aura-attack))
                1))}
  [state minion-id target-id]
  (run-event state minion-id :aura target-id))

(defn remove-aura-effect-on-all-minions
  "Sets aura-attack to zero on every minion on the board"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Ragnaros the Firelord" :id "rtf")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo" :buffs {:health 2 :attack 0 :tmp-health 1 :aura-attack 5})]}]
                                 )
                    (remove-aura-effect-on-all-minions)
                    (get-minion-buff "bo" :aura-attack))
                0))}
  [state]
  (let [all-minions-on-board (get-minion-ids-on-board state)]
    (update-minions-buff-aura-attack state all-minions-on-board 0)))

(defn update-aura-effect-from-neighbours
  "Updates the aura effect on minion-id from the neighbours"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Flame Imp" :id "ai")
                                             (create-minion "Dire Wolf Alpha" :id "dwa")
                                             (create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-aura-effect-from-neighbours "ai")
                    (get-minion-buff "ai" :aura-attack))
                1)
           (is= (-> (create-game [{:minions [(create-minion "Dire Wolf Alpha" :id "dwa")
                                             (create-minion "Flame Imp" :id "ai")
                                             (create-minion "Dire Wolf Alpha" :id "dwa2")
                                             (create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-aura-effect-from-neighbours "ai")
                    (get-minion-buff "ai" :aura-attack))
                2))}
  [state minion-id]
  (let [player-id (get-player-id-by-character-id state minion-id)
        minion-pos (get-minion-value state minion-id :position)
        left-neighbour-id (get-minion-value-by-position-index state player-id (- minion-pos 1) :id)
        right-neighbour-id (get-minion-value-by-position-index state player-id (+ minion-pos 1) :id)]
    (as-> state $
          (if (and left-neighbour-id (has-aura? $ left-neighbour-id))
            (run-aura-function $ left-neighbour-id minion-id)
            $)
          (if (and right-neighbour-id (has-aura? $ right-neighbour-id))
            (run-aura-function $ right-neighbour-id minion-id)
            $))))

(defn update-aura-effect-on-all-minions-on-board
  "Updates aura-buff for all characters on board"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Flame Imp" :id "ai")
                                             (create-minion "Dire Wolf Alpha" :id "dwa")
                                             (create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-aura-effect-from-neighbours "ai")
                    (get-minion-buff "ai" :aura-attack))
                1))}
  [state]
  (let [all-minion-ids-on-board (get-minion-ids-on-board state)]
    (reduce update-aura-effect-from-neighbours state all-minion-ids-on-board)))
(defn run-aura-event
  "Removes all the aura-buffs from all character and then updates aura-buff for all characters again"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Flame Imp" :id "ai")
                                             (create-minion "Dire Wolf Alpha" :id "dwa")
                                             (create-minion "Boulderfist Ogre" :id "bo" :buffs {:aura-attack 100})]}])
                    (run-aura-event)
                    (get-minion-buff "bo" :aura-attack))
                1))}
  [state]
  (-> (remove-aura-effect-on-all-minions state)
      (update-aura-effect-on-all-minions-on-board)))

(defn run-board-change-events
  "Run all minions aura effects"
  {:test (fn []
           ;One Ragnaros the Firelord should deal 8 dmg to the enemy hero
           (is= (-> (create-game [{:minions [(create-minion "Flame Imp" :id "fi")
                                             (create-minion "Dire Wolf Alpha" :id "dwa")
                                             (create-minion "Boulderfist Ogre" :id "bo")]}])
                    (run-board-change-events)
                    (get-minion-buff "bo" :aura-attack))
                1)
           (is= (-> (create-game [{:minions [(create-minion "Flame Imp" :id "fi")
                                             (create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Dire Wolf Alpha" :id "dwa")]}])
                    (run-board-change-events)
                    (get-minion-buff "fi" :aura-attack))
                0))}
  [state]
  (run-aura-event state))

(defn run-hero-take-damage-on-own-turn-events
  "Runs the all the hero-take-damage-event of a player"
  {:test (fn []
           ;lw2 should have + 3 attack
           (is= (-> (create-game [{:minions [(create-minion "Floating Watcher" :id "fw")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (run-hero-take-damage-on-own-turn-events "p1")
                    (get-minion-buff "fw" :attack))
                3))}
  [state player-id]
  (let [player-id-this-turn (:player-id-in-turn state)]
    (if (= player-id-this-turn player-id)
      (run-events state player-id :hero-take-damage-on-own-turn-event)
      (throw (Exception. "Not allowed to run hero-take-damage-on-own-turn-event if it's not our own turn")))))

(defn run-damage-event
  "Run a minions damage-turn event"
  {:test (fn []
           ;Should randomly pick an enemy character and deal 8 damage. p2 only has hero so its hero will get 8 damage-taken

           )}
  [state minion-id]
  (run-event state minion-id :damage-event))

(defn run-damage-events
  "Runs the all the damage-events of a player"
  {:test (fn []
           ;One Ragnaros the Firelord should deal 8 dmg to the enemy hero
           (is= (-> (create-game [{:minions [(create-minion "Frothing Berserker" :id "fbfbfb")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (run-damage-events "p1" "bo")
                    (get-minion-buff "fbfbfb" :attack))
                1)
           )}
  [state player-id minion-with-damage-taken]
  (run-events state player-id :damage-event minion-with-damage-taken))

(defn update-damage-taken
  "Returns a state with damage-taken updated input is hero-or-minion-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-damage-taken "bo", 4)
                    (get-damage-taken "bo"))
                4)
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1")}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-damage-taken "h1", 4)

                    (get-damage-taken "h1"))
                4))}
  [state id value]
  (let [hero (get-hero-value-by-hero-id state id :entity-type)
        player-id (get-player-id-by-character-id state id)
        player-id-this-turn (:player-id-in-turn state)
        next-player-id (get-next-player-id-in-turn state)
        old-damage-taken (get-damage-taken state id)]
    (if hero
      (if (= player-id player-id-this-turn)
        (-> (run-hero-take-damage-on-own-turn-events state player-id)
            (update-hero-value-by-hero-id id :damage-taken value))
        (update-hero-value-by-hero-id state id :damage-taken value))
      (if (>= value old-damage-taken)
        (-> (update-minion state id :damage-taken value)
            (run-damage-events player-id-this-turn id)
            (run-damage-events next-player-id id))
        (update-minion state id :damage-taken value)))))



(defn heal-character
  "Heals a character by reducing damage-taken"
  {:test (fn []
           ;heals more than damage taken, damage taken should be 0
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :damage-taken 2)]}])
                    (heal-character "bo" 10)
                    (get-damage-taken "bo"))
                0)
           ;heals hero, damage-taken should be 0
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :damage-taken 10)}])
                    (heal-character "h1" 10)
                    (get-damage-taken "h1"))
                0)
           ;damage-taken should have been reduced from 10 to 5
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :damage-taken 10)]}])
                    (heal-character "bo" 5)
                    (get-damage-taken "bo"))
                5)
           ;damage-taken should be 10 since no healing is done
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :damage-taken 10)]}])
                    (heal-character "bo" 0)
                    (get-damage-taken "bo"))
                10))}

  [state character-id heal]
  (let [old-damage-taken (:damage-taken (get-character-or-card state character-id))
        updated-damage-taken (- old-damage-taken heal)]

    (if (>= updated-damage-taken 0)
      (update-damage-taken state character-id updated-damage-taken)
      (update-damage-taken state character-id 0))))

(defn heal-characters
  "Heals all characters in a list by reducing damage-taken"
  {:test (fn []
           ;heals more than damage taken, damage taken should be 0
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :damage-taken 2)]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo1" :damage-taken 2)]}])
                    (heal-characters ["bo" "bo1"] 10)
                    (get-damage-taken "bo1"))
                0)
           ;heals hero, damage-taken should be 0
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :damage-taken 10)}])
                    (heal-characters ["h1"] 10)
                    (get-damage-taken "h1"))
                0)
           ;damage-taken should have been reduced from 10 to 5
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :damage-taken 10)]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo1" :damage-taken 10)]}])
                    (heal-characters ["bo" "bo1"] 5)
                    (get-damage-taken "bo"))
                5)
           ;damage-taken should be 10 since no healing is done
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :damage-taken 10)]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo1" :damage-taken 10)]}])
                    (heal-characters ["bo" "bo1"] 0)
                    (get-damage-taken "bo"))
                10))}

  [state list-of-target-ids heal]
  (->> list-of-target-ids
       (reduce-kv (fn [state _ character]
                    (heal-character state
                                    character
                                    heal))
                  state)))


(defn get-entity-type
  "Returns the entity-type given id. (Hero or minion)"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-entity-type "bo"))
                :minion)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-entity-type "h1"))
                :hero))}
  [state id]
  (let [hero (get-hero-value-by-hero-id state id :entity-type)]
    (if hero
      (get-hero-value-by-hero-id state id :entity-type)
      (get-minion-value state id :entity-type))))

(defn get-minion-default-attack
  "Returns the default-attack value of the minion with the given id."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-minion-default-attack "bo"))
                6))}
  [state id]
  (let [minion (get-minion state id)
        definition (get-definition (:name minion))]
    (:attack definition)))


(defn get-minion-default-health
  "Returns the default health of the minion with the given id."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-minion-default-health "bo"))
                7))}
  [state id]
  (let [minion (get-minion state id)
        definition (get-definition (:name minion))]
    (:health definition)))

(defn get-hero-name-by-player-id
  "get heroes id given player-id"
  {:test (fn []
           (is= (-> (create-game)
                    (get-hero-name-by-player-id "p1"))
                "Uther Lightbringer"))}
  [state player-id]
  (get-hero-value-by-player-id state player-id :name))

(defn get-hero-id-by-player-id
  "get heroes id given player-id"
  {:test (fn []
           (is= (-> (create-game)
                    (get-hero-id-by-player-id "p1"))
                "h1"))}
  [state player-id]
  (get-hero-value-by-player-id state player-id :id))

(defn has-zero-health?
  "Returns boolean wheter a minion or hero is dead given minion or hero id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (has-zero-health? "bo"))
                false)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :damage-taken 7)]}])
                    (has-zero-health? "bo"))
                true)
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :damage-taken 35)}])
                    (has-zero-health? "h1"))
                true))}
  [state id]
  (let [health (get-health state id)]
    (<= health 0))
  )

(defn end-game
  [state]
  "game has ended")

(defn game-over?
  "if hero is dead: end game
  if hero is alive: just continue"
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1" :damage-taken 9999)}] :player-id-in-turn "p2")
                    (game-over? "h1"))
                "game has ended"))}
  [state hero-id]
  (if (has-zero-health? state hero-id)
    (end-game state)
    state))


(defn is-minion-on-board?
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (is-minion-on-board? "bo"))
                true)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :damage-taken 7)]}])
                    (is-minion-on-board? "not-on-board"))
                false)
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :damage-taken 35)}])
                    (is-minion-on-board? "h1"))
                false))}
  [state minion-id]
  (let [minion-on-board (get-minion state minion-id)]
    (if minion-on-board
      true
      false)))



(defn get-minion-total-health
  "returns a minions total health, default health + buffs"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :buffs {:health 2 :attack 0 :tmp-health 1})]}])
                    (get-minion-total-health "bo"))
                10))}
  [state minion-id]
  (let [default-health (get-minion-default-health state minion-id)
        health-with-buffs (+ (:health (get-minion-buffs state minion-id)) (:tmp-health (get-minion-buffs state minion-id)) default-health)]
    health-with-buffs))

(defn get-minion-total-attack
  "returns a minions total attack, default attack + buffs"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :buffs {:health 2 :attack 10 :tmp-attack 5 :tmp-health 1 :aura-attack 1})]}])
                    (get-minion-total-attack "bo"))
                22))}
  [state minion-id]
  (let [default-attack (get-minion-default-attack state minion-id)
        attack-with-buffs (+ (:attack (get-minion-buffs state minion-id)) (:tmp-attack (get-minion-buffs state minion-id)) (:aura-attack (get-minion-buffs state minion-id)) default-attack)]
    attack-with-buffs))

(defn has-lifesteal?
  "Check if minion has Lifesteal by definition"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Acolyte of Agony" :id "aa")]}])
                    (has-lifesteal? "aa"))
                true)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (has-lifesteal? "bo"))
                false))}
  [state minion-id]
  (check-for-ability-in-minion state minion-id :lifesteal)
  )

(defn activate-lifesteal
  "Activate the lifesteal function"
  {:test (fn []
           (is= (-> (create-game [{:hero    (create-hero "Anduin Wrynn" :damage-taken 10)
                                   :minions [(create-minion "Acolyte of Agony" :id "aa")]}])
                    (activate-lifesteal "h1" 3)
                    (get-hero-damage-taken-by-player-id "p1"))
                7)
           )}
  [state hero-id value]
  (heal-character state hero-id value)
  )


(defn minion-attack-character
  "Returns state with the damage-taken on the target minion"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (minion-attack-character "ib", "bo")
                    (get-damage-taken "bo"))
                4)
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}])
                    (minion-attack-character "ib", "h2")
                    (get-damage-taken "h2"))
                4)
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib" :buffs {:attack 3, :tmp-attack 0 :aura-attack 0})]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (minion-attack-character "ib", "bo")
                    (get-damage-taken "bo"))
                7)
           (is= (-> (create-game [{:hero    (create-hero "Anduin Wrynn" :damage-taken 5)
                                   :minions [(create-minion "Acolyte of Agony" :id "aa" :buffs {:attack 2, :tmp-attack 1 :aura-attack 1})]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (minion-attack-character "aa" "bo")
                    (get-damage-taken "bo"))
                7)
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib" :can-attack true :buffs {:attack 3, :tmp-attack 0 :aura-attack 0})]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (minion-attack-character "ib", "bo")
                    (get-minion-value "ib" :can-attack))
                false))}
  [state attacker-id target-id]
  (let [attack-with-buffs (get-minion-total-attack state attacker-id)]
    (if (has-lifesteal? state attacker-id)
      (-> (activate-lifesteal state (get-hero-id-by-player-id state (get-player-id-by-character-id state attacker-id)) attack-with-buffs)
          (update-damage-taken target-id (+ (get-damage-taken state target-id) attack-with-buffs))
          (update-minion-can-attack attacker-id false))
      (-> (update-damage-taken state target-id (+ (get-damage-taken state target-id) attack-with-buffs))
          (update-minion-can-attack attacker-id false)))))


(defn attack-minion-by-value
  "Returns state with the damage-taken on the target minion"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (attack-minion-by-value "bo", 4)
                    (get-damage-taken "bo"))
                4))}
  [state target-id attack-value]
  (update-damage-taken state target-id (+ (get-damage-taken state target-id) attack-value)))

(defn minion-attack-character-by-value
  "Returns state with the damage-taken on the target character"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (minion-attack-character-by-value "bo", 4)
                    (get-damage-taken "bo"))
                4)
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (minion-attack-character-by-value "h1", 4)
                    (get-damage-taken "h1"))
                4))}
  [state target-id attack-value]
  (-> (update-damage-taken state target-id (+ (get-damage-taken state target-id) attack-value))))

(defn minion-attack-characters-by-value
  "Returns state with the damage-taken on the target minions"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (minion-attack-characters-by-value ["ib", "bo"] 3)
                    (get-damage-taken "bo"))
                3)
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (minion-attack-characters-by-value ["ib" "bo" "h2"] 3)
                    (get-damage-taken "h2"))
                3))}
  [state list-of-target-ids attack]
  (->> list-of-target-ids
       (reduce-kv (fn [state index minion]
                    (minion-attack-character-by-value state
                                                      minion
                                                      attack))
                  state)))






(defn get-all-hero-ids-on-board
  "Return a list of all minion-ids on the board"
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :damage-taken 35)}])
                    (get-all-hero-ids-on-board)
                    )
                '("h1" "h2"))
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h4", :damage-taken 35)}])
                    (get-all-hero-ids-on-board)
                    )
                '("h4" "h2"))
           )}
  [state]
  (let [player-ids ["p1" "p2"]]
    (-> (reduce (fn [acc player-id]
                  (conj acc (-> (get-hero-id-by-player-id state player-id))))
                [] player-ids)
        (flatten)
        (vec))))

(defn get-friendly-minions
  "Return a list of all friendly character-ids on the board"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo2" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi2" :abilities ["not-taunt" "yet-another-ability"])
                                             (create-minion "Boulderfist Ogre" :id "bo3")
                                             (create-minion "Flame Imp" :id "fi3")]}])
                    (get-friendly-minions "p1")
                    )
                '("bo2" "fi2" "bo3" "fi3"))
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h4", :damage-taken 35)}])
                    (get-friendly-minions "p1")
                    )
                '())
           )}
  [state player-id]
  (-> (conj (get-minion-ids-on-board state player-id))
      (flatten)
      (vec)))

(defn get-friendly-characters
  "Return a list of all friendly character-ids on the board"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo2" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi2" :abilities ["not-taunt" "yet-another-ability"])
                                             (create-minion "Boulderfist Ogre" :id "bo3")
                                             (create-minion "Flame Imp" :id "fi3")]}])
                    (get-friendly-characters "p1")
                    )
                '("h1" "bo2" "fi2" "bo3" "fi3"))
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h4", :damage-taken 35)}])
                    (get-friendly-characters "p1")
                    )
                '("h4"))
           )}
  [state player-id]
  (-> (conj [] (get-hero-id-by-player-id state player-id))
      (conj (get-minion-ids-on-board state player-id))
      (flatten)
      (vec)))

(defn get-friendly-characters-damaged
  "Return list with friendly characters that are damaged"
  {:test (fn []
           (is= (-> (create-game [{:hero    (create-hero "Anduin Wrynn" :id "h1", :damage-taken 10)
                                   :minions [(create-minion "Boulderfist Ogre" :id "bo2" :damage-taken 2)
                                             (create-minion "Flame Imp" :id "fi2" :damage-taken 10)
                                             (create-minion "Boulderfist Ogre" :id "bo3" :damage-taken 0)
                                             (create-minion "Flame Imp" :id "fi3" :damage-taken 2)]}])
                    (get-friendly-characters-damaged "p1")
                    )
                '("h1" "bo2" "fi2" "fi3"))
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h4", :damage-taken 25)}])
                    (get-friendly-characters-damaged "p1")
                    )
                '("h4"))
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h4",)}])
                    (get-friendly-characters-damaged "p1")
                    )
                [])
           )}
  [state player-id]
  (let [friendly-characters (get-friendly-characters state player-id)]
    (reduce (fn [acc character-id]
              (if (> (get-damage-taken state character-id) 0)
                (conj acc character-id)
                acc))
            [] friendly-characters)))

(defn get-minions-undamaged
  "Return list with minions that are undamaged"
  {:test (fn []
           (is= (-> (create-game [{:hero    (create-hero "Anduin Wrynn" :id "h1", :damage-taken 0)
                                   :minions [(create-minion "Boulderfist Ogre" :id "bo2" :damage-taken 0)
                                             (create-minion "Flame Imp" :id "fi2" :damage-taken 0)
                                             (create-minion "Boulderfist Ogre" :id "bo3" :damage-taken 10)
                                             (create-minion "Flame Imp" :id "fi3" :damage-taken 0)]}])
                    (get-minions-undamaged)
                    )
                '("bo2" "fi2" "fi3"))
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h4",)}])
                    (get-minions-undamaged)
                    )
                [])
           )}
  [state]
  (let [minions-on-board (get-minion-ids-on-board state)]
    (reduce (fn [acc character-id]
              (if (<= (get-damage-taken state character-id) 0)
                (conj acc character-id)
                acc))
            [] minions-on-board)))


(defn get-enemy-characters-ids
  "Return a list of all enemy character-ids on the board"
  {:test (fn []
           ;Should get player p1s characters
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo2" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi2" :abilities ["not-taunt" "yet-another-ability"])
                                             (create-minion "Boulderfist Ogre" :id "bo3")
                                             (create-minion "Flame Imp" :id "fi3")]}]
                                 :player-id-in-turn "p2")

                    (get-enemy-characters-ids)
                    )
                '("h1" "bo2" "fi2" "bo3" "fi3"))
           ;Should get player p2s characters
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi" :abilities ["not-taunt" "yet-another-ability"])
                                             (create-minion "Boulderfist Ogre" :id "bo1")
                                             (create-minion "Flame Imp" :id "fi1")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo2" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi2" :abilities ["not-taunt" "yet-another-ability"])
                                             (create-minion "Boulderfist Ogre" :id "bo3")
                                             (create-minion "Flame Imp" :id "fi3")]}]
                                 :player-id-in-turn "p1")
                    (get-enemy-characters-ids)
                    )
                '("h2" "bo2" "fi2" "bo3" "fi3"))
           ;Should return p2 hero h2
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1")}
                                  {:hero (create-hero "Anduin Wrynn" :id "h2")}])
                    (get-enemy-characters-ids)
                    )
                '("h2"))
           )}
  [state]
  (let [next-player-id-in-turn (get-next-player-id-in-turn state)]
    (-> (conj [] (get-hero-id-by-player-id state next-player-id-in-turn))
        (conj (get-minion-ids-on-board state next-player-id-in-turn))
        (flatten)
        (vec))))


(defn get-enemy-minion-ids
  "Return a list of all minions-ids on the board"
  {:test (fn []
           ;Should get player p1s minions
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo2")
                                             (create-minion "Flame Imp" :id "fi2")
                                             (create-minion "Boulderfist Ogre" :id "bo3")
                                             (create-minion "Flame Imp" :id "fi3")]}]
                                 :player-id-in-turn "p2")
                    (get-enemy-minion-ids))
                '("bo2" "fi2" "bo3" "fi3"))
           ;Should get player p2s minions
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Flame Imp" :id "fi")
                                             (create-minion "Boulderfist Ogre" :id "bo1")
                                             (create-minion "Flame Imp" :id "fi1")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo2")
                                             (create-minion "Flame Imp" :id "fi2")
                                             (create-minion "Boulderfist Ogre" :id "bo3")
                                             (create-minion "Flame Imp" :id "fi3")]}]
                                 :player-id-in-turn "p1")
                    (get-enemy-minion-ids)
                    )
                '("bo2" "fi2" "bo3" "fi3"))
           ;Should return empty list
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1")}
                                  {:hero (create-hero "Anduin Wrynn" :id "h2")}])
                    (get-enemy-minion-ids))
                []))}
  [state]
  (let [next-player-id-in-turn (get-next-player-id-in-turn state)]
    (get-minion-ids-on-board state next-player-id-in-turn)))

(defn attack-random-enemy-character
  "Returns state with the damage-taken on the target minion"
  {:test (fn []
           ;Should attack a random enemy with seed ("bo")
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (attack-random-enemy-character 8 1123123)
                    (get-damage-taken "bo"))
                8)
           ;Should be able to attack hero h2
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (attack-random-enemy-character 8 0)
                    (get-damage-taken "h2"))
                8))}
  [state attack-value seed]
  (let [list-with-seed-and-random-enemy (random-nth seed (get-enemy-characters-ids state))
        random-enemy (nth list-with-seed-and-random-enemy 1)
        new-seed (nth (random-nth seed (get-enemy-characters-ids state)) 0)]
    (->
      (minion-attack-character-by-value state random-enemy attack-value)
      (update-seed new-seed))))


(defn heal-friendly-characters
  "Heals all characters in a list by reducing damage-taken"
  {:test (fn []
           ;heals more than damage taken, damage taken should be 0
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :damage-taken 2)]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo1" :damage-taken 2)]}])
                    (heal-friendly-characters "p1" 10)
                    (get-damage-taken "bo"))
                0)
           ;heals friendly characters, opponents characters shouldnt be healed
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :damage-taken 2)]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo1" :damage-taken 2)]}])
                    (heal-friendly-characters "p1" 10)
                    (get-damage-taken "bo1"))
                2)
           ;hero should be healed
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :damage-taken 10)}])
                    (heal-friendly-characters "p1" 10)
                    (get-damage-taken "h1"))
                0))}

  [state player-id heal]
  (heal-characters state (get-friendly-characters state player-id) heal))

(defn attack-all-minions-on-board
  "Returns state with the damage-taken on the target minions"
  {:test (fn []
           ;should attack p1s minion
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (attack-all-minions-on-board 3)
                    (get-damage-taken "bo"))
                3)
           ;should attack p1s minion
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (attack-all-minions-on-board 3)
                    (get-damage-taken "ib"))
                3)
           ;Should not attack hero
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (attack-all-minions-on-board 3)
                    (get-damage-taken "h1"))
                0))}
  [state attack]
  (let [list-of-minion-ids (get-minion-ids-on-board state)]
    (minion-attack-characters-by-value state list-of-minion-ids attack))
  )

(defn get-all-character-ids-on-board
  "Get all the of the minion ids as a vector"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi" :abilities ["not-taunt" "yet-another-ability"])]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo2" :abilities ["taunt" "another-ability"])
                                             (create-minion "Flame Imp" :id "fi2" :abilities ["not-taunt" "yet-another-ability"])]}])
                    (get-all-character-ids-on-board)

                    )
                ["bo" "fi" "bo2" "fi2" "h1" "h2"])
           )}
  [state]
  (let [list-of-minion-ids (get-minion-ids-on-board state)
        list-of-hero-ids (get-all-hero-ids-on-board state)]
    (into [] (concat list-of-minion-ids list-of-hero-ids))))


(defn sleepy?
  "Checks if the minion with given id is sleepy."
  {:test (fn []
           (is (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}]
                                :minion-ids-summoned-this-turn ["bo"])
                   (sleepy? "bo")))
           (is-not (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                       (sleepy? "bo"))))}
  [state id]
  (seq-contains? (:minion-ids-summoned-this-turn state) id))









(defn valid-attack-taunt?
  "Returns true if the target has no minions with taunt or the target minion has taunt"
  {:test (fn []
           (is (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                            (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                   (valid-attack-taunt?, "bo")))
           (is (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                            (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                   (valid-attack-taunt?, "bo")))
           (is (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:not-taunt :another-ability])
                                            (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                   (valid-attack-taunt?, "fi")))
           )}
  [state target-minion-id]
  (or (check-for-ability-in-minion state target-minion-id :taunt)
      (not (check-for-ability-in-player state (get-player-id-by-character-id state target-minion-id) :taunt))))

(defn character-has-ability-cant-attack?
  "Returns true has the ability cant-attack"
  {:test (fn []
           ;has cant-attack should be true
           (is (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:cant-attack])
                                            (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                   (character-has-ability-cant-attack? "bo")
                   ))
           ;has cant-attack and additional abilities should be true
           (is (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:another-ability :cant-attack])
                                            (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                   (character-has-ability-cant-attack? "bo")))
           ;doesn't have cant-attack should be nil/false
           (is-not (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:cant-attack :another-ability])
                                                (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                       (character-has-ability-cant-attack? "fi")))
           )}
  [state target-minion-id]
  (check-for-ability-in-minion state target-minion-id :cant-attack)
  )

(defn valid-attack?
  "Checks if the attack is valid"
  {:test (fn []
           ; Should be able to attack an enemy minion
           (is (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}
                                 {:minions [(create-minion "Injured Blademaster" :id "ib")]}])
                   (valid-attack? "p1" "bo" "ib")))
           ; Should be able to attack an enemy hero
           (is (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                   (valid-attack? "p1" "bo" "h2")))
           ; Should not be able to attack your own minions
           (is-not (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                                (create-minion "Injured Blademaster" :id "ib")]}])
                       (valid-attack? "p1" "bo" "ib")))
           ; Should not be able to attack if it is not your turn
           (is-not (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}
                                     {:minions [(create-minion "Injured Blademaster" :id "ib")]}]
                                    :player-id-in-turn "p2")
                       (valid-attack? "p1" "bo" "ib")))
           ; Should not be able to attack if you are sleepy
           (is-not (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}
                                     {:minions [(create-minion "Injured Blademaster" :id "ib")]}]
                                    :minion-ids-summoned-this-turn ["bo"])
                       (valid-attack? "p1" "bo" "ib")))
           ; Should not be able to attack if you already attacked this turn
           (is-not (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :attacks-performed-this-turn 1)]}
                                     {:minions [(create-minion "Injured Blademaster" :id "ib")]}])
                       (valid-attack? "p1" "bo" "ib")))
           ;Tests the second attack route without player-id
           (is-not (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :attacks-performed-this-turn 1)]}
                                     {:minions [(create-minion "Injured Blademaster" :id "ib")]}])
                       (valid-attack? "p2" "ib")))
           ;Should not be able to attack as the attacker has the ability ":cant-attack"
           (is-not (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:cant-attack])]}
                                     {:minions [(create-minion "Injured Blademaster" :id "ib")]}])
                       (valid-attack? "p2" "ib"))))}

  ([state player-id attacker-id target-id]
   (let [attacker (get-minion state attacker-id)
         target (get-character-or-card state target-id)]
     (and attacker
          target
          (= (:player-id-in-turn state) player-id)
          (< (:attacks-performed-this-turn attacker) 1)
          (not (sleepy? state attacker-id))
          (not= (:owner-id attacker) (:owner-id target))
          (valid-attack-taunt? state target-id)
          (get-minion-value state attacker-id :can-attack)
          (not (character-has-ability-cant-attack? state attacker-id)))))
  ;Checks if an attacker can attack
  ([state player-id attacker-id]
   (let [attacker (get-minion state attacker-id)]
     (and attacker
          (= (:player-id-in-turn state) player-id)
          (< (:attacks-performed-this-turn attacker) 1)
          (not (sleepy? state attacker-id))
          (get-minion-value state attacker-id :can-attack)
          (not (character-has-ability-cant-attack? state attacker-id))))))




(defn update-hero-value-by-player-id
  "update hero value with value or function"
  {:test (fn []
           (is= (-> (create-game)
                    (update-hero-value-by-player-id "p1" :damage-taken 10)
                    (get-hero-value-by-player-id "p1" :damage-taken))
                10)
           (is= (-> (create-game)
                    (update-player-value "p1" :fatigue-counter inc)
                    (update-hero-value-by-player-id "p1" :damage-taken get-player-value :fatigue-counter)
                    (get-hero-value-by-player-id "p1" :damage-taken))
                1))}

  ([state player-id key function-or-value]
   (if (fn? function-or-value)
     (update-in state [:players player-id :hero key] function-or-value)
     (assoc-in state [:players player-id :hero key] function-or-value)))

  ([state player-id key function function-key]
   (assoc-in state [:players player-id :hero key] (function state player-id function-key))))

(defn update-hero-damage-taken-by-player-id
  "update heroes damage-taken given player-id and function or value"
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :damage-taken 10)}])
                    (get-hero-damage-taken-by-player-id "p1"))
                10))}
  [state player-id function-or-value]
  (update-hero-value-by-player-id state player-id :damage-taken function-or-value))


(defn should-get-card?
  {:test (fn []
           (is= (-> (create-game)
                    (add-cards-to-hand "p1" ["Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre"])
                    (should-get-card? "p1"))
                false))}

  [state player-id]
  (< (count (get-player-value state player-id :hand)) 10))

(defn remove-element-from-vector-by-index
  {:test (fn []
           (is= (remove-element-from-vector-by-index [0 1 2 3 4 5] 3)
                [0 1 2 4 5]))}

  [vector index]
  (let [coll vector
        i index]
    (concat (subvec coll 0 index)
            (subvec coll (inc i)))))

(defn get-number-of-cards
  "get number of cards on either hand or board given player-id"
  {:test (fn []
           ;number of cards on hand should be 2
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib") (create-card "Boulderfist Ogre" :id "ob")]}])
                    (get-number-of-cards "p1" :hand))
                2))}

  [state player-id key]
  (count (get-in state [:players player-id key])))


(defn get-number-of-minions
  "get number of minions given player-id"
  {:test (fn []
           ;number of minions should be 2
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib") (create-minion "Boulderfist Ogre" :id "ob")]}])
                    (get-number-of-minions "p1"))
                2))}
  [state player-id]
  (get-number-of-cards state player-id :minions))


(defn- get-card-by-vector-index
  "get card from either hand or deck by vector-idex"
  {:test (fn []
           ;get card on vector index 1
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib") (create-card "Boulderfist Ogre" :id "ob")]}])
                    (get-card-by-vector-index "p1" :hand 1)
                    (get :name))
                "Boulderfist Ogre")
           ;tries to get card on vector index that doesn't exists
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib") (create-card "Boulderfist Ogre" :id "ob")]}])
                    (get-card-by-vector-index "p1" :hand 2))
                nil))}

  [state player-id key index]
  (if (> (get-number-of-cards state player-id key) index)
    (nth (get-in state [:players player-id key]) index)
    nil))

(defn- get-card-by-id
  "gets card with the given id from hand or deck. key could be hand or deck"
  [state player-id key id]
  (->> (get-player-value state player-id key)
       (filter (fn [m] (= (:id m) id)))
       (first)))
(defn- get-card-by-name
  "gets card with the given id from hand or deck. key could be hand or deck"
  [state player-id key name]
  (->> (get-player-value state player-id key)
       (filter (fn [m] (= (:name m) name)))
       (first)))

(defn get-card-from-hand
  "gets card from hand either by index or id"
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib")]}])
                    (get-card-from-hand "p1" "ib")
                    (get :name))
                "Injured Blademaster")
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib")]}])
                    (get-card-from-hand "p1" 0)
                    (get :name))
                "Injured Blademaster"))}
  [state player-id id-or-index]
  (if (string? id-or-index)
    (get-card-by-id state player-id :hand id-or-index)
    (get-card-by-vector-index state player-id :hand id-or-index)))

;Get value from card, basically get-card-from-hand
(defn get-value-from-card
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib")]}])
                    (get-value-from-card "ib")
                    (get :name))
                "Injured Blademaster"))}
  [state card-id]
  (get-card-from-hand state (get-in state [:player-id-in-turn]) card-id)
  )

(defn- get-top-card
  "get card from either deck or hand on vector index 0"
  [state player-id key]
  (first (get-in state [:players player-id key])))


(defn get-top-card-from-hand
  "get card from hand on vector index 0"
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib")]}])
                    (get-top-card-from-hand "p1")
                    (get :name))
                "Injured Blademaster"))}
  [state player-id]
  (get-top-card state player-id :hand))



(defn get-top-card-from-deck
  "get card from deck on vector index 0"
  {:test (fn []
           (is= (-> (create-game [{:deck [(create-card "Injured Blademaster" :id "ib")]}])
                    (get-top-card-from-deck "p1")
                    (get :name))

                "Injured Blademaster"))}
  [state player-id]
  (get-top-card state player-id :deck))

(defn- remove-top-card
  "removes card from either deck or hand on vector index 0"
  {:test (fn []
           (is= (-> (create-game [{:deck [(create-card "Injured Blademaster" :id "ib") (create-card "Boulderfist Ogre" :id "ob")]}])
                    (remove-top-card "p1" :deck)
                    (get-top-card "p1" :deck)
                    (get :name))
                "Boulderfist Ogre")
           )}
  [state player-id key]
  (assoc-in state [:players player-id key] (drop 1 (get-in state [:players player-id key]))))


(defn remove-top-card-in-deck
  "removes card from deck on vector index 0"
  {:test (fn []
           (is= (-> (create-game [{:deck [(create-card "Injured Blademaster" :id "ib") (create-card "Boulderfist Ogre" :id "ob")]}])
                    (remove-top-card-in-deck "p1")
                    (get-top-card "p1" :deck)
                    (get :name))
                "Boulderfist Ogre"))}
  [state player-id]
  (remove-top-card state player-id :deck))


(defn burn-card
  "burns a card if player have 10 cards on hand"
  [state player-id]
  (remove-top-card-in-deck state player-id))


(defn remove-top-card-in-hand
  "removes card from hand on vector index 0"
  {:test (fn []
           ;removes top card "Injured Blademaster" from hand, top card should be "Boulderfist Ogre"
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib") (create-card "Boulderfist Ogre" :id "ob")]}])
                    (remove-top-card-in-hand "p1")
                    (get-top-card "p1" :hand)
                    (get :name))
                "Boulderfist Ogre")
           )}
  [state player-id]
  (remove-top-card state player-id :hand))



(defn remove-value-by-vector-index
  "removes a value by vector index from hand, deck , minions or graveyard"
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib")
                                          (create-card "Boulderfist Ogre" :id "ob")]}])
                    (remove-value-by-vector-index "p1" :hand 0)
                    (get-top-card "p1" :hand)
                    (get :name))
                "Boulderfist Ogre")
           (is= (-> (create-game [{:deck [(create-card "Injured Blademaster" :id "ib")
                                          (create-card "Boulderfist Ogre" :id "ob")]}])
                    (remove-value-by-vector-index "p1" :deck 0)
                    (get-top-card "p1" :deck)
                    (get :name))
                "Boulderfist Ogre")
           (is= (-> (create-game [{:graveyard [(create-minion "Injured Blademaster" :id "ib")
                                               (create-minion "Boulderfist Ogre" :id "ob")]}])
                    (remove-value-by-vector-index "p1" :graveyard 0)
                    (get-player-graveyard-ids "p1"))
                ["ob"])
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")
                                             (create-minion "Boulderfist Ogre" :id "ob")]}])
                    (remove-value-by-vector-index "p1" :minions 0)
                    (get-player-minion-ids "p1"))
                ["ob"]))}
  [state player-id key index]
  (if (> (get-number-of-cards state player-id key) index)
    (assoc-in state [:players player-id key] (remove-element-from-vector-by-index (get-in state [:players player-id key]) index))
    nil))


(defn add-value-by-vector-index
  "adds a value by vector index to hand, deck , minions or graveyard"
  {:test (fn []
           (is= (-> (create-game [{:graveyard [(create-minion "Injured Blademaster" :id "ib")
                                               (create-minion "Boulderfist Ogre" :id "ob")
                                               (create-minion "Unstable Ghoul" :id "ug")]}])
                    (add-value-by-vector-index "p1" :graveyard 1 (create-card "The Black Knight" :id "tbk"))
                    (get-player-graveyard-ids "p1"))
                ["ib" "tbk" "ob" "ug"])
           (is= (-> (create-game [{:deck [(create-card "Injured Blademaster" :id "ib")
                                          (create-card "Boulderfist Ogre" :id "ob")
                                          (create-card "Unstable Ghoul" :id "ug")]}])
                    (add-value-by-vector-index "p1" :deck 0 (create-card "The Black Knight" :id "tbk"))
                    (get-top-card "p1" :deck)
                    (get :name))
                "The Black Knight")
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib")
                                          (create-card "Boulderfist Ogre" :id "ob")
                                          (create-card "Unstable Ghoul" :id "ug")]}])
                    (add-value-by-vector-index "p1" :hand 0 (create-card "The Black Knight" :id "tbk"))
                    (get-top-card "p1" :hand)
                    (get :name))
                "The Black Knight"))}

  [state player-id key index value]
  (let [vector (get-in state [:players player-id key])
        [before after] (split-at index vector)
        new-vector (concat (vec before) [value] (vec after))]

    (assoc-in state [:players player-id key] new-vector)))


(defn remove-card-by-id
  "Removes card with the given id from hand or deck."
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib") (create-card "Boulderfist Ogre" :id "ob")]}])
                    (remove-card-by-id "p1" :hand "ib")
                    (get-top-card "p1" :hand)
                    (get :id))
                "ob"))}
  [state player-id key id]
  (update-in state
             [:players player-id key]
             (fn [hand-or-deck]
               (remove (fn [m] (= (:id m) id)) hand-or-deck))))


(defn remove-card-from-deck
  "removes card from deck either by vector-index or id"
  {:test (fn []
           ;removes card by id
           (is= (-> (create-game [{:deck [(create-card "Injured Blademaster" :id "ib") (create-card "Boulderfist Ogre" :id "ob")]}])
                    (remove-card-from-deck "p1" "ib")
                    (get-top-card "p1" :deck)
                    (get :id))
                "ob")
           ;removes card by vector-index
           (is= (-> (create-game [{:deck [(create-card "Injured Blademaster" :id "ib") (create-card "Boulderfist Ogre" :id "ob")]}])
                    (remove-card-from-deck "p1" 0)
                    (get-top-card "p1" :deck)
                    (get :id))
                "ob"))}
  [state player-id id-or-index]
  (if (string? id-or-index)
    (remove-card-by-id state player-id :deck id-or-index)
    (remove-value-by-vector-index state player-id :deck id-or-index)))


(defn remove-card-from-hand
  "removes card from hand either by vector-idex or id"
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib") (create-card "Boulderfist Ogre" :id "ob")]}])
                    (remove-card-from-hand "p1" "ib")
                    (get-top-card "p1" :hand)
                    (get :id))
                "ob"))}
  [state player-id id-or-index]
  (if (string? id-or-index)
    (remove-card-by-id state player-id :hand id-or-index)
    (remove-value-by-vector-index state player-id :hand id-or-index)))


(defn get-card-type
  {:test (fn []
           ;card "Convert" should be a spell
           (is= (get-card-type (create-card "Convert"))
                :spell)
           ;card "Injured Blademaster" should be a minion
           (is= (get-card-type (create-card "Injured Blademaster"))
                :minion))}
  [card]
  (-> (get-definition (:name card))
      :type))


(defn get-cards-by-card-id
  "Returns the cards on hand for the given player-id or for both players."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-cards-by-card-id "p1"))
                [])
           (is= (as-> (create-game [{:hand [(create-card "The Black Knight" :id "tbk")]}
                                    {:hand [(create-card "Boulderfist Ogre" :id "bo")
                                            (create-card "Unstable Ghoul" :id "ug1")
                                            (create-card "Unstable Ghoul" :id "ug2")]}]) $
                      (get-cards-by-card-id $)
                      (map :id $))
                ["tbk" "bo" "ug1" "ug2"])
           (is= (as-> (create-game [{:hand [(create-card "Boulderfist Ogre" :id "bo1") (create-card "Boulderfist Ogre" :id "bo2")]}]) $
                      (get-cards-by-card-id $ "p1")
                      (map :id $))
                ["bo1" "bo2"]))}
  ([state player-id]
   (:hand (get-player state player-id)))
  ([state]
   (->> (:players state)
        (vals)
        (map :hand)
        (apply concat))))


(defn get-card-by-card-id
  "Returns the card with the given id."
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Boulderfist Ogre" :id "bo")]}])
                    (get-card-by-card-id "bo")
                    (:name))
                "Boulderfist Ogre")
           (is= (-> (create-game [{:hand [(create-card "Nerubian Ambush!" :id "na")]}])
                    (get-card-by-card-id "na")
                    (:name))
                "Nerubian Ambush!"))}
  [state id]
  (->> (get-cards-by-card-id state)
       (filter (fn [m] (= (:id m) id)))
       (first)))


(defn is-card-a-spell?
  {:test (fn []
           ;card "Convert" should be a spell
           (is= (is-card-a-spell? (create-card "Convert"))
                true)
           ;card "Injured Blademaster" should not be a spell
           (is= (is-card-a-spell? (create-card "Injured Blademaster"))
                false))}
  [card]
  (if (= (get-card-type card) :spell)
    true
    false))


(defn is-card-a-minion?
  {:test (fn []
           ;card "Convert" should not be a minion
           (is= (is-card-a-minion? (create-card "Convert"))
                false)
           ;card "Injured Blademaster" should be a minion
           (is= (is-card-a-minion? (create-card "Injured Blademaster"))
                true))}
  [card]
  (if (= (get-card-type card) :minion)
    true
    false))

(defn has-draw-event?
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Nerubian Ambush!" :id "na")]}])
                    (has-draw-event? "Nerubian Ambush!"))
                true)
           (is= (-> (create-game [{:hand [(create-card "Injured Blademaster" :id "ib")]}])
                    (has-draw-event? "Injured Blademaster"))
                false))}
  [state card-name]
  (if (some? (-> (:draw-event (get-definition card-name))))
    true
    false))

(defn run-draw-event
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Nerubian Ambush!" :id "na")
                                          (create-card "Nerubian Ambush!" :id "na1")]}
                                  {:hand []}])
                    (run-draw-event "na" "p1")
                    (get-minions "p2")
                    (get-in [0 :name]))
                "Nerubian"))}
  [state card-id player-id]
  (let [card-name (get-definition (:name (get-card-by-card-id state card-id)))
        draw-event-function (:draw-event (get-definition card-name))]
    (->(draw-event-function state player-id)
    (remove-card-from-hand player-id card-id))))


(defn run-draw-event-if-it-exist
  [state player-id card-id]
  (-> (get-card-from-hand state player-id card-id))
    (run-draw-event state card-id player-id))


(defn move-top-card-from-deck-to-hand
  "move top card from deck to hand"
  {:test (fn []
           (is= (-> (create-game [{:deck [(create-card "Injured Blademaster" :id "ib") (create-card "Injured Blademaster" :id "ib1")]}])
                    (move-top-card-from-deck-to-hand "p1")
                    (get-top-card "p1" :hand)
                    (get :name))
                "Injured Blademaster")
           (is= (-> (create-game [{:deck [(create-card "Injured Blademaster" :id "ib") (create-card "Boulderfist Ogre" :id "ob")]}])
                    (move-top-card-from-deck-to-hand "p1")
                    (get-top-card "p1" :deck)
                    (get :name))
                "Boulderfist Ogre")
         (is= (-> (create-game [{:deck [(create-card "Nerubian Ambush!" :id "na")
                                        (create-card "Injured Blademaster" :id "ib")
                                        (create-card "Boulderfist Ogre" :id "ob")]}])
                  (move-top-card-from-deck-to-hand "p1")
                  (get-top-card "p1" :deck)
                  (get :name))
              "Boulderfist Ogre")
           (is= (-> (create-game [{:deck [(create-card "Nerubian Ambush!" :id "na")
                                          (create-card "Nerubian Ambush!" :id "n1")
                                          (create-card "Injured Blademaster" :id "ib")
                                          (create-card "Boulderfist Ogre" :id "ob")]}])
                    (move-top-card-from-deck-to-hand "p1")
                    (get-top-card "p1" :deck)
                    (get :name))
                "Boulderfist Ogre")
           (is= (-> (create-game [{:deck [(create-card "Nerubian Ambush!" :id "na")
                                          (create-card "Nerubian Ambush!" :id "n1")
                                          (create-card "Injured Blademaster" :id "ib")
                                          (create-card "Boulderfist Ogre" :id "ob")]}])
                    (move-top-card-from-deck-to-hand "p1")
                    (get-top-card "p1" :hand)
                    (get :name))
                "Injured Blademaster")
           )}

  [state player-id]
  (let [card-id (:id (get-top-card state player-id :deck))
        card-name (:name (get-top-card state player-id :deck))]
  (as->
    (add-card-to-hand state player-id (get-top-card state player-id :deck)) $
    (remove-top-card $ player-id :deck)
  (if (has-draw-event? $ card-name)
    (run-draw-event-if-it-exist $ player-id card-id)
    $ ))))


(defn move-card-from-hand-to-board
  "moves a card from player hand to board by using id or vector index "
  {:test (fn []
           ;using vector index 1 in hand. Summoned minion should be "Injured Blademaster"
           (is= (-> (create-game [{:hand [(create-card "Boulderfist Ogre" :id "bo") (create-card "Injured Blademaster" :id "ib")]}])
                    (move-card-from-hand-to-board "p1" 1 0)
                    (get-minion "ib")
                    (:position))
                0)
           ;minion should have the same id as card
           (is= (-> (create-game [{:hand [(create-card "Boulderfist Ogre" :id "ib")]}])
                    (move-card-from-hand-to-board "p1" "ib" 0)
                    (get-minion "ib")
                    (:position))
                0)
           ;should be one minion on board
           (is= (-> (create-game [{:hand [(create-card "Boulderfist Ogre" :id "ib")]}])
                    (move-card-from-hand-to-board "p1" "ib" 0)
                    (get-minions "p1")
                    (count))
                1)
           ;hand should be empty
           (is= (-> (create-game [{:hand [(create-card "Boulderfist Ogre" :id "ib")]}])
                    (move-card-from-hand-to-board "p1" "ib" 0)
                    (get-player-value "p1" :hand))
                ())
           ;can't move a spell-card to board, should throw exception
           (is= (try
                  (-> (create-game [{:hand [(create-card "Convert" :id "ib")]}])
                      (move-card-from-hand-to-board "p1" "ib" 0))
                  (catch Exception e (str "caught exception: " (.getMessage e))))
                "caught exception: Not allowed to move card from hand to board. Reason: to many minions on board or card is not of type minion"))}

  [state player-id id-or-index board-position]
  (if (and (< (get-number-of-minions state player-id) 7) (is-card-a-minion? (get-card-from-hand state player-id id-or-index)))
    (let [card-to-move (get-card-from-hand state player-id id-or-index)
          minion-to-summon (create-minion (:name card-to-move) :id (:id card-to-move))]

      (-> (add-minion-to-board state player-id minion-to-summon board-position)
          (remove-card-from-hand player-id (:id card-to-move))
          (run-board-change-events)))
    (throw (Exception. "Not allowed to move card from hand to board. Reason: to many minions on board or card is not of type minion"))))


(defn player-take-card-from-deck
  "player takes card from deck if allowed
  otherwise burn card or increase fatigue"
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :damage-taken 9)}])
                    (player-take-card-from-deck "p1")
                    (get-damage-taken "h1"))
                10)
           (is= (-> (create-game)
                    (add-cards-to-hand "p1" ["Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre"])
                    (add-cards-to-deck "p1" ["Boulderfist Ogre", "Injured Blademaster"])
                    (player-take-card-from-deck "p1")
                    (get-top-card-from-deck "p1")
                    (get :name))
                "Injured Blademaster")
           (is= (-> (create-game)
                    (player-take-card-from-deck "p1")
                    (get-top-card-from-hand "p1")
                    (get :name))
                nil)
           (is= (-> (create-game)
                    (add-cards-to-deck "p1" ["Boulderfist Ogre", "Injured Blademaster"])
                    (player-take-card-from-deck "p1")
                    (get-top-card-from-hand "p1")
                    (get :name))
                "Boulderfist Ogre")
           )}
  [state player-id]
  (-> (if (empty? (get-deck state player-id))
        (as-> (update-player-fatigue-counter state player-id inc) $
              (update-hero-damage-taken-by-player-id $ player-id (+ (get-player-fatigue-counter $ player-id) (get-hero-damage-taken-by-player-id $ player-id))))
        (if (should-get-card? state player-id)
          (move-top-card-from-deck-to-hand state player-id)
          (burn-card state player-id)))))


(defn get-minion-from-board-or-graveyard
  {:test (fn []
           (is= (as-> (create-game [{:minions [(create-minion "Leper Gnome" :id "lg")]}]) $
                      (:id (get-minion-from-board-or-graveyard $ "lg")))
                "lg")
           (is= (as-> (create-game [{:minions   [(create-minion "Boulderfist Ogre" :id "bo")]
                                     :graveyard [(create-minion "Leper Gnome" :id "lg")]}]) $
                      (:id (get-minion-from-board-or-graveyard $ "lg")))
                "lg"))}
  [state minion-id]
  (if (is-minion-in-graveyard? state minion-id)
    (get-minion-from-graveyard-given-id state minion-id)
    (get-minion state minion-id)))

(defn has-deathrattle?
  "Check if minion have Deathrattle"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Leper Gnome" :id "lg")]}])
                    (has-deathrattle? "lg"))
                true)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (has-deathrattle? "bo"))
                false)
           ;check if minion has a temporary death-rattle
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:tmp-deathrattle] :tmp-deathrattle 1)]}])
                    (has-deathrattle? "bo"))
                true))}
  [state minion-id]
  (let [minion (get-minion-from-board-or-graveyard state minion-id)
        minion-name (:name minion)]

    (if (or (some? (:deathrattle (get-definition minion-name)))
            (contains? minion :tmp-deathrattle))
      true
      false)))


(defn- run-definition-deathrattle-function
  "helper function to fetch-and-run-deathrattle-functions
   runs definition deathrattle function if it exists
   return state if it doesnt exist"
  [state minion-id]
  (let [minion (get-minion-from-board-or-graveyard state minion-id)
        minion-name (:name minion)
        minion-definition (get-definition minion-name)]

    (if (contains? minion-definition :deathrattle)
      (let [deathrattle-function (:deathrattle minion-definition)]
        (deathrattle-function state minion-id))
      state)))


(defn- run-tmp-deathrattle-function
  "helper function to fetch-and-run-deathrattle-functions
   runs tmp-deathrattle function if it exists
   return state if it doesnt exist"
  [state minion-id]
  (let [minion (get-minion-from-board-or-graveyard state minion-id)]
    (if (contains? minion :tmp-deathrattle)
      (let [tmp-deathrattle-function (:tmp-deathrattle minion)]
        (tmp-deathrattle-function state minion-id))
      state)

    ))

(defn fetch-and-run-deathrattle-functions
  "Get the Deathrattle effect from card  or temporary deathrattle and execute it.
  the minion can be either on board or in graveyard "
  {:test (fn []
           ;Should damage enemy hero
           (is= (-> (create-game [{:minions [(create-minion "Leper Gnome" :id "lg" :damage-taken 5)]}])
                    (fetch-and-run-deathrattle-functions "lg")
                    (get-hero-damage-taken-by-player-id "p2")
                    )
                2)
           ;Unstable Ghouls should damage all minions on board with 1 damage
           (is= (-> (create-game [{:minions [(create-minion "Unstable Ghoul" :id "ug" :damage-taken 10)
                                             (create-minion "Flame Imp" :id "fi")]}])
                    (fetch-and-run-deathrattle-functions "ug")
                    (get-minion-value "fi" :damage-taken)
                    )
                1)
           ;Unstable Ghouls should damage all minions on board with 1 damage
           (is= (-> (create-game [{:minions   [(create-minion "Flame Imp" :id "fi")]
                                   :graveyard [(create-minion "Unstable Ghoul" :id "ug" :damage-taken 10)]}])
                    (fetch-and-run-deathrattle-functions "ug")
                    (get-minion-value "fi" :damage-taken)
                    )
                1)
           ;should draw a card
           (is= (-> (create-game [{:minions [(create-minion "Loot Hoarder" :id "lh" :damage-taken 5)]}])
                    (fetch-and-run-deathrattle-functions "lh")
                    (get-hero-damage-taken-by-player-id "p1")
                    )
                1)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo"
                                                            :abilities [:tmp-deathrattle]
                                                            :tmp-deathrattle (fn [a b] "test"))]}])
                    (fetch-and-run-deathrattle-functions "bo")
                    )
                "test")
           )}
  [state minion-id]
  (if (has-deathrattle? state minion-id)
    (-> (run-definition-deathrattle-function state minion-id)
        (run-tmp-deathrattle-function minion-id))
    state))




(defn move-minion-from-board-to-graveyard
  "remove minion given id from board and puts it on graveyard"
  {:test (fn []
           ;"Injured Blademaster" and "Boulderfist Ogre" should be in graveyard
           (is= (as-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")] :graveyard [(create-minion "Boulderfist Ogre")]}]) $
                      (move-minion-from-board-to-graveyard $ "p1" "ib")
                      (get-player-graveyard $ "p1")
                      (map :name $))
                ["Boulderfist Ogre" "Injured Blademaster"])

           ;"Injured Blademaster" should be in graveyard
           (is= (as-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}]) $
                      (move-minion-from-board-to-graveyard $ "p1" "ib")
                      (get-player-graveyard $ "p1")
                      (map :name $))
                ["Injured Blademaster"])
           ;"Injured Blademaster" should be in graveyard with all stats
           (is= (as-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib" :abilities [:taunt])]}]) $
                      (move-minion-from-board-to-graveyard $ "p1" "ib")
                      (get-player-graveyard $ "p1")
                      (map :abilities $))
                [[:taunt]])
           ;minions on board should been moved to graveyard and board is empty
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}])
                    (move-minion-from-board-to-graveyard "p1" "ib")
                    (get-minions "p1"))
                []))}
  [state player-id id]
  (let [minion-to-move (get-minion state id)]
    (-> (add-minion-to-graveyard state player-id minion-to-move)
        (remove-minion (:id minion-to-move))
        (run-board-change-events))))


(defn resurrect-minion-from-graveyard
  {:test (fn []
           (is= (-> (create-game [{:graveyard [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (resurrect-minion-from-graveyard "p1" "bo")
                    (is-minion-in-graveyard? "bo"))
                false)
           (is= (-> (create-game [{:graveyard [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (resurrect-minion-from-graveyard "p1" "bo")
                    (get-minion-ids-on-board))
                ["bo"]))}
  [state player-id minion-id]
  (let [minion (get-minion-from-graveyard-given-id state minion-id)
        minion-id (:id minion)
        minion-name (:name minion)
        minion-to-summon (create-minion minion-name :id minion-id)]

    (-> (remove-minion-from-graveyard state player-id minion-id)
        (add-minion-to-board player-id minion-to-summon 6))))

(defn copy-minion-from-graveyard-to-board-randomly
  "Add minion to board from graveyard given seed"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}])
                    (move-minion-from-board-to-graveyard "p1" "ib")
                    (copy-minion-from-graveyard-to-board-randomly "p1" 0)
                    (get-minions "p1")
                    (get-in [0 :name]))
                "Injured Blademaster")
           )}
  [state player-id seed]
  (let [list-with-seed-and-minion-to-move (random-nth seed (get-player-graveyard state player-id))
        random-minion (nth list-with-seed-and-minion-to-move 1)
        copied-minion (create-minion (:name random-minion))
        new-seed (nth (random-nth seed (get-player-graveyard state player-id)) 0)]
    (->
      (add-minion-to-board state (get-in state [:player-id-in-turn]) copied-minion 6)
      (update-seed new-seed))))

(defn sleep-minion
  "put minion to sleep"
  {:test (fn []
           ;id "go" should be sleeping
           (is= (-> (create-game)
                    (sleep-minion "go")
                    (sleepy? "go"))
                true))}
  [state id]
  (update state :minion-ids-summoned-this-turn conj id))


(defn sleep-minions
  "put minion to sleep"
  {:test (fn []
           ;ids "bo" "go" and "vo" should be sleepy and put in minion-ids-summoned-this-turn
           (is= (-> (create-game)
                    (sleep-minions ["bo" "go" "vo"])
                    (get :minion-ids-summoned-this-turn))
                ["bo" "go" "vo"]))}
  [state ids]
  (reduce (fn [state ids]
            (sleep-minion state ids))
          state
          ids))

(defn wake-up-sleepy-minion
  "Wakes up a minion with the given id"
  {:test (fn []
           ;id "bo" should be awaken
           (is= (-> (create-game [] :minion-ids-summoned-this-turn ["ib" "bo"])
                    (wake-up-sleepy-minion "bo")
                    (sleepy? "bo"))
                false))}
  [state id]
  (update state :minion-ids-summoned-this-turn
          (fn [minions]
            (remove (fn [m] (= m id)) minions))))

(defn wake-up-sleepy-minions
  "Wakes up a minions with the given ids"
  {:test (fn []
           ;ib and bo should be awaken and removed from :minion-ids-summoned-this-turn and
           (is= (-> (create-game [] :minion-ids-summoned-this-turn ["ib" "bo"])
                    (wake-up-sleepy-minions ["ib" "bo"])
                    (sleepy? "ib")
                    (and (sleepy? "bo")))
                false))}
  [state ids]
  (reduce (fn [state ids]
            (wake-up-sleepy-minion state ids))
          state
          ids))

(defn get-sleeping-minions
  "returns sleeping minions"
  {:test (fn []
           ;should get ids "ib" and "bo" in :minion-ids-summoned-this-turn
           (is= (-> (create-game [] :minion-ids-summoned-this-turn ["ib" "bo"])
                    (get-sleeping-minions))
                ["ib" "bo"]))}
  [state]
  (get state :minion-ids-summoned-this-turn))






(defn get-player-sleepy-minions
  "get players sleeping minions"
  {:test (fn []
           ;should only return player "p1" sleepy minions "ib" and "bo"
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib") (create-minion "Boulderfist Ogre" :id "bo")]}]
                                 :minion-ids-summoned-this-turn ["ib" "bo" "ab" "cb"])
                    (get-player-sleepy-minions "p1"))
                ["ib" "bo"]))}
  [state player-id]
  (let [player-minion-ids (get-player-minion-ids state player-id)
        sleeping-minions (get-sleeping-minions state)]
    ;kolla om reduce kan ersätta med filter
    (reduce (fn [is-sleeping id]
              (if (some #(= id %) sleeping-minions)
                (conj is-sleeping id)
                is-sleeping))
            []
            player-minion-ids)))

(defn wake-up-player-sleepy-minions
  "wake up a players sleeping minions"
  {:test (fn []
           ;minion "ib" should be awakened
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}]
                                 :minion-ids-summoned-this-turn ["ib"])
                    (wake-up-player-sleepy-minions "p1")
                    (sleepy? "ib"))
                false))}
  [state player-id]
  (let [player-sleeping-minions (get-player-sleepy-minions state player-id)]
    (wake-up-sleepy-minions state player-sleeping-minions)))



(defn run-battlecry-function
  "get the battlecry function from the card. ¿activate it?"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Flame Imp" :id "fi")]}])
                    (run-battlecry-function "fi")
                    (get-hero-damage-taken-by-player-id "p1")
                    )
                3)
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}])
                    (run-battlecry-function "ib")
                    (get-minion-value "ib" :damage-taken)
                    )
                4)
           ;Darkscale Healers battlecry should heal all friendly characters by 2
           (is= (-> (create-game [{:minions [(create-minion "Darkscale Healer" :id "dh")
                                             (create-minion "Injured Blademaster" :id "ib" :damage-taken 2)]}])
                    (run-battlecry-function "dh")
                    (get-damage-taken "ib"))
                0)
           ;Faceless manipulator copied minion should be sleepy
           (is= (-> (create-game [{:minions [(create-minion "Faceless Manipulator" :id "fm")
                                             (create-minion "Injured Blademaster" :id "ib")]}])
                    (run-battlecry-function "fm" "ib")
                    (sleepy? "fm"))
                true)
           ;The Black Knight battlecry should destroy a minion with taunt
           (is= (-> (create-game [{:minions [(create-minion "The Black Knight" :id "tbk")]}
                                  {:minions [(create-minion "Injured Blademaster" :id "ib" :damage-taken 0 :abilities [:taunt :yet-another-ability])]}])
                    (run-battlecry-function "tbk" "ib")
                    (is-minion-on-board? "ib"))
                false)
           ;The Tinkmaster Overspark battlecry should transform another random minion into a 5/5 Devilsaur or a 1/1 Squirrel.
           (is= (as-> (create-game [{:minions [(create-minion "Tinkmaster Overspark" :id "to")]}
                                    {:minions [(create-minion "Injured Blademaster" :id "ib")]}] :seed 0) $
                      (run-battlecry-function $ "to")
                      (:name (get-minion $ "ib")))
                "Devilsaur")
           (is= (as-> (create-game [{:minions [(create-minion "Tinkmaster Overspark" :id "to")]}
                                    {:minions [(create-minion "Injured Blademaster" :id "ib")]}] :seed 1) $
                      (run-battlecry-function $ "to")
                      (:name (get-minion $ "ib")))
                "Squirrel"))}
  ([state minion-id]
   (let [minion-name (get-minion-value state minion-id :name)
         battlecry-function (:battlecry (get-definition minion-name))]
     (battlecry-function state minion-id nil)))
  ([state minion-id target-id]
   (let [minion-name (get-minion-value state minion-id :name)
         battlecry-function (:battlecry (get-definition minion-name))]
     (battlecry-function state minion-id target-id))))



;kan få alla minions med en given ability (get-minions-with-ability)
(defn run-inspire-function
  "runs the inspire function for a given hero-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Mukla's Champion" :id "mc"), (create-minion "Lowly Squire" :id "ls") (create-minion "Flame Imp" :id "fi")]}])
                    (run-inspire-function "mc")
                    (get-minion-buffs "fi"))
                {:attack 1, :health 1, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
           (is= (-> (create-game [{:minions [(create-minion "Boneguard Lieutenant" :id "bl")]}])
                    (run-inspire-function "bl")
                    (get-minion-buffs "bl"))
                {:attack 0, :health 1, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
           (is= (-> (create-game [{:minions [(create-minion "Mukla's Champion" :id "mc"), (create-minion "Lowly Squire" :id "ls")]}])
                    (run-inspire-function "mc")
                    (get-minion-buffs "mc"))
                {:attack 0, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
           (is= (-> (create-game [{:minions [(create-minion "Lowly Squire" :id "ls")]}])
                    (run-inspire-function "ls")
                    (get-minion-buffs "ls"))
                {:attack 1, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
           (is= (-> (create-game [{:minions [(create-minion "Argent Watchman" :id "aw")]}])
                    (run-inspire-function "aw")
                    (get-minion-value "aw" :can-attack))
                true))}
  [state minion-id]
  (let [minion-name (get-minion-value state minion-id :name)
        inspire-function (:inspire-function (get-definition minion-name))]
    (inspire-function state minion-id)))

(defn run-players-inspire-functions
  "Runs the all the inspire functions of a player"
  {:test (fn []
           ;both muklas and lowly squire's inspire function should run.
           (is= (-> (create-game [{:minions [(create-minion "Mukla's Champion" :id "mc") (create-minion "Lowly Squire" :id "ls")]}])
                    (run-players-inspire-functions "p1")
                    (get-minion-buffs "ls"))
                {:attack 2, :health 1, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
           ;the minions doesn't have any inspire function, nothing should happen
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}])
                    (run-players-inspire-functions "p1")
                    (get-minion-buffs "ib"))
                {:attack 0, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
           ;lowly Squires inspire function should run
           (is= (-> (create-game [{:minions [(create-minion "Lowly Squire" :id "ls")]}])
                    (run-players-inspire-functions "p1")
                    (get-minion-buffs "ls"))
                {:attack 1, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0}))}
  [state player-id]
  (let [minions-with-inspire-function (get-minions-with-ability state player-id :inspire)]
    (->> minions-with-inspire-function
         (reduce-kv (fn [state _ minion-id]
                      (run-inspire-function state
                                            minion-id))
                    state))))



(defn run-hero-power-function
  "Runs the hero-power and reduces players mana given hero-id and heal-target-or-player-id.
  heal-target-or-player-id: is either a target to heal or player-id depending on what"
  {:test (fn []
           (is= (-> (create-game [{:hero "Uther Lightbringer"}])
                    (run-hero-power-function "p1" "h1")
                    (get-number-of-minions "p1"))
                1)
           (is= (-> (create-game [{:hero "Uther Lightbringer" :minions [(create-minion "Flame Imp" :id "fi") (create-minion "Flame Imp") (create-minion "Flame Imp") (create-minion "Flame Imp") (create-minion "Flame Imp") (create-minion "Flame Imp") (create-minion "Flame Imp")]}])
                    (run-hero-power-function "p1" "p1")
                    (get-number-of-minions "p1"))
                7)
           (is= (-> (create-game [{:hero "Anduin Wrynn" :minions [(create-minion "Flame Imp" :id "fi" :damage-taken 2)]}])
                    (run-hero-power-function "p1" "fi")
                    (get-damage-taken "fi"))
                0)
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :damage-taken 10)}])
                    (run-hero-power-function "p1" "h1")
                    (get-damage-taken "h1"))
                8)
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :damage-taken 10)}])
                    (run-hero-power-function "p1" "h2")
                    (get-hero-power-used-by-hero-id "h1"))
                true)
           (is= (as-> (create-game [{:hero "Anduin Wrynn" :minions [(create-minion "Mukla's Champion" :id "mc")
                                                                    (create-minion "Lowly Squire" :id "ls")
                                                                    (create-minion "Argent Watchman" :id "aw")
                                                                    (create-minion "Boneguard Lieutenant" :id "bl")]}]) $
                      (run-hero-power-function $ "p1" "h1")
                      (and (is= (get-minion-buffs $ "ls") {:attack 2, :health 1, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
                           (is= (get-minion-buffs $ "bl") {:attack 1, :health 2, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
                           (is= (get-minion-buffs $ "mc") {:attack 0, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
                           (is= (get-minion-value $ "aw" :can-attack) true)))
                true)
           (is= (try
                  (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :hero-power-used 1)}])
                      (run-hero-power-function "p1" "h1"))
                  (catch Exception e (str "caught exception: " (.getMessage e))))
                "caught exception: Hero power already used, can't be used twice")
           )}
  [state player-id heal-target-or-player-id]
  (let [hero (get-player-value state player-id :hero)
        hero-id (:id hero)
        hero-power-name (:hero-power (get-hero state hero-id))
        hero-power-function (:hero-power-function (get-definition hero-power-name))
        hero-power-used (get-hero-power-used-by-hero-id state hero-id)
        hero-owner-id (get-player-id-by-character-id state hero-id)]

    (if (false? hero-power-used)
      (-> (update-mana-after-hero-power state player-id hero-power-name)
          (hero-power-function player-id heal-target-or-player-id)
          (run-players-inspire-functions hero-owner-id))
      (throw (Exception. "Hero power already used, can't be used twice")))))


(defn has-battlecry?
  "Check if minion has battlecry"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Flame Imp" :id "fi")]}])
                    (has-battlecry? "Flame Imp"))
                true)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (has-battlecry? "Boulderfist Ogre"))
                false))}
  [state minion-name]
  (if (some? (-> (get-definition minion-name)
                 (:battlecry)))
    true
    false))

(defn move-minion-to-graveyard-and-run-deathrattle
  "If the minion is dead run deathrattle and move the minion to the graveyard"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib" :damage-taken 9)]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (move-minion-to-graveyard-and-run-deathrattle "ib")
                    (is-minion-on-board? "ib")
                    )
                false)
           (is= (-> (create-game [{:minions [(create-minion "Unstable Ghoul" :id "ug" :damage-taken 10)
                                             (create-minion "Flame Imp" :id "fi")]}])
                    (move-minion-to-graveyard-and-run-deathrattle "ug")
                    (get-minion-value "fi" :damage-taken)
                    )
                1)
           ;"Injured Blademaster" should be in graveyard
           (is= (as-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib" :damage-taken 9)]}]) $
                      (move-minion-to-graveyard-and-run-deathrattle $ "ib")
                      (get-player-graveyard $ "p1")
                      (map :name $))
                ["Injured Blademaster"]))}
  [state minion-id]
  (if (has-deathrattle? state minion-id)
    (-> (move-minion-from-board-to-graveyard state ((get-minion state minion-id) :owner-id) minion-id)
        (fetch-and-run-deathrattle-functions minion-id))
    (move-minion-from-board-to-graveyard state ((get-minion state minion-id) :owner-id) minion-id)))




(defn destroy-minion
  "Sets target damage-taken to health and kills it"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib" :damage-taken 0)]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (destroy-minion "ib")
                    (is-minion-on-board? "ib")
                    )
                false)
           ;"Injured Blademaster" should be in graveyard
           (is= (as-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib" :damage-taken 0)]}]) $
                      (destroy-minion $ "ib")
                      (get-player-graveyard $ "p1")
                      (map :name $))
                ["Injured Blademaster"]))}
  [state minion-id]
  (let [health (get-health state minion-id)]
    (-> (update-damage-taken state minion-id (+ (get-damage-taken state minion-id) health))
        (move-minion-to-graveyard-and-run-deathrattle minion-id))))

(defn destroy-minion-with-taunt
  "Sets target damage-taken to health and kills it if the minion has taunt"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib" :damage-taken 0 :abilities [:taunt :yet-another-ability])]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (destroy-minion-with-taunt "ib")
                    (is-minion-on-board? "ib")
                    )
                false)
           (is= (try
                  (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib" :damage-taken 0)]}])
                      (destroy-minion-with-taunt "ib"))
                  (catch Exception e (str "caught exception: " (.getMessage e))))
                "caught exception: target-minion has no taunt"))}

  [state minion-id]
  (if (check-for-ability-in-minion state minion-id :taunt)
    (destroy-minion state minion-id)
    (throw (Exception. "target-minion has no taunt"))))



(defn update-state-after-attack
  "Returns the state after an attack (either updated damage-taken or removed minion)"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib" :damage-taken 2)]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-state-after-attack "ib")
                    (is-minion-on-board? "ib")
                    )
                true)
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib" :damage-taken 8)]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-state-after-attack "ib")
                    (is-minion-on-board? "ib")
                    )
                false)
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :damage-taken 35)}])
                    (update-state-after-attack "h1")
                    )
                "game has ended")
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :damage-taken 29)}])
                    (update-state-after-attack "h1")
                    (has-zero-health? "h1")
                    )
                false))}
  [state target-id]
  (let [is-hero (get-hero-value-by-hero-id state target-id :entity-type)]
    (if is-hero
      (if (has-zero-health? state target-id)
        (end-game state)
        state)

      (if (is-minion-on-board? state target-id)
        (if (has-zero-health? state target-id)
          (move-minion-to-graveyard-and-run-deathrattle state target-id)
          state)
        state))))

(defn run-spell-event
  "Runs  all the spell-events of a player"
  {:test (fn []
           ;Should randomly pick an enemy character and deal 8 damage. p2 only has hero so its hero will get 8 damage-taken
           (is= (-> (create-game [{:minions [(create-minion "Archmage Antonidas" :id "aa")]}]
                                 )
                    (run-spell-event)
                    (get-card-by-name "p1" :hand "Fireball")
                    (:name))
                "Fireball"))}
  [state]
  (let [player-id (:player-id-in-turn state)]
    (-> (run-events state player-id :spell-event)
        )))

(defn activate-spell
  "Activate spell by getting the spell function from cards, can handle spell with and without targets, removes card from hand after use."
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Fireball" :id "fb")]}])
                    (activate-spell "fb" "h1")
                    (get-damage-taken "h1"))
                6)
           (is= (-> (create-game [{:hand [(create-card "Consecration" :id "cos")]}])
                    (activate-spell "cos")
                    (get-damage-taken "h2"))
                2)
           (is= (-> (create-game [{:hand [(create-card "Entomb" :id "e")]
                                   :deck [(create-card "Boulderfist Ogre" :id "bo1")
                                          (create-card "Fireball" :id "fb")]}
                                  {:minions [(create-minion "Ragnaros the Firelord" :id "rtf")]}])
                    (activate-spell "e" "rtf")
                    (get-player-deck-ids "p1"))
                ["bo1" "c2" "fb"])
           (is= (as-> (create-game [{:hand [(create-card "Entomb" :id "e")]}
                                    {:minions [(create-minion "Ragnaros the Firelord" :id "rtf")]}]) $
                      (activate-spell $ "e" "rtf")
                      (:name (get-top-card-from-deck $ "p1")))
                "Ragnaros the Firelord")
           (is= (-> (create-game [{:hand [(create-card "Sprint" :id "sp")]}])
                    (activate-spell "sp")
                    (get-damage-taken "h1"))
                10)
           (is= (-> (create-game [{:hand [(create-card "Sprint" :id "sp")]}])
                    (add-cards-to-hand "p1" ["Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre"])
                    (add-cards-to-deck "p1" ["Boulderfist Ogre", "Injured Blademaster"])
                    (activate-spell "sp")
                    (get-damage-taken "h1"))
                3)
           (is= (-> (create-game [{:hand [(create-card "Sprint" :id "sp")]}])
                    (add-cards-to-hand "p1" [  "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre", "Boulderfist Ogre"])
                    (add-cards-to-deck "p1" ["Boulderfist Ogre", "Injured Blademaster" "Injured Blademaster" "Injured Blademaster"])
                    (activate-spell "sp")
                    (get-cards "p1")
                    (count))
                10)
           (is= (-> (create-game [{:hand    [(create-card "Ancestral Spirit" :id "as")]
                                   :minions [(create-minion "Ragnaros the Firelord" :id "rtf")]}])
                    (activate-spell "as" "rtf")
                    (check-for-ability-in-minion "rtf" :tmp-deathrattle))
                true)
           (is= (-> (create-game [{:hand    [(create-card "Ancestral Spirit" :id "as")]
                                   :minions [(create-minion "Boulderfist Ogre" :id "boggy")]}])
                    (activate-spell "as" "boggy")
                    (move-minion-to-graveyard-and-run-deathrattle "boggy")
                    (fetch-and-run-deathrattle-functions "boggy")
                    (is-minion-in-graveyard? "boggy"))
                false)
           )}
  ([state card-id target-id]

   (let [spell-name ((get-value-from-card state card-id) :name)
         spell-function (:spell-effect (get-definition spell-name))]
     (-> (remove-card-from-hand state (get-in state [:player-id-in-turn]) card-id)
         (spell-function target-id)
         (update-state-after-attack target-id)
         (run-spell-event)
         )))
  ([state card-id]
   (let [spell-name ((get-value-from-card state card-id) :name)
         spell-function (:spell-effect (get-definition spell-name))]
     (-> (remove-card-from-hand state (get-in state [:player-id-in-turn]) card-id)
         (spell-function)
         (run-spell-event)))))


(defn run-end-turn-event
  "Run a minions end-turn event"
  {:test (fn []
           ;Should randomly pick an enemy character and deal 8 damage. p2 only has hero so its hero will get 8 damage-taken
           (is= (-> (create-game [{:minions [(create-minion "Ragnaros the Firelord" :id "rtf")]}]
                                 :seed 2)
                    (run-end-turn-event "rtf")
                    (get-hero-damage-taken-by-player-id "p2")) 8)
           (is= (-> (create-game [{:minions [(create-minion "Argent Watchman" :id "aw" :can-attack true)]}])
                    (run-end-turn-event "aw")
                    (get-minion-value "aw" :can-attack))
                false)
           ;Should randomly pick an enemy character and deal 8 damage.
           (is= (-> (create-game [{:minions [(create-minion "Ragnaros the Firelord" :id "rtf")]}
                                  {:minions [(create-minion "Ragnaros the Firelord" :id "rtf2")
                                             (create-minion "Ragnaros the Firelord" :id "rtf3")]}]
                                 :seed 2)
                    (update-minion-buff-health "rtf3" 10)
                    (run-end-turn-event "rtf")
                    (get-damage-taken "rtf3")) 8)
           (is= (-> (create-game [{:minions [(create-minion "Militia Commander" :id "mc")]}])
                    (update-minion "mc" :buffs :tmp-attack 3)
                    (run-end-turn-event "mc")
                    (get-minion "mc")
                    (:buffs))
                {:attack 0, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
           )}
  [state minion-id]
  (run-event state minion-id :end-turn-event))



(defn run-end-turn-events
  "Runs the all the end-turn-events of a player"
  {:test (fn []
           ;One Ragnaros the Firelord should deal 8 dmg to the enemy hero
           (is= (-> (create-game [{:minions [(create-minion "Ragnaros the Firelord" :id "rtf")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}]
                                 :seed 2)
                    (run-end-turn-events "p1")
                    (get-hero-damage-taken-by-player-id "p2"))
                8)
           ;Two Ragnaros the Firelord should deal 16 dmg to the enemy hero
           (is= (-> (create-game [{:minions [(create-minion "Ragnaros the Firelord" :id "rtf")
                                             (create-minion "Ragnaros the Firelord" :id "rtf2")]}]
                                 :seed 2)
                    (run-end-turn-events "p1")
                    (get-hero-damage-taken-by-player-id "p2"))
                16))}
  [state player-id]
  (run-events state player-id :end-turn-event))


(defn run-heal-event
  "Run a minions heal-event"
  {:test (fn []
           ;lw2 should have + 2 attack
           (is= (-> (create-game [{:minions [(create-minion "Lightwarden" :id "lw")]}
                                  {:minions [(create-minion "Ragnaros the Firelord" :id "rtf2")
                                             (create-minion "Ragnaros the Firelord" :id "rtf3")]}]
                                 :seed 2)
                    (run-heal-event "lw")
                    (get-minion-buff "lw" :attack)
                    )
                3)
           ;Should draw a car when running Northshire Clerics heal-event.
           (is= (-> (create-game [{:minions [(create-minion "Northshire Cleric" :id "nc")]}
                                  {:minions [(create-minion "Ragnaros the Firelord" :id "rtf2")
                                             (create-minion "Ragnaros the Firelord" :id "rtf3")]}])
                    (add-cards-to-deck "p1" ["Boulderfist Ogre", "Injured Blademaster"])
                    (run-heal-event "nc")
                    (get-top-card-from-hand "p1")
                    (get :name))
                "Boulderfist Ogre")
           )}
  [state minion-id]
  (run-event state minion-id :heal-event))

(defn run-heal-events
  "Runs the all the heal-events of a player"
  {:test (fn []
           ;lw2 should have + 2 attack
           (is= (-> (create-game [{:minions [(create-minion "Lightwarden" :id "lw1")
                                             (create-minion "Lightwarden" :id "lw2")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}]
                                 :seed 2)
                    (run-heal-events "p1")
                    (get-minion-buff "lw2" :attack))
                3)
           )}
  [state player-id]
  (run-events state player-id :heal-event))



(defn run-rush-event
  "Wakes up a minion with the given id"
  {:test (fn []
           ;id "bo" should be awaken
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "lw1")
                                             (create-minion "Militia Commander" :id "mc" :abilities [:rush :another-ability])]}
                                  ])
                    (sleep-minions ["lw1" "mc"])
                    (run-rush-event "p1")
                    (sleepy? "mc"))
                false))}
  [state player-id]
  (let [minions-with-rush (get-minions-with-ability state player-id :rush)]
    (->> minions-with-rush
         (reduce-kv (fn [state _ minion]
                      (wake-up-sleepy-minion state minion))
                    state))
    ))



(defn get-minions-with-ability-from-target-list
  "Returns a list with all the minions with a specific ability from a provided list"
  {:test (fn []
           ;One minion has the ability should return one minion
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                             (create-minion "Flame Imp" :id "fi" :abilities [:not-taunt :yet-another-ability])]}])
                    (get-minions-with-ability-from-target-list ["h1" "bo" "fi"] :taunt))
                ["bo"]))}
  [state valid-targets ability]
  (reduce (fn [acc target-id]
            (if (check-for-ability-in-minion state target-id ability)
              (conj acc target-id)
              acc))
          [] valid-targets))

(defn remove-character-id-from-list
  "Removes the character-id the provided list of valid targets and character-id"
  {:test (fn []

           (is= (->
                  (remove-character-id-from-list ["h1" "lw1" "lw2" "h2"] "h2")
                  )
                ["h1" "lw1" "lw2"])
           )}
  [valid-targets character-id]
  (remove #(= character-id %) valid-targets))

(defn return-list-with-valid-taunt-targets
  "From a list of targets return the list with valid taunt-targets
  1. if there is a target with taunt: return only the targets with taunt
  2. if there are no targets with taunt: return the whole valid-target list"
  {:test (fn []
           ;Should only return bo as it has taunt
           (is= (-> (create-game [{:minions [(create-minion "Lightwarden" :id "lw1" :abilities [:another-ability])
                                             (create-minion "Lightwarden" :id "lw2")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                             (create-minion "Lightwarden" :id "lw3" :abilities [:another-ability])]}]
                                 )
                    (return-list-with-valid-taunt-targets ["lw3" "lw1" "lw2" "bo"])
                    )
                ["bo"])
           ;no character has :taunt return all chars in valid-targets
           (is= (-> (create-game [{:minions [(create-minion "Lightwarden" :id "lw1")
                                             (create-minion "Lightwarden" :id "lw2")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:another-ability])]}]
                                 )
                    (return-list-with-valid-taunt-targets ["h1" "lw1" "lw2" "bo"])
                    )
                ["h1" "lw1" "lw2" "bo"])
           )}
  [state valid-targets]
  (let [next-player-id-in-turn (get-next-player-id-in-turn state)]
    (if (check-for-ability-in-player state next-player-id-in-turn :taunt)
      (get-minions-with-ability-from-target-list state valid-targets :taunt)
      valid-targets)
    ))

(defn return-list-with-taunt-targets
  "From a list of targets return the list with targets that has taunt"
  {:test (fn []
           ;Should only return bo as it has taunt
           (is= (-> (create-game [{:minions [(create-minion "Lightwarden" :id "lw1" :abilities [:another-ability])
                                             (create-minion "Lightwarden" :id "lw2")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:taunt :another-ability])
                                             (create-minion "Lightwarden" :id "lw3" :abilities [:another-ability])]}]
                                 )
                    (return-list-with-taunt-targets ["lw3" "lw1" "lw2" "bo"])
                    )
                ["bo"])
           ;no character has :taunt return all chars in valid-targets
           (is= (-> (create-game [{:minions [(create-minion "Lightwarden" :id "lw1")
                                             (create-minion "Lightwarden" :id "lw2")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo" :abilities [:another-ability])]}]
                                 )
                    (return-list-with-taunt-targets ["h1" "lw1" "lw2" "bo"])
                    )
                [])
           )}
  [state valid-targets]
  (let [next-player-id-in-turn (get-next-player-id-in-turn state)]
    (if (check-for-ability-in-player state next-player-id-in-turn :taunt)
      (get-minions-with-ability-from-target-list state valid-targets :taunt)
      [])))


(defn remove-hero-id-from-valid-targets
  "Removes the enemy hero id from the provided list of valid targets"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Lightwarden" :id "lw1")
                                             (create-minion "Lightwarden" :id "lw2")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}]
                                 :seed 2)
                    (remove-hero-id-from-valid-targets ["h1" "lw1" "lw2" "h2"])
                    )
                ["h1" "lw1" "lw2"])
           )}
  [state valid-targets]
  (let [next-player-id-in-turn (get-next-player-id-in-turn state)
        next-player-hero-id (get-hero-id-by-player-id state next-player-id-in-turn)]
    (remove-character-id-from-list valid-targets next-player-hero-id)))


(defn minion-able-to-attack?
  "check if a minion should be able to attack"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Militia Commander" :id "mc")]}]
                                 :minion-ids-summoned-this-turn ["mc"])
                    (minion-able-to-attack? "mc"))
                true)
           (is= (-> (create-game [{:minions [(create-minion "Militia Commander" :id "mc" :attacks-performed-this-turn 1)]}]
                                 :minion-ids-summoned-this-turn ["mc"])
                    (minion-able-to-attack? "mc"))
                false)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}]
                                 :minion-ids-summoned-this-turn ["bo"])
                    (minion-able-to-attack? "bo"))
                false))}
  [state minion-id]
  (let [minion (get-character-or-card state minion-id)
        attacks-performed (:attacks-performed-this-turn minion)]

    (if (= attacks-performed 0)
      (cond
        (check-for-ability-in-minion state minion-id :rush)
        true

        (sleepy? state minion-id)
        false

        :else true)
      false)))



(defn get-minion-valid-targets
  "get valid targets for a minion without battlecry given minion-id"
  {:test (fn []
           ;Milita commander has ability rush, should be able to attack enemy minions
           (is= (-> (create-game [{:minions [(create-minion "Militia Commander" :id "mc")]}
                                  {:minions [(create-minion "Lightwarden" :id "lw1")
                                             (create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-minion-valid-targets "mc"))
                ["lw1" "bo"])
           ;Milita commander has ability rush, Unstable Ghoul has taunt, Milita commander should only be able to attack Unstable Ghoul
           (is= (-> (create-game [{:minions [(create-minion "Militia Commander" :id "mc")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Unstable Ghoul" :id "ug")]}])
                    (get-minion-valid-targets "mc"))
                ["ug"])
           ;Milita commander has ability rush, Unstable Ghoul has taunt, Milita commander should only be able to attack all Unstable Ghoul
           (is= (-> (create-game [{:minions [(create-minion "Militia Commander" :id "mc")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Unstable Ghoul" :id "ug1")
                                             (create-minion "Unstable Ghoul" :id "ug2")
                                             (create-minion "Unstable Ghoul" :id "ug3")]}])
                    (get-minion-valid-targets "mc"))
                ["ug1" "ug2" "ug3"])
           ;Faceless Manipulator is not sleepy, Unstable Ghoul has taunt, Faceless Manioulator should only be able to attack Unstable Ghoul
           (is= (-> (create-game [{:minions [(create-minion "Faceless Manipulator" :id "fm")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Unstable Ghoul" :id "ug1")
                                             (create-minion "Unstable Ghoul" :id "ug2")]}])
                    (get-minion-valid-targets "fm"))
                ["ug1" "ug2"])
           ;The Black Knight is sleep, should have no valid targets
           (is= (-> (create-game [{:minions [(create-minion "The Black Knight" :id "tbk")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Unstable Ghoul" :id "ug1")
                                             (create-minion "Unstable Ghoul" :id "ug2")]}] :minion-ids-summoned-this-turn ["tbk"])
                    (get-minion-valid-targets "tbk"))
                ["ug1" "ug2"]))}
  ([state minion-id]
   (let [enemy-characters-ids (get-enemy-characters-ids state)
         all-minion-ids (get-minion-ids-on-board state)
         minion-name (:name (get-character-or-card state minion-id))]

     (as-> (return-list-with-valid-taunt-targets state enemy-characters-ids) $
           (if (check-for-ability-in-minion state minion-id :rush)
             (remove-hero-id-from-valid-targets state $)
             $)))))




(defn get-spell-valid-targets
  {:test (fn []
           ;no enemy minions, should return empty list
           (is= (-> (create-game [{:minions [(create-minion "Lightwarden" :id "lw1")
                                             (create-minion "Faceless Manipulator" :id "fm")]
                                   :hand    [(create-card "Fireball" :id "fb")
                                             (create-card "Convert" :id "c")]}])
                    (get-spell-valid-targets "c"))
                [])
           ;should return enemy minions
           (is= (-> (create-game [{:minions [(create-minion "Lightwarden" :id "lw1")
                                             (create-minion "Faceless Manipulator" :id "fm")]
                                   :hand    [(create-card "Fireball" :id "fb")
                                             (create-card "Convert" :id "c")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Unstable Ghoul" :id "ug")]}])
                    (get-spell-valid-targets "c"))
                ["bo" "ug"])
           ;all characters on board are valid targets
           (is= (-> (create-game [{:hand [(create-card "Fireball" :id "fb")]}])
                    (get-spell-valid-targets "fb"))
                ["h1" "h2"])
           ;all characters on board are valid targets
           (is= (-> (create-game [{:minions [(create-minion "Lightwarden" :id "lw1")
                                             (create-minion "Faceless Manipulator" :id "fm")]
                                   :hand    [(create-card "Fireball" :id "fb")
                                             (create-card "Convert" :id "c")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Unstable Ghoul" :id "ug")]}])
                    (get-spell-valid-targets "fb"))
                ["lw1" "fm" "bo" "ug" "h1" "h2"]))}
  [state card-id]
  (let [all-characters-ids (get-all-character-ids-on-board state)
        spell-name (:name (get-card-by-card-id state card-id))
        enemy-minion-ids (get-enemy-minion-ids state)
        all-minion-ids (get-minion-ids-on-board state)
        undamaged-minions (get-minions-undamaged state)]

    (cond
      (= spell-name "Fireball") all-characters-ids
      (= spell-name "Convert") enemy-minion-ids
      (= spell-name "Rocket Boots") all-minion-ids
      (= spell-name "Light of the Naaru") all-characters-ids
      (= spell-name "Backstab") undamaged-minions
      (= spell-name "Ancestral Spirit") all-minion-ids
      (= spell-name "Entomb") enemy-minion-ids
      )))


(defn can-use-hero-power?
  "check if player can use hero-power"
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Uther Lightbringer" :id "h1")}])
                    (update-player-mana "p1" 1)
                    (can-use-hero-power? "h1"))
                false)
           (is= (-> (create-game [{:hero (create-hero "Uther Lightbringer" :id "h1")}])
                    (update-player-mana "p1" 2)
                    (can-use-hero-power? "h1"))
                true)
           )}
  [state hero-id]
  (let [player-id-in-turn (:player-id-in-turn state)
        owner-id (get-player-id-by-character-id state hero-id)
        hero (get-player-value state owner-id :hero)
        hero-power-mana-cost (:mana-cost (get-definition (:hero-power hero)))
        used? (get-hero-power-used-by-hero-id state hero-id)]
    (and
      (not used?)
      (= owner-id player-id-in-turn)
      (>= (get-player-mana state player-id-in-turn) hero-power-mana-cost))
    ))


(defn get-hero-power-valid-targets
  {:test (fn []
           ;no enemy minions, should return empty list
           (is= (-> (create-game [{:hero (create-hero "Uther Lightbringer" :id "h1")}
                                  {:minions [(create-minion "Lightwarden" :id "lw1")
                                             (create-minion "Faceless Manipulator" :id "fm")]
                                   :hand    [(create-card "Fireball" :id "fb")
                                             (create-card "Convert" :id "c")]}])
                    (get-hero-power-valid-targets "h1"))
                [])

           ;all characters on board are valid targets
           (is= (-> (create-game [{:hero "Anduin Wrynn" :minions [(create-minion "Mukla's Champion" :id "mc")
                                                                  (create-minion "Lowly Squire" :id "ls")
                                                                  (create-minion "Argent Watchman" :id "aw")
                                                                  (create-minion "Boneguard Lieutenant" :id "bl")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Unstable Ghoul" :id "ug")]}])
                    (get-hero-power-valid-targets "h1"))
                ["mc" "ls" "aw" "bl" "bo" "ug" "h1" "h2"]))}
  [state hero-id]
  (let [player-id (get-player-id-by-character-id state hero-id)
        hero (get-player-value state player-id :hero)
        all-characters-ids (get-all-character-ids-on-board state)
        enemy-minion-ids (get-enemy-minion-ids state)
        hero-power-name (:name (get-definition (:hero-power hero)))
        ]
    (cond
      (= hero-power-name "Reinforce") []
      (= hero-power-name "Lesser Heal") all-characters-ids)))


(defn activate-battlecry
  "if he doesn't have battlecry set valid-targets to empty list"

  [state card-id]
  (if (has-battlecry? state (get-minion-value state card-id :name))
    (run-battlecry-function state card-id)
    state
    ))


(defn- get-battlecry-valid-targets
  "Helper function to get-valid-targets"
  [state card-id]
  (let [enemy-player (get-next-player-id-in-turn state)
        all-minion-ids (get-minion-ids-on-board state)
        minion-name (:name (get-card-by-card-id state card-id))
        player-id (:player-id-in-turn state)
        friendly-minions (get-friendly-minions state player-id)]
    (cond
      (= minion-name "Faceless Manipulator") (remove-character-id-from-list all-minion-ids card-id)
      (= minion-name "The Black Knight") (get-minions-with-ability state enemy-player :taunt)
      (= minion-name "Princess Huhuran") friendly-minions
      :else
      (do []))))



(defn get-valid-targets
  {:test (fn []
           ;Milita commander has ability rush, should be able to attack enemy minions
           (is= (-> (create-game [{:minions [(create-minion "Militia Commander" :id "mc")]}
                                  {:minions [(create-minion "Lightwarden" :id "lw1")
                                             (create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-valid-targets "mc"))
                ["lw1" "bo"])
           ;all characters on board are valid targets
           (is= (-> (create-game [{:minions [(create-minion "Lightwarden" :id "lw1")
                                             (create-minion "Faceless Manipulator" :id "fm")]
                                   :hand    [(create-card "Fireball" :id "fb")
                                             (create-card "Convert" :id "c")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Unstable Ghoul" :id "ug")]}])
                    (get-valid-targets "fb"))
                ["lw1" "fm" "bo" "ug" "h1" "h2"]))}
  [state card-id]
  (let [character-or-card-name (:name (get-character-or-card state card-id))
        card-type (name (:type (get-definition character-or-card-name)))]
    (cond
      (= card-type "minion")
      (if (is-minion-on-board? state card-id)
        (get-minion-valid-targets state card-id)
        (get-battlecry-valid-targets state card-id))
      (= card-type "spell")
      (get-spell-valid-targets state card-id))))


(defn get-active-abilities-on-minion-as-strings
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Squirrel" :id "sq")]}])
                    (get-active-abilities-on-minion-as-strings "sq")
                    )
                [])
           (is= (-> (create-game [{:minions [(create-minion "Unstable Ghoul" :id "ug")]}])
                    (get-active-abilities-on-minion-as-strings "ug")
                    )
                ["taunt" "deathrattle"]))}
  [state minion-id]
  (let [active-abilities (get-active-abilities-from-minion state minion-id)]
    (-> (reduce (fn [acc active-abilities]
                  (conj acc (-> (name active-abilities))))
                [] active-abilities)
        (flatten)
        (vec)
        )))



(defn is-card-playble?
  "check player has enough mana to play card"
  {:test (fn []
           ;faceless manipulator cost 5 mana, current mana is 10, should be able to play card
           (is= (-> (create-game [{:hand [(create-card "Faceless Manipulator" :id "fm")]}])
                    (update-player-mana "p1" 10)
                    (is-card-playble? "p1" "fm"))
                true)
           ;faceless manipulator cost 5 mana, current mana is 4, should not be able to play card
           (is= (-> (create-game [{:hand [(create-card "Faceless Manipulator" :id "fm")]}])
                    (update-player-mana "p1" 4)
                    (is-card-playble? "p1" "fm"))
                false)
           ;faceless manipulator cost 5 mana, current mana is 5, should be able to play card
           (is= (-> (create-game [{:hand [(create-card "Faceless Manipulator" :id "fm")]}])
                    (update-player-mana "p1" 5)
                    (is-card-playble? "p1" "fm"))
                true)
           ;should be able to play spell card even if there are mximum minions on board
           (is= (-> (create-game [{:hand    [(create-card "Fireball" :id "fb")]
                                   :minions ["Faceless Manipulator"
                                             "Faceless Manipulator"
                                             "Faceless Manipulator"
                                             "Faceless Manipulator"
                                             "Faceless Manipulator"
                                             "Faceless Manipulator"
                                             "Faceless Manipulator"]}])
                    (update-player-mana "p1" 5)
                    (is-card-playble? "p1" "fb"))
                true)
           ;There is to many minions on board
           (is= (-> (create-game [{:hand    [(create-card "Faceless Manipulator" :id "fm")]
                                   :minions ["Faceless Manipulator"
                                             "Faceless Manipulator"
                                             "Faceless Manipulator"
                                             "Faceless Manipulator"
                                             "Faceless Manipulator"
                                             "Faceless Manipulator"
                                             "Faceless Manipulator"]}])
                    (update-player-mana "p1" 5)
                    (is-card-playble? "p1" "fm"))
                false))}
  [state player-id card-id]
  (let [current-mana (get-player-mana state player-id)
        card (get-card-from-hand state player-id card-id)
        mana-cost (:mana-cost (get-definition (:name card)))
        card-type (:type (get-definition (:name card)))
        valid-targets (get-valid-targets state card-id)]

    (if (>= current-mana mana-cost)
      (cond
        (= card-type :spell)
        (if (= valid-targets [])
          false
          true)

        (= card-type :minion)
        (if (< (get-number-of-minions state player-id) 7)
          true
          false)

        :else
        (do true))
      false)))

(defn replace-minion-on-board
  "Removes a minion on board given minion-id places a new minion on the same positon
  Keeping owner-id, id "
  {:test (fn []
           (is= (as-> (create-game [{:minions [(create-minion "Tinkmaster Overspark" :id "to")]}]) $
                      (replace-minion-on-board $ "to" "Faceless Manipulator")
                      (:name (get-minion $ "to")))
                "Faceless Manipulator"))}
  [state minion-to-be-replaced-id new-minion-name]
  (let [minion-to-be-replaced (get-minion state minion-to-be-replaced-id)
        position (:position minion-to-be-replaced)
        id (:id minion-to-be-replaced)
        owner-id (:owner-id minion-to-be-replaced)]

    (->
      (remove-minion state minion-to-be-replaced-id)
      (add-minion-to-board owner-id (create-minion new-minion-name :id id :owner-id owner-id) position)
      (run-board-change-events))))

(defn add-card-to-random-position-in-deck
  "Shuffles cards into deck"
  {:test (fn []
           ; Adding cards to deck
           (is= (as-> (create-game [{:deck [(create-card "Boulderfist Ogre" :id "bo1")
                                            (create-card "Boulderfist Ogre" :id "bo2")
                                            (create-card "Boulderfist Ogre" :id "bo3")
                                            (create-card "Boulderfist Ogre" :id "bo4")
                                            (create-card "Boulderfist Ogre" :id "bo5")
                                            (create-card "Boulderfist Ogre" :id "bo6")
                                            ]}] :seed 1) $
                      (add-card-to-random-position-in-deck $ "p1" "Nerubian Ambush!")
                      (get-deck $ "p1")
                      (map :name $))
                ["Boulderfist Ogre" "Nerubian Ambush!" "Boulderfist Ogre" "Boulderfist Ogre" "Boulderfist Ogre" "Boulderfist Ogre" "Boulderfist Ogre"])
           (is= (as-> (create-game [{:deck [(create-card "Boulderfist Ogre" :id "bo1")
                                            (create-card "Boulderfist Ogre" :id "bo2")
                                            (create-card "Boulderfist Ogre" :id "bo3")
                                            (create-card "Boulderfist Ogre" :id "bo4")
                                            (create-card "Boulderfist Ogre" :id "bo5")
                                            (create-card "Boulderfist Ogre" :id "bo6")
                                            ]}] :seed 1) $
                      (add-card-to-random-position-in-deck $ "p1" "Flame Imp")
                      (add-card-to-random-position-in-deck $ "p1" "Flame Imp")
                      (add-card-to-random-position-in-deck $ "p1" "Flame Imp")
                      (add-card-to-random-position-in-deck $ "p1" "Flame Imp")
                      (get-deck $ "p1")
                      (map :name $))
                ["Flame Imp" "Boulderfist Ogre" "Flame Imp" "Flame Imp" "Boulderfist Ogre" "Boulderfist Ogre" "Boulderfist Ogre" "Flame Imp" "Boulderfist Ogre" "Boulderfist Ogre"])

           )}
  [state player-id card-name]

  (let [[state value] (generate-id state)
        seed (get-seed state)
        card-to-be-added (create-card card-name :id (str "c" value))
        number-of-cards-in-deck (count (get-deck state player-id))
        [new-seed random-number] (random-nth seed (range number-of-cards-in-deck))]

    (-> (if (= number-of-cards-in-deck 0)
          (add-card-to-deck state player-id card-to-be-added)
          (add-value-by-vector-index state
                                     player-id
                                     :deck
                                     random-number
                                     card-to-be-added))
        (update-seed new-seed))
    ))



(defn add-random-card-to-deck
  "adds a random card from definitions to deck"
  {:test (fn []
           (is= (->(create-game)
                  (add-random-card-to-deck  "p1")
                   (get-player-deck-ids "p1"))
                ["c1"]))}
  [state player-id]
  (let [seed (get-seed state)
        all-cards (vec (map :name (get-definitions)))
        number-of-cards (count all-cards)
        [new-seed random-number] (random-nth seed (range number-of-cards))
        card-name-to-be-added (get all-cards random-number)]
    (->(add-card-to-deck state player-id card-name-to-be-added)
       (update-seed new-seed))
    ))


(defn draw-thirty-random-cards-to-player
  [state player-id]
  (->>  (vec (range 30))
       (reduce-kv (fn [state _ _]
                    (add-random-card-to-deck state player-id ))
                  state)))

(defn create-game-with-random-deck
  "creates a game with 30 random cards in each deck"
  {:test (fn []
           (is= (as-> (create-game-with-random-deck) $
                    (:id (get-top-card-from-deck $ "p2")))
                "c31"))}
  []
  (let [state (create-game [{:hand    [(create-card "Backstab" :id "fm")
                                       (create-card "Ancestral Spirit" :id "as")
                                       (create-card "Fireball" :id "fb")
                                       (create-card "Rocket Boots" :id "rb")
                                       (create-card "Entomb" :id "e")]}

                            {:hero    (create-hero "Anduin Wrynn")
                             :hand    [(create-card "Tinkmaster Overspark" :id "to")
                                       (create-card "Dire Wolf Alpha" :id "rl")
                                       (create-card "Fireball" :id "fb1")
                                       (create-card "Convert" :id "c")
                                       (create-card "Sprint" :id "s")]
                             }])]
    (-> (draw-thirty-random-cards-to-player state "p1")
       (draw-thirty-random-cards-to-player  "p2"))))
