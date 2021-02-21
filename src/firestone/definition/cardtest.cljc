(ns firestone.definition.cardtest
  (:require [firestone.definitions :refer [add-definitions!]]
            [ysera.test :refer [deftest is is-not is= error?]]
            [ysera.error :refer [error]]
            [ysera.collections :refer [seq-contains?]]
            [firestone.core :refer []]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-card
                                         create-game
                                         create-hero
                                         create-minion
                                         get-heroes
                                         get-minion
                                         get-minions
                                         get-deck
                                         add-card-to-deck
                                         add-card-to-hand
                                         create-empty-state
                                         add-minions-to-board
                                         add-minion-to-board]]))


