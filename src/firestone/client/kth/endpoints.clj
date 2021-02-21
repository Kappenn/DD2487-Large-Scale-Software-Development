(ns firestone.client.kth.endpoints
  (:require [clojure.pprint :refer [pprint]]
            [clojure.data.json :refer [read-str write-str]]
            [firestone.client.kth.api :refer [attack!
                                              create-game!
                                              end-turn!
                                              play-minion-card!
                                              play-spell-card!
                                              use-hero-power!]]))

(defn dispatch
  [uri params]
  (cond (= uri "/createGame")
        (create-game!)

        (= uri "/endTurn")
        (end-turn! (get params "playerId"))

        (= uri "/attack")
        (let [attacker-id (get params "attackerId")
              target-id (get params "targetId")]
          (attack! attacker-id target-id))

        (= uri "/playMinionCard")
        (let [card-id (get params "cardId")
              player-id (get params "playerId")
              position (get params "position")
              target-id (get params "targetId")]

          (play-minion-card! card-id player-id position target-id))

        (= uri "/useHeroPower")
        (let [player-id (get params "playerId")
              target-id (get params "targetId")]
          (use-hero-power! player-id target-id))

        (= uri "/playSpellCard")
        (let [card-id (get params "cardId")
              player-id (get params "playerId")
              target-id (get params "targetId")]
          (play-spell-card! card-id player-id target-id))


        :else
        (do (println "Nothing here at:" uri)
            "")
        ))


(defn handle-request! [request]
  (let [uri (:uri request)
        params (read-str (slurp (:body request)))
        result (dispatch uri params)]
    {:status  200
     :headers {"Access-Control-Allow-Origin" "https://www.conjoin-it.se"}
     :body    (write-str result)}))

