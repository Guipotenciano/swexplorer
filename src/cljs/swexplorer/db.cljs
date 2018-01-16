(ns swexplorer.db)

(def default-db {
  :name "re-frame"
  :endpoints ["http://java.icmc.usp.br:2372/repositories/Gazetteer" #_"http://java.icmc.usp.br:2472/repositories/Politics" "https://query.wikidata.org/sparql" "https://dbpedia.org/sparql"]
  :prefixes [
    ["wd" "http://www.wikidata.org/entity/"]
    ["wdt" "http://www.wikidata.org/prop/direct/"]]
  :interface {
    :title "Title"
    :loading {
      :display false
    }
    :toolbar {
      :ipt-search "http://www.wikidata.org/entity/Q10320255"
      :endpoint ""
    }
  }
  :history {
    :index 0
    :queries [{:subject "http://www.wikidata.org/entity/Q10320255" :predicate nil :object nil}]
  }
  :query-result {
    :head {
      :vars ["Predicate" "Object"]}
    :results {
      :bindings [#_{:predicate {:type "uri", :value "http://wikiba.se/ontology#identifiers"}, :object {:datatype "http://www.w3.org/2001/XMLSchema#integer", :type "literal", :value "3"}}]
      }}})
