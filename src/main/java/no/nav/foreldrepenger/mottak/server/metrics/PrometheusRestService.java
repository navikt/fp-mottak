package no.nav.foreldrepenger.mottak.server.metrics;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static no.nav.vedtak.log.metrics.MetricsUtil.REGISTRY;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import io.swagger.v3.oas.annotations.Operation;

@Path("/metrics")
@ApplicationScoped
public class PrometheusRestService {

    @GET
    @Path("/prometheus")
    @Produces(TEXT_PLAIN)
    public String prometheus() {
        return REGISTRY.scrape();
    }
}
