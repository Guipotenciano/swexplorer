(ns swexplorer.views
  (:require [re-frame.core :as re-frame]
            [cljs.pprint :refer [pprint] :as pprint]
            [swexplorer.subs :as subs]
            [swexplorer.events :as events]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :refer [get-mui-theme color]]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-react-material-ui.icons :as ic]
            [reagent.core :as r]
            [ajax.core :as ajax]
            [ajax.util :as ajax-util]))

(defn scrollTop []
  (set! js/document.documentElement.scrollTop 0)
  (set! js/document.body.scrollTop 0))

(defn json->clj "Convert the ajax response in clj" [json-string]
  (js->clj (.parse js/JSON json-string) :keywordize-keys true))

(defn handler-execute-query [response]
  (let [resp (json->clj response)
        head (:head resp)
        results (:results resp)]
    (re-frame/dispatch [::events/query-result {:head head :results results}])
    (re-frame/dispatch [::events/loading false]) ))

(defn execute-query [query]
  (scrollTop)
  (re-frame/dispatch [::events/loading true])
  (let [endpoint (re-frame/subscribe [::subs/endpoint])]
    (ajax/GET (str @endpoint) {
    :format {
      :write (ajax-util/to-utf8-writer identity)
      :content-type "application/rdf+json; charset=utf-8"
      :Access-Control-Allow-Origin "*"}
    :response-format :detect
    :params {
      :format "json"
      :query query}
    :handler handler-execute-query
    :error-handler #(js/console.log %)})) )


(defn create-query [query]
  ;;endpoints are used to make federated queries
  (let [prefixes (clojure.string/join " "
                    (map (fn [itm] (str "PREFIX " (get itm 0) ":<" (get itm 1) ">")) @(re-frame/subscribe [::subs/prefixes])))
        triple (str "<" (:subject query) "> ?predicate ?object .")
        services (clojure.string/join " "
                        (map (fn [itm] (str " UNION { SERVICE <" itm "> { " triple "} }")) (subvec @(re-frame/subscribe [::subs/endpoints]) 1))) ]
    ;;TODO - add suport to federated queries
    (execute-query
      (str prefixes " SELECT DISTINCT * WHERE { {" triple "} " services " } LIMIT 1000" )) ))

(defn add-history [query]
  (re-frame/dispatch [::events/add-history query])
  (re-frame/dispatch [::events/ipt-search (:subject query)])
  (create-query query))

(defn next-history []
  (let [history (re-frame/subscribe [::subs/history])
        new-index (inc (:index @history))]
    (if (< new-index (count (:queries @history)))
        (do
          (re-frame/dispatch [::events/history-index new-index])
          (re-frame/dispatch [::events/ipt-search (:subject (get (:queries @history) new-index))])
          (create-query (get (:queries @history) new-index)) ))))

(defn back-history []
  (let [history (re-frame/subscribe [::subs/history])
        new-index (dec (:index @history))]
    (if (>= new-index 0)
        (do
          (re-frame/dispatch [::events/history-index new-index])
          (re-frame/dispatch [::events/ipt-search (:subject (get (:queries @history) new-index))])
          (create-query (get (:queries @history) new-index)) ))))

(defn home-history []
  (let [history (re-frame/subscribe [::subs/history])]
    (re-frame/dispatch [::events/history-index 0])
    (re-frame/dispatch [::events/ipt-search (:subject (get (:queries @history) 0))])
    (create-query (get (:queries @history) 0)) ))

(defn wrapper-value [cell first class]
  (let [value (:value cell)
        click (fn [] (add-history {:subject value :predicate nil :object nil}) )]
    [:div {:class (str "col-lg-6 line " class (if first " first"))}
      (case (:type cell)
          "uri"     (if-not
                      (nil? (re-find #"\.(gif|jpg|jpeg|tiff|png)$" value))
                      [:img {
                        :src value
                        :alt value }]
                      [:span {
                        :class "link"
                        :on-Click click}
                        value] )
          #_()     value )] ))

(defn show-properties []
  (let [result (re-frame/subscribe [::subs/query-result])]
      [:div {
        :id    "properties-table"}
        [:div {
          :class "head container-fluid"}
          [:div {:class "row"}
            (map-indexed (fn [idx itm] [:div {:class "col-lg-6" :key (str idx)} (clojure.string/upper-case itm)] ) (:vars (:head @result)))] ]
        [:div {
          :class "body container-fluid"}
          (let [last-predicate (atom "")]
            (map-indexed
              (fn [idx itm]
                (let [predicate (:predicate itm)
                      object (:object itm)
                      same-pred (= (:value predicate) @last-predicate)]
                  [:div {:class "row" :key (str idx)}
                    (if-not same-pred
                      (do
                        (reset! last-predicate (:value predicate))
                        [wrapper-value predicate (if same-pred false true) "predicate"])
                      (do
                        [wrapper-value {:value ""} (if same-pred false true) "predicate"]) )

                    [wrapper-value object (if same-pred false true) "object"] ]

                )) (:bindings (:results @result))))] ]))

(defn toolbar []
  (let [ipt-search (re-frame/subscribe [::subs/ipt-search])]
    (fn []
      [:div {:id "toolbar"}
       [ui/toolbar
        [ui/toolbar-group
          [ui/icon-button {
            :on-click #(do (back-history) )} (ic/navigation-arrow-back {:color (color :blue500)})]
          [ui/icon-button {
            :on-click #(do (next-history) )} (ic/navigation-arrow-forward {:color (color :blue500)})]
          [ui/icon-button {
            :on-click #(do (home-history) )} (ic/action-home {:color (color :blue500)})]
          [ui/text-field {
             :id "ipt-search"
             :value (if (nil? @ipt-search) "" @ipt-search )
             :hintText (if (nil? @ipt-search) "" @ipt-search )
             :on-change #(re-frame/dispatch [::events/ipt-search (-> % .-target .-value)])
             :style {
                :width "400px"
             }}]
          [ui/icon-button {
           :on-click #(do (add-history {:subject @ipt-search :predicate nil :object nil}) )} (ic/content-send {:color (color :green500)})] ]]]
      )))

(defn loading []
  (let [loading-display (re-frame/subscribe [::subs/loading])]
    [:div {
      :style {
        :display "block" #_(if @loading-display "block" "none")
        :width "100%"
        :height "100%"
        :background-color "rgba(0,0,0,0.3)"
        :position "absolute"
        :top "0px"
        :left "0px"
        :padding-top "calc(50% - 25px)"
        :padding-left "calc(50% - 25px)"
      }}
      [:div {
        :style {

        }}
        [ui/refresh-indicator {
          :size 50
          :status "loading"
          :top 0
          :left 0
          :style {
            :position "relative"}}]]
      ]
    #_[ui/refresh-indicator {
      :size 50
      :status "loading"
      :top 50
      :left 50
      :style {
        :display (if @loading-display "block" "none")
        :top "calc(50% - 25px)"
        :left "calc(50% - 25px)"}}] ))

(defn main-panel []
  (let [query {:subject (-> @(re-frame/subscribe [::subs/history]) (:queries) (get 0) (:subject)) :predicate nil :object nil}]
    (create-query query)
    [ui/mui-theme-provider {
      :mui-theme (get-mui-theme {
        :palette {
          :text-color (color :green600)}})}
      [:div
        #_[loading]
        ;;Toolbar
        [toolbar]
        ;;Table
        [show-properties] ]]))
