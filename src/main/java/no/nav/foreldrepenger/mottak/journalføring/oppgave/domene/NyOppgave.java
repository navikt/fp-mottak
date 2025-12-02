package no.nav.foreldrepenger.mottak.journalføring.oppgave.domene;

import no.nav.foreldrepenger.mottak.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.mottak.journalføring.oppgave.lager.AktørId;

public record NyOppgave(JournalpostId journalpostId, String enhetId, AktørId aktørId, String saksref, BehandlingTema behandlingTema, String beskrivelse) {
    public static NyOppgaveBuilder builder() {
        return new NyOppgaveBuilder();
    }
}
