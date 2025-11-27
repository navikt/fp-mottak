package no.nav.foreldrepenger.mottak.mottak.felles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema;

public abstract sealed class DokumentInnhold permits SøknadInnhold, InntektsmeldingInnhold {

    private final String aktørId;
    private final BehandlingTema behandlingTema;
    private final LocalDate førsteFraværsdato;
    private final LocalDateTime mottattTidspunkt;
    private String payload;

    protected DokumentInnhold(String aktørId, BehandlingTema behandlingTema,
                               LocalDate førsteFraværsdato, LocalDateTime mottattTidspunkt) {
        this.aktørId = aktørId;
        this.behandlingTema = behandlingTema;
        this.førsteFraværsdato = førsteFraværsdato;
        this.mottattTidspunkt = mottattTidspunkt;
    }

    public String getAktørId() {
        return aktørId;
    }

    public BehandlingTema getBehandlingTema() {
        return behandlingTema;
    }

    public Optional<LocalDate> getFørsteFraværsdato() {
        return Optional.ofNullable(førsteFraværsdato);
    }

    public Optional<LocalDateTime> getMottattTidspunkt() {
        return Optional.ofNullable(mottattTidspunkt);
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
