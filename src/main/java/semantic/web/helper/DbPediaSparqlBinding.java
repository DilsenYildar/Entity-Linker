package semantic.web.helper;

public class DbPediaSparqlBinding {

    private URIResource resource;
    private URIResource type;

    public URIResource getResource() {
        return resource;
    }

    public void setResource(URIResource resource) {
        this.resource = resource;
    }

    public URIResource getType() {
        return type;
    }

    public void setType(URIResource type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "DbPediaSparqlBinding{" +
                "resource=" + resource +
                ", type=" + type +
                '}';
    }
}
