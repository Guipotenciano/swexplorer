(ns swexplorer.mdcr
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

;This is a test 
(defn wrapper
  [name comp did-mount will-mount]
  (reagent/create-class {
    :component-did-mount  did-mount
    :component-will-mount will-mount
    :display-name  name
    :reagent-render (fn [name comp] comp) }))
