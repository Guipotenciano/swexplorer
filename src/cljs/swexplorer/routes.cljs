(ns swexplorer.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require [secretary.core :as secretary]
            [goog.events :as gevents]
            [goog.history.EventType :as EventType]
            [re-frame.core :as re-frame]
            [swexplorer.events :as events]
            ))

(defn hook-browser-navigation! []
  (doto (History.)
    (gevents/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" [query-params]
    (re-frame/dispatch
      [::events/query {
        :entity     (:entity query-params)
        :subject    (:subject query-params)
        :predicate  (:predicate query-params)
        :object     (:object query-params)}])
    (re-frame/dispatch [::events/set-active-panel :home-panel]))

  (defroute "/about" [query-params]
    (re-frame/dispatch [::events/set-active-panel :about-panel]))


  ;; --------------------
  (hook-browser-navigation!))
