(ns firestone.client.api
  (:require [firestone.construct :refer [create-game
                                         create-minion
                                         create-hero
                                         create-card
                                         add-minion-to-board]]

            [firestone.core-api :refer [end-turn
                                        attack
                                        play-card-from-hand
                                        play-spell-card]]
            [firestone.core :refer [create-game-with-random-deck
                                    update-player-mana-capacity
                                    update-player-mana
                                    run-hero-power-function
                                    get-player-value
                                    update-seed]]

            [firestone.definitions-loader]))

(def state-atom (atom nil))

(defn create-game!
  []
  (reset! state-atom (-> (create-game-with-random-deck)
                         (update-seed (int (rand 100))))))

(defn get-state!
  []
  (deref state-atom))

(defn end-turn!
  [player-id]
  (swap! state-atom end-turn player-id))

(defn play-minion-card!
  [card-id player-id position target-id]
  (swap! state-atom play-card-from-hand card-id player-id position target-id))

(defn play-spell-card!
  [card-id player-id target-id]
  (swap! state-atom play-card-from-hand card-id player-id nil target-id))

(defn attack!
  [attacker-id target-id]
  (swap! state-atom attack attacker-id target-id)
  )

(defn use-hero-power!
  [player-id target-id]
  (swap! state-atom run-hero-power-function player-id target-id)
  )

(comment
  (create-game!)
  )

(read-string (str {:a 1}))