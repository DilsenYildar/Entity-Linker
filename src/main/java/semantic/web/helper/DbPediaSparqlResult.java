package semantic.web.helper;

import java.util.List;

public class DbPediaSparqlResult {
    private boolean distinct;

    private boolean ordered;

    private List<DbPediaSparqlBinding> bindings;

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }


    public List<DbPediaSparqlBinding> getBindings() {
        return bindings;
    }

    public void setBindings(List<DbPediaSparqlBinding> bindings) {
        this.bindings = bindings;
    }

    @Override
    public String toString() {
        return "DbPediaSparqlResult{" +
                "bindings=" + bindings +
                '}';
    }
}
