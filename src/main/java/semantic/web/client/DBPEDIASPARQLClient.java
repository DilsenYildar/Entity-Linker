package semantic.web.client;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Flowable;
import semantic.web.client.configuration.DBPediaConfiguration;
import semantic.web.helper.DbPediaSparqlResponse;

@Client(DBPediaConfiguration.DBPEDIA_SPARQL_URL)
public interface DBPEDIASPARQLClient {

    @Get("/sparql?default-graph-uri=http%3A%2F%2Fdbpedia" + ".org&query=SELECT+%3Fresource+%3Ftype+%3Flevel1class" +
            "+%3Flevel2class+%3Flevel3class+%3Flevel4class" + "+%3Flevel5class+%3Flevel6class+%3Flevel7class" +
            "+%3Flevel8class+%3Flevel9class+%3Flevel10class%0D%0AWHERE" + "+%7B%0D%0A%3Fresource+%3Chttp%3A%2F%2Fwww" + ".w3.org%2F2000%2F01%2Frdf-schema%23label%3E+%3Flabel+" + ".%0D%0AFILTER+%28lcase%28str%28%3Flabel%29%29" + "+%3D+%22{text}%22%29%0D%0AFILTER+langMatches%28lang%28" + "%3Flabel%29%2C%27en%27%29%0D%0A%3Fresource" + "+%3Chttp%3A%2F%2Fwww.w3" + ".org%2F1999%2F02%2F22-rdf-syntax-ns%23type%3E+%3Ftype+" + ".%0D%0AOPTIONAL" + "+%7B+%0D%0A%3Fresource+rdfs%3AsubClassOf+%3Flevel1class+" + ".%0D%0AOPTIONAL+%7B+%0D%0A%3Flevel1class" + "+rdfs%3AsubClassOf+%3Flevel2class+" + ".%0D%0AOPTIONAL+%7B+%0D%0A%3Flevel2class+rdfs%3AsubClassOf" + "+%3Flevel3class+" + ".%0D%0AOPTIONAL+%7B%0D%0A%3Flevel3class+rdfs%3AsubClassOf+%3Flevel4class+" + ".%0D" + "%0AOPTIONAL+%7B%0D%0A%3Flevel4class+rdfs%3AsubClassOf+%3Flevel5class+" + ".%0D%0AOPTIONAL+%7B%0D%0A" + "%3Flevel5class+rdfs%3AsubClassOf+%3Flevel6class+" + ".%0D%0AOPTIONAL+%7B%0D%0A%3Flevel6class+rdfs" + "%3AsubClassOf+%3Flevel7class+" + ".%0D%0AOPTIONAL+%7B%0D%0A%3Flevel7class+rdfs%3AsubClassOf" + "+%3Flevel8class+" + ".%0D%0AOPTIONAL+%7B%0D%0A%3Flevel8class+rdfs%3AsubClassOf+%3Flevel9class+" + ".%0D" + "%0AOPTIONAL+%7B%0D%0A%3Flevel9class+rdfs%3AsubClassOf+%3Flevel10class+" + ".%0D%0A%7D%0D%0A%7D%0D%0A%7D" + "%0D%0A%7D%0D%0A%7D%0D%0A%7D%0D%0A%7D%0D%0A%7D%0D%0A%7D%0D%0A%7D%0D%0A%7D%0D" + "%0ALIMIT+100%0D%0A" + "&format=application%2Fsparql-results%2Bjson&CXML_redir_for_subjs=121" + "&CXML_redir_for_hrefs=&timeout" + "=30000&debug=on&run=+Run+Query+")
    @Header(name = "Accept", value = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng," + "*/*;q=0.8," +
                    "application/signed-exchange;v=b3;q=0.9")
    Flowable<DbPediaSparqlResponse> sparqlQueryDBPEDIA(String text);
}