package no.nav.foreldrepenger.mottak.mottak.felles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;

import no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.Tema;
import no.nav.vedtak.felles.prosesstask.api.CommonTaskProperties;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

public class MottakMeldingDataWrapper {

    public static final String ARKIV_ID_KEY = "arkivId";
    public static final String AKTØR_ID_KEY = "aktoerId";
    public static final String SAKSNUMMER_KEY = CommonTaskProperties.SAKSNUMMER;
    public static final String TEMA_KEY = "tema";
    public static final String BEHANDLINGSTEMA_KEY = "behandlingstema";
    public static final String DOKUMENTTYPE_ID_KEY = "dokumentTypeId";
    public static final String STRUKTURERT_DOKUMENT = "strukturert.dokument";
    public static final String FORSENDELSE_MOTTATT_TIDSPUNKT_KEY = "forsendelse.mottatt.tidspunkt";
    public static final String JOURNAL_ENHET = "journalforende.enhet";
    public static final String EKSTERN_REFERANSE = "eksternreferanse";

    private final ProsessTaskData prosessTaskData;

    public MottakMeldingDataWrapper(ProsessTaskData eksisterendeData) {
        this.prosessTaskData = eksisterendeData;
    }

    public ProsessTaskData getProsessTaskData() {
        return prosessTaskData;
    }

    public MottakMeldingDataWrapper nesteSteg(TaskType stegnavn) {
        return nesteSteg(stegnavn, LocalDateTime.now());
    }

    public MottakMeldingDataWrapper nesteSteg(TaskType stegnavn, LocalDateTime nesteKjøringEtter) {
        var nesteStegProsessTaskData = ProsessTaskData.forTaskType(stegnavn);
        nesteStegProsessTaskData.setNesteKjøringEtter(nesteKjøringEtter);

        String sekvensnummer = getProsessTaskData().getSekvens();
        if (sekvensnummer != null) {
            long sekvens = Long.parseLong(sekvensnummer);
            sekvensnummer = Long.toString(sekvens + 1);
            nesteStegProsessTaskData.setSekvens(sekvensnummer);
        }

        var neste = new MottakMeldingDataWrapper(nesteStegProsessTaskData);
        neste.copyData(this);
        return neste;
    }

    private void copyData(MottakMeldingDataWrapper fra) {
        this.addProperties(fra.getProsessTaskData().getProperties());
        this.setPayload(fra.getProsessTaskData().getPayloadAsString());
        this.getProsessTaskData().setGruppe(fra.getProsessTaskData().getGruppe());
    }

    private void addProperties(Properties newProps) {
        prosessTaskData.getProperties().putAll(newProps);
    }

    public Properties hentAlleProsessTaskVerdier() {
        return prosessTaskData.getProperties();
    }

    public Long getId() {
        return prosessTaskData.getId();
    }

    public BehandlingTema getBehandlingTema() {
        return BehandlingTema.fraKode(prosessTaskData.getPropertyValue(BEHANDLINGSTEMA_KEY));
    }

    public void setBehandlingTema(BehandlingTema behandlingTema) {
        prosessTaskData.setProperty(BEHANDLINGSTEMA_KEY, behandlingTema.getKode());
    }

    public Tema getTema() {
        return Tema.fraKode(prosessTaskData.getPropertyValue(TEMA_KEY));
    }

    public void setTema(Tema tema) {
        prosessTaskData.setProperty(TEMA_KEY, tema.getKode());
    }

    public String getArkivId() {
        return prosessTaskData.getPropertyValue(ARKIV_ID_KEY);
    }

    public void setArkivId(String arkivId) {
        prosessTaskData.setProperty(ARKIV_ID_KEY, arkivId);
    }

    public Optional<String> getSaksnummer() {
        return Optional.ofNullable(prosessTaskData.getSaksnummer());
    }

    public void setSaksnummer(String saksnummer) {
        Optional.ofNullable(saksnummer).ifPresent(prosessTaskData::setSaksnummer);
    }

    public Optional<DokumentTypeId> getDokumentTypeId() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(DOKUMENTTYPE_ID_KEY)).map(DokumentTypeId::fraKodeDefaultUdefinert);
    }

    public void setDokumentTypeId(DokumentTypeId dokumentTypeId) {
        prosessTaskData.setProperty(DOKUMENTTYPE_ID_KEY, dokumentTypeId.getKode());
    }

    public final LocalDate getForsendelseMottatt() {
        return getForsendelseMottattTidspunkt().map(LocalDateTime::toLocalDate).orElse(null);
    }

    public Optional<LocalDateTime> getForsendelseMottattTidspunkt() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(FORSENDELSE_MOTTATT_TIDSPUNKT_KEY))
            .map(p -> LocalDateTime.parse(p, DateTimeFormatter.ISO_DATE_TIME));
    }

    public void setForsendelseMottattTidspunkt(LocalDateTime forsendelseMottattTidspunkt) {
        prosessTaskData.setProperty(FORSENDELSE_MOTTATT_TIDSPUNKT_KEY, forsendelseMottattTidspunkt.format(DateTimeFormatter.ISO_DATE_TIME));
    }

    public Optional<String> getJournalførendeEnhet() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(JOURNAL_ENHET));
    }

    public void setJournalførendeEnhet(String enhet) {
        prosessTaskData.setProperty(JOURNAL_ENHET, enhet);
    }

    public Optional<String> getEksternReferanseId() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(EKSTERN_REFERANSE));
    }

    public void setEksternReferanseId(String enhet) {
        prosessTaskData.setProperty(EKSTERN_REFERANSE, enhet);
    }

    public void setPayload(String payload) {
        prosessTaskData.setPayload(payload);
    }

    public Optional<String> getPayloadAsString() {
        return Optional.ofNullable(prosessTaskData.getPayloadAsString());
    }

    public Optional<String> getAktørId() {
        return Optional.ofNullable(prosessTaskData.getAktørId());
    }

    public void setAktørId(String aktørId) {
        prosessTaskData.setAktørId(aktørId);
    }

    public Optional<Boolean> erStrukturertDokument() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(STRUKTURERT_DOKUMENT)).map(Boolean::parseBoolean);
    }

    public void setStrukturertDokument(Boolean erStrukturertDokument) {
        prosessTaskData.setProperty(STRUKTURERT_DOKUMENT, String.valueOf(erStrukturertDokument));
    }


}
