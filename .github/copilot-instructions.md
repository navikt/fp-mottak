# fp-mottak

Receives and routes incoming documents and person events into the Foreldrepenger value chain.

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

## Repo-specific context

| Topic             | Details                                                            |
|-------------------|--------------------------------------------------------------------|
| Role              | Handles receipt, routing, and journalforing for incoming documents |
| Consumers         | Repo `fp-frontend`, app `fp-journalforing`                         |
| Tech stack        | Standard fp Java backend using `fp-prosesstask`; Avro schemas      |
| Main integrations | Joark, PDL, Gosys/Oppgave, `fp-sak`, `fptilbake`                   |
| Data              | PostgreSQL temporary storage of person events and journaling tasks |

- Document event flow: Try to route to existing/new case. Successful: finalize journaling and send to `fp-sak`. Unsuccessful: create `OppgaveEntitet`
- Manual journaling: Supply missing information and select a sak (from `fp-sak` or general person folder). Finalize journaling and send to `fp-sak`.
- Interaction central Oppgave system: May move journaling tasks to/from central system and use Gosys to finalize journaling.
- Person events flow: Initial delay to catch corrections, check if relevant for existing cases: Either submit to `fp-sak` or delete the event.

## Entry points

- `JournalHendelseConsumer`: Kafka consumer for incoming documents - handling journal entries that are not finalized
- `PdlLeesahHendelseConsumer`: Kafka consumer for Folkeregister / PDL person events - handling birth, death, emigration after a 24-hour delay
- `JournalføringRestTjeneste` and `FerdigstillJournalføringRestTjeneste`: Services for the manual journaling frontend

## Verification

- For integration impact, verify via `navikt/fp-autotest`.
- Most relevant suite: `verdikjede`.
