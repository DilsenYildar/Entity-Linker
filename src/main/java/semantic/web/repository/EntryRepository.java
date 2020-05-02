package semantic.web.repository;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semantic.web.client.Neo4jClient;

import static org.neo4j.driver.Values.parameters;


public class EntryRepository {

    private final Neo4jClient neo4jClient;

    private static final Logger LOG = LoggerFactory.getLogger(EntryRepository.class);

    public EntryRepository() {
        this.neo4jClient = Neo4jClient.getInstance();
        createIndex();
    }

    public void createIndex() {
        try (Session session = neo4jClient.getDriver().session()) {
            Query indexQuery = new Query("CREATE INDEX ON :Resource(uri)");
            session.run(indexQuery);
            LOG.debug("indexQuery: " + indexQuery.text());
        }
    }

    public void saveEntry(String filePath) {
        String adjustedFilePath = adjustFilePath(filePath);
        LOG.debug("adjustedFilePath: " + adjustedFilePath);
        try (Session session = neo4jClient.getDriver().session()) {
            Query query = new Query("CALL semantics.importRDF($tripleFile, 'N-Triples')", parameters("tripleFile", adjustedFilePath));
            LOG.debug("query: " + query.text());
            session.run(query);
        }
    }

    private String adjustFilePath(String oldFilepath) {
        return "file://" + oldFilepath.replace(System.getProperty("user.home"), "/var/lib");
    }


}
