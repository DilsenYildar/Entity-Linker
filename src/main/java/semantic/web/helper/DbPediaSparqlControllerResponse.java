package semantic.web.helper;

import java.util.ArrayList;
import java.util.List;

public class DbPediaSparqlControllerResponse {

    private String token;

    private List<String> relatedResources;

    private List<String> classes;

    public DbPediaSparqlControllerResponse() {
        this.relatedResources = new ArrayList<>();
        this.classes = new ArrayList<>();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<String> getRelatedResources() {
        return relatedResources;
    }

    public void setRelatedResources(List<String> relatedResources) {
        this.relatedResources = relatedResources;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }
}
