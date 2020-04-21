package semantic.web.nlp;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreSentence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NLPUtil {

    private static LanguageUtil languageUtil = new LanguageUtil();

    public static List<CoreLabel> removePunctuation(List<CoreLabel> tokens) {
        List<CoreLabel> cleanedList = new ArrayList<>();
        for (CoreLabel token : tokens) {
            if (!languageUtil.getPunctuations().contains(token.value())) {
                cleanedList.add(token);
            }
        }
        return cleanedList;
    }

    public static List<CoreLabel> removeStopWords(List<CoreLabel> tokens) {
        List<CoreLabel> cleanedList = new ArrayList<>();
        for (CoreLabel token : tokens) {
            if (!languageUtil.getStopWords().contains(token.value().toLowerCase(Locale.ENGLISH))) {
                cleanedList.add(token);
            }
        }
        return cleanedList;
    }

    public static List<CoreLabel> detectQuotes(List<CoreLabel> tokens) {
        List<Integer> quoteTokens = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            CoreLabel token = tokens.get(i);
            if ("\"".equals(token.value()) || 3072 == token.value().hashCode() || 1248 == token.value().hashCode()) {
                quoteTokens.add(i);
            }
        }
        if (quoteTokens.size() % 2 != 0)
            return tokens;
        for (int i = 0; i < quoteTokens.size(); i += 2) {
            int quoteStartPosition = quoteTokens.get(i);
            ;
            int quoteEndPosition = quoteTokens.get(i + 1);
            CoreLabel coreLabel = new CoreLabel();
            String labelValue = "";
            for (int j = quoteStartPosition + 1; j < quoteEndPosition; j++) {
                labelValue += tokens.get(j).value() + " ";
            }
            coreLabel.setValue(labelValue);
            coreLabel.setLemma(labelValue);
            tokens.add(coreLabel);
        }

        return tokens;

    }

    public static void addTermsIntoTokens(CoreSentence sentence, List<CoreLabel> tokens) {
        Set<String> terms = languageUtil.getTerms();
        for (String term : terms) {
            if (sentence.text().contains(term)) {
                CoreLabel coreLabel = new CoreLabel();
                coreLabel.setValue(term);
                coreLabel.setLemma(term);
                tokens.add(coreLabel);
            }

        }
    }

}
