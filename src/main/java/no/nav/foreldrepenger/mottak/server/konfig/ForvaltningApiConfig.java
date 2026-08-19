package no.nav.foreldrepenger.mottak.server.konfig;

import static no.nav.foreldrepenger.mottak.server.konfig.ApiConfig.getApplicationProperties;

import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.mottak.server.forvaltning.ForvaltningRestTjeneste;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;
import no.nav.vedtak.openapi.OpenApiUtils;
import no.nav.vedtak.server.rest.ForvaltningAuthorizationFilter;
import no.nav.vedtak.server.rest.AuthenticationFilter;
import no.nav.vedtak.server.rest.FpRestJacksonFeature;

@ApplicationPath(ForvaltningApiConfig.API_URI)
public class ForvaltningApiConfig extends ResourceConfig {
    public static final String API_URI = "/forvaltning/api";

    private static final Environment ENV = Environment.current();

    public ForvaltningApiConfig() {
        register(AuthenticationFilter.class);
        register(FpRestJacksonFeature.class);
        register(ForvaltningAuthorizationFilter.class); // Autorisering – drift
        registerOpenApi();
        registerClasses(getForvaltningKlasser());
        setProperties(getApplicationProperties());
    }

    public static Set<Class<?>> getForvaltningKlasser() {
        return Set.of(ProsessTaskRestTjeneste.class, ForvaltningRestTjeneste.class);
    }

    private void registerOpenApi() {
        OpenApiUtils.setupOpenApi("Fpmottak - mottak av dokumenter via kafka og journalføringsoppgaver",
            ENV.getProperty("context.path", "/fpmottak"), getForvaltningKlasser(), this);
        register(OpenApiResource.class);
    }
}
