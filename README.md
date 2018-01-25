# Semantic Web Explorer

SWExplorer is a semantic web browser developed using Clojure/ClojureScript.

## Under Development

## How execute
Download project folder

Download https://github.com/webcomponents/webcomponentsjs and put it in "/resources/public/web_components/webcomponentsjs"



## Web components config

``` clojure
{
...
  :web-components [
    {:type ((string) "uri" || "literal" || "bnode")
     :component ((string) "component-name")
     :attributes [
        [(string) (:keyword || (string))]
        ["data-value" :value]
        ["data-value" (keyword "value")]
        ["data-type" "text"]
     ]}]
...
}
```

## JSON SPARQL
The results of a SPARQL Query are serialized in JSON as a single top-level JSON object. This object has a "head" member and either a "results" member or a "boolean" member, depending on the query form.

This example shows the results of a SELECT query. The query solutions are represented in an array which is the value of the "bindings" key, in turn part of an object that is the value of the "results" key:

```JSON
{
   "head": {
       "link": ["http://www.w3.org/TR/rdf-sparql-XMLres/example.rq"],
       "vars": ["x", "hpage", "name", "mbox", "age", "blurb", "friend"]} ,
   "results": {
       "bindings": [
               {
                 "x" : {
                   "type": "bnode" ,
                   "value": "r1" },
                 "hpage" : {
                   "type": "uri",
                   "value": "http://work.example.org/alice/" },
                 "name" : {
                   "type": "literal",
                   "value": "Alice" } ,
	               "mbox" : {
                   "type": "literal",
                   "value": "" } ,
                 "blurb" : {
                   "datatype": "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                   "type": "literal",
                   "value": "<p xmlns=\"http://www.w3.org/1999/xhtml\">My name is <b>alice</b></p>"
                 },
                 "friend" : {
                   "type": "bnode",
                   "value": "r2" }
               },
               {
                 ...
               }
           ]
       }
}
```

### Encoding RDF terms

An RDF term (IRI, literal or blank node) is encoded as a JSON object. All aspects of the RDF term are represented. The JSON object has a "type" member and other members depending on the specific kind of RDF term.

| RDF Term | JSON form |
| ------ | ------ |
| IRI I | {"type": "uri", "value": "I"} |
| Literal S | {"type": "literal","value": "S"} |
| Literal S with language tag L | { "type": "literal", "value": "S", "xml:lang": "L"} |
| Literal S with datatype IRI D | { "type": "literal", "value": "S", "datatype": "D"} |
| Blank node, label B | {"type": "bnode", "value": "B"} |

The blank node label is scoped to the results object. That is, two blank nodes with the same label in a single SPARQL Results JSON object are the same blank node. This is not an indication of any internal system identifier the SPARQL processor may use. Use of the same label in another SPARQL Results JSON object does not imply it is the same blank node.

## Development Mode

### Compile css:

Compile css file once.

```
lein less once
```

Automatically recompile css file on change.

```
lein less auto
```

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```
