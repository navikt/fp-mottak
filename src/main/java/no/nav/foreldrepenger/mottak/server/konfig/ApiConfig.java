package no.nav.foreldrepenger.mottak.server.konfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import no.nav.vedtak.server.rest.GeneralRestExceptionMapper;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.mottak.journalføring.api.FerdigstillJournalføringRestTjeneste;
import no.nav.foreldrepenger.mottak.journalføring.api.JournalføringRestTjeneste;
import no.nav.vedtak.server.rest.AuthenticationFilter;
import no.nav.vedtak.server.rest.FpRestJacksonFeature;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends ResourceConfig {

    public static final String API_URI = "/api";

    public ApiConfig() {
        register(AuthenticationFilter.class);
        register(FpRestJacksonFeature.class);
        register(MultiPartFeature.class); // Multipart upload mellomlagring
        registerClasses(getApplicationClasses());
        setProperties(getApplicationProperties());
    }

    static Set<Class<?>> getApplicationClasses() {
        return Set.of(
            FerdigstillJournalføringRestTjeneste.class,
            JournalføringRestTjeneste.class
        );
    }

    static Map<String, Object> getApplicationProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }
}
