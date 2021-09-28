package core.apis.last;

import dao.exceptions.ChuuServiceException;

import java.io.InputStream;
import java.util.Properties;

public class LastFMFactory {
    private static final String apikey;
    private static final String secret;

    static {
        Properties properties = new Properties();
        try (InputStream in = LastFMFactory.class.getResourceAsStream("/" + "all.properties")) {
            properties.load(in);
            apikey = properties.getProperty("LASTFM_APIKEY");
            secret = properties.getProperty("LASTFM_APISECRET");
        } catch (Exception e) {
            throw new ChuuServiceException(e);
        }
    }

    private LastFMFactory() {
    }

    public static ConcurrentLastFM getNewInstance() {
        return new ConcurrentLastFM(apikey, secret);


    }
}
