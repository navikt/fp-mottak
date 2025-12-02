package no.nav.foreldrepenger.mottak.mottak.tjeneste;

import static no.nav.foreldrepenger.mottak.mottak.tjeneste.Destinasjon.ForsendelseStatus.FPSAK;

public record Destinasjon(ForsendelseStatus system, String saksnummer) {

    public static Destinasjon GOSYS = new Destinasjon(ForsendelseStatus.GOSYS, null);
    public static Destinasjon FPSAK_UTEN_SAK = new Destinasjon(FPSAK, null);

    public enum ForsendelseStatus { GOSYS, FPSAK }
}
