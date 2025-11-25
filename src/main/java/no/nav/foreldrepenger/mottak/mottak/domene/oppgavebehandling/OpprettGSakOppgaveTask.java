package no.nav.foreldrepenger.mottak.mottak.domene.oppgavebehandling;

import static no.nav.foreldrepenger.mottak.mottak.behandlendeenhet.EnhetsTjeneste.NK_ENHET_ID;
import static no.nav.foreldrepenger.mottak.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.mottak.felles.MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY;
import static no.nav.foreldrepenger.mottak.mottak.felles.MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY;
import static no.nav.foreldrepenger.mottak.mottak.felles.MottakMeldingDataWrapper.JOURNAL_ENHET;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.mottak.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.mottak.journalføring.oppgave.domene.NyOppgave;
import no.nav.foreldrepenger.mottak.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.mottak.tjeneste.ArkivUtil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * <p>
 * ProsessTask som oppretter en oppgave i GSAK for manuell behandling av
 * tilfeller som ikke kan håndteres automatisk av vedtaksløsningen.
 * <p>
 * </p>
 */
@Dependent
@ProsessTask(value = "integrasjon.gsak.opprettOppgave", prioritet = 2, maxFailedRuns = 2)
public class OpprettGSakOppgaveTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OpprettGSakOppgaveTask.class);

    private final Journalføringsoppgave oppgaverTjeneste;
    private final EnhetsTjeneste enhetsTjeneste;

    @Inject
    public OpprettGSakOppgaveTask(Journalføringsoppgave oppgaverTjeneste, EnhetsTjeneste enhetsTjeneste) {
        this.oppgaverTjeneste = oppgaverTjeneste;
        this.enhetsTjeneste = enhetsTjeneste;
    }

    private static String lagBeskrivelse(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId, ProsessTaskData data) {
        if (DokumentTypeId.UDEFINERT.equals(dokumentTypeId)) {
            return BehandlingTema.UDEFINERT.equals(behandlingTema) ? "Journalføring" : "Journalføring " + behandlingTema.getTermNavn();
        }
        String beskrivelse = dokumentTypeId.getTermNavn();
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            beskrivelse = beskrivelse + " (" + ytelseFraBehandlingTema(behandlingTema) + ")";
        }
        return beskrivelse;
    }

    private static String ytelseFraBehandlingTema(BehandlingTema behandlingTema) {
        return switch (behandlingTema) {
            case ENGANGSSTØNAD, ENGANGSSTØNAD_FØDSEL, ENGANGSSTØNAD_ADOPSJON -> "Engangsstønad";
            case FORELDREPENGER, FORELDREPENGER_FØDSEL, FORELDREPENGER_ADOPSJON -> "Foreldrepenger";
            case SVANGERSKAPSPENGER -> "Svangerskapspenger";
            default -> "Ukjent";
        };
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var behandlingTema = BehandlingTema.fraKodeDefaultUdefinert(prosessTaskData.getPropertyValue(BEHANDLINGSTEMA_KEY));
        var dokumentTypeId = Optional.ofNullable(prosessTaskData.getPropertyValue(DOKUMENTTYPE_ID_KEY))
            .map(DokumentTypeId::fraKodeDefaultUdefinert)
            .orElse(DokumentTypeId.UDEFINERT);
        behandlingTema = ArkivUtil.behandlingTemaFraDokumentType(behandlingTema, dokumentTypeId);

        var journalpostId = JournalpostId.fra(prosessTaskData.getPropertyValue(ARKIV_ID_KEY));
        if (oppgaverTjeneste.finnesÅpeneJournalføringsoppgaverFor(journalpostId)) {
            var ikkeLokalOppgave = Optional.ofNullable(prosessTaskData.getPropertyValue(JOURNAL_ENHET))
                .filter(OpprettGSakOppgaveTask::erGosysOppgave).isPresent();
            LOG.info("FPMOTTAK JFR-OPPGAVE: finnes allerede åpen oppgave for journalpostId: {}", journalpostId.getVerdi());
            // Behold oppgave hvis skal behandles i Gosys - ellers lag lokal oppgave
            if (ikkeLokalOppgave) {
                return;
            } else {
                oppgaverTjeneste.ferdigstillAlleÅpneJournalføringsoppgaverFor(journalpostId);
            }
        }

        String oppgaveId = opprettOppgave(prosessTaskData, behandlingTema, dokumentTypeId);

        LOG.info("FPMOTTAK JFR-OPPGAVE: opprettet oppgave med id {} for journalpostId: {}", oppgaveId, journalpostId.getVerdi());
    }

    private String opprettOppgave(ProsessTaskData prosessTaskData, BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId) {
        var enhetInput = prosessTaskData.getPropertyValue(JOURNAL_ENHET);
        // Oppgave har ikke mapping for alle undertyper fødsel/adopsjon
        var brukBT = BehandlingTema.forYtelseUtenFamilieHendelse(behandlingTema);

        var journalpostId = prosessTaskData.getPropertyValue(ARKIV_ID_KEY);

        // Overstyr saker fra NFP+NK, deretter egen logikk hvis fødselsnummer ikke er
        // oppgitt
        var enhetId = enhetsTjeneste.hentFordelingEnhetId(Optional.ofNullable(enhetInput), prosessTaskData.getAktørId());

        var beskrivelse = lagBeskrivelse(behandlingTema, dokumentTypeId, prosessTaskData);
        var saksref = prosessTaskData.getSaksnummer();

        var journalpost = JournalpostId.fra(journalpostId);

        var nyOppgave = NyOppgave.builder()
            .medJournalpostId(journalpost)
            .medEnhetId(enhetId)
            .medAktørId(prosessTaskData.getAktørId())
            .medSaksref(saksref)
            .medBehandlingTema(brukBT)
            .medBeskrivelse(beskrivelse)
            .build();

        if (erGosysOppgave(enhetId)) {
            LOG.info("Oppretter en gosys oppgave for {} med {}", journalpost, dokumentTypeId);
            return oppgaverTjeneste.opprettGosysJournalføringsoppgaveFor(nyOppgave);
        } else {
            LOG.info("Oppretter en lokal oppgave for {} med {}", journalpost, dokumentTypeId);
            return oppgaverTjeneste.opprettJournalføringsoppgaveFor(nyOppgave);
        }
    }

    private static boolean erGosysOppgave(String enhet) {
        return NK_ENHET_ID.equals(enhet);
    }
}
