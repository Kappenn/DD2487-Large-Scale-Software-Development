(ns firestone.definition.hero
  (:require [firestone.definitions :refer [add-definitions!]]))

(def hero-definitions
  {

   "Anduin Wrynn"
   {:name       "Anduin Wrynn"
    :type       :hero
    :health     30
    :class      :priest
    :hero-power "Lesser Heal"}

   "Uther Lightbringer"
   {:name       "Uther Lightbringer"
    :type       :hero
    :health     30
    :class      :paladin
    :hero-power "Reinforce"}

   })

(add-definitions! hero-definitions)