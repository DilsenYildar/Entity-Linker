package semantic.web.client;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jClient implements AutoCloseable {

    private final Driver driver;

    private static final Neo4jClient client = new Neo4jClient();

    private Neo4jClient() {
        this.driver = GraphDatabase.driver("", AuthTokens.basic("", ""));
    }

    public static Neo4jClient getInstance() {
        return client;
    }

    @Override
    public void close() throws Exception {
        driver.close();
        
    }
}
