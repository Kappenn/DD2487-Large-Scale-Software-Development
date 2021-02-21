(ns firestone.client.kth.adapter
  (:require [ysera.test :refer [is is= is-not]]
            [clojure.spec.alpha :as spec]
            [firestone.construct :refer [create-card
                                         create-game
                                         create-minion
                                         get-deck
                                         get-minion
                                         get-minions
                                         get-players]]
            [firestone.definitions :refer [get-definition]]
            [firestone.core :refer [can-use-hero-power?
                                    get-active-abilities-on-minion-as-strings
                                    get-card-from-hand
                                    get-health
                                    get-hero-power-used-by-hero-id
                                    get-hero-power-valid-targets
                                    get-hero-value-by-hero-id
                                    get-minion-total-attack
                                    get-minion-total-health
                                    get-minion-valid-targets
                                    get-player-id-by-character-id
                                    get-player-mana
                                    get-player-mana-capacity
                                    get-player-value
                                    get-valid-targets
                                    is-card-playble?
                                    sleepy?
                                    valid-attack?]]
            [firestone.client.kth.spec]))

(defn to-client-hero-power
  {:test (fn []
           (let [client-player (let [state (create-game)]
                                 (->> (get-players state)
                                      (first)
                                      (:hero)
                                      (to-client-hero-power state)))]
             (is (or (spec/valid? :firestone.client.kth.spec/hero-power client-player)
                     (spec/explain :firestone.client.kth.spec/hero-power client-player)))))}
  [state hero]
  (let [

        hero-id (:id hero)
        player-id (get-player-id-by-character-id state hero-id)
        definition (get-definition (:hero-power hero))
        hero-power-name (:name definition)
        ]

    {:can-use            (can-use-hero-power? state hero-id)
     :owner-id           player-id
     :entity-type        "hero-power"
     :has-used-your-turn (get-hero-power-used-by-hero-id state hero-id)
     :name               hero-power-name
     :description        (:description definition)
     :mana-cost          2
     :original-mana-cost 2
     :valid-target-ids   (get-hero-power-valid-targets state hero-id)}))

(defn to-client-hero
  {:test (fn []
           (let [client-player (let [state (create-game)]
                                 (->> (get-players state)
                                      (first)
                                      (:hero)
                                      (to-client-hero state)))]
             (is (or (spec/valid? :firestone.client.kth.spec/hero client-player)
                     (spec/explain :firestone.client.kth.spec/hero client-player)))))}
  [state hero]
  (let [player-id (get-player-id-by-character-id state (:id hero))
        definition (get-definition (:name hero))]
    {:armor            0
     :owner-id         player-id
     :entity-type      "hero"
     :attack           0
     :can-attack       false
     :health           (get-health state (:id hero))
     :max-health       (:health definition)
     :id               (:id hero)
     :mana             (get-player-mana state player-id)
     :max-mana         (get-player-mana-capacity state player-id)
     :name             (:name hero)
     :states           []
     :valid-attack-ids #{}
     :hero-power       (to-client-hero-power state (get-player-value state player-id :hero))}))




(defn to-client-minion
  {:test (fn []
           (let [client-minion (let [state (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" )]}])]
                                 (->> (get-minion state "bo")
                                      (to-client-minion state)))]
             (is (or (spec/valid? :firestone.client.kth.spec/minion client-minion)
                     (spec/explain :firestone.client.kth.spec/minion client-minion)))))}
  [state minion]

  (let [minion-definition (get-definition (:name minion))
        owner-id (get-player-id-by-character-id state (:id minion))]

    {:attack           (get-minion-total-attack state (:id minion))
     :can-attack       (valid-attack? state owner-id (:id minion))
     :entity-type      (name (:entity-type minion))
     :health           (get-health state (:id minion))
     :id               (:id minion)
     :name             (:name minion)
     :mana-cost        (:mana-cost minion-definition)
     :max-health       (get-minion-total-health state (:id minion))
     :original-attack  (:attack minion-definition)
     :original-health  (:health minion-definition)
     :owner-id         (:owner-id minion)
     :description      (str (:description minion-definition))
     :position         (:position minion)
     :set              (name (:set minion-definition))
     :sleepy           (sleepy? state (:id minion))
     :states           (get-active-abilities-on-minion-as-strings state (:id minion))
     :valid-attack-ids (get-valid-targets state (:id minion))}))


(defn to-client-card
  {:test (fn []
           (let [client-card (let [state (create-game [{:hand [(create-card "Boulderfist Ogre" :id "bo")]}])]
                               (->> (get-card-from-hand state "p1" "bo")
                                    (to-client-card state)))]
             (is (or (spec/valid? :firestone.client.kth.spec/card client-card)
                     (spec/explain :firestone.client.kth.spec/card client-card)))))}
  [state card]
  (let [card-name (:name card)
        card-definition (get-definition card-name)
        player-id (:owner-id card)]
    (merge {:entity-type        (name (:entity-type card))
            :name               card-name
            :mana-cost          (card-definition :mana-cost)
            :original-mana-cost (card-definition :mana-cost)
            :type               (name (card-definition :type))
            ;OPT
            :owner-id           player-id
            :id                 (:id card)
            :attack             (card-definition :attack)
            :original-attack    (card-definition :attack)
            :health             (card-definition :health)
            :original-health    (card-definition :health)
            :playable           (is-card-playble? state player-id (:id card))
            :valid-target-ids   (get-valid-targets state (:id card))}
           (when-let [rarity (card-definition :rarity)]
             {:rarity (name rarity)})
           (when-let [description (card-definition :description)]
             {:description description})
           (when-let [class (:class card-definition)]
             {:class (name class)}))))

(defn to-client-player
  {:test (fn []
           (let [client-player (let [state (create-game)]
                                 (->> (get-players state)
                                      (first)
                                      (to-client-player state)))]
             (is (or (spec/valid? :firestone.client.kth.spec/player client-player)
                     (spec/explain :firestone.client.kth.spec/player client-player))))
           )}
  [state player]
  {:board-entities (->> (:minions player)
                        (map (fn [p]
                               (to-client-minion state p))))
   :active-secrets []
   :hand           (->> (:hand player)
                        (map (fn [p]
                               (to-client-card state p))))
   :deck-size      (count (:deck player))
   :id             (:id player)
   :hero           (to-client-hero state (:hero player))})


(defn to-client-state
  {:test (fn []
           (let [client-state (->> (create-game)
                                   (to-client-state))]
             (is (or (spec/valid? :firestone.client.kth.spec/game-states client-state)
                     (spec/explain :firestone.client.kth.spec/game-states client-state))))
           )}
  [state]
  [{:id             "the-game-id"
    :player-in-turn (:player-id-in-turn state)
    :supports-undo  false
    :supports-redo  false
    :action-index   1
    :turn-index     1
    :players        (->> (get-players state)
                         (map (fn [p]
                                (to-client-player state p))))}])


