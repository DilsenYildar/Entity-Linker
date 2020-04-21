package semantic.web.helper;

public class EntryResult {
    private String token;

    private DbPediaLookUpResult dbPediaLookUpResult;

    public DbPediaLookUpResult getDbPediaLookUpResult() {
        return dbPediaLookUpResult;
    }

    public void setDbPediaLookUpResult(DbPediaLookUpResult dbPediaLookUpResult) {
        this.dbPediaLookUpResult = dbPediaLookUpResult;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
