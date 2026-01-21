package no.nav.foreldrepenger.mottak.mottak.felles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema;

public final class SøknadInnhold extends DokumentInnhold {

    private String brukerRolle;
    private String annenPartAktørId;
    private LocalDate termindato;
    private LocalDate fødselsdato;
    private LocalDate omsorgsovertakelsesdato;
    private List<LocalDate> adopsjonsbarnFødselsdatoer;
    private String saksnummer;

    public SøknadInnhold(String aktørId, BehandlingTema behandlingTema, LocalDate førsteFraværsdato, LocalDateTime mottattTidspunkt) {
        super(aktørId, behandlingTema, førsteFraværsdato, mottattTidspunkt);
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
        if (this.adopsjonsbarnFødselsdatoer == null) {
            this.adopsjonsbarnFødselsdatoer = new ArrayList<>();
        }
        this.adopsjonsbarnFødselsdatoer.addAll(adopsjonsbarnFødselsdatoer);
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }
}
