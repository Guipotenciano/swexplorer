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
  ::prefixes
  (fn [db]
    (:prefixes db)))

(re-frame/reg-sub
  ::web-component
  (fn [db [_ type]]
    (let [fil (filter (fn [itm] (= type (:type itm))) (:web-components db))]
      (if-not (empty? fil) (nth fil 0) nil ))))

(re-frame/reg-sub
  ::web-components
  (fn [db]
    (:web-components db)))

(re-frame/reg-sub
  ::ipt-search
  (fn [db]
    (-> db (:interface) (:toolbar) (:ipt-search))))

(re-frame/reg-sub
  ::default-uri
  (fn [db]
    (:default-uri db)))

(re-frame/reg-sub
  ::query
  (fn [db]
    (:query db)))

(re-frame/reg-sub
  ::query-result
  (fn [db]
    (:query-result db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))
