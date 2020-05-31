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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semantic.web.client.DBPEDIASPARQLClient;
import semantic.web.file.EntryWriter;
import semantic.web.helper.TextAnalyzeRequest;
import semantic.web.repository.EntryRepository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;

import static org.apache.jena.riot.RDFDataMgr.write;

@Controller("/dbpedia")
public class DBPEDIAController {
    private static final Logger LOG = LoggerFactory.getLogger(DBPEDIAController.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private final DBPEDIASPARQLClient dbpediasparqlClient;

    private static final Set<String> ALLOWED_NAMESPACES = new HashSet<>(Arrays.asList("http://dbpedia.org", "http://www.w3.org"));

    private static final Set<String> processedTokenMap = new HashSet<>();

    public DBPEDIAController(DBPEDIASPARQLClient dbpediasparqlClient) {
        this.dbpediasparqlClient = dbpediasparqlClient;
    }

    @Post(value = "/sparql", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    String dbpediaSparql(TextAnalyzeRequest request) throws IOException {
        Set<String> processedTokens = getProcessedTokens(request.getText());
        EntryWriter entryWriter = new EntryWriter();
        LOG.debug(String.format("Processed tokens: %s", processedTokens));
        for (String token : processedTokens) {
            try {
                Model model = ModelFactory.createDefaultModel();
                LOG.info(String.format("Token: %s", token));
                if (!processedTokenMap.contains(token) || (token.startsWith("the ") && !processedTokenMap.contains(token.substring(3)))) {
                    String rdfTypeResults = dbpediasparqlClient.sparqlRdfType(token);
                    Model rdfTypeModel = ModelFactory.createDefaultModel();
                    RDFParser.create().lang(Lang.NTRIPLES).source(new ByteArrayInputStream(rdfTypeResults.getBytes())).parse(model);
                    model.add(rdfTypeModel);
                    filterSuitableNamespaces(model);

                    StmtIterator stmtIterator = model.listStatements();
                    Model tempRootModel = ModelFactory.createDefaultModel();
                    tempRootModel.add(model);
                    boolean hasResult = false;
                    while (stmtIterator.hasNext()) {
                        hasResult = true;
                        Statement stmt = stmtIterator.nextStatement();
                        Resource subject = stmt.getSubject();
                        LOG.info(String.format("Subject URI: %s", subject.getURI()));
                        // örnek beatles ın data ve object propertyleri
                        if (subject.getURI().contains("http://dbpedia.org/resource")) {
                            LOG.info("Found resource...");
                            String resourceProperties = dbpediasparqlClient.sparqlResourceProperties(
                                    "<".concat(subject.getURI()).concat(">"));
                            Model resourcesModel = ModelFactory.createDefaultModel();
                            RDFParser.create().lang(Lang.NTRIPLES).source(
                                    new ByteArrayInputStream(resourceProperties.getBytes())).parse(resourcesModel);
                            filterSuitableNamespaces(resourcesModel);
                            tempRootModel.add(resourcesModel);
                        }
                        // örnek beatles ın üst sınıflarının data ve object propertyleri
                        RDFNode object = stmt.getObject();
                        if ((object instanceof Resource)) {
                            LOG.info("Found class...");
                            String classHierarchy = dbpediasparqlClient.sparqlClassHierarchy("<".concat(((Resource) object).getURI()).concat(">"));
                            // todo: s ile o aynı ise temizle
                            Model classModel = ModelFactory.createDefaultModel();
                            RDFParser.create().lang(Lang.NTRIPLES).source(new ByteArrayInputStream(classHierarchy.getBytes())).parse(classModel);
                            filterSuitableNamespaces(classModel);
                            tempRootModel.add(classModel);

                            StmtIterator stmtIterator2 = classModel.listStatements();
                            Model tempClassModel = ModelFactory.createDefaultModel();
                            tempClassModel.add(classModel);
                            while (stmtIterator2.hasNext()) {
                                Statement statement = stmtIterator2.nextStatement();
                                Resource classSubj = statement.getSubject();
                                LOG.info(String.format("Class subject URI: %s", classSubj.getURI()));
                                String classProperties = dbpediasparqlClient.sparqlClassProperties("<".concat(classSubj.getURI()).concat(">"));
                                Model classPropModel = ModelFactory.createDefaultModel();
                                RDFParser.create().lang(Lang.NTRIPLES).source(
                                        new ByteArrayInputStream(classProperties.getBytes())).parse(classPropModel);
                                tempClassModel.add(classPropModel);
                            }
                            tempRootModel.add(tempClassModel);
                        }
                    }
                    if (hasResult) {
                        processedTokenMap.add(token.startsWith("the ") ? token.substring(3) : token);
                    }
                    model.add(tempRootModel);

                    String filePath = entryWriter.generateFilePath(token);
                    FileOutputStream fileOs = new FileOutputStream(filePath);
                    File file = new File(filePath);
                    if (file.length() > 0) {
                        write(fileOs, model, RDFFormat.NTRIPLES);
                        LOG.info(String.format("File has written: %s", filePath));

                        EntryRepository entryRepository = new EntryRepository();
                        entryRepository.saveEntry(filePath);
                    }
                }
            } catch (Exception e) {
                LOG.error(String.format("could not processed token : %s ", token), e);
            }
        }
        LOG.info("saved success ");
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
