package no.nav.foreldrepenger.mottak.leesah.migrer;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.mottak.leesah.domene.HendelseType;
import no.nav.foreldrepenger.mottak.leesah.domene.HåndtertStatusType;


public record MigreringHendelseDto(@Valid HendelseType type,
                              @Size @Pattern(regexp = "^[\\p{P}\\p{L}\\p{N}\\p{Alnum}\\p{Punct}\\p{Space}\\\\_.\\-]*$") String payload,
                              LocalDateTime haandteresEtter,
                              @Valid HåndtertStatusType haandtertStatus,
                              LocalDateTime sendtTid,
                              @Size @Pattern(regexp = "^[\\p{P}\\p{L}\\p{N}\\p{Alnum}\\p{Punct}\\p{Space}_.\\-]*$") String hendelseId,
                              @Size @Pattern(regexp = "^[\\p{P}\\p{L}\\p{N}\\p{Alnum}\\p{Punct}\\p{Space}_.\\-]*$") String tidligereHendelseId,
                              LocalDateTime opprettetTid) {
}

