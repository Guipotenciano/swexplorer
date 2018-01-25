(ns swexplorer.views
  (:require [re-frame.core :as re-frame]
            [swexplorer.mdcr :as mdcr]
            [cljs.pprint :refer [pprint] :as pprint]
            [swexplorer.subs :as subs]
            [swexplorer.events :as events]
            [reagent.core :as reagent]
            [ajax.core :as ajax]
            [ajax.util :as ajax-util]))

(defn json->clj "Convert the ajax response in clj" [json-string]
  (js->clj (.parse js/JSON json-string) :keywordize-keys true))

(defn handler-execute-query [response]
  (let [resp (json->clj response)
        head (:head resp)
        results (:results resp)]
    (re-frame/dispatch [::events/query-result {:head head :results results}]) ))

(defn execute-query [query]
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
  ;;force clean results
  (re-frame/dispatch [::events/query-result nil])
  ;;endpoints are used to make federated queries
  (let [prefixes (clojure.string/join " "
                    (map (fn [itm] (str "PREFIX " (get itm 0) ":<" (get itm 1) ">")) @(re-frame/subscribe [::subs/prefixes])))
        triple (str "<" (:subject query) "> ?predicate ?object .")
        services (clojure.string/join " "
                        (map (fn [itm] (str " UNION { SERVICE <" itm "> { " triple "} }")) (subvec @(re-frame/subscribe [::subs/endpoints]) 1))) ]
    (execute-query
      (str prefixes " SELECT DISTINCT ?predicate ?object WHERE { {" triple "} " services " } LIMIT 1000" )) ))


(defn wrapper-value [cell first class]
  (let [value (:value cell)
        type (:type cell)
        component @(re-frame/subscribe [::subs/web-component type])
        component-attrs (into (hash-map)
                              (map (fn [it]
                                      (let [key (nth it 0)
                                            val (if (keyword? (nth it 1))
                                                    ((nth it 1) cell)
                                                    (nth it 1) )]
                                        [key , (if (nil? val) "" val)] ))
                                   (:attributes component)) )]
    [:div {:class (str "mdc-layout-grid__cell mdc-layout-grid__cell--span-6 line " class (if first " first"))}
      [(if (nil? (:component component)) :div (:component component)) (if (empty? component-attrs) value component-attrs) ]] ))

(defn loading []
  [:div {
    :style {
      :display "flex"
      :width "100%"
      :height "100%"
      :background-color "rgba(0,0,0,0.3)"
      :position "fixed"
      :top "70px"
      :left "0px"
      :z-index 0
    }}
    [:div {
      :style {
        :position "relative"
        :top "calc(50% - 25px)"
        :left "calc(50% - 25px)"
      }}
      #_[ui/refresh-indicator {
        :size 50
        :status "loading"
        :top 0
        :left 0 }]] ])

(defn show-properties []
  (let [result (re-frame/subscribe [::subs/query-result])]
      (if-not (nil? @result)
              [:div {
                :id  "properties-table"}
                [:div {
                  :class "head mdc-layout-grid"}
                  [:div {:class "mdc-layout-grid__inner"}
                    (map-indexed (fn [idx itm] [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-6" :key (str idx)} (clojure.string/upper-case itm)] ) (:vars (:head @result)))] ]
                [:div {
                  :class "body mdc-layout-grid"}
                  (if (> (count (:bindings (:results @result))) 0)
                      (let [last-predicate (atom "")]
                        (map-indexed
                          (fn [idx itm]
                            (let [predicate (:predicate itm)
                                  object (:object itm)
                                  same-pred (= (:value predicate) @last-predicate)]
                              [:div {:class "mdc-layout-grid__inner" :key (str idx)}
                                (if-not same-pred
                                  (do
                                    (reset! last-predicate (:value predicate))
                                    [wrapper-value predicate (if same-pred false true) "predicate"])
                                  (do
                                    [wrapper-value {:value ""} (if same-pred false true) "predicate"]) )

                                [wrapper-value object (if same-pred false true) "object"] ]

                            )) (:bindings (:results @result))))
                      [:div {:class "mdc-layout-grid__inner"}
                        [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-12"}
                          "Nenhum resultado encontrado"]])] ]
                [loading]) ))

#_(defn show-properties []
  (let [result (re-frame/subscribe [::subs/query-result])]
      (if-not (nil? @result)
              [:div {
                :id  "properties-table"}
                [:div {
                  :class "head container-fluid"}
                  [:div {:class "row"}
                    (map-indexed (fn [idx itm] [:div {:class "col-lg-6" :key (str idx)} (clojure.string/upper-case itm)] ) (:vars (:head @result)))] ]
                [:div {
                  :class "body container-fluid"}
                  (if (> (count (:bindings (:results @result))) 0)
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

                            )) (:bindings (:results @result))))
                      [:div {:class "row"}
                        [:div {:class "col-xs-12"}
                          "Nenhum resultado encontrado"]])] ]
                [loading]) ))

(defn toolbar []
  [mdcr/wrapper "toolbar"
    (let [ipt-search (re-frame/subscribe [::subs/ipt-search])]
      [:div {:id "toolbar"}
        [:header.mdc-toolbar.mdc-toolbar--fixed
          [:div.mdc-toolbar__row
            [:section.mdc-toolbar__section
              [:div.mdc-elevation--z1 {:id "bar-search"}
                [:form {:method "get" :action (str "#/?subject=" @ipt-search)}
                  [:input {
                    :id "ipt-search"
                    :value (if (nil? @ipt-search) "" @ipt-search )
                    :on-change #(re-frame/dispatch [::events/ipt-search (-> % .-target .-value)])
                    }]
                  [:button {
                    :class ""
                    :id "btn-search"
                    :type "submit"}
                    [:i.material-icons "search"] ]]

              ]]
          ]]])])

(defn home-panel []
  (let [query (re-frame/subscribe [::subs/query])
        duri  (re-frame/subscribe [::subs/default-uri])]
    ;;After panel is loaded, query is created with get params
    (if-not (nil? (:subject @query))
            (do (re-frame/dispatch [::events/ipt-search (:subject @query)])
                (create-query @query))
            ;; TODO pass others configs in get request
            ;;If dont have subject, create query with default subject (if have one)
            (if-not (empty? @duri) (set! js/window.location.href (str "#/?subject=" @duri))) )
      [:div
        ;;Toolbar
        [toolbar]
        ;;Table
        [show-properties] ]))

;;remover
(defn about-panel []
  [:div "This is the About Page."
   [:div [:a {:href "#/"} "go to Home Page"]]])

;; Routes
(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
