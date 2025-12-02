package no.nav.foreldrepenger.mottak.leesah.pdl.tjeneste;

import static no.nav.foreldrepenger.mottak.leesah.pdl.tjeneste.HendelseTjenesteHjelper.hentUtAktørIderFraString;

import no.nav.foreldrepenger.mottak.leesah.domene.HendelseOpplysningType;
import no.nav.foreldrepenger.mottak.leesah.domene.KlarForSorteringResultat;
import no.nav.foreldrepenger.mottak.leesah.domene.eksternt.PdlDødfødsel;
import no.nav.foreldrepenger.mottak.leesah.domene.eksternt.PdlEndringstype;
import no.nav.foreldrepenger.mottak.leesah.domene.internt.PdlDødfødselHendelsePayload;
import no.nav.foreldrepenger.mottak.leesah.tjeneste.HendelseTjeneste;
import no.nav.foreldrepenger.mottak.leesah.tjeneste.HendelseTypeRef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
@HendelseTypeRef(HendelseOpplysningType.PDL_DØDFØDSEL_HENDELSE)
public class PdlDødfødselHendelseTjeneste implements HendelseTjeneste<PdlDødfødselHendelsePayload> {

    private static final Logger LOG = LoggerFactory.getLogger(PdlDødfødselHendelseTjeneste.class);

    private HendelseTjenesteHjelper hendelseTjenesteHjelper;

    public PdlDødfødselHendelseTjeneste() {
        // CDI
    }

    @Inject
    public PdlDødfødselHendelseTjeneste(HendelseTjenesteHjelper hendelseTjenesteHjelper) {
        this.hendelseTjenesteHjelper = hendelseTjenesteHjelper;
    }

    @Override
    public PdlDødfødselHendelsePayload payloadFraJsonString(String payload) {
        var pdlDødfødsel = DefaultJsonMapper.fromJson(payload, PdlDødfødsel.class);

        return new PdlDødfødselHendelsePayload.Builder().hendelseId(pdlDødfødsel.getHendelseId())
            .tidligereHendelseId(pdlDødfødsel.getTidligereHendelseId())
            .hendelseType(pdlDødfødsel.getHendelseType().getKode())
            .endringstype(pdlDødfødsel.getEndringstype().name())
            .hendelseOpprettetTid(pdlDødfødsel.getOpprettet())
            .aktørId(hentUtAktørIderFraString(pdlDødfødsel.getPersonidenter(), pdlDødfødsel.getHendelseId()))
            .dødfødselsdato(pdlDødfødsel.getDødfødselsdato())
            .build();
    }

    @Override
    public boolean vurderOmHendelseKanForkastes(PdlDødfødselHendelsePayload payload) {
        return hendelseTjenesteHjelper.vurderOmHendelseKanForkastes(payload, this::payloadFraJsonString);
    }

    @Override
    public KlarForSorteringResultat vurderOmKlarForSortering(PdlDødfødselHendelsePayload payload) {
        if (payload.getAktørId().isPresent() && (payload.getDødfødselsdato().isPresent()
            || payload.getDødfødselsdato().isEmpty() && PdlEndringstype.ANNULLERT.name().equals(payload.getEndringstype()))) {
            return new KlarForSorteringResultat(true);
        }
        return new KlarForSorteringResultat(false);
    }

    @Override
    public void loggFeiletHendelse(PdlDødfødselHendelsePayload payload) {
        var basismelding = "Hendelse {} med type {} som ble opprettet {} kan fremdeles ikke sorteres og blir derfor ikke behandlet videre. ";
        var årsak = "Årsaken er ukjent - bør undersøkes av utvikler.";
        if (payload.getDødfødselsdato().isEmpty()) {
            årsak = "Årsaken er at dødfødselsdato mangler på hendelsen.";
        } else if (payload.getAktørId().isEmpty()) {
            årsak = "Årsaken er at aktørId mangler på hendelsen.";
        }
        var melding = basismelding + årsak;
        if (LOG.isWarnEnabled()) {
            LOG.warn(melding, payload.getHendelseId(), payload.getHendelseType(), payload.getHendelseOpprettetTid());
        }
    }
}
