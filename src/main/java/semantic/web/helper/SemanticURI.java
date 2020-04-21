package semantic.web.helper;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class SemanticURI {

    private String URI;
    private String label;

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "SemanticURI{" +
                "URI='" + URI + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
