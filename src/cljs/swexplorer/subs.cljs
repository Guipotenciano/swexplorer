(ns swexplorer.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
  ::endpoint
  (fn [db]
    (first (:endpoints db))))

(re-frame/reg-sub
  ::endpoints
  (fn [db]
    (:endpoints db)))

(re-frame/reg-sub
 ::loading
 (fn [db]
   (:display (:loading (:interface db)))))

(re-frame/reg-sub
  ::prefixes
  (fn [db]
    (:prefixes db)))

(re-frame/reg-sub
  ::ipt-search
  (fn [db]
    (-> db (:interface) (:toolbar) (:ipt-search))))

(re-frame/reg-sub
  ::history
  (fn [db]
    (:history db)))

(re-frame/reg-sub
  ::query
  (fn [db]
    (get (:queries (:history db)) (:index (:history db)))))

(re-frame/reg-sub
  ::query-result
  (fn [db]
    (:query-result db)))
