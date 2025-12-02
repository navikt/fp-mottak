package no.nav.foreldrepenger.mottak.mottak.felles;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema;

class SøknadInnholdTest {



    @Test
    void skal_få_samme_dato_tilbake() {
        LocalDate now = LocalDate.now();
        LocalDate dato1 = now.minusDays(1);
        LocalDate dato2 = now.minusMonths(1);
        LocalDate dato3 = now.plusDays(1);

        var søknadInnhold = new SøknadInnhold("123", BehandlingTema.ENGANGSSTØNAD_FØDSEL, now, now.atStartOfDay());
        søknadInnhold.leggTilAdopsjonsbarnFødselsdatoer(Arrays.asList(now, dato1, dato2));
        søknadInnhold.setFødselsdato(dato1);
        søknadInnhold.setTermindato(dato3);
        søknadInnhold.setOmsorgsovertakelsesdato(dato2);

        assertThat(søknadInnhold.getAdopsjonsbarnFødselsdatoer()).isEqualTo(Arrays.asList(now, dato1, dato2));
        assertThat(søknadInnhold.getTermindato()).hasValueSatisfying(s -> assertThat(s).isEqualTo(dato3));
        assertThat(søknadInnhold.getOmsorgsovertakelsesdato()).hasValueSatisfying(s -> assertThat(s).isEqualTo(dato2));
        assertThat(søknadInnhold.getFødselsdato()).hasValueSatisfying(s -> assertThat(s).isEqualTo(dato1));
    }

    @Test
    void skal_kunne_sette_rolle_og_hente_ut_igjen() {
        var søknadInnhold = new SøknadInnhold("123", BehandlingTema.SVANGERSKAPSPENGER, LocalDate.now(), null);
        søknadInnhold.setBrukerRolle("MOR");
        assertThat(søknadInnhold.getBrukerRolle()).hasValue("MOR");
    }


    @Test
    void skal_returnere_tom_optional_når_inntektsmelding_startdato_ikke_er_satt() {
        var søknadInnhold = new SøknadInnhold("123", BehandlingTema.FORELDREPENGER_FØDSEL, null, null);
        assertThat(søknadInnhold.getFørsteFraværsdato()).isEmpty();
    }

}
