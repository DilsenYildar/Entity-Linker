package semantic.web.helper;

import java.util.List;

public class DbPediaLookUpResult {

    private List<DbPediaLookUpResultElement> results;

    public List<DbPediaLookUpResultElement> getResults() {
        return results;
    }

    public void setResults(List<DbPediaLookUpResultElement> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "DbPediaResult{" +
                "results=" + results +
                '}';
    }
}
