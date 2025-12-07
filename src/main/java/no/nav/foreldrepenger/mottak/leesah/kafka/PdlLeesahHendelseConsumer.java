package no.nav.foreldrepenger.mottak.leesah.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.person.pdl.leesah.Personhendelse;
import no.nav.vedtak.felles.integrasjon.kafka.KafkaConsumerManager;
import no.nav.vedtak.server.Controllable;
import no.nav.vedtak.server.LiveAndReadinessAware;

@ApplicationScoped
public class PdlLeesahHendelseConsumer implements LiveAndReadinessAware, Controllable {

    private static final Logger LOG = LoggerFactory.getLogger(PdlLeesahHendelseConsumer.class);
    private static final Environment ENV = Environment.current();

    private KafkaConsumerManager<String, Personhendelse> kcm;

    PdlLeesahHendelseConsumer() {
    }

    @Inject
    public PdlLeesahHendelseConsumer(PdlLeesahHendelseHåndterer håndterer) {
        if (ENV.isLocal()) {
            this.kcm = new KafkaConsumerManager<>(håndterer);
        }
    }

    @Override
    public boolean isAlive() {
        if (ENV.isLocal()) {
            return kcm.allRunning();
        } else {
            return true;
        }
    }

    @Override
    public boolean isReady() {
        return isAlive();
    }

    @Override
    public void start() {
        if (ENV.isLocal()) {
            LOG.info("Starter konsumering av topics={}", kcm.topicNames());
            kcm.start((t, e) -> LOG.error("{} :: Caught exception in stream, exiting", t, e));
        }
    }

    @Override
    public void stop() {
        if (ENV.isLocal()) {
            LOG.info("Starter shutdown av topics={} med 10 sekunder timeout", kcm.topicNames());
            kcm.stop();
        }
    }

}
