package no.nav.foreldrepenger.mottak.leesah.tjeneste;


import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.mottak.leesah.domene.HendelsePayload;
import no.nav.foreldrepenger.mottak.leesah.domene.HåndtertStatusType;
import no.nav.foreldrepenger.mottak.leesah.domene.InngåendeHendelse;

import static no.nav.foreldrepenger.mottak.leesah.domene.HåndtertStatusType.HÅNDTERT;


@ApplicationScoped
public class InngåendeHendelseTjeneste {

    private HendelseRepository repo;
    private HendelseTjenesteProvider hendelseTjenesteProvider;

    InngåendeHendelseTjeneste() {
        // CDI
    }

    @Inject
    public InngåendeHendelseTjeneste(HendelseRepository repo, HendelseTjenesteProvider hendelseTjenesteProvider) {
        this.repo = repo;
        this.hendelseTjenesteProvider = hendelseTjenesteProvider;
    }

    public InngåendeHendelse finnHendelse(String hendelseId) {
        return repo.finnHendelseFraIdHvisFinnes(hendelseId).orElseThrow();
    }

    public Optional<InngåendeHendelse> finnHendelseSomErSendtTilSortering(String hendelseId) {
        Objects.requireNonNull(hendelseId, "mangler hendelseId for inngående hendelse");
        return repo.finnHendelseSomErSendtTilSortering(hendelseId);
    }

    public void oppdaterHåndtertStatus(InngåendeHendelse h, HåndtertStatusType type) {
        repo.oppdaterHåndtertStatus(h, type);
    }

    public void oppdaterHendelseSomSendtNå(HendelsePayload hendelsePayload) {
        repo.finnGrovsortertHendelse(hendelsePayload.getHendelseId()).ifPresent(h -> {
            repo.markerHendelseSomSendtNå(h);
            repo.oppdaterHåndtertStatus(h, HÅNDTERT);
        });
    }

    public void markerHendelseSomHåndtertOgFjernPayload(InngåendeHendelse h) {
        repo.oppdaterHåndtertStatus(h, HÅNDTERT);
        repo.fjernPayload(h);
    }

    public void fjernPayloadTidligereHendelser(InngåendeHendelse h) {
        var tidligereHendelseId = h.getTidligereHendelseId();
        while (tidligereHendelseId != null) {
            var tidligereHendelse = repo.finnHendelseFraIdHvisFinnes(tidligereHendelseId);
            tidligereHendelse.filter(th -> th.getPayload() != null && !th.erSendtTilFpsak()).ifPresent(th -> {
                repo.fjernPayload(th);
                repo.lagreInngåendeHendelse(th);
            });
            tidligereHendelseId = tidligereHendelse.map(InngåendeHendelse::getTidligereHendelseId).orElse(null);
        }
        repo.oppdaterHåndtertStatus(h, HÅNDTERT);
        repo.fjernPayload(h);
    }

    public HendelsePayload hentUtPayloadFraInngåendeHendelse(InngåendeHendelse h) {
        return hendelseTjenesteProvider.finnTjeneste(h.getHendelseType(), h.getHendelseId()).payloadFraJsonString(h.getPayload());
    }
}
