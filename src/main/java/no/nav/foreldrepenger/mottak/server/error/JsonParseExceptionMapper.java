package no.nav.foreldrepenger.mottak.server.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonParseExceptionMapper.class);

    @Override
    public Response toResponse(JsonParseException e) {
        LOG.warn("Feil ved parsing av json", e);
        return Response.status(Response.Status.BAD_REQUEST).entity(new FeilDto(e.getMessage())).type(MediaType.APPLICATION_JSON).build();
    }
}
