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
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
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

import java.io.*;
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
        LOG.info(String.format("Processed tokens: %s", processedTokens));
        JsonObject jsonObject = tokenProcessOperation(processedTokens, entryWriter);
        LOG.info(String.format("saved success: %s ", jsonObject.toString()));
        return jsonObject.toString();
    }

    private JsonObject tokenProcessOperation(Set<String> processedTokens, EntryWriter entryWriter) {
        JsonObject resultJson = new JsonObject();
        for (String token : processedTokens) {
            try {
                Model rootModel = ModelFactory.createDefaultModel();
                LOG.info(String.format("Token: %s", token));
                if (!processedTokenMap.contains(token) || (token.startsWith("the ") && !processedTokenMap.contains(token.substring(3)))) {
                    JsonArray tokenUris = new JsonArray();
                    writeDbpediaRdfTypeResultsToModel(token, rootModel);
                    resultJson.put(token, tokenUris);
                    StmtIterator stmtIterator = rootModel.listStatements();
                    Model tempRootModel = ModelFactory.createDefaultModel();
                    tempRootModel.add(rootModel);
                    boolean hasResult = false;
                    while (stmtIterator.hasNext()) {
                        hasResult = true;
                        Statement stmt = stmtIterator.nextStatement();
                        if (stmt.getObject() instanceof Resource) {
                            Resource resource = (Resource) stmt.getObject();
                            if (stmt.getSubject().getURI().startsWith("http://dbpedia.org/") && stmt.getPredicate().getURI().equals(
                                    "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && resource.getURI().startsWith("http://dbpedia.org/ontology")) {
                                tokenUris.add(resource.getURI());
                            }
                        }
                        // örnek beatles ın data ve object propertyleri
                        writeDbPediaResourceResultsToModel(tempRootModel, stmt);
                        // örnek beatles ın üst sınıflarının data ve object propertyleri
                        writeDbPediaClassHierarchiesToModel(tempRootModel, stmt);
                    }
                    rootModel.add(tempRootModel);
                    if (hasResult) {
                        processedTokenMap.add(token.startsWith("the ") ? token.substring(3) : token);
                    }
                    writeModelToNeo4j(entryWriter, token, rootModel);
                }
            } catch (Exception e) {
                LOG.error(String.format("could not processed token : %s ", token), e);
            }
        }
        return resultJson;
    }

    private void writeDbPediaClassHierarchiesToModel(Model tempRootModel, Statement stmt) {
        RDFNode object = stmt.getObject();
        if (object instanceof Resource) {
        LOG.info("Found class...");
        // todo: s ile o aynı ise temizle
        String classHierarchy = dbpediasparqlClient.sparqlClassHierarchy(
                "<".concat(((Resource) object).getURI()).concat(">"));
            Model classModel = ModelFactory.createDefaultModel();
            RDFParser.create().lang(Lang.NTRIPLES).source(new ByteArrayInputStream(classHierarchy.getBytes())).parse(classModel);
            filterSuitableNamespaces(classModel);

            StmtIterator stmtIterator2 = classModel.listStatements();
            Model tempClassModel = ModelFactory.createDefaultModel();
            tempClassModel.add(classModel);
            while (stmtIterator2.hasNext()) {
                Statement statement = stmtIterator2.nextStatement();
                Resource classSubj = statement.getSubject();
                LOG.info(String.format("Class subject URI: %s", classSubj.getURI()));
                String classProperties = dbpediasparqlClient.sparqlClassProperties(
                        "<".concat(classSubj.getURI()).concat(">"));
                Model classPropModel = ModelFactory.createDefaultModel();
                RDFParser.create().lang(Lang.NTRIPLES).source(new ByteArrayInputStream(classProperties.getBytes())).parse(classPropModel);
                tempClassModel.add(classPropModel);
            }
            tempRootModel.add(tempClassModel);
        }
    }

    private void writeDbPediaResourceResultsToModel(Model tempRootModel, Statement stmt) {
        Resource subject = stmt.getSubject();
        LOG.info(String.format("Subject URI: %s", subject.getURI()));
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
    }

    private void writeDbpediaRdfTypeResultsToModel(String token, Model model) {
        String rdfTypeResults = dbpediasparqlClient.sparqlRdfType(token);
        Model rdfTypeModel = ModelFactory.createDefaultModel();
        RDFParser.create().lang(Lang.NTRIPLES).source(new ByteArrayInputStream(rdfTypeResults.getBytes())).parse(model);
        model.add(rdfTypeModel);
        filterSuitableNamespaces(model);
    }

    private void writeModelToNeo4j(EntryWriter entryWriter, String token, Model model) throws FileNotFoundException {
        String filePath = entryWriter.generateFilePath(token);
        FileOutputStream fileOs = new FileOutputStream(filePath);
        File file = new File(filePath);
        write(fileOs, model, RDFFormat.NTRIPLES);
        LOG.info(String.format("File has written: %s", filePath));
        if (file.length() > 0) {
            LOG.info("Triples has written to neo4j");
            EntryRepository entryRepository = new EntryRepository();
            entryRepository.saveEntry(filePath);
        }
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
