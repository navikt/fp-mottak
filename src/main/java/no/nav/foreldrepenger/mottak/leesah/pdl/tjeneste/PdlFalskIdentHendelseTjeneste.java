package no.nav.foreldrepenger.mottak.leesah.pdl.tjeneste;


import java.util.Set;

import no.nav.foreldrepenger.mottak.leesah.domene.HendelseOpplysningType;
import no.nav.foreldrepenger.mottak.leesah.domene.KlarForSorteringResultat;
import no.nav.foreldrepenger.mottak.leesah.domene.eksternt.PdlFalskIdentitet;
import no.nav.foreldrepenger.mottak.leesah.domene.internt.PdlFalskIdentHendelsePayload;
import no.nav.foreldrepenger.mottak.leesah.pdl.oppslag.UtflyttingsDatoTjeneste;
import no.nav.foreldrepenger.mottak.leesah.tjeneste.HendelseTjeneste;
import no.nav.foreldrepenger.mottak.leesah.tjeneste.HendelseTypeRef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

import static no.nav.foreldrepenger.mottak.leesah.pdl.tjeneste.HendelseTjenesteHjelper.hentUtAktørIderFraString;


@ApplicationScoped
@HendelseTypeRef(HendelseOpplysningType.PDL_FALSKIDENT_HENDELSE)
public class PdlFalskIdentHendelseTjeneste implements HendelseTjeneste<PdlFalskIdentHendelsePayload> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdlFalskIdentHendelseTjeneste.class);

    private HendelseTjenesteHjelper hendelseTjenesteHjelper;

    public PdlFalskIdentHendelseTjeneste() {
        // CDI
    }

    @Inject
    public PdlFalskIdentHendelseTjeneste(HendelseTjenesteHjelper hendelseTjenesteHjelper, UtflyttingsDatoTjeneste utflyttingTjeneste) {
        this.hendelseTjenesteHjelper = hendelseTjenesteHjelper;
    }

    @Override
    public PdlFalskIdentHendelsePayload payloadFraJsonString(String payload) {
        var pdlFalskIdentitet = DefaultJsonMapper.fromJson(payload, PdlFalskIdentitet.class);

        return new PdlFalskIdentHendelsePayload.Builder().hendelseId(pdlFalskIdentitet.getHendelseId())
            .tidligereHendelseId(pdlFalskIdentitet.getTidligereHendelseId())
            .hendelseType(pdlFalskIdentitet.getHendelseType().getKode())
            .endringstype(pdlFalskIdentitet.getEndringstype().name())
            .hendelseOpprettetTid(pdlFalskIdentitet.getOpprettet())
            .aktørId(hentUtAktørIderFraString(pdlFalskIdentitet.getPersonidenter(), pdlFalskIdentitet.getHendelseId()))
            .erFalsk(pdlFalskIdentitet.getErFalsk())
            .build();
    }

    @Override
    public boolean vurderOmHendelseKanForkastes(PdlFalskIdentHendelsePayload payload) {
        return hendelseTjenesteHjelper.vurderOmHendelseKanForkastes(payload, this::payloadFraJsonString);
    }

    @Override
    public KlarForSorteringResultat vurderOmKlarForSortering(PdlFalskIdentHendelsePayload payload) {
        var aktuellAktør = payload.getAktørId().orElse(Set.of()).stream().findFirst();
        if (aktuellAktør.isEmpty()) {
            LOGGER.warn("Hendelse {} med type {} har ikke aktørid", payload.getHendelseId(), payload.getHendelseType());
            return new FalskIdentKlarForSorteringResultat(false, false);
        }
        return new FalskIdentKlarForSorteringResultat(true);
    }


    @Override
    public void loggFeiletHendelse(PdlFalskIdentHendelsePayload payload) {
        String basismelding = "Hendelse {} med type {} som ble opprettet {} kan fremdeles ikke sorteres og blir derfor ikke behandlet videre. {}";
        if (payload.getAktørId().isEmpty()) {
            LOGGER.warn(basismelding, "Årsaken er at aktørId mangler på hendelsen.", payload.getHendelseId(), payload.getHendelseType(),
                payload.getHendelseOpprettetTid());
        } else {
            LOGGER.warn(basismelding, "Årsaken er ukjent - bør undersøkes av utvikler.", payload.getHendelseId(), payload.getHendelseType(),
                payload.getHendelseOpprettetTid());
        }

    }

    private static class FalskIdentKlarForSorteringResultat extends KlarForSorteringResultat {

        public FalskIdentKlarForSorteringResultat(boolean resultat) {
            super(resultat);
        }

        public FalskIdentKlarForSorteringResultat(boolean resultat, boolean prøveIgjen) {
            super(resultat, prøveIgjen);
        }
    }
}
