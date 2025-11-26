package no.nav.foreldrepenger.mottak.mottak.domene.v1;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;
import no.nav.foreldrepenger.mottak.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.mottak.felles.DokumentInnhold;
import no.nav.foreldrepenger.mottak.mottak.felles.InntektsmeldingInnhold;
import no.nav.foreldrepenger.mottak.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Skjemainnhold;

public class Inntektsmelding extends MottattStrukturertDokument<InntektsmeldingM> {

    private static final Logger LOG = LoggerFactory.getLogger(Inntektsmelding.class);

    public Inntektsmelding(InntektsmeldingM skjema) {
        super(skjema);
    }

    @Override
    protected DokumentInnhold hentUtDokumentInnhold(Function<String, Optional<String>> aktørIdFinder) {
        var innhold = new InntektsmeldingInnhold(hentBrukerAktørId(aktørIdFinder), getStartdatoForeldrepengeperiode(), null);
        innhold.setInntektsmeldingYtelse(getYtelse());
        innhold.setÅrsakTilInnsending(getÅrsakTilInnsending());
        innhold.setVirksomhetsnummer(getVirksomhetsnummer());
        getArbeidsforholdsid().ifPresent(innhold::setArbeidsforholdsId);
        return innhold;
    }

    public String hentBrukerAktørId(Function<String, Optional<String>> aktørIdFinder) {
        Optional<String> aktørId = aktørIdFinder.apply(getArbeidstakerFnr());
        if (aktørId.isEmpty()) {
            LOG.warn(new TekniskException("FP-513732",
                String.format("Finner ikke aktørID for bruker på %s", this.getClass().getSimpleName())).getMessage());
        }
        return aktørId.orElse(null);
    }

    @Override
    protected void validerSkjemaSemantisk(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        Optional<String> aktørIdFraSkjema = aktørIdFinder.apply(getArbeidstakerFnr());
        Optional<String> aktørIdFraJournalpost = dataWrapper.getAktørId();
        if (aktørIdFraJournalpost.isPresent()) {
            if (aktørIdFraSkjema.filter(aktørIdFraJournalpost.get()::equals).isEmpty()) {
                throw new FunksjonellException("FP-401246", "Ulike personer i journalpost og inntektsmelding.", null);
            }
        }
    }

    private LocalDate getStartdatoForeldrepengeperiode() {
        final Skjemainnhold skjemainnhold = getSkjema().getSkjemainnhold();
        if (skjemainnhold == null) {
            return null;
        }
        final JAXBElement<LocalDate> startdatoForeldrepengeperiode = skjemainnhold.getStartdatoForeldrepengeperiode();
        if (startdatoForeldrepengeperiode == null) {
            return null;
        }
        return startdatoForeldrepengeperiode.getValue();
    }

    private String getÅrsakTilInnsending() {
        return getSkjema().getSkjemainnhold().getAarsakTilInnsending();
    }

    private String getArbeidstakerFnr() {
        return getSkjema().getSkjemainnhold().getArbeidstakerFnr();
    }

    private String getVirksomhetsnummer() {
        return getSkjema().getSkjemainnhold().getArbeidsgiver().getVirksomhetsnummer();
    }

    private String getYtelse() {
        return getSkjema().getSkjemainnhold().getYtelse();
    }

    private Optional<String> getArbeidsforholdsid() {
        if ((getSkjema().getSkjemainnhold().getArbeidsforhold() != null) && (
            getSkjema().getSkjemainnhold().getArbeidsforhold().getValue().getArbeidsforholdId() != null)) {
            return Optional.ofNullable(getSkjema().getSkjemainnhold().getArbeidsforhold().getValue().getArbeidsforholdId().getValue());
        }
        return Optional.empty();
    }
}
