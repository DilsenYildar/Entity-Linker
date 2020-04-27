package semantic.web.nlp;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LanguageUtil {

    private Set<String> punctuations;

    private Set<String> stopWords;

    private Set<String> terms;

    public LanguageUtil() {
        this.punctuations = createPunctuations();
        this.stopWords = createDictionary("stop_words.csv");
        this.terms = createDictionary("terms.csv");
    }

    private Set<String> createPunctuations() {
        return new HashSet<>(
                Arrays.asList(".", ",", "?", "!", "'", ";", "\"", ":", "-", "_", "–", "—", "(", ")", "[", "]", "...",
                        "``", "''"));
    }

    public Set<String> getStopWords() {
        return stopWords;
    }

    public Set<String> createDictionary(String filename) {
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:" + filename);
        return readCSV(resource);
    }

    private Set<String> readCSV(Optional<URL> resource) {
        HashSet<String> termsDictionary = new HashSet<>();
        Path stopWordsPath = null;
        try {
            stopWordsPath = Paths.get(resource.get().toURI());
            List<String> lines = Files.readAllLines(stopWordsPath, StandardCharsets.UTF_8);
            termsDictionary.addAll(Arrays.asList(lines.get(0).split(",")));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return termsDictionary;
    }


    public Set<String> getPunctuations() {
        return punctuations;
    }

    public Set<String> getTerms() {
        return terms;
    }
}
