package no.nav.foreldrepenger.mottak.mottak.felles;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema;

class InntektsmeldingInnholdTest {


    @Test
    void skal_kunne_sette_inn_startdatoForeldrepengerPeriode_og_hente_ut_igjen() {
        final LocalDate actualDate = LocalDate.now();
        var im = new InntektsmeldingInnhold(null, BehandlingTema.SVANGERSKAPSPENGER, actualDate, null);
        assertThat(im.getFørsteFraværsdato()).isPresent().hasValueSatisfying(f -> assertThat(f).isEqualTo(actualDate));
    }

    @Test
    void skal_kunne_sette_inn_årsakTilInnsending_og_hente_ut_igjen() {
        final String actualÅrsak = "Endring";
        var im = new InntektsmeldingInnhold(null, BehandlingTema.FORELDREPENGER, null, null);
        im.setÅrsakTilInnsending(actualÅrsak);
        assertThat(im.getÅrsakTilInnsending()).isPresent().hasValueSatisfying(f -> assertThat(f).isEqualTo(actualÅrsak));
    }

}
