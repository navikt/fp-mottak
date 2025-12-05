package no.nav.foreldrepenger.mottak.leesah.task;

import no.nav.foreldrepenger.mottak.leesah.domene.HendelsePayload;
import no.nav.foreldrepenger.mottak.leesah.fpsak.HendelserKlient;

import no.nav.foreldrepenger.mottak.leesah.tjeneste.AbonnentHendelserFeil;
import no.nav.foreldrepenger.mottak.leesah.tjeneste.InngåendeHendelseTjeneste;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask("hendelser.sendHendelse")
public class SendHendelseTask implements ProsessTaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendHendelseTask.class);

    private final HendelserKlient hendelser;
    private final InngåendeHendelseTjeneste inngåendeHendelseTjeneste;

    @Inject
    public SendHendelseTask(HendelserKlient hendelser, InngåendeHendelseTjeneste inngåendeHendelseTjeneste) {
        this.hendelser = hendelser;
        this.inngåendeHendelseTjeneste = inngåendeHendelseTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var dataWrapper = new HendelserDataWrapper(prosessTaskData);
        var hendelsePayload = getHendelsePayload(dataWrapper);

        hendelser.sendHendelse(hendelsePayload);
        inngåendeHendelseTjeneste.oppdaterHendelseSomSendtNå(hendelsePayload);
        LOGGER.info("Sendt hendelse: [{}] til FPSAK.", hendelsePayload.getHendelseId());
    }

    private HendelsePayload getHendelsePayload(HendelserDataWrapper dataWrapper) {
        var inngåendeHendelseId = dataWrapper.getHendelseId()
            .orElseThrow(() -> AbonnentHendelserFeil.manglerInngåendeHendelseIdPåProsesstask(dataWrapper.getProsessTaskData().taskType(),
                dataWrapper.getProsessTaskData().getId()));
        var inngåendeHendelse = inngåendeHendelseTjeneste.finnHendelse(inngåendeHendelseId);
        return inngåendeHendelseTjeneste.hentUtPayloadFraInngåendeHendelse(inngåendeHendelse);
    }
}
