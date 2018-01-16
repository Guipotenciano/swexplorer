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
 ::loading
 (fn [db [_ result]]
   (assoc-in db [:interface :loading :display] result)))

(re-frame/reg-event-db
  ::prefixes
  (fn [db [_ result]]
    (assoc-in db [:prefixes] result)))

(re-frame/reg-event-db
  ::ipt-search
  (fn [db [_ result]]
    (assoc-in db [:interface :toolbar :ipt-search] result)))

(re-frame/reg-event-db
  ::history-index
  (fn [db [_ result]]
      (assoc-in db [:history :index] result)))

(re-frame/reg-event-db
  ::add-history
  (fn [db [_ result]]
    (assoc-in db [:history] {:index (-> db (:history) (:index) (inc)) :queries (merge (-> db (:history) (:queries)) result)})))

(re-frame/reg-event-db
  ::clear-history
  (fn [db [_ result]]
      (assoc-in db [:history ] result)))

(re-frame/reg-event-db
  ::query-result
  (fn [db [_ result]]
    (assoc-in db [:query-result] result)))
