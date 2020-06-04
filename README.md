# EntityLinker

dbPediaQueries

types of given word:\ 
`CONSTRUCT { ?resource <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type . }
 WHERE {
 ?resource <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type .
 ?resource <http://www.w3.org/2000/01/rdf-schema#label> ?label .
 ?label bif:contains "'the phone'" .
 FILTER (langMatches(lang(?label),'en'))
 FILTER (lcase(str(?label)) = "the phone")
  }
`

class hierarchy:\
`CONSTRUCT {
                <http://dbpedia.org/ontology/Organisation> rdfs:subClassOf ?parentclass .
                 }
                 WHERE {
                 <http://dbpedia.org/ontology/Organisation> rdfs:subClassOf* ?parentclass .
             }`
             
class properties:\
`CONSTRUCT {
            <http://dbpedia.org/ontology/Organisation>  ?propertyType   ?property .
                }
                WHERE {
                values ?propertyType { owl:DatatypeProperty owl:ObjectProperty }
                ?property a ?propertyType ;
                rdfs:domain/rdfs:subClassOf* <http://dbpedia.org/ontology/Organisation>.
            }`
            
resource properties:\
`CONSTRUCT {
 <http://dbpedia.org/resource/The_Beatles>   ?property   ?value .
 }
 WHERE {
 { ?property a owl:DatatypeProperty } UNION { ?property a owl:ObjectProperty }      
  <http://dbpedia.org/resource/The_Beatles> ?property  ?value 
  FILTER (?property != <http://dbpedia.org/ontology/abstract>)            
 }`