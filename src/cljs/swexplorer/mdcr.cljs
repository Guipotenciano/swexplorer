(ns swexplorer.mdcr
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(defn wrapper
  [name comp]
  (reagent/create-class {
    :component-did-mount  (fn [state] (js/console.log "Mount: " name) (.autoInit js/mdc) )
    :component-will-mount #()
    :display-name  name
    :reagent-render (fn [name comp] comp) }))
