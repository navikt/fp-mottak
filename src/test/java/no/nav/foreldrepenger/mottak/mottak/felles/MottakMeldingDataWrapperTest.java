package no.nav.foreldrepenger.mottak.mottak.felles;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.Tema;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

class MottakMeldingDataWrapperTest {

    private static final TaskType PROSESSTASK_STEG1 = new TaskType("prosesstask.steg1");
    private static final TaskType PROSESSTASK_STEG2 = new TaskType("prosesstask.steg2");
    private static final TaskType PROSESSTASK_STEG3 = new TaskType("prosesstask.steg3");

    private ProsessTaskData eksisterendeData;
    private MottakMeldingDataWrapper wrapper;

    @BeforeEach
    void setUp() {
        eksisterendeData = ProsessTaskData.forTaskType(PROSESSTASK_STEG1);
        wrapper = new MottakMeldingDataWrapper(eksisterendeData);
    }

    @Test
    void test_kan_opprette_og_kopiere_wrapper_uten_eksisterende_properties() {
        assertThat(wrapper.hentAlleProsessTaskVerdier()).as("Forventer at wrapper i utgangspunktet blir opprettet uten properties").isNotNull();
        var wrapperNesteSteg = wrapper.nesteSteg(PROSESSTASK_STEG2);
        assertThat(wrapper.hentAlleProsessTaskVerdier()).as("").isEqualTo(wrapperNesteSteg.hentAlleProsessTaskVerdier());
    }

    @Test
    void test_beholder_properties_og_payload_fra_forrige_steg() {
        wrapper.setArkivId("arkiv_1234");
        wrapper.setPayload("<xml>test</xml>");
        var wrapperNesteSteg = wrapper.nesteSteg(PROSESSTASK_STEG2);
        assertThat(wrapperNesteSteg.getArkivId()).as("Forventer at arkivId blir med til neste steg.").isEqualTo(wrapper.getArkivId());
        assertThat(wrapperNesteSteg.getProsessTaskData().getPayloadAsString()).as("Forventer at payload også blir kopiert over").isNotNull();
    }

    @Test
    void test_overskriv_eksisterende_property() {
        String INITIELL_ARKIVID = "arkiv_1234";
        String NY_ARKIVID = "nyid_987";
        wrapper.setArkivId(INITIELL_ARKIVID);
        var wrapperNesteSteg = wrapper.nesteSteg(PROSESSTASK_STEG2);
        wrapperNesteSteg.setArkivId(NY_ARKIVID);
        assertThat(wrapperNesteSteg.getArkivId()).as("Forventer at arkivId blir overskrevet med nyTerminbekreftelse verdi.").isEqualTo(NY_ARKIVID);
    }

    @Test
    void test_property_fra_flere_steg_blir_kopiert() {
        String ARKIVID = "arkiv_1234";
        Tema tema = Tema.FORELDRE_OG_SVANGERSKAPSPENGER;
        wrapper.setArkivId(ARKIVID);
        assertThat(wrapper.getArkivId()).as("Forventer at wrapper inneholder properties fra input dataklasse.").isEqualTo(ARKIVID);
        var wrapperAndreSteg = wrapper.nesteSteg(PROSESSTASK_STEG2);
        wrapperAndreSteg.setTema(tema);
        var wrapperTredjeSteg = wrapperAndreSteg.nesteSteg(PROSESSTASK_STEG3);
        assertThat(wrapperTredjeSteg.getArkivId()).as("Forventer at alle properties har blitt med hele veien gjennom flyten.").isEqualTo(ARKIVID);
        assertThat(wrapperTredjeSteg.getTema()).as("Forventer at alle properties har blitt med hele veien gjennom flyten.").isEqualTo(tema);
    }

    @Test
    void test_skal_kunne_sette_behandlingstemakode_og_hente_ut_igjen() {
        wrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        assertThat(wrapper.getBehandlingTema().getKode()).isEqualTo(BehandlingTema.ENGANGSSTØNAD_FØDSEL.getKode());
    }

    @Test
    void test_skal_kunne_sette_dokumenttypeid_og_hente_ut_igjen() {
        wrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        assertThat(wrapper.getDokumentTypeId()).hasValueSatisfying(
            s -> assertThat(s.getKode()).isEqualTo(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getKode()));
    }

    @Test
    void test_skal_kunne_sette_aktørid_og_hente_ut_igjen() {
        final String aktørId = "1";
        wrapper.setAktørId(aktørId);
        assertThat(wrapper.getAktørId()).hasValueSatisfying(it -> assertThat(it).isEqualTo(aktørId));
    }


    @Test
    void test_skal_gi_prosess_task_id() {
        eksisterendeData.setId(1377L);
        var wrapper = new MottakMeldingDataWrapper(eksisterendeData);
        assertThat(wrapper.getId()).isEqualTo(1377L);
    }

    @Test
    void test_skal_gi_null_prosess_task_id_for_log() {
        eksisterendeData.setId(null);
        var wrapper = new MottakMeldingDataWrapper(eksisterendeData);
        assertThat(wrapper.getId()).isNull();
    }

    @Test
    void skal_sette_og_hente_er_strukturert_dokument() {
        assertThat(wrapper.erStrukturertDokument()).isNotPresent();
        wrapper.setStrukturertDokument(true);
        assertThat(wrapper.erStrukturertDokument()).hasValue(true);
    }

    @Test
    void skal_kunne_sette_inn_strukturertDokument_og_hente_ut_igjen() {
        wrapper.setStrukturertDokument(true);
        assertThat(wrapper.erStrukturertDokument()).isPresent();
        assertThat(wrapper.erStrukturertDokument().get()).isTrue();
    }

}
