package semantic.web.repository;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semantic.web.client.Neo4jClient;
import semantic.web.controller.DBPEDIAController;

import static org.neo4j.driver.Values.parameters;


public class EntryRepository {

    private Neo4jClient neo4jClient;

    public EntryRepository(Neo4jClient neo4jClient) {
        this.neo4jClient = Neo4jClient.getInstance();
    }

    private static final Logger LOG = LoggerFactory.getLogger(EntryRepository.class);

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
