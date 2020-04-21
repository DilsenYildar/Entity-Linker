package semantic.web.helper;

public class DbPediaSparqlResponse {

    private DbPediaSparqlResponseHead head;
    private DbPediaSparqlResult results;

    public DbPediaSparqlResult getResults() {
        return results;
    }

    public void setResults(DbPediaSparqlResult results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "DbPediaSparqlResponse{" +
                "results=" + results +
                '}';
    }
}
