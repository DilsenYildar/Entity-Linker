package semantic.web.controller;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semantic.web.client.DBPEDIAClient;
import semantic.web.client.DBPEDIASPARQLClient;
import semantic.web.helper.*;
import semantic.web.nlp.NLPProcessor;
import semantic.web.nlp.NLPUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller("/dbpedia")
public class DBPEDIAController {
    private static final Logger LOG = LoggerFactory.getLogger(DBPEDIAController.class);

    private final DBPEDIAClient dbpediaClient;

    private final DBPEDIASPARQLClient dbpediasparqlClient;

    private NLPProcessor nlpProcessor;

    public DBPEDIAController(DBPEDIAClient dbpediaClient, DBPEDIASPARQLClient dbpediasparqlClient) {
        this.dbpediaClient = dbpediaClient;
        this.dbpediasparqlClient = dbpediasparqlClient;
        this.nlpProcessor = new NLPProcessor();
    }


    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    List<EntryResult> dbpediaLookUp(TextAnalyzeRequest request) {
        List<EntryResult> results = new ArrayList<>();
        CoreDocument document = nlpProcessor.analyzeText(request.getText());
        List<CoreSentence> sentences = nlpProcessor.getSentences(document);
        for (CoreSentence sentence : sentences) {
//            Runnable runnableTask = () -> {
            List<CoreLabel> tokens = nlpProcessor.getTokens(sentence);
            tokens = NLPUtil.detectQuotes(tokens);
            tokens = NLPUtil.removePunctuation(tokens);
            tokens = NLPUtil.removeStopWords(tokens);
            NLPUtil.addTermsIntoTokens(sentence, tokens);
            for (CoreLabel token : tokens) {
                String stemmedToken = nlpProcessor.stemToken(token);
                Flowable<DbPediaLookUpResult> DBPEDIAResult = dbpediaClient.queryDBPEDIA(stemmedToken.toLowerCase(Locale.ENGLISH));
                EntryResult entryResult = new EntryResult();
                entryResult.setDbPediaLookUpResult(DBPEDIAResult.blockingFirst());
                entryResult.setToken(stemmedToken);
                results.add(entryResult);
            }
//            };
//            executor.execute(runnableTask);
        }

        return results;
    }

    @Post(value = "/sparql", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    List<DbPediaSparqlControllerResponse> dbpediaSparql(TextAnalyzeRequest request) {
        List<DbPediaSparqlControllerResponse> response = new ArrayList<>();
        CoreDocument document = nlpProcessor.analyzeText(request.getText());
        List<CoreSentence> sentences = nlpProcessor.getSentences(document);
        for (CoreSentence sentence : sentences) {
            LOG.debug("Sentence : " + sentence.text());
            List<CoreLabel> tokens = nlpProcessor.getTokens(sentence);
            tokens = NLPUtil.detectQuotes(tokens);
            tokens = NLPUtil.removePunctuation(tokens);
            tokens = NLPUtil.removeStopWords(tokens);
            NLPUtil.addTermsIntoTokens(sentence, tokens);
            LOG.debug("Tokens : " + tokens);
            for (CoreLabel token : tokens) {
                String stemmedToken = nlpProcessor.stemToken(token);
                Flowable<DbPediaSparqlResponse> DBPEDIAResult = dbpediasparqlClient.sparqlQueryDBPEDIA(stemmedToken.toLowerCase(Locale.ENGLISH));
                DbPediaSparqlResult sparqlResult = DBPEDIAResult.blockingFirst().getResults();
                List<DbPediaSparqlBinding> bindings = sparqlResult.getBindings();
                DbPediaSparqlControllerResponse dbPediaSparqlControllerResponse = new DbPediaSparqlControllerResponse();
                dbPediaSparqlControllerResponse.setToken(stemmedToken);
                for (DbPediaSparqlBinding result : bindings) {
                    dbPediaSparqlControllerResponse.getClasses().add(result.getType().getValue());
                    dbPediaSparqlControllerResponse.getRelatedResources().add(result.getResource().getValue());
                }
                response.add(dbPediaSparqlControllerResponse);
            }


        }
        return response;
    }

}
