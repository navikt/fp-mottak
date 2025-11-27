package no.nav.foreldrepenger.mottak.mottak.felles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema;

public final class InntektsmeldingInnhold extends DokumentInnhold {

    private String årsakTilInnsending;
    private String virksomhetsnummer;
    private String arbeidsgiverAktørId;
    private String arbeidsforholdsId;

    public InntektsmeldingInnhold(String aktørId, BehandlingTema behandlingTema, LocalDate førsteFraværsdato, LocalDateTime mottattTidspunkt) {
        super(aktørId, behandlingTema, førsteFraværsdato, mottattTidspunkt);
    }

    public Optional<String> getÅrsakTilInnsending() {
        return Optional.ofNullable(årsakTilInnsending);
    }

    public void setÅrsakTilInnsending(String årsakTilInnsending) {
        this.årsakTilInnsending = årsakTilInnsending;
    }

    public Optional<String> getVirksomhetsnummer() {
        return Optional.ofNullable(virksomhetsnummer);
    }

    public void setVirksomhetsnummer(String virksomhetsnummer) {
        this.virksomhetsnummer = virksomhetsnummer;
    }

    public Optional<String> getArbeidsgiverAktørId() {
        return Optional.ofNullable(arbeidsgiverAktørId);
    }

    public void setArbeidsgiverAktørId(String arbeidsgiverAktørId) {
        this.arbeidsgiverAktørId = arbeidsgiverAktørId;
    }

    public Optional<String> getArbeidsforholdsId() {
        return Optional.ofNullable(arbeidsforholdsId);
    }

    public void setArbeidsforholdsId(String arbeidsforholdsId) {
        this.arbeidsforholdsId = arbeidsforholdsId;
    }
}
