package semantic.web.helper;

import io.micronaut.core.annotation.Introspected;

import java.util.List;

@Introspected
public class DbPediaLookUpResultElement {
    private String uri;
    private String label;
    private String description;
    private long refCount;
    private List<SemanticURI> classes;
    private List<SemanticURI> categories;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getRefCount() {
        return refCount;
    }

    public void setRefCount(long refCount) {
        this.refCount = refCount;
    }

    public List<SemanticURI> getClasses() {
        return classes;
    }

    public void setClasses(List<SemanticURI> classes) {
        this.classes = classes;
    }

    public List<SemanticURI> getCategories() {
        return categories;
    }

    public void setCategories(List<SemanticURI> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "DbPediaResult{" +
                "uri='" + uri + '\'' +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", refCount=" + refCount +
                ", classes=" + classes +
                ", categories=" + categories +
                '}';
    }
}
