package no.nav.foreldrepenger.mottak.server.error;

public enum FeilType {
    MANGLER_TILGANG_FEIL,
    TOMT_RESULTAT_FEIL,
    GENERELL_FEIL;

    @Override
    public String toString() {
        return name();
    }
}
