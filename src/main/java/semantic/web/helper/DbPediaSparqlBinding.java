package semantic.web.helper;

@lombok.Data
public class DbPediaSparqlBinding {

    private URIResource resource;
    private URIResource type;
    private URIResource level1class;
    private URIResource level2class;
    private URIResource level3class;
    private URIResource level4class;
    private URIResource level5class;
    private URIResource level6class;
    private URIResource level7class;
    private URIResource level8class;
    private URIResource level9class;
    private URIResource level10class;


    @Override
    public String toString() {
        return "DbPediaSparqlBinding{" +
                "resource=" + resource +
                ", type=" + type +
                '}';
    }
}
