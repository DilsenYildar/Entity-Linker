package semantic.web.client;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import semantic.web.client.configuration.DBPediaConfiguration;

@Client(DBPediaConfiguration.DBPEDIA_SPARQL_URL)
public interface DBPEDIASPARQLClient {

    @Get("/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&format=text%2Fplain&timeout=50000&query=CONSTRUCT+%7B++%0D%0A+++++++++++++++%3Fresource+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23type%3E+%3Ftype+.%0D%0A++++++++++++++++%3Fresource+rdfs%3AsubClassOf+%3Fclass+.%0D%0A++++++++++++++++%7D%0D%0A++++++++++++++++WHERE+%7B%0D%0A++++++++++++++++%3Fresource+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23label%3E+%3Flabel+.%0D%0A++++++++++++++++FILTER+%28lcase%28str%28%3Flabel%29%29+%3D+%22{text}%22%29%0D%0A++++++++++++++++FILTER+langMatches%28lang%28%3Flabel%29%2C%27en%27%29%0D%0A++++++++++++++++%3Fresource+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23type%3E+%3Ftype+.%0D%0A++++++++++++++++%3Fresource+rdfs%3AsubClassOf*+%3Fclass+.%0D%0A++++++++++++%7D&format=text%2Fplain&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000&debug=on&run=+Run+Query+")
    @Header(name = "Accept", value = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    String sparqlQueryDBPEDIA(String text);

    @Get("/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=CONSTRUCT+%7B+%3Fresource+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23type%3E+%3Ftype+.+%7D%0D%0AWHERE+%7B%0D%0A%3Fresource+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23type%3E+%3Ftype+.%0D%0A%3Fresource+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23label%3E+%3Flabel+.%0D%0A%3Flabel+bif%3Acontains+%22%27{text}%27%22+.%0D%0AFILTER+%28langMatches%28lang%28%3Flabel%29%2C%27en%27%29%29%0D%0AFILTER+%28lcase%28str%28%3Flabel%29%29+%3D+%22{text}%22%29%0D%0A+%7D%0D%0A&format=text%2Fplain&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=1000000&debug=on&run=+Run+Query+")
    @Header(name = "Accept", value = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    String sparqlRdfType(String text);

    @Get("/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=CONSTRUCT+%7B+{classUri}+rdfs%3AsubClassOf+%3Fparentclass+.%0D%0A%3Fparentclass+rdfs%3AsubClassOf+%3Fsuperclass+.%0D%0A%3Fparentclass+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23type%3E++%3Ftype+.%0D%0A+%7D+WHERE+%7B%0D%0A{classUri}+rdfs%3AsubClassOf*+%3Fparentclass+.+%0D%0A%3Fparentclass+rdfs%3AsubClassOf*+%3Fsuperclass+.+%0D%0A%3Fparentclass+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23type%3E++%3Ftype+.%0D%0A%0D%0A%7D&format=text%2Fplain&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000&debug=on&run=+Run+Query+")
    @Header(name = "Accept", value = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    String sparqlClassHierarchy(String classUri);

    @Get("/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=CONSTRUCT+%7B+{classUri}+%3FpropertyType+%3Fproperty+.%0D%0A%3Fproperty++rdf%3Atype+%3Ftype%7D+%0D%0AWHERE+%7B+values+%3FpropertyType+%7B+owl%3ADatatypeProperty+owl%3AObjectProperty+%7D+%0D%0A%3Fproperty+a+%3FpropertyType+%3B+rdfs%3Adomain%2Frdfs%3AsubClassOf*+{classUri}.+%0D%0A%3Fproperty++rdf%3Atype+%3Ftype%0D%0A%7D&format=text%2Fplain&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000&debug=on&run=+Run+Query+")
    @Header(name = "Accept", value = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng," + "*/*;q=0.8," +
            "application/signed-exchange;v=b3;q=0.9")
    String sparqlClassProperties(String classUri);

    @Get("/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=CONSTRUCT+%7B%0D%0A+{resourceUri}+++%3Fproperty+++%3Fvalue+.%0D%0A++++%3Fproperty+rdf%3Atype+%3Ftype+.%0D%0A+%7D%0D%0A+WHERE+%7B%0D%0A+%7B+%3Fproperty+a+owl%3ADatatypeProperty+%7D+UNION+%7B+%3Fproperty+a+owl%3AObjectProperty+%7D++++++%0D%0A++{resourceUri}+%3Fproperty++%3Fvalue+.%0D%0A++++++%3Fproperty+rdf%3Atype+%3Ftype+.%0D%0A++FILTER+%28%3Fproperty+%21%3D+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2Fabstract%3E%29++++++++++++%0D%0A+%7D&format=text%2Fplain&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000&debug=on&run=+Run+Query+")
    @Header(name = "Accept", value = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng," + "*/*;q=0.8," +
            "application/signed-exchange;v=b3;q=0.9")
    String sparqlResourceProperties(String resourceUri);
}