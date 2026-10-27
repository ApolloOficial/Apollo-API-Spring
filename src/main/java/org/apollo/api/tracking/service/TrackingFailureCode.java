package org.apollo.api.tracking.service;
import com.mongodb.MongoException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoTimeoutException;
import javax.net.ssl.SSLException;
public final class TrackingFailureCode {
    private TrackingFailureCode() {}
    public static String classify(Throwable failure) {
        boolean timeout = false;
        boolean dns = false;
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SSLException) return "MONGO_TLS_FAILED";
            if (cause instanceof MongoSecurityException) return "MONGO_AUTHENTICATION_FAILED";
            if (cause instanceof MongoException mongo) {
                if (mongo.getCode() == 13) return "MONGO_PERMISSION_DENIED";
                if (mongo.getCode() == 18) return "MONGO_AUTHENTICATION_FAILED";
                if (mongo.getCode() == 121) return "MONGO_SCHEMA_REJECTED";
            }
            timeout |= cause instanceof MongoTimeoutException;
            dns |= cause instanceof java.net.UnknownHostException
                    || cause instanceof javax.naming.NamingException;
        }
        return dns ? "MONGO_DNS_FAILED" : timeout ? "MONGO_CONNECTION_TIMEOUT" : "TRACKING_PERSISTENCE_FAILED";
    }
}
