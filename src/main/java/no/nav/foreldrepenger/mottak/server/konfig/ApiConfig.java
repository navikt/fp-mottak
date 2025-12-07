package no.nav.foreldrepenger.mottak.server.konfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.mottak.journalføring.api.FerdigstillJournalføringRestTjeneste;
import no.nav.foreldrepenger.mottak.journalføring.api.JournalføringRestTjeneste;
import no.nav.foreldrepenger.mottak.leesah.migrer.MigrerHendelseRestTjeneste;
import no.nav.foreldrepenger.mottak.server.JacksonJsonConfig;
import no.nav.foreldrepenger.mottak.server.error.ConstraintViolationMapper;
import no.nav.foreldrepenger.mottak.server.error.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.mottak.server.error.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.mottak.server.error.JsonParseExceptionMapper;
import no.nav.foreldrepenger.mottak.server.sikkerhet.AuthenticationFilter;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends ResourceConfig {

    public static final String API_URI = "/api";

    public ApiConfig() {
        registerClasses(getFellesConfigClasses());
        register(MultiPartFeature.class); // Multipart upload mellomlagring
        registerClasses(getApplicationClasses());
        setProperties(getApplicationProperties());
    }

    static Set<Class<?>> getApplicationClasses() {
        return Set.of(
            FerdigstillJournalføringRestTjeneste.class,
            JournalføringRestTjeneste.class,
            MigrerHendelseRestTjeneste.class
        );
    }

    static Set<Class<?>> getFellesConfigClasses() {
        return  Set.of(
            AuthenticationFilter.class, // Autentisering
            ConstraintViolationMapper.class, // Exception handlers
            JsonMappingExceptionMapper.class, // Exception handlers
            JsonParseExceptionMapper.class, // Exception handlers
            GeneralRestExceptionMapper.class, // Exception handlers
            JacksonJsonConfig.class // Json
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
