(ns firestone.definition.hero-power
  (:require [firestone.definitions :refer [add-definitions!]]
            [firestone.construct :refer [generate-id
                                         get-player-id-in-turn
                                         get-hero
                                         add-minion-to-board
                                         create-minion
                                         create-card]]
            [firestone.core :refer [get-hero-power-used-by-hero-id
                                    get-number-of-minions
                                    get-player-value
                                    heal-character
                                    sleep-minion
                                    update-hero-power-used-by-hero-id
                                    run-board-change-events]]))

(def hero-definitions
  {

   "Lesser Heal"
   {:name                "Lesser Heal"
    :mana-cost           2
    :type                :hero-power
    :class               :priest
    :description         "restore 2 healh to any character"
    :hero-power-function (fn [state player-id heal-target-id]
                           (let [hero (get-player-value state player-id :hero)
                                 hero-id (:id hero)
                                 hero-power-used (get-hero-power-used-by-hero-id state hero-id)]
                             (when (false? hero-power-used)
                               (-> (heal-character state heal-target-id 2)
                                   (update-hero-power-used-by-hero-id hero-id true)))))}


   "Reinforce"
   {:name                "Reinforce"
    :mana-cost           2
    :type                :hero-power
    :class               :paladin
    :description         "summon a \"Silver Hand Recruit\" to board and put it to sleep"
    :hero-power-function (fn [state player-id _]
                           (let [[state value] (generate-id state)
                                 hero (get-player-value state player-id :hero)
                                 hero-id (:id hero)
                                 number-of-minions-on-board (get-number-of-minions state player-id)
                                 hero-power-used (get-hero-power-used-by-hero-id state hero-id)
                                 generated-minion-id (str "hp" value)
                                 silver-hand-recruit (create-minion "Silver Hand Recruit" :id generated-minion-id)]
                             (if (and (< number-of-minions-on-board 7) (false? hero-power-used))
                               (-> (add-minion-to-board state player-id silver-hand-recruit number-of-minions-on-board)
                                   (update-hero-power-used-by-hero-id hero-id true)
                                   (sleep-minion generated-minion-id)
                                   (run-board-change-events))
                               state)))}})

(add-definitions! hero-definitions)