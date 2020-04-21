package semantic.web.helper;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class TextAnalyzeRequest {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
