(ns firestone.construct
  (:require [ysera.test :refer [is is-not is= error?]]
            [firestone.definitions :refer [get-definition
                                           get-definitions]]))


(defn get-hero-class
  {:test (fn []
           (is= (get-hero-class "Uther Lightbringer")
                :paladin)
           (is= (get-hero-class "Anduin Wrynn")
                :priest))}
  [hero-name]
  (:class (get-definition hero-name)))



(defn create-hero
  "Creates a hero from its definition by the given hero name. The additional key-values will override the default values."
  {:test (fn []
           (is= (create-hero "Uther Lightbringer")
                {:name            "Uther Lightbringer"
                 :entity-type     :hero
                 :damage-taken    0
                 :hero-power-used false
                 :class           :paladin
                 :hero-power      "Reinforce"})
           (is= (create-hero "Anduin Wrynn" :damage-taken 10)
                {:name            "Anduin Wrynn"
                 :entity-type     :hero
                 :damage-taken    10
                 :hero-power-used false
                 :class           :priest
                 :hero-power      "Lesser Heal"}))}
  [name & kvs]
  (let [hero {:name            name
              :entity-type     :hero
              :damage-taken    0
              :hero-power-used false
              :class           (get-hero-class name)
              :hero-power      (:hero-power (get-definition name))}]
    (if (empty? kvs)
      hero
      (apply assoc hero kvs))))

(defn create-card
  "Creates a card from its definition by the given card name. The additional key-values will override the default values."
  {:test (fn []
           (is= (create-card "Boulderfist Ogre" :id "bo")
                {:id          "bo"
                 :entity-type :card
                 :name        "Boulderfist Ogre"}))}
  [name & kvs]
  (let [card {:name        name
              :entity-type :card}]
    (if (empty? kvs)
      card
      (apply assoc card kvs))))

(defn create-minion
  "Creates a minion from its definition by the given minion name. The additional key-values will override the default values."
  {:test (fn []
           (is= (create-minion "Boulderfist Ogre" :id "bo" :attacks-performed-this-turn 1)
                {:attacks-performed-this-turn 1
                 :damage-taken                0
                 :entity-type                 :minion
                 :name                        "Boulderfist Ogre"
                 :id                          "bo"
                 :buffs                       {:attack 0, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0}
                 :active-abilities            nil
                 :abilities                   nil
                 :can-attack                  true})
           (is= (create-minion "Unstable Ghoul" :id "ug" :attacks-performed-this-turn 1 :active-abilities [:taunt :deathrattle :frozen])
                {:attacks-performed-this-turn 1
                 :damage-taken                0
                 :entity-type                 :minion
                 :name                        "Unstable Ghoul"
                 :id                          "ug"
                 :buffs                       {:attack 0, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0}
                 :abilities                   [:taunt :deathrattle]
                 :active-abilities            [:taunt :deathrattle :frozen]
                 :can-attack                  true}))}
  [name & kvs]
  (let [definition (get-definition name)                    ; Will be used later
        minion {:damage-taken                0
                :entity-type                 :minion
                :name                        name
                :attacks-performed-this-turn 0
                :buffs                       {:attack 0 :health 0 :tmp-attack 0 :tmp-health 0 :aura-attack 0}
                :active-abilities            (definition :active-abilities)
                :abilities                   (definition :abilities)
                :can-attack                  (definition :can-attack)}]
    (if (empty? kvs)
      minion
      (apply assoc minion kvs))))


(defn create-empty-state
  "Creates an empty state with the given heroes."
  {:test (fn []
           ; Rexxar will be the default hero
           (is= (create-empty-state [(create-hero "Uther Lightbringer")
                                     (create-hero "Uther Lightbringer")])
                (create-empty-state))

           (is= (create-empty-state [(create-hero "Uther Lightbringer" :id "r")
                                     (create-hero "Anduin Wrynn")])
                {:player-id-in-turn             "p1"
                 :players                       {"p1" {:id              "p1"
                                                       :fatigue-counter 0
                                                       :mana            1
                                                       :mana-capacity   1
                                                       :deck            []
                                                       :hand            []
                                                       :graveyard       []
                                                       :minions         []
                                                       :hero            {:name            "Uther Lightbringer"
                                                                         :id              "r"
                                                                         :damage-taken    0
                                                                         :hero-power-used false
                                                                         :entity-type     :hero
                                                                         :class           :paladin
                                                                         :hero-power      "Reinforce"}}
                                                 "p2" {:id              "p2"
                                                       :fatigue-counter 0
                                                       :mana            1
                                                       :mana-capacity   1
                                                       :deck            []
                                                       :hand            []
                                                       :graveyard       []
                                                       :minions         []
                                                       :hero            {:name            "Anduin Wrynn"
                                                                         :id              "h2"
                                                                         :damage-taken    0
                                                                         :hero-power-used false
                                                                         :entity-type     :hero
                                                                         :class           :priest
                                                                         :hero-power      "Lesser Heal"}}}
                 :counter                       1
                 :minion-ids-summoned-this-turn []
                 :seed                          1}))}
  ([heroes]
   ; Creates Uther Lightbringer heroes if heroes are missing.
   (let [heroes (->> (concat heroes [(create-hero "Uther Lightbringer")
                                     (create-hero "Uther Lightbringer")])
                     (take 2))]
     {:player-id-in-turn             "p1"
      :players                       (->> heroes
                                          (map-indexed (fn [index hero]
                                                         {:id              (str "p" (inc index))
                                                          :fatigue-counter 0
                                                          :mana            1
                                                          :mana-capacity   1
                                                          :deck            []
                                                          :hand            []
                                                          :graveyard       []
                                                          :minions         []
                                                          :hero            (if (contains? hero :id)
                                                                             hero
                                                                             (assoc hero :id (str "h" (inc index))))}))
                                          (reduce (fn [a v]
                                                    (assoc a (:id v) v))
                                                  {}))
      :counter                       1
      :minion-ids-summoned-this-turn []
      :seed                          1}))
  ([]
   (create-empty-state [])))

(defn get-player
  "Returns the player with the given id."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-player "p1")
                    (:id))
                "p1"))}
  [state player-id]
  (get-in state [:players player-id]))

(defn get-player-id-in-turn
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-player-id-in-turn))
                "p1"))}
  [state]
  (:player-id-in-turn state))

(defn get-next-player-id-in-turn
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-next-player-id-in-turn)
                    )
                "p2"))}
  [state]
  (let [player-change-fn {"p1" "p2"
                          "p2" "p1"}
        next-player (get player-change-fn (get-player-id-in-turn state))]
    next-player))

(declare create-game)

(defn get-minions
  "Returns the minions on the board for the given player-id or for both players."
  {:test (fn []
           ; Getting minions is also tested in add-minion-to-board.
           (is= (-> (create-empty-state)
                    (get-minions "p1"))
                [])
           (is= (-> (create-empty-state)
                    (get-minions))
                [])
           (is= (as-> (create-game [{:minions ["Boulderfist Ogre"]}]) $
                      (get-minions $ "p1")
                      (map :name $))
                ["Boulderfist Ogre"]))}
  ([state player-id]
   (:minions (get-player state player-id)))
  ([state]
   (->> (:players state)
        (vals)
        (map :minions)
        (apply concat))))

(defn get-cards
  "Returns the cards on hand for the given player-id or for both players."
  {:test (fn []
           ; Getting minions is also tested in add-minion-to-board.
           (is= (-> (create-empty-state)
                    (get-cards "p1"))
                [])
           (is= (-> (create-empty-state)
                    (get-cards))
                [])
           (is= (as-> (create-game [{:hand ["Boulderfist Ogre"]}
                                    {:hand ["Fireball"]}]) $
                      (get-cards $)
                      (map :name $))
                ["Boulderfist Ogre" "Fireball"])
           (is= (as-> (create-game [{:hand ["Boulderfist Ogre"]}]) $
                      (get-cards $ "p1")
                      (map :name $))
                ["Boulderfist Ogre"]))}
  ([state player-id]
   (:hand (get-player state player-id)))
  ([state]
   (->> (:players state)
        (vals)
        (map :hand)
        (apply concat))))

(defn get-deck
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-deck "p1"))
                []))}
  [state player-id]
  (get-in state [:players player-id :deck]))

(defn get-hand
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-hand "p1"))
                []))}
  [state player-id]
  (get-in state [:players player-id :hand]))

(defn generate-id
  "Generates an id and returns a tuple with the new state and the generated id."
  {:test (fn []
           (is= (generate-id {:counter 6})
                [{:counter 7} 6]))}
  [state]
  {:pre [(contains? state :counter)]}
  [(update state :counter inc) (:counter state)])

(defn- generate-time-id
  "Generates a number and returns a tuple with the new state and the generated number."
  {:test (fn []
           (is= (generate-id {:counter 6})
                [{:counter 7} 6]))}
  [state]
  {:pre [(contains? state :counter)]}
  [(update state :counter inc) (:counter state)])

(defn add-minion-to-board
  "Adds a minion with a given position to a player's minions and updates the other minions' positions."
  {:test (fn []
           ; Adding a minion to an empty board
           (is= (as-> (create-empty-state) $
                      (add-minion-to-board $ "p1" (create-minion "Boulderfist Ogre" :id "bo") 0)
                      (get-minions $ "p1")
                      (map (fn [m] {:id (:id m) :name (:name m)}) $))
                [{:id "bo" :name "Boulderfist Ogre"}])
           ; Adding a minion and update positions
           (let [minions (-> (create-empty-state)
                             (add-minion-to-board "p1" (create-minion "Boulderfist Ogre" :id "bo1") 0)
                             (add-minion-to-board "p1" (create-minion "Boulderfist Ogre" :id "bo2") 0)
                             (add-minion-to-board "p1" (create-minion "Boulderfist Ogre" :id "bo3") 1)
                             (get-minions "p1"))]
             (is= (map :id minions) ["bo1" "bo2" "bo3"])
             (is= (map :position minions) [2 0 1]))
           ; Generating an id for the new minion
           (let [state (-> (create-empty-state)
                           (add-minion-to-board "p1" (create-minion "Boulderfist Ogre") 0))]
             (is= (-> (get-minions state "p1")
                      (first)
                      (:name))
                  "Boulderfist Ogre")
             (is= (:counter state) 3)))}
  [state player-id minion position]
  {:pre [(map? state) (string? player-id) (map? minion) (number? position)]}
  (let [[state id] (if (contains? minion :id)
                     [state (:id minion)]
                     (let [[state value] (generate-id state)]
                       [state (str "m" value)]))
        [state time-id] (generate-time-id state)
        ready-minion (assoc minion :position position
                                   :owner-id player-id
                                   :id id
                                   :added-to-board-time-id time-id)]
    (update-in state
               [:players player-id :minions]
               (fn [minions]
                 (conj (->> minions
                            (mapv (fn [m]
                                    (if (< (:position m) position)
                                      m
                                      (update m :position inc)))))
                       ready-minion)))))

(defn add-minions-to-board
  {:test (fn []
           (is= (as-> (create-empty-state) $
                      (add-minions-to-board $ "p1" [(create-minion "Boulderfist Ogre")
                                                    "Silver Hand Recruit"
                                                    (create-minion "Injured Blademaster")])
                      (get-minions $ "p1")
                      (map :name $))
                ["Boulderfist Ogre" "Silver Hand Recruit" "Injured Blademaster"]))}
  [state player-id minions]
  (->> minions
       (reduce-kv (fn [state index minion]
                    (add-minion-to-board state
                                         player-id
                                         (if (string? minion)
                                           (create-minion minion)
                                           minion)
                                         index))
                  state)))


(defn add-minion-to-graveyard
  {:test (fn []
           (is= (as-> (create-game) $
                      (add-minion-to-graveyard $ "p1" (create-minion "Boulderfist Ogre" :id "bo"))
                      (get-in $ [:players "p1" :graveyard])
                      (map :name $))
                ["Boulderfist Ogre"])
           (is= (as-> (create-game) $
                      (add-minion-to-graveyard $ "p1" (create-minion "Boulderfist Ogre" :id "bo"))
                      (get-in $ [:players "p1" :graveyard])
                      (map :id $))
                ["bo"]))}
  [state player-id minion]
  (update-in state [:players player-id :graveyard] conj minion))

(defn add-minions-to-graveyard
  {:test (fn []
           (is= (as-> (create-empty-state) $
                      (add-minions-to-graveyard $ "p1" [(create-minion "Boulderfist Ogre")
                                                        "Silver Hand Recruit"
                                                        (create-minion "Injured Blademaster")])
                      (get-in $ [:players "p1" :graveyard])
                      (map :name $))
                ["Boulderfist Ogre" "Silver Hand Recruit" "Injured Blademaster"]))}
  [state player-id minions]
  (->> minions
       (reduce-kv (fn [state index minion]
                    (add-minion-to-graveyard state
                                             player-id
                                             (if (string? minion)
                                               (create-minion minion)
                                               minion)))
                  state)))

(defn- add-card-to
  "Adds a card to either the hand or the deck."
  {:test (fn []
           ; Adding cards to deck
           (is= (as-> (create-empty-state) $
                      (add-card-to $ "p1" "Boulderfist Ogre" :deck)
                      (add-card-to $ "p1" "Injured Blademaster" :deck)
                      (get-deck $ "p1")
                      (map :name $))
                ["Boulderfist Ogre" "Injured Blademaster"])
           ; Adding cards to hand
           (is= (as-> (create-empty-state) $
                      (add-card-to $ "p1" "Boulderfist Ogre" :hand)
                      (add-card-to $ "p1" "Injured Blademaster" :hand)
                      (get-hand $ "p1")
                      (map :name $))
                ["Boulderfist Ogre" "Injured Blademaster"]))}
  [state player-id card-or-name place]
  (let [card (if (string? card-or-name)
               (create-card card-or-name)
               card-or-name)
        [state id] (if (contains? card :id)
                     [state (:id card)]
                     (let [[state value] (generate-id state)]
                       [state (str "c" value)]))
        ready-card (assoc card :owner-id player-id
                               :id id)]
    (update-in state [:players player-id place] conj ready-card)))

(defn add-card-to-deck
  [state player-id card]
  (add-card-to state player-id card :deck))

(defn add-card-to-hand
  [state player-id card]
  (add-card-to state player-id card :hand))

(defn add-cards-to-deck
  [state player-id cards]
  (reduce (fn [state card]
            (add-card-to-deck state player-id card))
          state
          cards))

(defn add-cards-to-hand
  [state player-id cards]
  (reduce (fn [state card]
            (add-card-to-hand state player-id card))
          state
          cards))

(defn create-game
  "Creates a game with the given deck, hand, minions (placed on the board), and heroes."
  {:test (fn []
           (is= (create-game) (create-empty-state))

           (is= (create-game [{:hero (create-hero "Anduin Wrynn")}])
                (create-game [{:hero "Anduin Wrynn"}]))

           (is= (create-game [{:minions [(create-minion "Boulderfist Ogre")]}])
                (create-game [{:minions ["Boulderfist Ogre"]}]))

           (is= (create-game [{:minions ["Boulderfist Ogre"]
                               :deck    ["Injured Blademaster"]
                               :hand    ["Silver Hand Recruit"]}
                              {:hero "Anduin Wrynn"}]
                             :player-id-in-turn "p2")
                {:player-id-in-turn             "p2"
                 :players                       {"p1" {:id              "p1"
                                                       :fatigue-counter 0
                                                       :mana            1
                                                       :mana-capacity   1
                                                       :deck            [{:entity-type :card
                                                                          :id          "c3"
                                                                          :name        "Injured Blademaster"
                                                                          :owner-id    "p1"}]
                                                       :hand            [{:entity-type :card
                                                                          :id          "c4"
                                                                          :name        "Silver Hand Recruit"
                                                                          :owner-id    "p1"}]
                                                       :graveyard       []
                                                       :minions         [{:damage-taken                0
                                                                          :attacks-performed-this-turn 0
                                                                          :added-to-board-time-id      2
                                                                          :entity-type                 :minion
                                                                          :name                        "Boulderfist Ogre"
                                                                          :id                          "m1"
                                                                          :position                    0
                                                                          :owner-id                    "p1"
                                                                          :buffs                       {:attack 0 :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0}
                                                                          :active-abilities            nil
                                                                          :abilities                   nil
                                                                          :can-attack                  true}]
                                                       :hero            {:name            "Uther Lightbringer"
                                                                         :id              "h1"
                                                                         :entity-type     :hero
                                                                         :damage-taken    0
                                                                         :hero-power-used false
                                                                         :class           :paladin
                                                                         :hero-power      "Reinforce"}}
                                                 "p2" {:id              "p2"
                                                       :fatigue-counter 0
                                                       :mana            1
                                                       :mana-capacity   1
                                                       :deck            []
                                                       :hand            []
                                                       :minions         []
                                                       :graveyard       []
                                                       :hero            {:name            "Anduin Wrynn"
                                                                         :id              "h2"
                                                                         :entity-type     :hero
                                                                         :damage-taken    0
                                                                         :hero-power-used false
                                                                         :class           :priest
                                                                         :hero-power      "Lesser Heal"}}}
                 :counter                       5
                 :minion-ids-summoned-this-turn []
                 :seed                          1}))}
  ([data & kvs]
   (let [players-data (map-indexed (fn [index player-data]
                                     (assoc player-data :player-id (str "p" (inc index))))
                                   data)
         state (as-> (create-empty-state (map (fn [player-data]
                                                (cond (nil? (:hero player-data))
                                                      (create-hero "Uther Lightbringer")

                                                      (string? (:hero player-data))
                                                      (create-hero (:hero player-data))

                                                      :else
                                                      (:hero player-data)))
                                              data)) $
                     (reduce (fn [state {player-id :player-id
                                         minions   :minions
                                         deck      :deck
                                         graveyard :graveyard
                                         hand      :hand}]
                               (-> state
                                   (add-minions-to-board player-id minions)
                                   (add-cards-to-deck player-id deck)
                                   (add-minions-to-graveyard player-id graveyard)
                                   (add-cards-to-hand player-id hand)))
                             $
                             players-data))]
     (if (empty? kvs)
       state
       (apply assoc state kvs))))
  ([]
   (create-game [])))



(defn get-minion
  "Returns the minion with the given id."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-minion "bo")
                    (:name))
                "Boulderfist Ogre"))}
  [state id]
  (->> (get-minions state)
       (filter (fn [m] (= (:id m) id)))
       (first)))

(defn get-minion-by-position-index
  "Returns the minion with the given index."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-minion-by-position-index "p1" 0)
                    (:id))
                "bo"))}
  [state player-id index]
  (->> (get-minions state player-id)
       (filter (fn [m] (= (:position m) index)))
       (first)))


(defn get-minion-value-by-position-index
  "Returns value in minion given minion-id"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (get-minion-value-by-position-index "p1" 0 :entity-type))
                :minion))}
  [state player-id index key]
  (get-in (get-minion-by-position-index state player-id index) [key]))

(defn get-players
  {:test (fn []
           (is= (->> (create-game)
                     (get-players)
                     (map :id))
                ["p1" "p2"]))}
  [state]
  (->> (:players state)
       (vals)))

(defn get-heroes
  {:test (fn []
           (is= (->> (create-game [{:hero "Anduin Wrynn"}])
                     (get-heroes)
                     (map :name))
                ["Anduin Wrynn" "Uther Lightbringer"]))}
  [state]
  (->> (get-players state)
       (map :hero)))

(defn get-hero
  "Returns the hero with the given id."
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "aw")}])
                    (get-hero "aw")
                    (:id)
                    )
                "aw"))}
  [state id]
  (->> (get-heroes state)
       (filter (fn [m] (= (:id m) id)))
       (first)))



(defn replace-minion
  "Replaces a minion with the same id as the given new-minion."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "m")]}])
                    (replace-minion (create-minion "Silver Hand Recruit" :id "m"))
                    (get-minion "m")
                    (:name))
                "Silver Hand Recruit")
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "m")]}
                                  {:minions [(create-minion "Flame Imp" :id "fi")]}])
                    (replace-minion (create-minion "Silver Hand Recruit" :id "m"))
                    (get-minion "m")
                    (:name))
                "Silver Hand Recruit"))}
  [state new-minion]
  (let [owner-id (or (:owner-id new-minion)
                     (:owner-id (get-minion state (:id new-minion))))]
    (update-in state
               [:players owner-id :minions]
               (fn [minions]
                 (map (fn [m]
                        (if (= (:id m) (:id new-minion))
                          new-minion
                          m))
                      minions)))))

(defn update-minion
  "Updates the value of the given key for the minion with the given id. If function-or-value is a value it will be the
   new value, else if it is a function it will be applied on the existing value to produce the new value."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion "bo" :damage-taken inc)
                    (get-minion "bo")
                    (:damage-taken))
                1)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion "bo" :can-attack false)
                    (get-minion "bo")
                    (:can-attack))
                false)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion "bo" :damage-taken inc)
                    (get-minion "bo")
                    (:damage-taken))
                1)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion "bo" :damage-taken 2)
                    (get-minion "bo")
                    (:damage-taken))
                2)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion "bo" :buffs :attack inc)
                    (get-minion "bo")
                    (:buffs))
                {:attack 1, :health 0, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion "bo" :buffs :health 1)
                    (get-minion "bo")
                    (:buffs))
                {:attack 0, :health 1, :tmp-attack 0, :tmp-health 0 :aura-attack 0})
           (is= (-> (create-game [{:minions [(create-minion "Unstable Ghoul" :id "ug" :attacks-performed-this-turn 1
                                                            :active-abilities [:taunt :deathrattle])]}])
                    (update-minion "ug" :active-abilities [:frozen])
                    (get-minion "ug")
                    (:active-abilities))
                [:frozen])
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (update-minion "bo" :buffs :testing 1)
                    (get-minion "bo")
                    (:buffs))
                {:attack 0, :health 0, :tmp-attack 0, :tmp-health 0, :testing 1 :aura-attack 0})
           )}
  ([state id key function-or-value]
   (let [minion (get-minion state id)]
     (replace-minion state (if (fn? function-or-value)
                             (update minion key function-or-value)
                             (assoc minion key function-or-value)))))
  ([state id buffs key function-or-value]
   (let [minion (get-minion state id)]
     (replace-minion state (if (fn? function-or-value)
                             (update-in minion [:buffs key] function-or-value)
                             (assoc-in minion [:buffs key] function-or-value))
                     ))))



(defn remove-minion
  "Removes a minion with the given id from the state."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (remove-minion "bo")
                    (get-minions))
                []))}
  [state id]
  (let [owner-id (:owner-id (get-minion state id))
        position (:position (get-minion state id))]
    (-> (update-in state
                   [:players owner-id :minions]
                   (fn [minions]
                     (remove (fn [m] (= (:id m) id)) minions)))
        (update-in
          [:players owner-id :minions]
          (fn [minions]
            (->> minions
                 (mapv (fn [m] (if (< (:position m) position) m (update m :position dec)))))
            )))))

(defn remove-minions
  "Removes the minions with the given ids from the state."
  {:test (fn []
           (is= (as-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo1")
                                               (create-minion "Boulderfist Ogre" :id "bo2")]}
                                    {:minions [(create-minion "Boulderfist Ogre" :id "bo3")
                                               (create-minion "Boulderfist Ogre" :id "bo4")]}]) $
                      (remove-minions $ "bo1" "bo4")
                      (get-minions $)
                      (map :id $))
                ["bo2" "bo3"]))}
  [state & ids]
  (reduce remove-minion state ids))





