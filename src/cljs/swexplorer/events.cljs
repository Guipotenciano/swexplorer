(ns swexplorer.events
  (:require [re-frame.core :as re-frame]
            [swexplorer.db :as db]))

(re-frame/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::add-endpoint
 (fn [db [_ result]]
   (assoc-in db [:endpoints] (merge (-> db (:endpoints)) result))))

(re-frame/reg-event-db
  ::prefixes
  (fn [db [_ result]]
    (assoc-in db [:prefixes] result)))

(re-frame/reg-event-db
  ::ipt-search
  (fn [db [_ result]]
    (assoc-in db [:interface :toolbar :ipt-search] result)))

(re-frame/reg-event-db
  ::query
  (fn [db [_ result]]
      (assoc-in db [:query] result)))

(re-frame/reg-event-db
  ::query-result
  (fn [db [_ result]]
    (assoc-in db [:query-result] result)))

(re-frame/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))
