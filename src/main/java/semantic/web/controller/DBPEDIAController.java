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
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semantic.web.client.DBPEDIAClient;
import semantic.web.client.DBPEDIASPARQLClient;
import semantic.web.helper.*;
import semantic.web.nlp.NLPUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;

@Controller("/dbpedia")
public class DBPEDIAController {
    private static final Logger LOG = LoggerFactory.getLogger(DBPEDIAController.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private final DBPEDIAClient dbpediaClient;

    private final DBPEDIASPARQLClient dbpediasparqlClient;

//    private NLPProcessor nlpProcessor;

    public DBPEDIAController(DBPEDIAClient dbpediaClient, DBPEDIASPARQLClient dbpediasparqlClient) {
        this.dbpediaClient = dbpediaClient;
        this.dbpediasparqlClient = dbpediasparqlClient;
//        this.nlpProcessor = new NLPProcessor();
    }


    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    List<EntryResult> dbpediaLookUp(TextAnalyzeRequest request) {
        List<EntryResult> results = new ArrayList<>();
//        CoreDocument document = nlpProcessor.analyzeText(request.getText());
//        List<CoreSentence> sentences = nlpProcessor.getSentences(document);
//        for (CoreSentence sentence : sentences) {
//            Runnable runnableTask = () -> {
//            List<CoreLabel> tokens = nlpProcessor.getTokens(sentence);
//            tokens = NLPUtil.detectQuotes(tokens);
//            tokens = NLPUtil.removePunctuation(tokens);
//            tokens = NLPUtil.removeStopWords(tokens);
//            NLPUtil.addTermsIntoTokens(sentence, tokens);
//            for (CoreLabel token : tokens) {
//                String stemmedToken = nlpProcessor.stemToken(token);
//                Flowable<DbPediaLookUpResult> DBPEDIAResult = dbpediaClient.queryDBPEDIA(stemmedToken.toLowerCase(Locale.ENGLISH));
//                EntryResult entryResult = new EntryResult();
//                entryResult.setDbPediaLookUpResult(DBPEDIAResult.blockingFirst());
//                entryResult.setToken(stemmedToken);
//                results.add(entryResult);
//            }
//            };
//            executor.execute(runnableTask);
//        }

        return results;
    }

    @Post(value = "/sparql", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    List<DbPediaSparqlControllerResponse> dbpediaSparql(TextAnalyzeRequest request) throws JsonProcessingException {
        List<DbPediaSparqlControllerResponse> response = new ArrayList<>();
        Set<String> processedTokens = getProcessedTokens(request.getText());
        // todo:
        processedTokens = NLPUtil.removePunctuationAndStopWords(processedTokens);
//        for (String token : processedTokens) {
            Flowable<DbPediaSparqlResponse> DBPEDIAResult = dbpediasparqlClient.sparqlQueryDBPEDIA("device");
            DbPediaSparqlResult sparqlResult = DBPEDIAResult.blockingFirst().getResults();
            List<DbPediaSparqlBinding> bindings = sparqlResult.getBindings();
            DbPediaSparqlControllerResponse dbPediaSparqlControllerResponse = new DbPediaSparqlControllerResponse();
            dbPediaSparqlControllerResponse.setToken("device");
            for (DbPediaSparqlBinding result : bindings) {
                dbPediaSparqlControllerResponse.getClasses().add(result.getType().getValue());
                dbPediaSparqlControllerResponse.getRelatedResources().add(result.getResource().getValue());
            }
            response.add(dbPediaSparqlControllerResponse);
//        }
        return response;
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
