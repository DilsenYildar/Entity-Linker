package semantic.web.client.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

@ConfigurationProperties(value = DBPediaConfiguration.PREFIX)
@Requires(property = DBPediaConfiguration.PREFIX)
public class DBPediaConfiguration {

    public static final String PREFIX = "dbPedia";
    public static final String DBPEDIA_API_URL = "http://lookup.dbpedia.org";
    public static final String DBPEDIA_SPARQL_URL = "https://dbpedia.org";
}
