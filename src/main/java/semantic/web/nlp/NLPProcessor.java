package semantic.web.nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.List;
import java.util.Properties;

public class NLPProcessor {

    private StanfordCoreNLP pipeline;

    public NLPProcessor() {
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        // build pipeline
        this.pipeline = new StanfordCoreNLP(props);
    }

    public CoreDocument analyzeText(String text) {
        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);
        return document;
    }

    public List<CoreSentence> getSentences(CoreDocument document) {
        return document.sentences();
    }

    public List<CoreLabel> getTokens(CoreSentence sentence) {
        return sentence.tokens();
    }

    public String stemToken(CoreLabel token) {
        return token.get(CoreAnnotations.LemmaAnnotation.class).trim();
    }


}
