package no.nav.foreldrepenger.mottak.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@Provider
public class JacksonJsonConfig implements ContextResolver<ObjectMapper> {

    private static final ObjectMapper MAPPER = DefaultJsonMapper.getObjectMapper();

    public JacksonJsonConfig() {
        // CDI
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return MAPPER;
    }

}
