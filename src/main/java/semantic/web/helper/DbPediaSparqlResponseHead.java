package semantic.web.helper;

import java.util.List;

public class DbPediaSparqlResponseHead {
    private List<String> link;
    private List<String> vars;

    public List<String> getLink() {
        return link;
    }

    public void setLink(List<String> link) {
        this.link = link;
    }

    public List<String> getVars() {
        return vars;
    }

    public void setVars(List<String> vars) {
        this.vars = vars;
    }

    @Override
    public String toString() {
        return "DbPediaSparqlResponseHead{" +
                "link=" + link +
                ", vars=" + vars +
                '}';
    }
}
