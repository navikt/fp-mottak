package no.nav.foreldrepenger.mottak.leesah.testutilities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import no.nav.foreldrepenger.mottak.leesah.domene.AktørId;
import no.nav.foreldrepenger.mottak.leesah.domene.HendelseType;
import no.nav.foreldrepenger.mottak.leesah.domene.HåndtertStatusType;
import no.nav.foreldrepenger.mottak.leesah.domene.InngåendeHendelse;
import no.nav.foreldrepenger.mottak.leesah.domene.eksternt.PdlEndringstype;
import no.nav.foreldrepenger.mottak.leesah.domene.eksternt.PdlFødsel;
import no.nav.foreldrepenger.mottak.leesah.domene.internt.PdlFødselHendelsePayload;



public class HendelseTestDataUtil {

    public static final HendelseType MELDINGSTYPE = HendelseType.PDL_FØDSEL_OPPRETTET;
    public static final String HENDELSE_ID = UUID.randomUUID().toString();
    public static final LocalDate FØDSELSDATO = LocalDate.of(2018, 1, 30);
    public static final String AKTØR_ID_BARN = AktørId.dummy().getId();
    public static final String AKTØR_ID_MOR = AktørId.dummy().getId();
    public static final String AKTØR_ID_FAR = AktørId.dummy().getId();

    public static PdlFødsel lagFødselsmelding() {
        PdlFødsel.Builder fødsel = new PdlFødsel.Builder();
        fødsel.medHendelseId(HENDELSE_ID);
        fødsel.medHendelseType(MELDINGSTYPE);
        fødsel.medEndringstype(PdlEndringstype.OPPRETTET);
        fødsel.medFødselsdato(FØDSELSDATO);
        fødsel.leggTilPersonident(AKTØR_ID_BARN);

        PdlFødsel pdlFødsel = fødsel.build();
        pdlFødsel.setAktørIdForeldre(Set.of(AKTØR_ID_MOR, AKTØR_ID_FAR));

        return pdlFødsel;
    }

    public static PdlFødsel lagFødselsmelding(Set<String> aktørIdBarn, Set<String> aktørIdForeldre, LocalDate fødselsdato) {
        return lagFødselsmelding(HENDELSE_ID, aktørIdBarn, aktørIdForeldre, fødselsdato);
    }

    public static PdlFødsel lagFødselsmelding(String hendelseId, Set<String> aktørIdBarn, Set<String> aktørIdForeldre, LocalDate fødselsdato) {
        PdlFødsel.Builder fødsel = new PdlFødsel.Builder();
        fødsel.medHendelseId(hendelseId);
        fødsel.medHendelseType(MELDINGSTYPE);
        fødsel.medEndringstype(PdlEndringstype.OPPRETTET);
        fødsel.medFødselsdato(fødselsdato);
        aktørIdBarn.stream().forEach(fødsel::leggTilPersonident);

        PdlFødsel pdlFødsel = fødsel.build();
        pdlFødsel.setAktørIdForeldre(aktørIdForeldre);

        return pdlFødsel;
    }

    public static PdlFødselHendelsePayload lagFødselsHendelsePayload() {
        PdlFødselHendelsePayload.Builder builder = new PdlFødselHendelsePayload.Builder();
        return builder.hendelseId(HENDELSE_ID)
            .hendelseType(MELDINGSTYPE.getKode())
            .endringstype("OPPRETTET")
            .aktørIdBarn(new HashSet<>(List.of(AKTØR_ID_BARN)))
            .aktørIdForeldre(Set.of(AKTØR_ID_MOR, AKTØR_ID_FAR))
            .fødselsdato(FØDSELSDATO)
            .build();
    }

    public static InngåendeHendelse lagInngåendeFødselsHendelse(String hendelseId, HåndtertStatusType håndtertStatus) {
        return InngåendeHendelse.builder()
            .hendelseId(hendelseId)
            .hendelseType(MELDINGSTYPE)
            .payload("payload")
            .håndtertStatus(håndtertStatus)
            .build();
    }
}
