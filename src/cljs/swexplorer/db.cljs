(ns swexplorer.db)

#_(comment
  {
  :name (string)
  :endpoints [(string)]
  :prefixes [[(string) (string)]]
  :web-components [
    {:type ((string) "uri" || "literal" || "bnode")
     :component ((string) "component-name")
     :attributes [
        [(string) (:keyword || (string))]
        ["data-value" :value]
        ["data-value" (keyword "value")]
        ["data-type" "text"]
     ]}]
  :interface {
   :title (string)
   :toolbar {
     :ipt-search (string)
   }
  }
  :query {
    :subject {
      :entity   (string)
      :type     ((string) "uri" || "literal" || "bnode")
      :lang     (string)
      :datatype (string)
    }
    :predicate ((string) || {} || nil)
    :object ((string) || {} || nil)
  }
  :query-result {
    :head {
      :vars [(string) (string)]}
    :results {
      :bindings [
        {:predicate {
            :type (string) ,
            :value (string)} ,
         :object {
           :datatype (string) ,
           :type (string),
           :value (string) }}]
      }}

})

(def default-db {
  :name "re-frame"
  :endpoints [#_"http://java.icmc.usp.br:2372/repositories/Gazetteer" "http://java.icmc.usp.br:2472/repositories/Politics" "https://query.wikidata.org/sparql" "https://dbpedia.org/sparql"]
  :prefixes [
    ["wd" "http://www.wikidata.org/entity/"]
    ["wdt" "http://www.wikidata.org/prop/direct/"]]
  :default-uri "http://www.wikidata.org/entity/Q5"
  :web-components [
    {:type "uri"
     :component "data-view"
     :attributes [
        ["data-value" :value]
        ["data-type"  :type]] } ,
    {:type "literal"
     :component "data-view"
     :attributes [
        ["data-value"         :value]
        ["data-literal-type"  :datatype]
        ["data-type"          :type]
        ["data-lang"          (keyword "xml:lang")]] } ,
    {:type "bnode"
     :component "data-view"
     :attributes [
        ["data-value" :value]
        ["data-type"  :type]] }
  ]
  :interface {
    :title "Title"
    :toolbar {
      :ipt-search ""
    }
  }
  :query {
    :subject    nil
    :predicate  nil
    :object     nil
  }
  :query-result nil
})
