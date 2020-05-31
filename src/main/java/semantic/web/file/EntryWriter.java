package semantic.web.file;

import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class EntryWriter {

    public String writeFiles(String term, String context) throws IOException {
        String filePath = generateFilePath(term);
        try (FileWriter myWriter = new FileWriter(filePath)) {
            myWriter.write(context);
        }
        return filePath;
    }

    public String generateFilePath(String term) {
        return System.getProperty("user.home") + "/neo4j/data/" + term.concat("-").concat(UUID.randomUUID().toString()) + ".nt";
    }

}
