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


#_(defn create-query [query]
  ;;force clean results
  (re-frame/dispatch [::events/query-result nil])
  ;;endpoints are used to make federated queries
  (let [prefixes (clojure.string/join " "
                    (map (fn [itm] (str "PREFIX " (get itm 0) ":<" (get itm 1) ">")) @(re-frame/subscribe [::subs/prefixes])))
        triple (str "<" (:entity query) "> ?predicate ?object .")
        services (clojure.string/join " "
                        (map (fn [itm] (str " UNION { SERVICE <" itm "> { " triple "} }")) (subvec @(re-frame/subscribe [::subs/endpoints]) 1))) ]
    (execute-query
      (str prefixes " SELECT DISTINCT ?predicate ?object WHERE { {" triple "} " services " } ORDER BY ASC(?predicate) LIMIT 1000" )) ))


(defn create-query [query]
  ;;force clean results
  (re-frame/dispatch [::events/query-result nil])
  ;;endpoints are used to make federated queries
  (let [prefixes (clojure.string/join " "
                    (map (fn [itm] (str "PREFIX " (get itm 0) ":<" (get itm 1) ">")) @(re-frame/subscribe [::subs/prefixes])))
        sub-triple (str "<" (:entity query) "> ?property ?value .")
        obj-triple (str "?value ?property <" (:entity query) "> ." )
        services (clojure.string/join " "
                        (map (fn [itm] (str " UNION { SERVICE <" itm "> { {" sub-triple " BIND (\"sub\" AS ?func) } UNION {" obj-triple " BIND (\"obj\" AS ?func) }} }")) (subvec @(re-frame/subscribe [::subs/endpoints]) 1))) ]
    (execute-query
      (str prefixes " SELECT DISTINCT ?property ?value ?func WHERE { {{" sub-triple " BIND (\"sub\" AS ?func)} UNION {" obj-triple " BIND (\"obj\" AS ?func)}} " services " } ORDER BY ASC(?property) LIMIT 10000" )) ))


(defn wrapper-value [cell cpm-key]
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
      [(if (nil? (:component component)) :div (:component component)) (-> (if (empty? component-attrs) value (conj {:key cpm-key :style {:display "inline-block"}} component-attrs)) ) ] ))

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
              [:div.mdc-toolbar-fixed-adjust {
                :id  "properties-table"}
                [:div {
                  :class "head mdc-layout-grid"}
                  [:div {:class "mdc-layout-grid__inner mdc-elevation--z1"}
                    [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-6" :key "prop-head"} "Property"]
                    [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-6" :key "value-head"} "Value"] ]]
                [:div {
                  :class "body mdc-layout-grid"}
                  (if (> (count (:bindings (:results @result))) 0)
                      ;;In loop 0 last-(predicate/object) starts with the first result
                      (let [last-property (atom (:property (nth (:bindings (:results @result)) 0)))
                            last-value    (atom (:value (nth (:bindings (:results @result)) 0)))
                            last-func     (atom (:func (nth (:bindings (:results @result)) 0)))
                            values        (atom [:ul])
                            lines          (atom [])
                            striped        (atom false)]
                        ;;Run a loop over result vector
                        (dotimes [idx (count (:bindings (:results @result)))]
                          (let [itm (nth (:bindings (:results @result)) idx)
                                property (:property itm)
                                value (:value itm)
                                func  (:func itm)
                                same-prop (= (:value property) (:value @last-property))]

                            (if-not same-prop
                              (do
                                (if (= [:ul] @values)
                                    (reset! values [:ul [:li {:key (str "li-" idx)} (wrapper-value @last-value (str (:type @last-value) "-" idx))]]) )
                                (reset! lines
                                  (conj @lines
                                    [:div {:class (str "mdc-layout-grid__inner " (if @striped "striped" "") ) :key (str idx)}
                                      [:div {:class "mdc-layout-grid__cell--span-6" :style {:display "inline-block"}}
                                        [:div {:style {:display "inline-block" :padding "0 5px 0 5px"}} (if (= (:value @last-func) "obj") "Is object of : " "")
                                          (wrapper-value @last-property (str (:type @last-property) "-" idx)) ]]
                                      [:div {:class "mdc-layout-grid__cell--span-6"}
                                         @values]]))
                                (reset! striped (if @striped false true))
                                (reset! last-property property)
                                (reset! last-value value)
                                (reset! last-func func)
                                (reset! values [:ul]) )
                              (do
                                (reset! values (conj @values [:li {:key (str "li-" idx)} (wrapper-value value (str (:type value) "-" idx))]))  ))
                            ))
                          [:div {:class "inner-div mdc-elevation--z1"} (map (fn [itm] itm) @lines)]
                      )
                      [:div {:class "mdc-layout-grid__inner"}
                        [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-12"}
                          "Nenhum resultado encontrado"]])] ]
                [loading]) ))


(defn toolbar []
  (reagent/create-class {
    :component-did-mount
      (fn [state]
        (js/console.log "Mount: ""toolbar")
        #_(let [menuEl (js/document.querySelector "#demo-menu")
              menu (js/mdc.menu.MDCMenu. menuEl)
              menuButtonEl  (js/document.querySelector "#menu-button")
              corner (.-BOTTOM_START js/mdc.menu.MDCMenuFoundation.Corner)]
              (.addEventListener menuButtonEl "click" (fn [] (set! (.-open menu) (if (.-open menu) false true))))
              (.setAnchorCorner menu corner) ))
    :component-will-mount #()
    :display-name  "toolbar"
    :reagent-render
      (fn []
        (let [ipt-search (re-frame/subscribe [::subs/ipt-search])]
          [:div {:id "toolbar"}
            [:header.mdc-toolbar.mdc-toolbar--fixed
              [:div.mdc-toolbar__row
                #_[:section.mdc-toolbar__section.mdc-toolbar__section--align-start.mdc-toolbar__section--shrink-to-fit.mdc-toolbar__menu-icon
                  [:a.material-icons.align-icons {:arial "menu" :alt "menu" :onClick #(js/alert "haha")} "menu"]]
                [:section.mdc-toolbar__section
                  [:div.mdc-elevation--z1 {:id "bar-search"}
                    [:form {:method "get" :action (str "#/?entity=" @ipt-search)}
                      [:input {
                        :id "ipt-search"
                        :value (if (nil? @ipt-search) "" @ipt-search )
                        :on-change #(re-frame/dispatch [::events/ipt-search (-> % .-target .-value)])
                        }]
                      [:button {
                        :class ""
                        :id "btn-search"
                        :type "submit"}
                        [:i.material-icons "search"] ]] ]]
                #_[:section.mdc-toolbar__section.mdc-toolbar__section--align-end.mdc-toolbar__section--shrink-to-fit.mdc-menu-anchor
                  [:a.material-icons.mdc-toolbar__icon.align-icons {
                    :id "menu-button"}
                    "more_vert"]
                  [:div {
                    :tabIndex "-1"
                    :id "demo-menu"
                    :class "mdc-menu"
                    :style {:position "absolute"}}
                    [:ul.mdc-menu__items.mdc-list {:role "menu" :aria-hidden "true"}
                      [:li.mdc-list-item {:role "menuitem" :tabIndex "0"} "Endpoints" ]
                      [:li.mdc-list-item {:role "menuitem" :tabIndex "0"} "Langs" ]
                      [:li.mdc-list-item {:role "menuitem" :tabIndex "0"} "Web Components" ]
                      ]]]
              ]]])
        ) }))

(defn home-panel []
  (let [query (re-frame/subscribe [::subs/query])
        duri  (re-frame/subscribe [::subs/default-uri])]
    ;;After panel is loaded, query is created with get params
    (if-not (nil? (:entity @query))
            (do (re-frame/dispatch [::events/ipt-search (:entity @query)])
                (create-query @query))
            ;; TODO pass others configs in get request
            ;;If dont have subject, create query with default subject (if have one)
            (if-not (empty? @duri) (set! js/window.location.href (str "#/?entity=" @duri))) )
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
