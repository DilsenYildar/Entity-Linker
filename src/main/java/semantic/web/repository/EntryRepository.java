package semantic.web.repository;

import org.neo4j.driver.Session;
import semantic.web.client.Neo4jClient;

import static org.neo4j.driver.Values.parameters;


public class EntryRepository {

    private Neo4jClient neo4jClient;

    public EntryRepository(Neo4jClient neo4jClient) {
        this.neo4jClient = Neo4jClient.getInstance();
    }

    private void saveEntry(String filePath) {
        try (Session session = neo4jClient.getDriver().session()) {
            session.run("CALL semantics.importRDF(\"file://$tripleFile\",\"N-Triples\")", parameters("tripleFile", filePath));
        }
    }



}
