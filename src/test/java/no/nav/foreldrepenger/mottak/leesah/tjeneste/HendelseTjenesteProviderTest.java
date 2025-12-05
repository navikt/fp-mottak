package no.nav.foreldrepenger.mottak.leesah.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import no.nav.foreldrepenger.mottak.leesah.domene.HendelsePayload;

import no.nav.foreldrepenger.mottak.leesah.domene.HendelseType;
import no.nav.foreldrepenger.mottak.leesah.pdl.tjeneste.PdlFødselHendelseTjeneste;

import org.junit.jupiter.api.Test;


import no.nav.vedtak.felles.testutilities.cdi.UnitTestLookupInstanceImpl;

class HendelseTjenesteProviderTest {


    private HendelseTjenesteProvider hendelseTjenesteProvider;

    @Test
    void skal_finne_hendelsetjeneste() {
        // Act
        hendelseTjenesteProvider = new HendelseTjenesteProvider(new UnitTestLookupInstanceImpl<>(new PdlFødselHendelseTjeneste()));
        HendelseTjeneste<HendelsePayload> hendelseTjeneste = hendelseTjenesteProvider.finnTjeneste(HendelseType.PDL_FØDSEL_OPPRETTET, "1");

        // Assert
        assertThat(hendelseTjeneste).isNotNull().isInstanceOf(PdlFødselHendelseTjeneste.class);
    }

}
