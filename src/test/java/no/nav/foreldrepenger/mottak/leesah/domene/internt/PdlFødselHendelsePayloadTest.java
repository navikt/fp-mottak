package no.nav.foreldrepenger.mottak.leesah.domene.internt;

import no.nav.foreldrepenger.kontrakter.abonnent.v2.AktørIdDto;
import no.nav.foreldrepenger.kontrakter.abonnent.v2.Endringstype;
import no.nav.foreldrepenger.kontrakter.abonnent.v2.pdl.FødselHendelseDto;

import no.nav.foreldrepenger.mottak.leesah.testutilities.HendelseTestDataUtil;

import org.junit.jupiter.api.Test;

import static no.nav.foreldrepenger.mottak.leesah.testutilities.HendelseTestDataUtil.AKTØR_ID_FAR;
import static no.nav.foreldrepenger.mottak.leesah.testutilities.HendelseTestDataUtil.AKTØR_ID_MOR;
import static no.nav.foreldrepenger.mottak.leesah.testutilities.HendelseTestDataUtil.FØDSELSDATO;
import static no.nav.foreldrepenger.mottak.leesah.testutilities.HendelseTestDataUtil.HENDELSE_ID;
import static org.assertj.core.api.Assertions.assertThat;


class PdlFødselHendelsePayloadTest {

    @Test
    void skal_mappe_til_FødselHendelseDto() {
        // Act
        var hendelseWrapperDto = HendelseTestDataUtil.lagFødselsHendelsePayload().mapPayloadTilDto();

        // Assert
        assertThat(hendelseWrapperDto).isNotNull();
        var hendelseDto = hendelseWrapperDto.getHendelse();
        assertThat(hendelseDto.getId()).isEqualTo(FødselHendelseDto.HENDELSE_TYPE + "_" + HENDELSE_ID);
        assertThat(hendelseDto.getHendelsetype()).isEqualTo(FødselHendelseDto.HENDELSE_TYPE);
        assertThat(hendelseDto.getEndringstype()).isEqualTo(Endringstype.OPPRETTET);
        var fødselHendelseDto = (FødselHendelseDto) hendelseDto;
        assertThat(fødselHendelseDto.getFødselsdato()).isEqualTo(FØDSELSDATO);
        assertThat(fødselHendelseDto.getAktørIdForeldre()).containsExactlyInAnyOrder(new AktørIdDto(AKTØR_ID_FAR), new AktørIdDto(AKTØR_ID_MOR));
    }
}
