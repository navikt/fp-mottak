package no.nav.foreldrepenger.mottak.journalføring.api;

import static no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema.gjelderAdopsjon;
import static no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema.gjelderForeldrepenger;
import static no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema.gjelderFødsel;
import static no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema.ikkeSpesifikkHendelse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakV2Dto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.kontrakter.fordel.YtelseTypeDto;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.mottak.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.mottak.journalføring.ManuellOpprettSakValidator;
import no.nav.foreldrepenger.mottak.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.mottak.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.mottak.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.mottak.domene.oppgavebehandling.FerdigstillOppgaveTask;
import no.nav.foreldrepenger.mottak.mottak.felles.InntektsmeldingInnhold;
import no.nav.foreldrepenger.mottak.mottak.felles.SøknadInnhold;
import no.nav.foreldrepenger.mottak.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.mottak.klient.FagsakYtelseTypeDto;
import no.nav.foreldrepenger.mottak.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.mottak.tjeneste.ArkivUtil;
import no.nav.foreldrepenger.mottak.mottak.tjeneste.VLKlargjører;
import no.nav.foreldrepenger.mottak.typer.AktørId;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class FerdigstillJournalføringTjeneste {

    static final String BRUKER_MANGLER = "Journalpost mangler knyting til bruker - prøv igjen om et halv minutt";
    static final String JOURNALPOST_IKKE_INNGÅENDE = "Journalpost ikke Inngående";
    private static final Logger LOG = LoggerFactory.getLogger(FerdigstillJournalføringTjeneste.class);
    private VLKlargjører klargjører;
    private Fagsak fagsak;
    private PersonInformasjon pdl;
    private Journalføringsoppgave oppgaver;
    private ProsessTaskTjeneste taskTjeneste;
    private ArkivTjeneste arkivTjeneste;
    private ManuellOpprettSakValidator manuellOpprettSakValidator;

    FerdigstillJournalføringTjeneste() {
        //CDI
    }

    @Inject
    public FerdigstillJournalføringTjeneste(VLKlargjører klargjører,
                                            Fagsak fagsak,
                                            PersonInformasjon pdl,
                                            Journalføringsoppgave oppgaver,
                                            ProsessTaskTjeneste taskTjeneste,
                                            ArkivTjeneste arkivTjeneste,
                                            ManuellOpprettSakValidator manuellOpprettSakValidator) {
        this.klargjører = klargjører;
        this.fagsak = fagsak;
        this.pdl = pdl;
        this.oppgaver = oppgaver;
        this.taskTjeneste = taskTjeneste;
        this.arkivTjeneste = arkivTjeneste;
        this.manuellOpprettSakValidator = manuellOpprettSakValidator;
    }

    public void oppdaterJournalpostOgFerdigstill(String enhetId,
                                                 String saksnummer,
                                                 ArkivJournalpost journalpost,
                                                 String nyJournalpostTittel,
                                                 List<DokumenterMedNyTittel> dokumenterMedNyTittel,
                                                 DokumentTypeId nyDokumentTypeId) {

        validerJournalposttype(journalpost.getJournalposttype());

        LOG.info("FPMOTTAK RESTJOURNALFØRING: Ferdigstiller journalpostId: {}", journalpost.getJournalpostId());

        var fagsakInfomasjon = hentOgValiderFagsak(saksnummer, journalpost);

        final var behandlingTemaFagsak = BehandlingTema.fraOffisiellKode(fagsakInfomasjon.behandlingstemaOffisiellKode());
        final var aktørIdFagsak = fagsakInfomasjon.aktørId();

        var dokumentTypeId = journalpost.getHovedtype();
        var oppdatereTitler = nyJournalpostTittel != null || !dokumenterMedNyTittel.isEmpty();
        if (nyDokumentTypeId != null) {
            dokumentTypeId = nyDokumentTypeId;
        }

        final var behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);
        final var behandlingTema = validerOgVelgBehandlingTema(behandlingTemaFagsak, behandlingTemaDok, dokumentTypeId);
        final var dokumentKategori = ArkivUtil.utledKategoriFraDokumentType(dokumentTypeId);
        var brukDokumentTypeId = DokumentTypeId.UDEFINERT.equals(dokumentTypeId) ? DokumentTypeId.ANNET : dokumentTypeId;

        validerKanJournalføreKlageDokument(behandlingTemaFagsak, brukDokumentTypeId, dokumentKategori);

        if (Journalstatus.MOTTATT.equals(journalpost.getTilstand())) {
            oppdaterJournalpostMedTittelOgMangler(journalpost, nyJournalpostTittel, dokumenterMedNyTittel, aktørIdFagsak, behandlingTema);
            try {
                arkivTjeneste.settTilleggsOpplysninger(journalpost, brukDokumentTypeId, oppdatereTitler);
            } catch (Exception e) {
                LOG.info("FPMOTTAK RESTJOURNALFØRING: Feil ved setting av tilleggsopplysninger for journalpostId {}", journalpost.getJournalpostId());
            }
            LOG.info("FPMOTTAK RESTJOURNALFØRING: Kaller til Journalføring"); // NOSONAR
            try {
                arkivTjeneste.oppdaterMedSak(journalpost.getJournalpostId(), saksnummer, aktørIdFagsak);
                arkivTjeneste.ferdigstillJournalføring(journalpost.getJournalpostId(), enhetId);
            } catch (Exception e) {
                LOG.warn("FPMOTTAK RESTJOURNALFØRING: oppdaterJournalpostOgFerdigstill feiler for {}", journalpost.getJournalpostId(), e);
                throw new TekniskException("FP-15689", lagUgyldigInputMelding("Bruker", BRUKER_MANGLER));
            }
        }

        String eksternReferanseId = journalpost.getEksternReferanseId();
        if (DokumentTypeId.INNTEKTSMELDING.equals(brukDokumentTypeId) && eksternReferanseId == null) {
            eksternReferanseId = arkivTjeneste.hentEksternReferanseId(journalpost.getOriginalJournalpost()).orElse(null);
        }

        var mottattTidspunkt = Optional.ofNullable(journalpost.getDatoOpprettet()).orElseGet(LocalDateTime::now);

        final var xml = hentDokumentSettMetadata(behandlingTema, aktørIdFagsak, journalpost);
        klargjører.klargjør(xml, saksnummer, journalpost.getJournalpostId(), brukDokumentTypeId, mottattTidspunkt, behandlingTema,
                dokumentKategori, enhetId, eksternReferanseId);

        opprettFerdigstillOppgaveTask(JournalpostId.fra(journalpost.getJournalpostId()));
    }

    public void oppdaterJournalpostOgFerdigstillGenerellSak(String enhetId,
                                                            ArkivJournalpost journalpost,
                                                            String aktørId,
                                                            String nyJournalpostTittel,
                                                            List<DokumenterMedNyTittel> dokumenterMedNyTittel,
                                                            DokumentTypeId nyDokumentTypeId) {

        validerJournalposttype(journalpost.getJournalposttype());

        LOG.info("FPMOTTAK RESTJOURNALFØRING GENERELL: Ferdigstiller journalpostId: {}", journalpost.getJournalpostId());

        var dokumentTypeId = journalpost.getHovedtype();
        if (nyDokumentTypeId != null) {
            dokumentTypeId = nyDokumentTypeId;
        }

        final var behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);
        final var behandlingTema = validerOgVelgBehandlingTema(BehandlingTema.UDEFINERT, behandlingTemaDok, dokumentTypeId);

        if (Journalstatus.MOTTATT.equals(journalpost.getTilstand())) {
            oppdaterJournalpostMedTittelOgMangler(journalpost, nyJournalpostTittel, dokumenterMedNyTittel, aktørId, behandlingTema);
            LOG.info("FPMOTTAK RESTJOURNALFØRING GENERELL: Kaller til Journalføring"); // NOSONAR
            try {
                arkivTjeneste.oppdaterMedGenerellSak(journalpost.getJournalpostId(), aktørId);
                arkivTjeneste.ferdigstillJournalføring(journalpost.getJournalpostId(), enhetId);
            } catch (Exception e) {
                LOG.warn("FPMOTTAK RESTJOURNALFØRING GENERELL: oppdaterJournalpostOgFerdigstill feiler for {}", journalpost.getJournalpostId(), e);
                throw new TekniskException("FP-15689", lagUgyldigInputMelding("Bruker", BRUKER_MANGLER));
            }
        }

        opprettFerdigstillOppgaveTask(JournalpostId.fra(journalpost.getJournalpostId()));
    }


    public void oppdaterJournalpostMedTittelOgMangler(ArkivJournalpost journalpost,
                                                      String nyJournalpostTittel,
                                                      List<DokumenterMedNyTittel> dokumenterMedNyTittel,
                                                      String aktørId,
                                                      BehandlingTema behandlingTema) {
        var journalpostId = journalpost.getJournalpostId();
        var kanal = journalpost.getKanal();

        if ((nyJournalpostTittel != null || !dokumenterMedNyTittel.isEmpty()) && List.of(MottakKanal.SELVBETJENING.getKode(),
            MottakKanal.ALTINN.getKode()).contains(kanal)) {
            // TFP-5791: Det er spesialhåndtering av tilfeller hvor klage/anke journalpost tittel ble endret i gosys uten at man har endret dokument tittel samtidig.
            if (nyJournalpostTittel == null && dokumenterMedNyTittel.size() == 1) {
                LOG.info(
                    "FPMOTTAK RESTJOURNALFØRING: Det endres dokument tittel på et dokument som kommer fra {}. Gjelder journalpost: {}:{}. Ny dokument tittel: {}.",
                    kanal, journalpostId, journalpost.getTittel(), dokumenterMedNyTittel.getFirst().dokumentTittel());
            } else {
                throw new FunksjonellException("FP-963071",
                    String.format("Kan ikke endre tittel på journalpost med id %s som kommer fra Selvbetjening eller Altinn.", journalpostId),
                    "Tittel kan ikke endres når journalposten kommer fra selvbetjening eller altinn");
            }
        }

        LOG.info("FPMOTTAK RESTJOURNALFØRING: Oppdaterer generelle mangler og titler for journalpostId: {}", journalpostId);

        //Fjernes når vi har fått informasjon om hvor ofte dette skjer
        if (nyJournalpostTittel != null) {
            var dokumenterFraArkiv = journalpost.getOriginalJournalpost().dokumenter();

            Set<DokumentTypeId> nyeDokumenttyper = utledDokumentTyper(dokumenterMedNyTittel, dokumenterFraArkiv);

            var utledetDokType = ArkivUtil.utledHovedDokumentType(nyeDokumenttyper);

            if (!utledetDokType.getTermNavn().equals(nyJournalpostTittel)) {
                LOG.info("FPMOTTAK RESTJOURNALFØRING: Ny journalpost-tittel: {} avviker fra utledet journalpost-tittel: {} for journalpostId: {}",
                    nyJournalpostTittel, utledetDokType.getTermNavn(), journalpostId);
            }
        }
        List<OppdaterJournalpostRequest.DokumentInfoOppdater> dokumenterÅOppdatere = new ArrayList<>();

        if (!dokumenterMedNyTittel.isEmpty()) {
            dokumenterÅOppdatere = mapDokumenterTilOppdatering(dokumenterMedNyTittel);
        }
        arkivTjeneste.oppdaterJournalpostVedManuellJournalføring(journalpostId, nyJournalpostTittel, dokumenterÅOppdatere, journalpost, aktørId,
            behandlingTema);

    }

    public record DokumenterMedNyTittel(String dokumentId, String dokumentTittel) {
    }

    private List<no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest.DokumentInfoOppdater> mapDokumenterTilOppdatering(List<DokumenterMedNyTittel> dokumenter) {
        return dokumenter.stream()
            .filter(dt -> dt.dokumentId() != null && dt.dokumentTittel() != null)
            .map(dt -> new no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest.DokumentInfoOppdater(dt.dokumentId(),
                dt.dokumentTittel(), null))
            .toList();
    }

    private static Set<DokumentTypeId> utledDokumentTyper(List<DokumenterMedNyTittel> dokumenterMedNyTittel, List<DokumentInfo> dokumenterFraArkiv) {
        Set<DokumentTypeId> dokumentTyper = new HashSet<>();
        Set<String> oppdatereDokIder = dokumenterMedNyTittel.stream().map(DokumenterMedNyTittel::dokumentId).collect(Collectors.toSet());

        for (DokumentInfo dokumentFraArkiv : dokumenterFraArkiv) {
            if (!oppdatereDokIder.contains(dokumentFraArkiv.dokumentInfoId())) {
                dokumentTyper.add(DokumentTypeId.fraTermNavn(dokumentFraArkiv.tittel()));
            }
        }
        for (DokumenterMedNyTittel dokumentNyTittel : dokumenterMedNyTittel) {
            dokumentTyper.add(DokumentTypeId.fraTermNavn(dokumentNyTittel.dokumentTittel()));
        }
        return dokumentTyper;
    }

    public ArkivJournalpost hentJournalpost(String arkivId) {
        try {
            return arkivTjeneste.hentArkivJournalpost(arkivId);
        } catch (Exception e) {
            LOG.warn("FORDEL fikk feil fra hentjournalpost: ", e);
            throw new TekniskException("FP-15676", lagUgyldigInputMelding("Journalpost", "Finner ikke journalpost med dokumentId " + arkivId));
        }
    }

    String opprettSak(ArkivJournalpost journalpost, FerdigstillJournalføringRestTjeneste.OpprettSak opprettSakInfo, DokumentTypeId nyDokumentTypeId) {
        manuellOpprettSakValidator.validerKonsistensForOpprettSak(journalpost, opprettSakInfo.ytelseType(), opprettSakInfo.aktørId(), nyDokumentTypeId);
        return fagsak.opprettSak(opprettSakRequest(journalpost.getJournalpostId(), opprettSakInfo)).saksnummer();
    }

    // Validerer mot eksisterende men sikrer at det opprettes ny sak
    String opprettNySak(ArkivJournalpost journalpost, FerdigstillJournalføringRestTjeneste.OpprettSak opprettSakInfo, DokumentTypeId nyDokumentTypeId) {
        manuellOpprettSakValidator.validerKonsistensForOpprettSak(journalpost, opprettSakInfo.ytelseType(), opprettSakInfo.aktørId(), nyDokumentTypeId);
        return fagsak.opprettSak(opprettSakRequest(null, opprettSakInfo)).saksnummer();
    }

    private static OpprettSakV2Dto opprettSakRequest(String journalpostId, FerdigstillJournalføringRestTjeneste.OpprettSak opprettSakInfo) {
        return new OpprettSakV2Dto(journalpostId, mapYtelseTypeV2TilDto(opprettSakInfo.ytelseType()), opprettSakInfo.aktørId().getId());
    }

    static YtelseTypeDto mapYtelseTypeV2TilDto(FagsakYtelseTypeDto ytelseType) {
        return switch (ytelseType) {
            case FORELDREPENGER -> YtelseTypeDto.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> YtelseTypeDto.SVANGERSKAPSPENGER;
            case ENGANGSTØNAD -> YtelseTypeDto.ENGANGSTØNAD;
            case null -> throw new IllegalStateException("YtelseType må være satt.");
        };
    }

    public JournalpostId knyttTilAnnenSak(ArkivJournalpost journalpost, String enhetId, String saksnummer) {
        var saksinfo = hentFagsakInfo(saksnummer).orElseThrow();
        final var behandlingTemaFagsak = BehandlingTema.fraOffisiellKode(saksinfo.behandlingstemaOffisiellKode());
        final var aktørIdFagsak = saksinfo.aktørId();

        var dokumentTypeId = journalpost.getHovedtype();
        final var behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);
        final var behandlingTema = validerOgVelgBehandlingTema(behandlingTemaFagsak, behandlingTemaDok, dokumentTypeId);
        final var dokumentKategori = ArkivUtil.utledKategoriFraDokumentType(dokumentTypeId);
        var brukDokumentTypeId = DokumentTypeId.UDEFINERT.equals(dokumentTypeId) ? DokumentTypeId.ANNET : dokumentTypeId;

        validerKanJournalføreKlageDokument(behandlingTemaFagsak, brukDokumentTypeId, dokumentKategori);

        manuellOpprettSakValidator.validerKonsistensForKnyttTilAnnenSak(journalpost, behandlingTema.utledYtelseType(), new AktørId(aktørIdFagsak), journalpost.getHovedtype());

        // Do the business
        var nyJournalpostId = arkivTjeneste.knyttTilAnnenSak(journalpost, enhetId, saksnummer, aktørIdFagsak);

        // Bruk fra opprinnelig
        final var xml = hentDokumentSettMetadata(behandlingTema, aktørIdFagsak, journalpost);
        var mottattTidspunkt = Optional.ofNullable(journalpost.getDatoOpprettet()).orElseGet(LocalDateTime::now);
        String eksternReferanseId = journalpost.getEksternReferanseId();
        if (DokumentTypeId.INNTEKTSMELDING.equals(brukDokumentTypeId) && eksternReferanseId == null) {
            eksternReferanseId = arkivTjeneste.hentEksternReferanseId(journalpost.getOriginalJournalpost()).orElse(null);
        }

        klargjører.klargjør(xml, saksnummer, nyJournalpostId, brukDokumentTypeId, mottattTidspunkt, behandlingTema,
                dokumentKategori, enhetId, eksternReferanseId);

        return Optional.ofNullable(nyJournalpostId).map(JournalpostId::fra).orElse(null);
    }

    // Forvaltning only
    public void sendInnPåSak(ArkivJournalpost journalpost, String saksnummer) {
        var saksinfo = hentFagsakInfo(saksnummer).orElseThrow();
        final var behandlingTemaFagsak = BehandlingTema.fraOffisiellKode(saksinfo.behandlingstemaOffisiellKode());
        final var aktørIdFagsak = saksinfo.aktørId();

        var dokumentTypeId = journalpost.getHovedtype();
        final var behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);
        final var behandlingTema = validerOgVelgBehandlingTema(behandlingTemaFagsak, behandlingTemaDok, dokumentTypeId);
        final var dokumentKategori = ArkivUtil.utledKategoriFraDokumentType(dokumentTypeId);
        var brukDokumentTypeId = DokumentTypeId.UDEFINERT.equals(dokumentTypeId) ? DokumentTypeId.ANNET : dokumentTypeId;

        // Bruk fra opprinnelig
        final var xml = hentDokumentSettMetadata(behandlingTema, aktørIdFagsak, journalpost);
        var mottattTidspunkt = Optional.ofNullable(journalpost.getDatoOpprettet()).orElseGet(LocalDateTime::now);
        String eksternReferanseId = journalpost.getEksternReferanseId();
        if (DokumentTypeId.INNTEKTSMELDING.equals(brukDokumentTypeId) && eksternReferanseId == null) {
            eksternReferanseId = arkivTjeneste.hentEksternReferanseId(journalpost.getOriginalJournalpost()).orElse(null);
        }

        klargjører.klargjør(xml, saksnummer, journalpost.getJournalpostId(), brukDokumentTypeId, mottattTidspunkt, behandlingTema,
                dokumentKategori, null, eksternReferanseId);
    }


    private static BehandlingTema validerOgVelgBehandlingTema(BehandlingTema behandlingTemaFagsak,
                                                              BehandlingTema behandlingTemaDok,
                                                              DokumentTypeId dokumentTypeId) {
        if (BehandlingTema.UDEFINERT.equals(behandlingTemaDok) && !BehandlingTema.UDEFINERT.equals(behandlingTemaFagsak)) {
            return behandlingTemaFagsak;
        }
        if (BehandlingTema.UDEFINERT.equals(behandlingTemaFagsak)) {
            return behandlingTemaDok;
        }
        if (!DokumentTypeId.erSøknadType(dokumentTypeId)) {
            return behandlingTemaFagsak;
        }
        if ((gjelderForeldrepenger(behandlingTemaFagsak) && !gjelderForeldrepenger(behandlingTemaDok)) || (
            BehandlingTema.gjelderEngangsstønad(behandlingTemaFagsak) && !BehandlingTema.gjelderEngangsstønad(behandlingTemaDok)) || (
            BehandlingTema.gjelderSvangerskapspenger(behandlingTemaFagsak) && !BehandlingTema.gjelderSvangerskapspenger(behandlingTemaDok))) {
            throw new FunksjonellException("FP-963079", "Dokumentet samsvarer ikke med sakens type - kan ikke journalføre",
                "Journalfør på annen sak eller opprett ny sak");
        }
        if (!ikkeSpesifikkHendelse(behandlingTemaDok) && !ikkeSpesifikkHendelse(behandlingTemaFagsak)) {
            if ((gjelderFødsel(behandlingTemaDok) && !gjelderFødsel(behandlingTemaFagsak)) || (gjelderAdopsjon(behandlingTemaDok) && !gjelderAdopsjon(
                behandlingTemaFagsak))) {
                throw new FunksjonellException("FP-963080", "Dokumentet samsvarer ikke med sakens hendelse type (fødsel/adopsjon) - kan ikke journalføre",
                    "Journalfør på annen sak eller opprett ny sak");
            }
        }
        return ikkeSpesifikkHendelse(behandlingTemaDok) ? behandlingTemaFagsak : behandlingTemaDok;
    }

    private static void validerKanJournalføreKlageDokument(BehandlingTema behandlingTema,
                                                           DokumentTypeId dokumentTypeId,
                                                           DokumentKategori dokumentKategori) {
        if (BehandlingTema.UDEFINERT.equals(behandlingTema) && (DokumentTypeId.KLAGE_DOKUMENT.equals(dokumentTypeId)
            || DokumentKategori.KLAGE_ELLER_ANKE.equals(dokumentKategori))) {
            throw new FunksjonellException("FP-963074", "Klager må journalføres på sak med tidligere behandling",
                "Journalføre klagen på sak med avsluttet behandling");
        }
    }

    private static void validerDokumentData(LocalDate imStartDato,
                                            BehandlingTema behandlingTema,
                                            DokumentTypeId dokumentTypeId,
                                            BehandlingTema behandlingTemaFraIM,
                                            LocalDate tidligsteDato) {
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            if (gjelderForeldrepenger(behandlingTemaFraIM)) {
                if (imStartDato == null) { // Kommer ingen vei uten startdato
                    throw new FunksjonellException("FP-963076", "Inntektsmelding mangler startdato - kan ikke journalføre",
                        "Be om ny Inntektsmelding med startdato");

                } else if (!gjelderForeldrepenger(behandlingTema)) { // Prøver journalføre på annen
                    // fagsak - ytelsetype
                    throw new FunksjonellException("FP-963075", "Inntektsmelding årsak samsvarer ikke med sakens type - kan ikke journalføre",
                        "Be om ny Inntektsmelding for Foreldrepenger");
                }
            } else if (!behandlingTemaFraIM.equals(behandlingTema)) {
                throw new FunksjonellException("FP-963075", "Inntektsmelding årsak samsvarer ikke med sakens type - kan ikke journalføre",
                    "Be om ny Inntektsmelding for Foreldrepenger");
            }
        }
        if (gjelderForeldrepenger(behandlingTema) && tidligsteDato.isBefore(KonfigVerdier.ENDRING_BEREGNING_DATO)) {
            throw new FunksjonellException("FP-963077", "For tidlig uttak",
                "Søknad om uttak med oppstart i 2018 skal journalføres mot sak i Infotrygd");
        }
    }

    private static String lagUgyldigInputMelding(String feltnavn, String verdi) {
        return String.format("Ugyldig input: %s med verdi: %s er ugyldig input.", feltnavn, verdi);
    }

    private FagsakInfomasjonDto hentOgValiderFagsak(String saksnummer, ArkivJournalpost journalpost) {
        // Finn sak i fpsak med samme aktør
        final var brukerAktørId = journalpost.getBrukerAktørId();

        final var fagsakFraRequestSomTrefferRettAktør = hentFagsakInfo(saksnummer).filter(
            f -> brukerAktørId.isEmpty() || Objects.equals(f.aktørId(), brukerAktørId.get()));

        if (fagsakFraRequestSomTrefferRettAktør.isEmpty()) {
            throw new FunksjonellException("FP-963070", "Kan ikke journalføre på saksnummer: " + saksnummer,
                "Journalføre dokument på annen sak i VL");
        }

        LOG.info("FPMOTTAK RESTJOURNALFØRING: Fant en FP-sak med saksnummer {} som har rett aktør", saksnummer);
        return fagsakFraRequestSomTrefferRettAktør.get();
    }

    private Optional<FagsakInfomasjonDto> hentFagsakInfo(String saksnummerFraArkiv) {
        return fagsak.finnFagsakInfomasjon(new SaksnummerDto(saksnummerFraArkiv));
    }

    private String hentDokumentSettMetadata(BehandlingTema behandlingTema, String aktørId, ArkivJournalpost journalpost) {
        final var xml = journalpost.getStrukturertPayload();
        if (journalpost.getInnholderStrukturertInformasjon()) {
            return validerXml(journalpost, behandlingTema, aktørId, xml);
        }
        return xml;
    }

    private String validerXml(ArkivJournalpost journalpost, BehandlingTema behandlingTema, String aktørId, String xml) {
        MottattStrukturertDokument<?> mottattDokument;
        try {
            mottattDokument = MeldingXmlParser.unmarshallXml(xml);
        } catch (Exception _) {
            LOG.info("FPMOTTAK RESTJOURNALFØRING: Journalpost med type {} er strukturert men er ikke gyldig XML", journalpost.getHovedtype());
            return null;
        }
        if (DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD.equals(journalpost.getHovedtype()) && !ikkeSpesifikkHendelse(behandlingTema)) {
            behandlingTema = BehandlingTema.FORELDREPENGER;
        }
        var innhold = mottattDokument.hentDokumentInnhold(pdl::hentAktørIdForPersonIdent);
        try {
            mottattDokument.validerDokumentInnhold(Optional.ofNullable(aktørId), behandlingTema, pdl::hentAktørIdForPersonIdent);
        } catch (TekniskException|FunksjonellException e) {
            // Her er det "greit" - da har man bestemt seg, men kan lage rot i saken.
            if ("FP-401246".equals(e.getKode())) {
                var logMessage = e.getMessage();
                LOG.info("FPMOTTAK RESTJOURNALFØRING: {}", logMessage);
            } else {
                throw e;
            }
        }
        var imStartdato = innhold instanceof InntektsmeldingInnhold im ? im.getFørsteFraværsdato().orElse(null) : null;
        var tidligsteDato = Optional.ofNullable(innhold)
            .flatMap(i -> i instanceof SøknadInnhold s && s.getOmsorgsovertakelsesdato().isPresent() ? s.getOmsorgsovertakelsesdato() : i.getFørsteFraværsdato())
            .orElse(Tid.TIDENES_ENDE);
        validerDokumentData(imStartdato, behandlingTema, journalpost.getHovedtype(), innhold.getBehandlingTema(), tidligsteDato);
        return xml;
    }

    private static void validerJournalposttype(Journalposttype type) {
        if (!Journalposttype.INNGÅENDE.equals(type)) {
            throw new TekniskException("FP-15680", lagUgyldigInputMelding("JournalpostType", JOURNALPOST_IKKE_INNGÅENDE));
        }
    }

    void opprettFerdigstillOppgaveTask(JournalpostId journalpostId) {
        if (journalpostId != null) {
            try {
                oppgaver.ferdigstillAlleÅpneJournalføringsoppgaverFor(journalpostId);
            } catch (Exception e) {
                LOG.warn("FPMOTTAK RESTJOURNALFØRING: Ferdigstilt oppgave med dokumentId {} feiler ", journalpostId, e);
                var ferdigstillOppgaveTask = ProsessTaskData.forProsessTask(FerdigstillOppgaveTask.class);
                ferdigstillOppgaveTask.setProperty(FerdigstillOppgaveTask.JOURNALPOSTID_KEY, journalpostId.getVerdi());
                taskTjeneste.lagre(ferdigstillOppgaveTask);
            }
        }
    }
}
