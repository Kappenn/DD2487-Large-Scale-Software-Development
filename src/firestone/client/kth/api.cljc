(ns firestone.client.kth.api
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [firestone.client.api :as api]
            [firestone.client.kth.adapter :refer [to-client-state]]
            [firestone.client.kth.spec]))

(defn check-spec
  [result]
  result)

(defn create-game!
  []
  (-> (api/create-game!)
      (to-client-state)
      (check-spec)))

(defn end-turn!
  [player-id]
  (-> (api/end-turn! player-id)
      (to-client-state)
      (check-spec)))

(defn attack!
  [attacker-id target-id]
  (-> (api/attack! attacker-id target-id)
      (to-client-state)
      (check-spec)))


(defn use-hero-power!
  [player-id target-id]
  (-> (api/use-hero-power! player-id target-id)
      (to-client-state)
      (check-spec)))

(defn play-minion-card!
  [card-id player-id position target-id]
  (-> (api/play-minion-card! card-id player-id position target-id)
      (to-client-state)
      (check-spec)))


(defn play-spell-card!
  [card-id player-id target-id]
  (-> (api/play-spell-card! card-id player-id target-id)
      (to-client-state)
      (check-spec)))





(s/fdef check-spec
        :args (s/coll-of :firestone.client.kth.spec/game-states))

(comment
  (stest/instrument 'firestone.client.kth.api/check-spec)
  )






