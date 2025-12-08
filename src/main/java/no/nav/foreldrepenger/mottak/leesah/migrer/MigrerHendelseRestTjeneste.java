package no.nav.foreldrepenger.mottak.leesah.migrer;

import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.mottak.leesah.domene.InngåendeHendelse;
import no.nav.foreldrepenger.mottak.leesah.tjeneste.HendelseRepository;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

/**
 * Enkelt REST tjeneste for å oppdatere og ferdigstille journalføring på dokumenter som kunne ikke
 * journalføres automatisk på fpsak saker. Brukes for å klargjøre og sende over saken til videre behandling i VL.
 * Gir mulighet å opprette saken i fpsak og så journalføre dokumentet på den nye saken.
 */
@Path("/migrer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Transactional
public class MigrerHendelseRestTjeneste {

    private HendelseRepository hendelseRepository;
    private static final Logger LOG = LoggerFactory.getLogger(MigrerHendelseRestTjeneste.class);



    protected MigrerHendelseRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public MigrerHendelseRestTjeneste(HendelseRepository hendelseRepository) {
        this.hendelseRepository = hendelseRepository;
    }

    @POST
    @Operation(description = "Lagrer hendelser som skal migreres", tags = "Migrering",
        summary = ("Lagre hendelser som skal migreres"),
        responses = {@ApiResponse(responseCode = "200", description = "Hendelser")})
    @Path("/hendelse")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = false)
    public Response lagreHendelser(@TilpassetAbacAttributt(supplierClass = MigreringAbacSupplier.class)
                                   @NotNull @Parameter(name = "hendelser") @Valid MigreringHendelseDto hendelse) {
        lagreEllerOppdater(hendelse);
        return Response.ok().build();
    }

    private void lagreEllerOppdater(MigreringHendelseDto hendelse) {
        var eksisterende = hendelseRepository.finnHendelseFraIdHvisFinnes(hendelse.hendelseId()).orElse(null);
        if (eksisterende != null) {
            LOG.info("Mottok og lagret eksisterende hendelse id {} av type {}", hendelse.hendelseId(), hendelse.type().name());
            /* Ikke i runde 1 i mottak. Andre migrering - oppdater dersom ulik */
            return;
            //eksisterende.setHåndtertStatus(hendelseDto.haandtertStatus());
            //eksisterende.setHåndteresEtterTidspunkt(hendelseDto.haandteresEtter());
            //eksisterende.setSendtTidspunkt(hendelseDto.sendtTid());
            //hendelseRepository.lagreInngåendeHendelse(eksisterende);
        } else {
            var nyHendelse = fraHendelseDto(hendelse);
            Optional.ofNullable(hendelse.opprettetTid()).ifPresent(nyHendelse::setOpprettetTidspunkt);
            hendelseRepository.lagreInngåendeHendelse(nyHendelse);
            LOG.info("Mottok og lagret hendelse id {} av type {}", hendelse.hendelseId(), hendelse.type().name());
        }
    }

    private static InngåendeHendelse fraHendelseDto(MigreringHendelseDto hendelse) {
        return InngåendeHendelse.builder()
            .hendelseType(hendelse.type())
            .payload(hendelse.payload())
            .håndteresEtterTidspunkt(hendelse.haandteresEtter())
            .håndtertStatus(hendelse.haandtertStatus())
            .sendtTidspunkt(hendelse.sendtTid())
            .hendelseId(hendelse.hendelseId())
            .tidligereHendelseId(hendelse.tidligereHendelseId())
            .build();
    }

    public static class MigreringAbacSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }

}
