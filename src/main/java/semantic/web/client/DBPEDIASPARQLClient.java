package semantic.web.client;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import semantic.web.client.configuration.DBPediaConfiguration;

@Client(DBPediaConfiguration.DBPEDIA_SPARQL_URL)
public interface DBPEDIASPARQLClient {

    @Get("/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&format=text%2Fplain&timeout=50000&query=CONSTRUCT%20%7B%20%0A%3Fresource%20%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23type%3E%20%3Ftype%20.%0A%3Fresource%20%3Ftype%20%3Fclass%20.%0A%3Fresource%20rdfs%3AsubClassOf%20%3Fclass%20.%0A%7D%0AWHERE%20%7B%0A%3Fresource%20%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23label%3E%20%3Flabel%20.%0AFILTER%20%28lcase%28str%28%3Flabel%29%29%20%3D%20%22{text}%22%29%0AFILTER%20langMatches%28lang%28%3Flabel%29%2C%27en%27%29%0A%3Fresource%20%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23type%3E%20%3Ftype%20.%0A%3Fresource%20rdfs%3AsubClassOf%2A%20%3Fclass%20.%0A%7D")
    @Header(name = "Accept", value = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng," + "*/*;q=0.8," +
                    "application/signed-exchange;v=b3;q=0.9")
    String sparqlQueryDBPEDIA(String text);
}