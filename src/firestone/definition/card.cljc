(ns firestone.definition.card
  (:require [firestone.definitions :refer [add-definitions!]]
            [firestone.construct :refer [add-card-to-deck
                                         add-minion-to-board
                                         create-card
                                         create-minion
                                         update-minion
                                         replace-minion
                                         remove-minion
                                         get-deck
                                         get-minion
                                         get-minions
                                         get-next-player-id-in-turn
                                         add-card-to-hand
                                         generate-id]]
            [ysera.random :refer [random-nth get-random-int]]
            [firestone.core :refer [add-value-by-vector-index
                                    attack-all-minions-on-board
                                    attack-random-enemy-character
                                    copy-minion-from-graveyard-to-board-randomly
                                    destroy-minion
                                    destroy-minion-with-taunt
                                    fetch-and-run-deathrattle-functions
                                    get-damage-taken
                                    get-enemy-characters-ids
                                    get-friendly-characters-damaged
                                    get-friendly-minions
                                    get-minion-from-graveyard-given-id
                                    get-minion-ids-on-board
                                    get-minion-value
                                    get-number-of-minions
                                    get-player-id-by-character-id
                                    get-seed
                                    heal-character
                                    heal-friendly-characters
                                    minion-attack-characters-by-value
                                    minion-attack-character-by-value
                                    move-top-card-from-deck-to-hand
                                    move-minion-to-graveyard-and-run-deathrattle
                                    player-take-card-from-deck
                                    remove-ability
                                    replace-minion-on-board
                                    resurrect-minion-from-graveyard
                                    run-rush-event
                                    sleep-minion
                                    update-abilities
                                    add-card-to-random-position-in-deck
                                    get-hero-id-by-player-id
                                    update-ability
                                    update-damage-taken
                                    update-hero-damage-taken-by-player-id
                                    update-minion-buff-aura-attack
                                    update-minion-buff-attack
                                    update-minion-buff-health
                                    update-seed
                                    update-state-after-attack]]))




(def ability-list-minion
  {
   "Minion Ability List"
   {:list {:aura          "aura"
           :deathrattle   ":deathrattle"
           :divine-shield ":divine-shield"
           :effect        "effect"
           :elusive       "elusive"
           :enrage        "enrage"
           :frozen        "frozen"
           :immune        "immune"
           :inspire       "inspire"
           :lifesteal     "lifesteal"
           :mega-windfury "mega-windfury"
           :poisonous     "poisonous"
           :silenced      "silenced"
           :spell-damage  "spell-damage"
           :stealth       "stealth"
           :taunt         "taunt"
           :windfury      "windfury"}}
   })

(add-definitions! ability-list-minion)

(def card-definitions
  {

   "Boulderfist Ogre"
   {:name       "Boulderfist Ogre"
    :can-attack true
    :attack     6
    :health     7
    :mana-cost  6
    :set        :basic
    :type       :minion}

   "Flame Imp"
   {:name        "Flame Imp"
    :can-attack  true
    :attack      3
    :health      2
    :mana-cost   1
    :set         :classic
    :type        :minion
    :description "Battlecry: Deal 3 damage to your Hero."
    :battlecry   (fn [state minion-id _]
                   (minion-attack-character-by-value state (get-hero-id-by-player-id state (:player-id-in-turn state )) 3))}

   "Injured Blademaster"
   {:name        "Injured Blademaster"
    :can-attack  true
    :attack      4
    :health      7
    :mana-cost   3
    :set         :classic
    :type        :minion
    :description "Battlecry: Deal 4 damage to HIMSELF."
    :battlecry   (fn [state minion-id _]
                   (update-minion state minion-id :damage-taken 4))}

   "Silver Hand Recruit"
   {:name       "Silver Hand Recruit"
    :can-attack true
    :attack     1
    :health     1
    :mana-cost  1
    :set        :classic
    :type       :minion}

   "Squirrel"
   {:name       "Squirrel"
    :can-attack true
    :attack     1
    :health     1
    :mana-cost  1
    :set        :classic
    :rarity     :common
    :type       :minion}

   ; sprint-2
   "Argent Watchman"
   {:name             "Argent Watchman"
    :can-attack       false
    :attack           2
    :health           4
    :mana-cost        2
    :type             :minion
    :set              :the-grand-tournament
    :rarity           :rare
    :description      "Can't attack. Inspire: Can attack as normal this turn."
    :abilities        [:inspire :end-turn-event]
    :inspire-function (fn [state minion-id]
                        (update-minion state minion-id :can-attack true))
    :end-turn-event   (fn [state minion-id]
                        (update-minion state minion-id :can-attack false))}

   "Boneguard Lieutenant"
   {:name             "Boneguard Lieutenant"
    :can-attack       true
    :attack           3
    :health           2
    :mana-cost        2
    :type             :minion
    :set              :the-grand-tournament
    :rarity           :common
    :description      "Inspire: Gain +1 Health."
    :abilities        [:inspire]
    :inspire-function (fn [state minion-id]
                        (update-minion state minion-id :buffs :health inc))}

   "Convert"
   {:name         "Convert"
    :can-attack   false
    :mana-cost    2
    :type         :spell
    :class        :priest
    :set          :the-grand-tournament
    :rarity       :rare
    :description  "Put a copy of an enemy minion into your hand."
    ;Target måste uppdateras pga ska bara vara ENEMY MINIONS
    :spell-effect (fn [state target]
                    (let [minion-name (get-minion-value state target :name)]
                      (add-card-to-hand state (get-in state [:player-id-in-turn]) minion-name)))}


   "Darkscale Healer"
   {:name        "Darkscale Healer"
    :can-attack  true
    :attack      4
    :health      5
    :mana-cost   5
    :type        :minion
    :set         :basic
    :description "Battlecry: Restore 2 Health to all friendly characters."
    :battlecry   (fn [state minion-id _]
                   (let [player-id (get-minion-value state minion-id :owner-id)]
                     (heal-friendly-characters state player-id 2)))}

   "Faceless Manipulator"
   {:name        "Faceless Manipulator"
    :can-attack  true
    :attack      3
    :health      3
    :mana-cost   5
    :type        :minion
    :set         :classic
    :rarity      :epic
    :description "Battlecry: Choose a minion and become a copy of it."
    :battlecry   (fn [state minion-id target-id]
                   (let [
                         minion (get-minion state minion-id)
                         target-minion (get-minion state target-id)
                         updated-minion (create-minion (:name target-minion)
                                                       :damage-taken (:damage-taken target-minion)
                                                       :attack (:attack target-minion)
                                                       :abilities (:abilities target-minion)
                                                       :buffs (:buffs target-minion)
                                                       :id minion-id
                                                       :added-to-board-time-id (:added-to-board-time-id minion))]

                     (-> (remove-minion state minion-id)
                         (add-minion-to-board (:player-id-in-turn state) updated-minion (:position minion))
                         (sleep-minion minion-id))
                     ))}

   "Fireball"
   {:name         "Fireball"
    :can-attack   true
    :mana-cost    4
    :type         :spell
    :class        :mage
    :set          :basic
    :description  "Deal 6 damage."
    :spell-effect (fn [state target-id]
                    (minion-attack-character-by-value state target-id 6))}

   "Floating Watcher"
   {
    :can-attack                         true
    :race                               :demon
    :name                               "Floating Watcher"
    :type                               :minion
    :class                              :warlock
    :mana-cost                          5
    :health                             4
    :set                                :goblins-vs-gnomes
    :rarity                             :common
    :attack                             4
    :description                        "Whenever your hero takes damage on your turn, gain +2/+2."
    :abilities                          [:hero-take-damage-on-own-turn-event]
    :hero-take-damage-on-own-turn-event (fn [state minion-id]
                                          (-> (update-minion-buff-attack state minion-id (inc 2))
                                              (update-minion-buff-health minion-id (inc 2))))}

   "Leper Gnome"
   {:name             "Leper Gnome"
    :can-attack       true
    :attack           1
    :health           1
    :mana-cost        1
    :type             :minion
    :set              :classic
    :rarity           :common
    :description      "Deathrattle: Deal 2 damage to the enemy hero."
    :active-abilities [:deathrattle]
    :deathrattle      (fn [state _]
                        (update-hero-damage-taken-by-player-id state (get-next-player-id-in-turn state) 2))}


   "Lightwarden"
   {:name        "Lightwarden"
    :can-attack  true
    :attack      1
    :health      2
    :mana-cost   1
    :type        :minion
    :set         :classic
    :rarity      :rare
    :description "Whenever a character is healed, gain +2 Attack."
    :abilities   [:heal-event]
    :heal-event  (fn [state minion-id]
                   (update-minion state minion-id :buffs :attack (inc 2)))}


   "Loot Hoarder"
   {:name             "Loot Hoarder"
    :can-attack       true
    :attack           2
    :health           1
    :mana-cost        2
    :type             :minion
    :set              :classic
    :rarity           :common
    :description      "Deathrattle: Draw a card."
    :abilities        [:deathrattle]
    :active-abilities [:deathrattle]
    :deathrattle      (fn [state _]
                        (player-take-card-from-deck state (get-in state [:player-id-in-turn])))}

   "Lowly Squire"
   {:name             "Lowly Squire"
    :can-attack       true
    :attack           1
    :health           2
    :mana-cost        1
    :type             :minion
    :set              :the-grand-tournament
    :rarity           :common
    :description      "Inspire: Gain +1 Attack."
    :abilities        [:inspire]
    :inspire-function (fn [state minion-id]
                        (update-minion state minion-id :buffs :attack inc))}

   "Militia Commander"
   {:name           "Militia Commander"
    :can-attack     true
    :attack         2
    :health         5
    :mana-cost      4
    :type           :minion
    :class          :warrior
    :set            :the-witchwood
    :rarity         :rare
    :description    "Rush. Battlecry: Gain +3 Attack this turn."
    :abilities      [:rush :end-turn-event]
    :battlecry      (fn [state minion-id _]
                      (update-minion state minion-id :buffs :tmp-attack 3))
    :end-turn-event (fn [state minion-id]
                      (update-minion state minion-id :buffs :tmp-attack 0))}



   ;increase attack and health on all friendly minion
   "Mukla's Champion"
   {:name             "Mukla's Champion"
    :can-attack       true
    :attack           4
    :health           3
    :mana-cost        5
    :type             :minion
    :set              :the-grand-tournament
    :race             :beast
    :rarity           :common
    :description      "Inspire: Give your other minions +1/+1."
    :abilities        [:inspire]
    :inspire-function (fn [state minion-id]
                        (let [player-id (:owner-id (get-minion state minion-id))
                              minions (vec (get-minions state player-id))]
                          (->> minions
                               (reduce-kv (fn [state _ minion]
                                            (if (not= (:id minion) minion-id)
                                              (-> (update-minion-buff-attack state (:id minion) inc)
                                                  (update-minion-buff-health (:id minion) inc))
                                              state))
                                          state))))}

   "Northshire Cleric"
   {:name        "Northshire Cleric"
    :can-attack  true
    :attack      1
    :health      3
    :mana-cost   1
    :type        :minion
    :class       :priest
    :set         :basic
    :description "Whenever a minion is healed, draw a card."
    :abilities   [:heal-event]
    :heal-event  (fn [state _]
                   (player-take-card-from-deck state (get-in state [:player-id-in-turn])))}

   "Ragnaros the Firelord"
   {:name           "Ragnaros the Firelord"
    :can-attack     false
    :attack         8
    :health         8
    :mana-cost      8
    :type           :minion
    :set            :hall-of-fame
    :race           :elemental
    :rarity         :legendary
    :description    "Can't attack. At the end of your turn, deal 8 damage to a random enemy."
    :abilities      [:end-turn-event]
    :end-turn-event (fn [state _]
                      (let [seed (get-seed state)
                            [new-seed random-enemy-id] (random-nth seed (get-enemy-characters-ids state))]
                        (-> (minion-attack-character-by-value state random-enemy-id 8)
                            (update-state-after-attack random-enemy-id)
                            (update-seed new-seed))))}

   "Resurrect"
   {:name         "Resurrect"
    :can-attack   false
    :mana-cost    2
    :type         :spell
    :class        :priest
    :set          :blackrock-mountain
    :rarity       :rare
    :description  "Summon a random friendly minion that died this game."
    :spell-effect (fn [state]
                    (copy-minion-from-graveyard-to-board-randomly state (get-in state [:player-id-in-turn]) (get-seed state)))}



   "Sprint"
   {:name         "Sprint"
    :can-attack   false
    :mana-cost    7
    :type         :spell
    :class        :rogue
    :set          :basic
    :description  "Draw 4 cards."
    :spell-effect (fn [state]
                    (let [list [1 2 3 4]]
                      (->> list
                           (reduce-kv (fn [state _ _]
                                        (player-take-card-from-deck state
                                                                    (get-in state [:player-id-in-turn])))
                                      state))))}


   ;1. Om kraven uppfylls - enable target-picker
   "The Black Knight"
   {:name        "The Black Knight"
    :can-attack  false
    :attack      4
    :health      5
    :mana-cost   6
    :type        :minion
    :set         :classic
    :rarity      :legendary
    :description "Battlecry: Destroy an enemy minion with Taunt."
    :battlecry   (fn [state minion-id target-id]
                   (if (= target-id nil)
                     state
                     (destroy-minion-with-taunt state target-id)))}


   "Unstable Ghoul"
   {:name             "Unstable Ghoul"
    :can-attack       true
    :attack           1
    :health           3
    :mana-cost        2
    :type             :minion
    :set              :curse-of-naxxramas
    :rarity           :common
    :description      "Taunt. Deathrattle: Deal 1 damage to all minions."
    :deathrattle      (fn [state minion-id]
                        (let [all-minions-on-board (get-minion-ids-on-board state)
                              all-other-minions-on-board (vec (remove #(= minion-id %) all-minions-on-board))]
                          (->> all-other-minions-on-board
                               (reduce-kv (fn [state index minion]
                                            (-> (minion-attack-character-by-value state minion 1)
                                                (update-state-after-attack minion)
                                                ))
                                          state))))

    :abilities        [:taunt :deathrattle]
    :active-abilities [:taunt :deathrattle]}

   ; sprint 3

   "Entomb"
   {:name         "Entomb",
    :mana-cost    6,
    :type         :spell,
    :class        :priest,
    :set          :the-league-of-explorers,
    :rarity       :common,
    :description  "Choose an enemy minion. Shuffle it into your deck.",
    :spell-effect (fn [state target-id]
                    (let [target-name (:name (get-minion state target-id))]
                      (->(add-card-to-random-position-in-deck state (:player-id-in-turn state) target-name)
                         (remove-minion target-id))

                      ))
    },

   "Dire Wolf Alpha"
   {:description "Adjacent minions have +1 Attack.",
    :can-attack  true,
    :race        :beast,
    :name        "Dire Wolf Alpha",
    :type        :minion,
    :mana-cost   2,
    :health      2,
    :set         :classic,
    :rarity      :common,
    :attack      2
    :abilities   [:aura]
    :aura        (fn [state minion-id target-id]
                   (update-minion-buff-aura-attack state target-id inc))},


   "Consecration"
   {:name         "Consecration",
    :mana-cost    4,
    :type         :spell,
    :class        :paladin,
    :set          :basic,
    :description  "Deal 2 damage to all enemies."
    :spell-effect (fn [state]
                    (let [enemy-characters-ids (get-enemy-characters-ids state)]
                      (->> enemy-characters-ids
                           (reduce-kv (fn [state _ minion-id]
                                        (-> (minion-attack-character-by-value state minion-id 2)
                                            (update-state-after-attack minion-id)))
                                      state))))}



   "Tinkmaster Overspark"
   {:name        "Tinkmaster Overspark",
    :can-attack  true,
    :attack      3,
    :health      3,
    :mana-cost   3,
    :type        :minion,
    :set         :classic,
    :rarity      :legendary,
    :description "Battlecry: Transform another random minion into a 5/5 Devilsaur or a 1/1 Squirrel."
    :battlecry   (fn [state minion-id _]
                   (let [seed (get-seed state)
                         [new-seed random-number] (random-nth seed [0 1])
                         all-minions-on-board (get-minion-ids-on-board state)
                         [new-seed2 random-minion-id] (random-nth new-seed (vec (remove #(= minion-id %) all-minions-on-board)))]

                     (if (> (count all-minions-on-board) 1)
                     (-> (if (= random-number 0)
                           (replace-minion-on-board state random-minion-id "Devilsaur")
                           (replace-minion-on-board state random-minion-id "Squirrel"))
                         (update-seed new-seed2))
                     state)))},


   "Acolyte of Agony"
   {:description      "Lifesteal",
    :name             "Acolyte of Agony",
    :can-attack       true,
    :type             :minion,
    :mana-cost        3,
    :class            :priest,
    :health           3,
    :set              :knights-of-the-frozen-throne,
    :rarity           :common,
    :attack           3,
    :abilities        [:lifesteal],
    :active-abilities [:lifesteal]},

   "Backstab"
   {:name         "Backstab",
    :mana-cost    0,
    :type         :spell,
    :class        :rogue,
    :set          :basic,
    :description  "Deal 2 damage to an undamaged minion."
    :spell-effect (fn [state target-id]
                    (minion-attack-character-by-value state target-id 2))}

   "Ancestral Spirit"
   {:name         "Ancestral Spirit",
    :mana-cost    2,
    :type         :spell,
    :class        :shaman,
    :set          :classic,
    :rarity       :rare,
    :description  "Give a minion \"Deathrattle: Resummon this minion.\""
    :spell-effect (fn [state target-id]
                    (let [target-minion (get-minion state target-id)
                          target-minion-owner (:owner-id target-minion)
                          deathrattle-function (fn [state target-id]
                                                 (resurrect-minion-from-graveyard state target-minion-owner target-id))]

                      (-> (replace-minion state (assoc target-minion :tmp-deathrattle deathrattle-function))
                          (update-ability target-id :tmp-deathrattle))))},

   "Princess Huhuran"
   {
    :race        :beast,
    :can-attack  true,
    :name        "Princess Huhuran",
    :type        :minion,
    :mana-cost   5,
    :class       :hunter,
    :health      5,
    :set         :whispers-of-the-old-gods,
    :rarity      :legendary,
    :attack      6
    :description "Battlecry: Trigger a friendly minion's Deathrattle effect immediately.",
    :battlecry   (fn [state _ target-id]
                   (if (= target-id nil)
                     state
                     (fetch-and-run-deathrattle-functions state target-id)))},

   "Frothing Berserker"
   {:name         "Frothing Berserker"
    :can-attack   true,
    :attack       2
    :health       4
    :mana-cost    3
    :class        :warrior
    :rarity       :rare
    :type         :minion
    :set          :classic
    :description  "Whenever a minion takes damage, gain +1 Attack."
    :abilities    [:damage-event]
    :damage-event (fn [state minion-id _]
                    (update-minion state minion-id :buffs :attack inc))}

   "Archmage Antonidas"
   {:description "Whenever you cast a spell, add a 'Fireball' spell to your hand.",
    :name        "Archmage Antonidas",
    :can-attack  true,
    :type        :minion,
    :mana-cost   7,
    :class       :mage,
    :health      7,
    :set         :classic,
    :rarity      :legendary,
    :attack      5
    :abilities   [:spell-event]
    :spell-event (fn [state minion-id]
                   (let [player-id (get-player-id-by-character-id state minion-id)]
                     (add-card-to-hand state player-id "Fireball")))}

   "Houndmaster Shaw"
   {:description "Your other minions have Rush.",
    :name        "Houndmaster Shaw",
    :can-attack  true,
    :type        :minion,
    :mana-cost   4,
    :class       :hunter,
    :health      6,
    :set         :the-witchwood,
    :rarity      :legendary,
    :attack      3
    :battlecry   (fn [state minion-id _]
                   (let [player-id (get-player-id-by-character-id state minion-id)
                         friendly-minions (get-friendly-minions state player-id)]
                     (-> (update-abilities state friendly-minions :rush)
                         (remove-ability minion-id :rush)
                         (run-rush-event player-id))
                     ))}

   "Light of the Naaru"
   {:name         "Light of the Naaru",
    :mana-cost    1,
    :can-attack   true,
    :type         :spell,
    :class        :priest,
    :set          :goblins-vs-gnomes,
    :rarity       :rare,
    :description  "Restore 3 Health. If the target is still damaged summon a Lightwarden."
    :spell-effect (fn [state target-id]
                    (let [[state value] (generate-id state)
                          player-id (:player-id-in-turn state)
                          number-of-minions-on-board (get-number-of-minions state player-id)
                          generated-minion-id (str "lw" value)
                          Lightwarden (create-minion "Lightwarden" :id generated-minion-id)]
                      (as-> (heal-character state target-id 3) $
                            (if (> (get-damage-taken $ target-id) 0)
                              (if (< number-of-minions-on-board 7)
                                (-> (add-minion-to-board $ player-id Lightwarden number-of-minions-on-board)
                                    (sleep-minion generated-minion-id))
                                $)
                              $
                              )
                            )))}
   "Nerubian Ambush!"
   {:name        "Nerubian Ambush!",
    :mana-cost   0,
    :can-attack  true,
    :type        :spell,
    :set         :the-league-of-explorers,
    :description "Casts When Drawn. Summon a 4/4 Nerubian for your opponent.",
    :abilities [:draw-event],
    :draw-event (fn [state player-id]
                    (let [player-change-fn {"p1" "p2"
                                            "p2" "p1"}
                          opponent-player (get player-change-fn player-id)
                          current-player-id player-id
                          opponent-number-of-minions-on-board (get-number-of-minions state opponent-player)]
                      (if (< (get-number-of-minions state opponent-player) 7)
                        (-> (add-minion-to-board state opponent-player (create-minion "Nerubian")opponent-number-of-minions-on-board)
                            (player-take-card-from-deck current-player-id))
                        (player-take-card-from-deck state current-player-id))
                    ))},


   "Hogger, Doom of Elwynn"
   {:name         "Hogger, Doom of Elwynn",
    :can-attack   true,
    :attack       6,
    :health       6,
    :mana-cost    7,
    :type         :minion,
    :set          :whispers-of-the-old-gods,
    :rarity       :legendary,
    :description  "Whenever this minion takes damage, summon a 2/2 Gnoll with Taunt."
    :abilities    [:damage-event]
    :damage-event (fn [state minion-id target-id]
                    (let [player-id-of-hogger (get-player-id-by-character-id state minion-id)]
                      (if (and (= minion-id target-id) (< (get-number-of-minions state player-id-of-hogger) 7))
                        (add-minion-to-board state player-id-of-hogger (create-minion "Gnoll") 0)
                        state)))},

   "Devilsaur"
   {:name       "Devilsaur",
    :can-attack true,
    :attack     5,
    :health     5,
    :mana-cost  5,
    :type       :minion,
    :set        :journey-to-un'goro,
    :race       :beast,
    :rarity     :common},

   "Nat, the Darkfisher"
   {:name           "Nat, the Darkfisher",
    :can-attack     true,
    :attack         2,
    :health         4,
    :mana-cost      2,
    :type           :minion,
    :set            :whispers-of-the-old-gods,
    :rarity         :legendary,
    :description    "At the start of your opponent's turn, they have a 50% chance to draw an extra card."
    :abilities      [:end-turn-event]
    :end-turn-event (fn [state _]
                      (let [seed (get-seed state)
                            [new-seed random-number] (random-nth seed [0 1])
                            next-player-id (get-next-player-id-in-turn state)]
                        (if (= random-number 1)
                          (-> (player-take-card-from-deck state next-player-id)
                              (update-seed new-seed))
                          (update-seed state new-seed))))
    },


   "Beneath the Grounds"
   {:name         "Beneath the Grounds",
    :can-attack   true,
    :mana-cost    3,
    :type         :spell,
    :class        :rogue,
    :set          :the-grand-tournament,
    :rarity       :epic,
    :spell-effect (fn [state]
                    (let [next-player-id-in-turn (get-next-player-id-in-turn state)
                          list [1 2 3]]
                      (->> list
                           (reduce-kv (fn [state _ _]
                                        (add-card-to-random-position-in-deck state next-player-id-in-turn "Nerubian Ambush!")
                                        )
                                      state))))
    :description  "Shuffle 3 Ambushes into your opponent's deck. When drawn you summon a 4/4 Nerubian."},



   "Gnoll"
   {:name             "Gnoll",
    :can-attack       true,
    :attack           2,
    :health           2,
    :mana-cost        2,
    :type             :minion,
    :set              :classic,
    :rarity           :common,
    :abilities        [:taunt],
    :active-abilities [:taunt],
    :description      "Taunt"},

   "Rocket Boots"
   {:name         "Rocket Boots",
    :mana-cost    2,
    :type         :spell,
    :class        :warrior,
    :set          :the-boomsday-project,
    :rarity       :common,
    :description  "Give a minion Rush. Draw a card."
    :spell-effect (fn [state target-minion]
                    (let [player-id (get-player-id-by-character-id state target-minion)]
                      (-> (update-ability state target-minion :rush)
                          (run-rush-event player-id)
                          (player-take-card-from-deck (get-in state [:player-id-in-turn]))
                          )
                      ))},

   "Mistress of Pain"
   {:description      "Lifesteal",
    :can-attack       true,
    :race             :demon,
    :name             "Mistress of Pain",
    :type             :minion,
    :mana-cost        2,
    :class            :warlock,
    :health           4,
    :set              :goblins-vs-gnomes,
    :rarity           :rare,
    :attack           1,
    :abilities        [:lifesteal],
    :active-abilities [:lifesteal]},

   "Nerubian"
   {:name       "Nerubian",
    :can-attack true,
    :attack     4,
    :health     4,
    :mana-cost  4,
    :type       :minion,
    :class      :rogue,
    :set        :the-grand-tournament},

   "Ragnaros, Lightlord"
   {:can-attack     false,
    :race           :elemental,
    :name           "Ragnaros, Lightlord",
    :type           :minion,
    :mana-cost      8,
    :class          :paladin,
    :health         8,
    :set            :whispers-of-the-old-gods,
    :rarity         :legendary,
    :attack         8,
    :description    "At the end of your turn, restore 8 health to a damaged friendly character.",
    :abilities      [:end-turn-event]
    :end-turn-event (fn [state minion-id]
                      (let [seed (get-seed state)
                            player-id (:player-id-in-turn state)
                            friendly-characters-damaged (get-friendly-characters-damaged state player-id)
                            friendly-characters-damaged-self-removed (remove #(= minion-id %) friendly-characters-damaged)
                            [new-seed random-friendly-character] (random-nth seed friendly-characters-damaged-self-removed)]
                        (if (> (count random-friendly-character) 0)
                          (-> (heal-character state random-friendly-character 8)
                              (update-seed new-seed))
                          (update-seed state new-seed))))}

   })

(add-definitions! card-definitions)