package semantic.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.DefaultHttpClient;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.HttpClientConfiguration;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semantic.web.client.DBPEDIASPARQLClient;
import semantic.web.file.EntryWriter;
import semantic.web.helper.TextAnalyzeRequest;
import semantic.web.repository.EntryRepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;

import static org.apache.jena.riot.RDFDataMgr.read;
import static org.apache.jena.riot.RDFDataMgr.write;

@Controller("/dbpedia")
public class DBPEDIAController {
    private static final Logger LOG = LoggerFactory.getLogger(DBPEDIAController.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private final DBPEDIASPARQLClient dbpediasparqlClient;

    private static final Set<String> ALLOWED_NAMESPACES = new HashSet<>(Arrays.asList("http://dbpedia.org", "http://www.w3.org"));

    public DBPEDIAController(DBPEDIASPARQLClient dbpediasparqlClient) {
        this.dbpediasparqlClient = dbpediasparqlClient;
    }

    @Post(value = "/sparql", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    String dbpediaSparql(TextAnalyzeRequest request) throws IOException {
        Set<String> processedTokens = getProcessedTokens(request.getText());
        EntryWriter entryWriter = new EntryWriter();
        for (String token : processedTokens) {
            String rdfTypeResults = dbpediasparqlClient.sparqlRdfType(token);

            Model model = ModelFactory.createDefaultModel();
            String filePath = entryWriter.writeFiles(token, rdfTypeResults);
            read(model, filePath);
            filterSuitableNamespaces(model);

            StmtIterator stmtIterator = model.listStatements();
            while (stmtIterator.hasNext()) {
                Statement stmt = stmtIterator.nextStatement();
                Resource subject = stmt.getSubject();
                // örnek beatles ın data ve object propertyleri
//                if (subject.getURI().contains("http://dbpedia.org/resource")) {
//                    String resourceProperties =
//                            dbpediasparqlClient.sparqlResourceProperties("<".concat(subject.getURI()).concat(">"));
//                    Model resourcesModel = ModelFactory.createDefaultModel();
//                    RDFParser.create().lang(Lang.NTRIPLES).source(new ByteArrayInputStream(resourceProperties.getBytes())).parse(resourcesModel);
//                    model.add(resourcesModel);
//                }
                // örnek beatles ın üst sınıflarının data ve object propertyleri
                RDFNode object = stmt.getObject();
                if ((object instanceof Resource)) {
                    String classHierarchy = dbpediasparqlClient.sparqlClassHierarchy(((Resource) object).getURI());
                    // todo: s ile o aynı ise temizle
                    Model model2 = ModelFactory.createDefaultModel();
                    String filePath2 = entryWriter.writeFiles(token, classHierarchy);
                    read(model2, filePath2);

                    // todo: model2deki her class için alttaki 4 satırı yap
//                    String classProperties = dbpediasparqlClient.sparqlClassProperties("<".concat(subject.getURI()).concat(">"));
//                    Model classPropModel = ModelFactory.createDefaultModel();
//                    RDFParser.create().lang(Lang.NTRIPLES).source(new ByteArrayInputStream(classProperties.getBytes())).parse(classPropModel);
//                    model2.add(classPropModel);
                }
            }

//            Set<Resource> subjects = new HashSet<>();
//            subjects.add(subject);
//            subjects.forEach(subject -> {
//                if (subject.getURI().contains("http://dbpedia.org/resource")) {
//                    String resourceProperties =
//                            dbpediasparqlClient.sparqlResourceProperties("<".concat(subject.getURI()).concat(">"));
//                    Model resourcesModel = ModelFactory.createDefaultModel();
//                    RDFParser.create().lang(Lang.NTRIPLES).source(new ByteArrayInputStream(resourceProperties.getBytes())).parse(resourcesModel);
//                    model.add(resourcesModel);
//                } else if (subject.getURI().contains("http://dbpedia.org/ontology")) {
//                    String classProperties = dbpediasparqlClient.sparqlClassProperties("<".concat(subject.getURI()).concat(">"));
//                    Model classPropModel = ModelFactory.createDefaultModel();
//                    RDFParser.create().lang(Lang.NTRIPLES).source(new ByteArrayInputStream(classProperties.getBytes())).parse(classPropModel);
//                    model.add(classPropModel);
//                }
//            });
            FileOutputStream file2 = new FileOutputStream(filePath);
            write(file2, model, RDFFormat.NTRIPLES);

            EntryRepository entryRepository = new EntryRepository();
            entryRepository.saveEntry(filePath);
            LOG.debug("saved success ");
        }
        return "success";
    }

    private void filterSuitableNamespaces(Model model) {
        StmtIterator stmtIterator = model.listStatements();
        List<Statement> statements = new ArrayList<>();
        while (stmtIterator.hasNext()) {
            Statement stmt = stmtIterator.nextStatement();
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();
            if (ALLOWED_NAMESPACES.stream().noneMatch(
                    ns -> subject.getNameSpace().startsWith(ns)) ||
                    ALLOWED_NAMESPACES.stream().noneMatch(ns2 -> predicate.getNameSpace().startsWith(ns2)) || ALLOWED_NAMESPACES.stream().noneMatch(
                            ns3 -> ((object instanceof Resource) && ((Resource) object).getNameSpace().startsWith(
                                    ns3)) || object instanceof Literal)) {
                statements.add(stmt);
            }
        }
        model.remove(statements);
    }


    private Set<String> getProcessedTokens(String doc) throws JsonProcessingException {
        Set<String> allTokens = new HashSet<>();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("document", doc);
        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        try {
            BlockingHttpClient blockingHttpClient = getTaskClient();
            String responseBody = blockingHttpClient.retrieve(
                    HttpRequest.POST("/process-doc", jsonString).header("Content-Type", "application/json").accept(
                            MediaType.of(MediaType.APPLICATION_JSON)));
            JsonNode actualObj = mapper.readTree(responseBody);
            ObjectReader reader = mapper.readerFor(new TypeReference<Set<String>>() {
            });
            allTokens.addAll(reader.<Set<String>>readValue(actualObj.get("Entities")));
            allTokens.addAll(reader.<Set<String>>readValue(actualObj.get("Verbs")));
            allTokens.addAll(reader.<Set<String>>readValue(actualObj.get("NounPhrases")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allTokens;
    }

    private BlockingHttpClient getTaskClient() throws MalformedURLException {
        HttpClientConfiguration configuration = new DefaultHttpClientConfiguration();
        configuration.setReadTimeout(Duration.ofSeconds(60));
        return new DefaultHttpClient(new URL("http://localhost:5000"), configuration).toBlocking();
    }
}
