package no.nav.foreldrepenger.mottak.mottak.felles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SøknadInnhold extends DokumentInnhold {

    private String brukerRolle;
    private String annenPartAktørId;
    private LocalDate termindato;
    private LocalDate fødselsdato;
    private LocalDate omsorgsovertakelsesdato;
    private final List<LocalDate> adopsjonsbarnFødselsdatoer;

    public SøknadInnhold(String aktørId, LocalDate førsteFraværsdato, LocalDateTime mottattTidspunkt) {
        super(aktørId, førsteFraværsdato, mottattTidspunkt);
        this.adopsjonsbarnFødselsdatoer = new ArrayList<>();
    }

    public Optional<String> getBrukerRolle() {
        return Optional.ofNullable(brukerRolle);
    }

    public void setBrukerRolle(String rolle) {
        this.brukerRolle = rolle;
    }

    public Optional<String> getAnnenPartAktørId() {
        return Optional.ofNullable(annenPartAktørId);
    }

    public void setAnnenPartAktørId(String annenPartAktørId) {
        this.annenPartAktørId = annenPartAktørId;
    }

    public Optional<LocalDate> getTermindato() {
        return Optional.ofNullable(termindato);
    }

    public void setTermindato(LocalDate termindato) {
        this.termindato = termindato;
    }

    public Optional<LocalDate> getFødselsdato() {
        return Optional.ofNullable(fødselsdato);
    }

    public void setFødselsdato(LocalDate fødselsdato) {
        this.fødselsdato = fødselsdato;
    }

    public Optional<LocalDate> getOmsorgsovertakelsesdato() {
        return Optional.ofNullable(omsorgsovertakelsesdato);
    }

    public void setOmsorgsovertakelsesdato(LocalDate omsorgsovertakelsesdato) {
        this.omsorgsovertakelsesdato = omsorgsovertakelsesdato;
    }

    public List<LocalDate> getAdopsjonsbarnFødselsdatoer() {
        return adopsjonsbarnFødselsdatoer;
    }

    public void leggTilAdopsjonsbarnFødselsdatoer(List<LocalDate> adopsjonsbarnFødselsdatoer) {
        this.adopsjonsbarnFødselsdatoer.addAll(adopsjonsbarnFødselsdatoer);
    }
}
