package semantic.web.client;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Flowable;
import semantic.web.client.configuration.DBPediaConfiguration;
import semantic.web.helper.DbPediaLookUpResult;

@Client(DBPediaConfiguration.DBPEDIA_API_URL)
public interface DBPEDIAClient {

    @Get("/api/search/KeywordSearch?MaxHits=1&QueryString={text}")
    Flowable<DbPediaLookUpResult> queryDBPEDIA(String text);
}
