package no.nav.foreldrepenger.mottak.journalføring.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.vedtak.util.Fritekst;

public record OppdaterJournalpostMedTittelDto(@Valid @Fritekst String journalpostTittel,
                                              @Size List<OppdaterJournalpostMedTittelDto.@Valid DokummenterMedTitler> dokumenter) {
    public record DokummenterMedTitler(@NotNull @Digits(integer = 18, fraction = 0) String dokumentId, @NotNull @Fritekst String tittel) {
    }
}
