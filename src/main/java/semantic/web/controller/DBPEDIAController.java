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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semantic.web.client.DBPEDIASPARQLClient;
import semantic.web.file.EntryWriter;
import semantic.web.helper.TextAnalyzeRequest;
import semantic.web.repository.EntryRepository;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Controller("/dbpedia")
public class DBPEDIAController {
    private static final Logger LOG = LoggerFactory.getLogger(DBPEDIAController.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private final DBPEDIASPARQLClient dbpediasparqlClient;


    public DBPEDIAController(DBPEDIASPARQLClient dbpediasparqlClient) {
        this.dbpediasparqlClient = dbpediasparqlClient;
    }

    @Post(value = "/sparql", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    String dbpediaSparql(TextAnalyzeRequest request) throws IOException {
        Set<String> processedTokens = getProcessedTokens(request.getText());
        EntryWriter entryWriter = new EntryWriter();
        for (String token : processedTokens) {
            String DBPEDIAResult = dbpediasparqlClient.sparqlQueryDBPEDIA(token);
            // todo: parse http://dbpedia.org/ontology while writing file.
            String filePath = entryWriter.writeFiles(token, DBPEDIAResult);
            File file = new File(filePath);
            // todo: read files by filtering resource and class.

            EntryRepository entryRepository = new EntryRepository();
            entryRepository.saveEntry(filePath);
            LOG.debug("saved success ");
        }
        return "success";
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
