package no.nav.foreldrepenger.mottak.server.konfig;


import no.nav.foreldrepenger.mottak.server.healthcheck.HealthCheckRestService;

import no.nav.foreldrepenger.mottak.server.metrics.PrometheusRestService;

import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;

@ApplicationPath(InternalApiConfig.API_URI)
public class InternalApiConfig extends ResourceConfig {

    public static final String API_URI = "/internal";

    public InternalApiConfig() {
        register(HealthCheckRestService.class);
        register(PrometheusRestService.class);
    }
}
