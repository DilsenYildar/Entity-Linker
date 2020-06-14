# Dbpedia Sorguları

##### 1- Verilen sözcüğün rdf type bilgisini döndürür.

```SPARQL
 CONSTRUCT { ?resource <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type . }
 WHERE {
 ?resource <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type .
 ?resource <http://www.w3.org/2000/01/rdf-schema#label> ?label .
 ?label bif:contains "'the beatles'" .
 FILTER (langMatches(lang(?label),'en'))
 FILTER (lcase(str(?label)) = "the beatles")
  }
```

```SPARQL
<http://dbpedia.org/resource/The_Beatles>	<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>   <http://dbpedia.org/ontology/Agent>.
<http://dbpedia.org/resource/The_Beatles>	<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>	<http://dbpedia.org/ontology/Organisation> .
<http://dbpedia.org/resource/The_Beatles>	<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>	<http://dbpedia.org/ontology/Band> .
```



##### 2- Verilen urinin class hiyararşisini döndürür.

```SPARQL
CONSTRUCT { <http://dbpedia.org/ontology/Organisation> rdfs:subClassOf ?parentclass . 
             ?parentclass rdfs:subClassOf ?superclass . ?parentclass
            <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  ?type .  
}WHERE { 
            <http://dbpedia.org/ontology/Organisation> rdfs:subClassOf* ?parentclass . 
            ?parentclass rdfs:subClassOf* ?superclass . 
            ?parentclass <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  ?type . 
     }`
```

```SPARQL
<http://dbpedia.org/ontology/Agent>  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type  <http://www.w3.org/2002/07/owl#Class> .
<http://dbpedia.org/ontology/Agent>  <http://www.w3.org/2000/01/rdf-schema#subClassOf <http://www.w3.org/2002/07/owl#Thing> .
<http://dbpedia.org/ontology/Agent  <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://dbpedia.org/ontology/Agent> .
<http://dbpedia.org/ontology/Organisation>  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type <http://www.w3.org/2002/07/owl#Class> .
<http://dbpedia.org/ontology/Organisation> <http://www.w3.org/2000/01/rdf-schema#subClassOf <http://dbpedia.org/ontology/Agent> .
<http://dbpedia.org/ontology/Organisation> <http://www.w3.org/2000/01/rdf-schema#subClassOf <http://www.w3.org/2002/07/owl#Thing> .
<http://dbpedia.org/ontology/Organisation>  <http://www.w3.org/2000/01/rdf-schema#subClassOf <http://dbpedia.org/ontology/Organisation> .

```



##### 3- Verilen class urisinin class propertylerini döndürür.

```SPARQL
CONSTRUCT { 
        <http://dbpedia.org/ontology/Organisation> ?propertyType ?property .
        ?property  rdf:type ?type
} WHERE { 
    values ?propertyType { owl:DatatypeProperty owl:ObjectProperty } 
    ?property a ?propertyType ; rdfs:domain/rdfs:subClassOf* <http://dbpedia.org/ontology/Organisation>. 
    ?property  rdf:type ?type
}
```

```SPARQL
<http://dbpedia.org/ontology/Organisation  <http://www.w3.org/2002/07/owl#DatatypeProperty> <http://dbpedia.org/ontology/facultySize> .
<http://dbpedia.org/ontology/facultySize <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . 
<http://dbpedia.org/ontology/facultySize <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty> .
<http://dbpedia.org/ontology/Organisation <http://www.w3.org/2002/07/owl#ObjectProperty> <http://dbpedia.org/ontology/ceo> .
<http://dbpedia.org/ontology/ceo  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> . 
<http://dbpedia.org/ontology/ceo <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .
```



##### 4- Verilen resource urisinin propertylerini döndürür.

```SPARQL
CONSTRUCT {
     <http://dbpedia.org/resource/The_Beatles>   ?property   ?value .
     ?property rdf:type ?type .
  } WHERE {
  { ?property a owl:DatatypeProperty } UNION { ?property a owl:ObjectProperty }      
   <http://dbpedia.org/resource/The_Beatles> ?property  ?value .
   ?property rdf:type ?type .
   FILTER (?property != <http://dbpedia.org/ontology/abstract>)            
  }
```

```SPARQL
<http://dbpedia.org/resource/The_Beatles>  <http://dbpedia.org/ontology/formerBandMember> <http://dbpedia.org/resource/Ringo_Starr> .
<http://dbpedia.org/resource/The_Beatles>  <http://dbpedia.org/ontology/formerBandMember> <http://dbpedia.org/resource/George_Harrison> .
<http://dbpedia.org/resource/The_Beatles>  <http://dbpedia.org/ontology/formerBandMember> <http://dbpedia.org/resource/John_Lennon> .
<http://dbpedia.org/resource/The_Beatles> <http://dbpedia.org/ontology/formerBandMember> <http://dbpedia.org/resource/Paul_McCartney> .
<http://dbpedia.org/ontology/formerBandMember>  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://www.w3.org/2002/07/owl#ObjectProperty> .
<http://dbpedia.org/ontology/formerBandMember> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .

```


class instances:\
`   
SELECT DISTINCT ?s 
WHERE { ?s rdf:type <http://dbpedia.org/ontology/Organisation> }
`

property limits:\
` 
CONSTRUCT {
      <http://dbpedia.org/ontology/formerBandMember> rdfs:range ?range .
      <http://dbpedia.org/ontology/formerBandMember> rdfs:domain ?domain .
   }
WHERE {
 OPTIONAL {
      <http://dbpedia.org/ontology/formerBandMember> rdfs:range ?range .
 }
 OPTIONAL {
     <http://dbpedia.org/ontology/formerBandMember> rdfs:domain ?domain .   
 }
}
`
