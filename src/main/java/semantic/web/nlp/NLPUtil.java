package semantic.web.nlp;

import java.util.*;

public class NLPUtil {

    private static final LanguageUtil languageUtil = new LanguageUtil();

    public static Set<String> removePunctuationAndStopWords(Set<String> tokens) {
        Set<String> cleanedList = new HashSet<>();
        for (String token : tokens) {
            if (!languageUtil.getPunctuations().contains(token) &&
                    !languageUtil.getStopWords().contains(token.toLowerCase(Locale.ENGLISH))) {
                cleanedList.add(token);
            }
        }
        return cleanedList;
    }

}
