package no.nav.foreldrepenger.mottak.server.sikkerhet;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * AbacAttributtTyper som er i bruk i FPMOTTAK.
 */
public class AppAbacAttributtType {

    public static final AbacAttributtType AKTØR_ID = StandardAbacAttributtType.AKTØR_ID;
    public static final AbacAttributtType FNR = StandardAbacAttributtType.FNR;
    public static final AbacAttributtType JOURNALPOST_ID = StandardAbacAttributtType.JOURNALPOST_ID;

    private AppAbacAttributtType() {
        // utility
    }
}
