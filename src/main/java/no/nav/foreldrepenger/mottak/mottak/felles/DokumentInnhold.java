package no.nav.foreldrepenger.mottak.mottak.felles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public abstract sealed class DokumentInnhold permits SøknadInnhold, InntektsmeldingInnhold {

    private final String aktørId;
    private final LocalDate førsteFraværsdato;
    private final LocalDateTime mottattTidspunkt;

    protected  DokumentInnhold(String aktørId, LocalDate førsteFraværsdato, LocalDateTime mottattTidspunkt) {
        this.aktørId = aktørId;
        this.førsteFraværsdato = førsteFraværsdato;
        this.mottattTidspunkt = mottattTidspunkt;
    }

    public String getAktørId() {
        return aktørId;
    }

    public Optional<LocalDate> getFørsteFraværsdato() {
        return Optional.ofNullable(førsteFraværsdato);
    }

    public Optional<LocalDateTime> getMottattTidspunkt() {
        return Optional.ofNullable(mottattTidspunkt);
    }
}
