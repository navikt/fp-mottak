package no.nav.foreldrepenger.mottak.leesah.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.mottak.leesah.domene.HendelsePayload;
import no.nav.foreldrepenger.mottak.leesah.domene.internt.PdlFødselHendelsePayload;



class AktørIdTjenesteTest {

    private static final String GYLDIG = "1000017373893";
    private static final String UGYLDIG = "x000017373893";

    @Test
    void skal_bare_returnere_gyldige_aktørIder() {
        // Arrange
        HendelsePayload hendelse = new PdlFødselHendelsePayload.Builder().aktørIdForeldre(Set.of(GYLDIG, UGYLDIG)).build();

        // Act
        var aktørIderForSortering = AktørIdTjeneste.getAktørIderForSortering(hendelse);

        // Assert
        assertThat(aktørIderForSortering).containsOnly(GYLDIG);
    }
}
