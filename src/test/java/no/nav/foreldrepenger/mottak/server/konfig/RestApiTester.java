package no.nav.foreldrepenger.mottak.server.konfig;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;

public class RestApiTester {

    private static final Set<Class<? extends Annotation>> REST_METHOD_ANNOTATIONS = Set.of(GET.class, POST.class, DELETE.class, PATCH.class, PUT.class);

    static Collection<Method> finnAlleRestMetoder() {
        return finnAlleRestTjenester().stream()
            .map(Class::getDeclaredMethods)
            .flatMap(Arrays::stream)
            .filter(RestApiTester::erMetodenEtRestEndepunkt)
            .collect(Collectors.toSet());
    }

    static Collection<Class<?>> finnAlleJsonSubTypeClasses(Class<?> klasse) {
        var resultat = new ArrayList<Class<?>>();
        if (klasse.isAnnotationPresent(JsonSubTypes.class)) {
            var jsonSubTypes = klasse.getAnnotation(JsonSubTypes.class);
            for (var subtype : jsonSubTypes.value()) {
                resultat.add(subtype.value());
            }
        }
        return resultat;
    }

    private static Set<Class<?>> finnAlleRestTjenester() {
        var resultat = new ArrayList<Class<?>>();
        resultat.addAll(ApiConfig.getApplicationClasses());
        resultat.addAll(ForvaltningApiConfig.getForvaltningKlasser());
        return Set.copyOf(resultat);
    }

    private static boolean erMetodenEtRestEndepunkt(Method method) {
        return REST_METHOD_ANNOTATIONS.stream().anyMatch(method::isAnnotationPresent);
    }
}
