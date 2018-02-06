(ns swexplorer.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [swexplorer.events :as events]
            [swexplorer.routes :as routes]
            [swexplorer.views :as views]
            [swexplorer.subs :as subs]
            [swexplorer.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
