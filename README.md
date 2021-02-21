# README

It explains the architecture and design principles that this team has chosen and serves as a guide for teams on-boarding this project.  


# Namespace structure:
We've mostly followed the intended structure for the namespaces, with some modification to some files.

* Construct: creates the states for the game and the support needed for it, eg. create-game, add-card-to. Not a lot of work has been done to this file.

* core-api: handles all the actions the player can interact with, eg. end-turn, attack.

* core: the main body of the program, mostly support function. However we've decided to work mostly in core, so most of the added code is here, including some that might fit better in construct.


# Dependencies:

The main flow of the program is: definitions-loader -> card -> hero -> Hero_power -> core-api -> core -> construct -> definitions -> ysera

* definition-loader: card, hero, hero_power
* card: core-api, definitions
* hero: definitions
* hero_power: definitions
* core-api: definitions, core, construct, ysera
* core: construct, definitions, ysera
* construct depends on definition, ysera
* definitions: ysera
 

# State structure:

## state structure
A map {} containing:

* :player-id-in-turn - string with current turn player-id
* :players - map with maps of the structure of the players, this is were hero, minions, deck etc are located.
* :counter - an interger used for creating uniq ids for cards and minions, increments each round.
* :minion-ids-summoned-this-turn - contains a vector of the ids of the minions summoned this turn, used for sleep etc.
* :seed - an integer with a seed used for function which need to use randomness

## player structure
A map {} containing:

* p1 - containing a map with hero 
* :mana-capacity - integer with current maximal mana capacity
* :deck - a vector with the cards in the deck
* :mana - integer with current mana
* :hand - a vector with the current cards in hand
* :id - string with player-id
* :graveyard - a vector with a players graveyard, all the players minion who have died end up here.
* :fatigue - counter an integer that counts how much fatigue damage the player recived last time
* :minions - a vector with the players minions (minions are when cards are on the board)

## hero structure
A map {} containing:

* :name - a string with the heroes name
* :entity-type - a key which define the entity, :hero in this case
* :damage-taken - an integer which indicates how much damage the hero has taken
* :hero-power-used - boolean, true if the hero power has been used this turn
* :class - key which indicated which class the hero has
* :hero-power - value of which hero power the hero has
* :id - the id of the hero

## card structure
A map {} containing:
{:name Injured Blademaster, :entity-type :card, :id ib, :owner-id p1}

* :name - name of the card, the name is the one found in card.cljc
* :entity-type - the entity type of the map in this case :card
* :id - a uniq id to each card
* :owner-id - the id of the owner of the card

## minion structure
A map {} containing:

* :buffs - a map containing:
    * :attack - amount of attack to add to ordinary attack
    * :health - amount of health to add to ordinary health
    * :tmp-attack - amount of attack to add to ordinary attack for one round
    * :tmp-health - amount of health to add to ordinary health for one round
    * :aura-attack - amount of attack given to neighbours
* :can-attack - boolean to track if the minion has attacked this turn
* :entity-type - indicating which entity the map is
* :name - name of the minion
* :added-to-board-time-id - time of the placement of the minion
* :abilities - if the minion has any abilities, such as taunt, end turn event etc, it will be in this list
* :attacks-performed-this-turn - how many attacks a minion has performed this turn
* :id - uniq id to the minion 
* :owner-id - indicates which player owns the minion
* :active-abilities - if there are any active abilities on the minion, such as lifesteal

If an ability is active, it simply exist in the list abilities or active-abilities.
Example: Ancestral spirit adds deathrattle to a minion, hence we add deathrattle to the active-abilities list.

If a minion has an ability which is a function, the function will be put into the card definitions and a function in core will get it depending on which ability it is.

# Name convention
* ? - a function ending in "?" returns true/false
* get - functions which start with get, usally get a map {} from the state e.g get-card-by-id returns a card map based on the id
* check - functions which starts with check looks for a specific value or condition, it returns true/false in some cases
* update - functions which start with update return a updated state 
* add - functions which start with add returns a new state where something has been added, for example a card to the hand.
* remove - remove something from the state, for example remove a card from the deck
* move - functions which start with move usally indicates that something has been moved, for example a card from the deck to the hand. Our program does not physically move cards, but rather creates a copy of a card and adds it then remove the old one. Usally utilize add and remove functions together.
    * The exception to this rule is move-minion-from-board-to-graveyard it **moves** the minion to the graveyard

# Design decisions
The end-turn function in core-api is where a lot of changes happen. Here the current player press end turn and gives the next player mana, card and wake up the next players minions.

All the tests are in core-api and core.

