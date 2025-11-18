package no.nav.foreldrepenger.mottak.server.error;

public record FeltFeilDto(String navn, String melding, String metainformasjon) {

    public FeltFeilDto(String navn, String melding) {
        this(navn, melding, null);
    }
}
