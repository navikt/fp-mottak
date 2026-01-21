package no.nav.foreldrepenger.mottak.mottak.klient;

import static no.nav.foreldrepenger.mottak.mottak.behandlendeenhet.EnhetsTjeneste.NK_ENHET_ID;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.foreldrepenger.kontrakter.fordel.BehandlendeFagsystemDto;
import no.nav.foreldrepenger.kontrakter.fordel.BrukerRolleDto;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakV2Dto;
import no.nav.foreldrepenger.kontrakter.fordel.SakInfoV2Dto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.mottak.felles.DokumentInnhold;
import no.nav.foreldrepenger.mottak.mottak.felles.InntektsmeldingInnhold;
import no.nav.foreldrepenger.mottak.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.mottak.felles.SøknadInnhold;
import no.nav.foreldrepenger.mottak.mottak.tjeneste.ArkivUtil;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, application = FpApplication.FPSAK)
public class FagsakKlient implements Fagsak {

    private static final String JOURNALPOSTTILKNYTNING_PATH = "/api/fordel/fagsak/knyttJournalpost";
    private static final String FAGSAKINFORMASJON_PATH = "/api/fordel/fagsak/informasjon";
    private static final String FAGSAK_OPPRETT_PATH = "/api/fordel/fagsak/opprett";
    private static final String VURDER_FAGSYSTEM_PATH = "/api/fordel/vurderFagsystem";
    private static final String KLAGEINSTANS_FAGSYSTEM_PATH = "/api/fordel/klageinstans";

    private static final String FINN_FAGSAKER_PATH = "/api/fordel/finnFagsaker/v2";
    private static final Logger LOG = LoggerFactory.getLogger(FagsakKlient.class);

    private final URI knytningEndpoint;
    private final URI fagsakinfoEndpoint;
    private final URI opprettsakEndpoint;
    private final URI fagsystemEndpoint;
    private final URI klageinstansEndpoint;

    private final URI finnFagsakerEndpoint;
    private final RestClient klient;
    private final RestConfig restConfig;


    public FagsakKlient() {
        this.klient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        var endpoint = restConfig.fpContextPath();
        this.knytningEndpoint = lagURI(endpoint, JOURNALPOSTTILKNYTNING_PATH);
        this.fagsakinfoEndpoint = lagURI(endpoint, FAGSAKINFORMASJON_PATH);
        this.opprettsakEndpoint = lagURI(endpoint, FAGSAK_OPPRETT_PATH);
        this.fagsystemEndpoint = lagURI(endpoint, VURDER_FAGSYSTEM_PATH);
        this.klageinstansEndpoint = lagURI(endpoint, KLAGEINSTANS_FAGSYSTEM_PATH);
        this.finnFagsakerEndpoint = lagURI(endpoint, FINN_FAGSAKER_PATH);
    }

    @Override
    public Optional<FagsakInfomasjonDto> finnFagsakInfomasjon(SaksnummerDto saksnummerDto) {
        LOG.info("Finner fagsakinformasjon");
        var request = RestRequest.newPOSTJson(saksnummerDto, fagsakinfoEndpoint, restConfig);
        var info = klient.send(request, FagsakInfomasjonDto.class);
        LOG.info("Fant fagsakinformasjon OK");
        return Optional.ofNullable(info);
    }

    @Override
    public SaksnummerDto opprettSak(OpprettSakDto opprettSakDto) {
        LOG.info("Oppretter sak");
        var request = RestRequest.newPOSTJson(opprettSakDto, opprettsakEndpoint, restConfig);
        var sak = klient.send(request, SaksnummerDto.class);
        LOG.info("Opprettet sak OK");
        return sak;
    }

    @Override
    public SaksnummerDto opprettSak(OpprettSakV2Dto opprettSakDto) {
        LOG.info("Oppretter sak");
        var request = RestRequest.newPOSTJson(opprettSakDto, lagURI(opprettsakEndpoint, "/v2"), restConfig);
        var sak = klient.send(request, SaksnummerDto.class);
        LOG.info("Opprettet sak OK");
        return sak;
    }

    @Override
    public void knyttSakOgJournalpost(JournalpostKnyttningDto dto) {
        LOG.info("Knytter sak og journalpost");
        var request = RestRequest.newPOSTJson(dto, knytningEndpoint, restConfig);
        klient.sendReturnOptional(request, String.class);
    }

    @Override
    public VurderFagsystemResultat vurderFagsystem(MottakMeldingDataWrapper w, DokumentInnhold innhold) {
        var aktørId = w.getAktørId().orElseThrow();
        boolean strukturertSøknad = w.erStrukturertDokument().orElse(Boolean.FALSE);
        var dokumentTypeId = w.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT);
        var dokumentKategori = ArkivUtil.utledKategoriFraDokumentType(dokumentTypeId);
        String behandlingTemaString = BehandlingTema.UDEFINERT.equals(w.getBehandlingTema()) ? w.getBehandlingTema().getKode() : w.getBehandlingTema()
            .getOffisiellKode();

        var dto = new VurderFagsystemDto(w.getArkivId(), strukturertSøknad, aktørId, behandlingTemaString);
        w.getSaksnummer().ifPresent(dto::setSaksnummer);
        // VurderFagsystemDto burde hatt et felt for første uttaksdag for søknad. For å
        // ikke kaste mottatt søknad til manuell journalføring i fpsak, sender vi her første
        // uttaksdag i et felt som brukes til det samme for inntektsmelding. Kontrakten bør endres
        Optional.ofNullable(innhold).flatMap(DokumentInnhold::getFørsteFraværsdato).ifPresent(dto::setStartDatoForeldrepengerInntektsmelding);
        w.getForsendelseMottattTidspunkt().ifPresent(dto::setForsendelseMottattTidspunkt);
        dto.setForsendelseMottatt(w.getForsendelseMottatt());
        dto.setDokumentTypeIdOffisiellKode(dokumentTypeId.getOffisiellKode());
        dto.setDokumentKategoriOffisiellKode(dokumentKategori.getOffisiellKode());

        if (innhold instanceof SøknadInnhold søknadInnhold) {
            // Denne må enten være null eller ha minst en dato. Validering i kontrakt
            var adopsjonsbarn = Optional.ofNullable(søknadInnhold.getAdopsjonsbarnFødselsdatoer())
                .filter(liste -> !liste.isEmpty()).orElse(null);
            dto.setAdopsjonsBarnFodselsdatoer(adopsjonsbarn);
            søknadInnhold.getTermindato().ifPresent(dto::setBarnTermindato);
            søknadInnhold.getFødselsdato().ifPresent(dto::setBarnFodselsdato);
            søknadInnhold.getOmsorgsovertakelsesdato().ifPresent(dto::setOmsorgsovertakelsedato);
            søknadInnhold.getAnnenPartAktørId().ifPresent(dto::setAnnenPart);
            søknadInnhold.getBrukerRolle().map(FagsakKlient::mapBrukerRolle).ifPresent(dto::setBrukerRolle);
        } else if (innhold instanceof InntektsmeldingInnhold inntektsmelding) {
            inntektsmelding.getÅrsakTilInnsending().ifPresent(dto::setÅrsakInnsendingInntektsmelding);
            inntektsmelding.getVirksomhetsnummer().ifPresent(dto::setVirksomhetsnummer);
            inntektsmelding.getArbeidsgiverAktørId().ifPresent(dto::setArbeidsgiverAktørId);
            inntektsmelding.getArbeidsforholdsId().ifPresent(dto::setArbeidsforholdsid);
        }

        LOG.info("Vurderer resultat");

        var brukPath = w.getJournalførendeEnhet()
            .filter(NK_ENHET_ID::equals)
            .isPresent() ? klageinstansEndpoint : fagsystemEndpoint;

        var request = RestRequest.newPOSTJson(dto, brukPath, restConfig);
        var respons = klient.send(request, BehandlendeFagsystemDto.class);

        var vurdering = VurderFagsystemResultat.fra(respons);

        LOG.info("Vurderert resultat OK");
        return vurdering;
    }

    private static BrukerRolleDto mapBrukerRolle(String rolle) {
        return switch (rolle) {
            case "MOR" -> BrukerRolleDto.MOR;
            case "FAR" -> BrukerRolleDto.FAR;
            case "MEDMOR" -> BrukerRolleDto.MEDMOR;
            case null, default -> null;
        };
    }

    @Override
    public List<SakInfoV2Dto> hentBrukersSaker(AktørIdDto dto) {
        LOG.info("Henter alle saker for en bruker");
        var target = UriBuilder.fromUri(finnFagsakerEndpoint).build();
        var request = RestRequest.newPOSTJson(dto, target, restConfig);
        return klient.sendReturnList(request, SakInfoV2Dto.class);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + fagsystemEndpoint + "]";
    }

    private URI lagURI(URI context, String api) {
        return UriBuilder.fromUri(context).path(api).build();
    }

}
