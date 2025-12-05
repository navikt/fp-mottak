package no.nav.foreldrepenger.mottak.server.konfig;

import static no.nav.foreldrepenger.mottak.server.konfig.ApiConfig.getApplicationProperties;
import static no.nav.foreldrepenger.mottak.server.konfig.ApiConfig.getFellesConfigClasses;

import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.mottak.server.forvaltning.ForvaltningRestTjeneste;
import no.nav.foreldrepenger.mottak.server.konfig.swagger.OpenApiUtils;
import no.nav.foreldrepenger.mottak.server.sikkerhet.ForvaltningAuthorizationFilter;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ForvaltningApiConfig.API_URI)
public class ForvaltningApiConfig extends ResourceConfig {
    public static final String API_URI = "/forvaltning/api";

    public ForvaltningApiConfig() {
        register(ForvaltningAuthorizationFilter.class); // Autorisering – drift
        registerClasses(getFellesConfigClasses());
        registerOpenApi();
        registerClasses(getForvaltningKlasser());
        setProperties(getApplicationProperties());
    }

    private static Set<Class<?>> getForvaltningKlasser() {
        return Set.of(
            ProsessTaskRestTjeneste.class,
            ForvaltningRestTjeneste.class
        );
    }

    private void registerOpenApi() {
        OpenApiUtils.openApiConfigFor("Fpmottak - mottak av dokumenter via kafka og journalføringsoppgaver", this)
            .registerClasses(getForvaltningKlasser())
            .buildOpenApiContext();
        register(OpenApiResource.class);
    }
}
