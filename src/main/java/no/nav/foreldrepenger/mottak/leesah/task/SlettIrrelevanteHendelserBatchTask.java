package no.nav.foreldrepenger.mottak.leesah.task;

import no.nav.foreldrepenger.mottak.leesah.tjeneste.HendelseRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(value = "slett.hendelserAndre", cronExpression = "0 1 2 * * *", maxFailedRuns = 1)
public class SlettIrrelevanteHendelserBatchTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SlettIrrelevanteHendelserBatchTask.class);

    private final HendelseRepository hendelseRepository;

    @Inject
    public SlettIrrelevanteHendelserBatchTask(HendelseRepository hendelseRepository) {
        this.hendelseRepository = hendelseRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var slettet = hendelseRepository.slettIrrelevanteHendelser();
        LOG.info("Slettet {} hendelser som ikke ble sendt til fpsak.", slettet);
        var slettet2 = hendelseRepository.slettGamleHendelser();
        slettet2 += hendelseRepository.slettGamleHendelser2();
        LOG.info("Slettet {} hendelser som er foreldet.", slettet2);
    }
}
