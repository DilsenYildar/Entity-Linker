package semantic.web.client.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

@ConfigurationProperties(value = NLPProcessorConfiguration.PREFIX)
@Requires(property = NLPProcessorConfiguration.PREFIX)
public class NLPProcessorConfiguration {

    public static final String PREFIX = "nlp";
    public static final String NLP_API_URL = "localhost:5000";

}
