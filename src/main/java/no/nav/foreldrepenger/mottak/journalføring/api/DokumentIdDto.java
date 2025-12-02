package no.nav.foreldrepenger.mottak.journalføring.api;

import jakarta.validation.constraints.Digits;

public record DokumentIdDto(@Digits(integer = 18, fraction = 0) String dokumentId) {
}
