(ns firestone.core-api
  (:require [ysera.test :refer [is= error?]]
            [ysera.error :refer [error]]
            [firestone.construct :refer [get-deck
                                         create-game
                                         create-minion
                                         create-card
                                         create-hero
                                         get-player-id-in-turn
                                         get-players
                                         add-card-to-deck
                                         add-cards-to-deck
                                         add-card-to-hand
                                         add-cards-to-hand
                                         get-minion]]
            [firestone.definitions :refer [get-definition]]
            [firestone.core :refer [activate-spell
                                    burn-card
                                    check-for-ability-in-minion
                                    game-over?
                                    get-abilities-from-minion
                                    get-card-from-hand
                                    get-damage-taken
                                    get-entity-type
                                    get-hero-damage-taken-by-player-id
                                    get-hero-id-by-player-id
                                    get-hero-power-used-by-hero-id
                                    get-hero-value-by-hero-id
                                    get-hero-value-by-player-id
                                    get-minion-value
                                    get-number-of-minions
                                    get-player-fatigue-counter
                                    get-player-id-by-character-id
                                    get-player-mana
                                    get-player-mana-capacity
                                    get-player-minion-ids
                                    get-player-sleepy-minions
                                    get-player-value
                                    get-top-card-from-deck
                                    get-top-card-from-hand
                                    has-battlecry?
                                    is-card-a-minion?
                                    minion-attack-character
                                    minion-attack-character
                                    move-top-card-from-deck-to-hand
                                    move-card-from-hand-to-board
                                    player-take-card-from-deck
                                    remove-top-card-in-deck
                                    remove-top-card-in-hand
                                    run-battlecry-function
                                    run-end-turn-events
                                    should-get-card?
                                    should-update-mana-capacity?
                                    should-update-mana?
                                    sleep-minion
                                    update-hero-damage-taken-by-player-id
                                    update-hero-power-used-by-hero-id
                                    update-hero-value-by-player-id
                                    update-mana-after-played-card
                                    update-minions-can-attack-by-minion-definition
                                    update-player-fatigue-counter
                                    update-player-mana
                                    update-player-mana-capacity
                                    update-player-value
                                    update-state-after-attack
                                    valid-attack?
                                    wake-up-player-sleepy-minions]]))



(defn- update-mana-end-turn
  "update mana to max capacity"
  {:test (fn []
           (is= (-> (create-game)
                    (update-mana-end-turn "p2")
                    (get-player-mana "p2"))
                2)
           (is= (-> (create-game)
                    (update-mana-end-turn "p2")
                    (get-player-mana-capacity "p2"))
                2)
           (is= (-> (create-game)
                    (update-player-mana-capacity "p2" 10)
                    (update-player-mana "p2" 5)
                    (update-mana-end-turn "p2")
                    (get-player-mana "p2"))
                10))}
  [state next-player]
  (if (should-update-mana-capacity? state next-player)
    (-> (update-player-mana-capacity state next-player inc)
        (update-player-value next-player :mana get-player-value :mana-capacity))
    (update-player-value state next-player :mana (get-player-value state next-player :mana-capacity))))



(defn attack
  "attacks minion or hero"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (attack "ib", "bo")
                    (get-damage-taken "bo"))
                4)
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (attack "ib", "bo")
                    (get-damage-taken "ib"))
                6)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")]}])
                    (attack "bo", "h1")
                    (get-damage-taken "h1"))
                6)
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo" :attacks-performed-this-turn 1)]}])
                    (attack "bo", "h1"))
                "Not valid attack")
           ;Should not be a valid attack as Unstable Ghoul has Taunt.
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Boulderfist Ogre" :id "bo1")]}
                                  {:minions [(create-minion "Unstable Ghoul" :id "ug")
                                             (create-minion "Boulderfist Ogre" :id "bo2")]}])
                    (attack "bo1", "bo2")
                    )
                "Not valid attack")
           ;Should return nil as Unstable Ghoul is dead and removed from the board.
           (is= (-> (create-game [{:minions [(create-minion "Boulderfist Ogre" :id "bo")
                                             (create-minion "Boulderfist Ogre" :id "bo1")]}
                                  {:minions [(create-minion "Unstable Ghoul" :id "ug")
                                             (create-minion "Boulderfist Ogre" :id "bo2")]}])
                    (attack "bo1", "ug")
                    (get-minion "ug")
                    )
                nil))}

  [state attacker-id target-id]
  (let [player-id (get-player-id-by-character-id state attacker-id)
        target-is-hero (get-hero-value-by-hero-id state target-id :entity-type)]
    (if (valid-attack? state player-id attacker-id target-id)
      (if target-is-hero
        (-> (minion-attack-character state attacker-id target-id)
            (update-state-after-attack target-id)
            (update-state-after-attack attacker-id))
        (-> (minion-attack-character state attacker-id target-id)
            (minion-attack-character target-id attacker-id)
            (update-state-after-attack target-id)
            (update-state-after-attack attacker-id)
            )) "Not valid attack")))

(defn end-turn
  {:test (fn []
           (is= (-> (create-game)
                    (end-turn "p1")
                    (get-player-id-in-turn))
                "p2")
           (is= (-> (create-game)
                    (end-turn "p1")
                    (end-turn "p2")
                    (end-turn "p1")
                    (get-player-id-in-turn))
                "p2")
           (is= (-> (create-game)
                    (end-turn "p1")
                    (end-turn "p2")
                    (get-player-id-in-turn))
                "p1")
           (is= (-> (create-game)
                    (end-turn "p1")
                    (end-turn "p2")
                    (get-player-value "p1" :mana))
                2)
           (is= (-> (create-game)
                    (update-player-value "p2" :mana-capacity 10)
                    (update-player-value "p2" :mana get-player-value :mana-capacity)
                    (end-turn "p1")
                    (get-player-value "p2" :mana))
                10)
           (is= (-> (create-game)
                    (end-turn "p1")
                    (get-hero-value-by-player-id "p2" :damage-taken))
                1)
           (is= (-> (create-game)
                    (add-card-to-deck "p2" "Boulderfist Ogre")
                    (end-turn "p1")
                    (get-top-card-from-hand "p2")
                    (get :name))
                "Boulderfist Ogre")
           (is= (-> (create-game)
                    (add-card-to-deck "p2" "Boulderfist Ogre")
                    (end-turn "p1")
                    (get-top-card-from-deck "p2"))
                nil)
           ;check that hero-power-used is set to zero after end turn
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :hero-power-used 100)}] :player-id-in-turn "p2")
                    (end-turn "p2")
                    (get-hero-power-used-by-hero-id "h1"))
                false)
           (is= (-> (create-game)
                    (add-card-to-hand "p2" "Boulderfist Ogre")
                    (move-card-from-hand-to-board "p2" "c1" 0)
                    (sleep-minion "c1")
                    (end-turn "p1")
                    (get-player-sleepy-minions "p2"))
                [])
           (is= (-> (create-game [{:minions [(create-minion "Ragnaros the Firelord" :id "rtf")]}
                                  {:minions [(create-minion "Boulderfist Ogre" :id "bo")]}]
                                 :seed 2)
                    (add-card-to-deck "p2" "Boulderfist Ogre")
                    (end-turn "p1")
                    (get-hero-damage-taken-by-player-id "p2"))
                8)
           ;game is over
           (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :id "h1", :damage-taken 100)}] :player-id-in-turn "p2")
                    (end-turn "p2"))
                "game has ended")
           (error? (-> (create-game)
                       (end-turn "p2"))))}
  [state player-id]
  (when-not (= (get-player-id-in-turn state) player-id)
    (error "The player with id " player-id " is not in turn."))
  (let [player-change-fn {"p1" "p2"
                          "p2" "p1"}
        next-player (get player-change-fn (get-player-id-in-turn state))
        next-player-hero-id (get-hero-id-by-player-id state next-player)]
    (as-> (run-end-turn-events state player-id) $
          (update-mana-end-turn $ next-player)
          (player-take-card-from-deck $ next-player)
          (wake-up-player-sleepy-minions $ next-player)
          (update-hero-power-used-by-hero-id $ next-player-hero-id false)
          (update $ :player-id-in-turn player-change-fn)
          (update-minions-can-attack-by-minion-definition $ (get-player-minion-ids $ next-player))
          (game-over? $ next-player-hero-id)
          )))


(defn activate-battlecry
  "if he doesn't have battlecry set valid-targets to empty list"
  {:test (fn []
           (is= (-> (create-game [{:minions ["Flame Imp"]}])
                    (activate-battlecry "p1" "m1" nil)
                    (get-hero-damage-taken-by-player-id "p1")
                    )
                3))
   }
  [state player-id card-id target-id]
  (if (has-battlecry? state (get-minion-value state card-id :name))
    (run-battlecry-function state card-id target-id)
    state
    ))

(defn should-sleep?
  "check if a minion should sleep when put on board"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Militia Commander" :id "ml")]}])
                    (should-sleep? "ml"))
                false)
           (is= (-> (create-game [{:minions [(create-minion "Injured Blademaster" :id "ib")]}])
                    (should-sleep? "ml"))
                true))}
  [state minion-id]
  (cond
    (= (check-for-ability-in-minion state minion-id :rush) true)
    false

    :else true))


(defn play-minion-card
  [state player-id card-id board-position target-id]
  (if (< (get-number-of-minions state player-id) 7)
    (as-> (move-card-from-hand-to-board state player-id card-id board-position) $
          (if (should-sleep? $ card-id)
            (sleep-minion $ card-id)
            $)
          (activate-battlecry $ player-id card-id target-id)
          )
    state))

(defn play-spell-card
  [state player-id card-id target-id]
  (if (= nil target-id)
    (activate-spell state card-id)
    (activate-spell state card-id target-id)))



(defn play-card-from-hand
  {:test (fn []
           (is= (-> (create-game [{:deck ["Injured Blademaster"]
                                   :hand ["Flame Imp"]}
                                  {:hero "Anduin Wrynn"}])
                    (play-card-from-hand "c2" "p1" 0 0)
                    (get-minion-value "c2" :name)
                    )
                "Flame Imp")
           (is= (-> (create-game [{:deck ["Injured Blademaster"]
                                   :hand ["Flame Imp"]}
                                  {:hero "Anduin Wrynn"}])
                    (play-card-from-hand "c2" "p1" 0 0)
                    (get-hero-damage-taken-by-player-id "p1"))
                3))}
  [state card-id player-id board-position target-id]
  (as-> (update-mana-after-played-card state player-id card-id) $
        (if (is-card-a-minion? (get-card-from-hand $ player-id card-id))
          (play-minion-card $ player-id card-id board-position target-id)
          (play-spell-card $ player-id card-id target-id))))