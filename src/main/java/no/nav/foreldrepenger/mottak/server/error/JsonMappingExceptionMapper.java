package no.nav.foreldrepenger.mottak.server.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import no.nav.vedtak.exception.TekniskException;

public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonMappingExceptionMapper.class);

    @Override
    public Response toResponse(JsonMappingException exception) {
        var feil = new TekniskException("FP-252294", "JSON-mapping feil", exception);
        LOG.warn(feil.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(new FeilDto(feil.getMessage())).type(MediaType.APPLICATION_JSON).build();
    }

}
