package semantic.web.file;

import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class EntryWriter {

    public String writeFiles(String context) throws IOException {
        String filePath = generateFilePath();
        try (FileWriter myWriter = new FileWriter(filePath)) {
            myWriter.write(context);
        }
        return filePath;
    }

    private String generateFilePath() {
        return System.getProperty("user.home") + "/neo4j/data/" + UUID.randomUUID().toString() + ".nt";
    }

}
