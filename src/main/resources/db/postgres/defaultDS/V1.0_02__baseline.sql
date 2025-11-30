-- Journalføringsoppgaver

CREATE TABLE OPPGAVE (
    JOURNALPOST_ID VARCHAR(32)  NOT NULL,
    STATUS         VARCHAR(20)  NOT NULL,
    ENHET          VARCHAR(10)  NOT NULL,
    FRIST          TIMESTAMP(3) NOT NULL,
    BRUKER_ID      VARCHAR(19),
    YTELSE_TYPE    VARCHAR(20),
    BESKRIVELSE    VARCHAR(200),
    RESERVERT_AV   VARCHAR(20),
    VERSJON        INTEGER      NOT NULL DEFAULT 0,
    OPPRETTET_AV   VARCHAR(20)  NOT NULL DEFAULT 'VL',
    OPPRETTET_TID  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ENDRET_AV      VARCHAR(20),
    ENDRET_TID     TIMESTAMP(3),
    CONSTRAINT PK_OPPGAVE PRIMARY KEY (JOURNALPOST_ID)
);

COMMENT ON TABLE OPPGAVE IS 'Inneholder oppgaver som skal løses av saksbehandlere i journalføring.';
COMMENT ON COLUMN OPPGAVE.STATUS IS 'Status av oppgaven. Foreløpig AAPENT, FERDIGSTILT.';
COMMENT ON COLUMN OPPGAVE.JOURNALPOST_ID IS 'ID til journalposten i JOARK';
COMMENT ON COLUMN OPPGAVE.BRUKER_ID IS 'ID til avsenderen av et dokument';
COMMENT ON COLUMN OPPGAVE.YTELSE_TYPE IS 'YtelseType fra kodeverk. FP, SVP, EN';
COMMENT ON COLUMN OPPGAVE.BESKRIVELSE IS 'Oppgave beskrivelse.';
COMMENT ON COLUMN OPPGAVE.ENHET IS 'Tildelt enhet som skal løse oppgaven.';
COMMENT ON COLUMN OPPGAVE.FRIST IS 'Frist dato for å løse oppgaven.';
COMMENT ON COLUMN OPPGAVE.RESERVERT_AV IS 'Lagrer identen til SBH som reserverer oppgaven.';
COMMENT ON COLUMN OPPGAVE.VERSJON IS 'Teknisk versjonering av endringer.';

CREATE INDEX idx_oppgave_status_enhet ON OPPGAVE (STATUS, ENHET);

-- Hendelser fra Folkeregisteret via PDL

create table INNGAAENDE_HENDELSE
(
    HENDELSE_ID           VARCHAR(100) NOT NULL,
    TYPE                  VARCHAR(100) NOT NULL,
    TIDLIGERE_HENDELSE_ID VARCHAR(100),
    PAYLOAD               TEXT,
    OPPRETTET_AV          VARCHAR(20)  NOT NULL DEFAULT 'VL',
    OPPRETTET_TID         TIMESTAMP(3)      default CURRENT_TIMESTAMP,
    ENDRET_AV             VARCHAR(20),
    ENDRET_TID            TIMESTAMP(3),
    HAANDTERES_ETTER      TIMESTAMP(3),
    HAANDTERT_STATUS      VARCHAR(100) NOT NULL default 'MOTTATT',
    SENDT_TID             TIMESTAMP(3),
    CONSTRAINT PK_INNGAAENDE_HENDELSE PRIMARY KEY (HENDELSE_ID)
);

comment on table INNGAAENDE_HENDELSE is 'Alle hendelser som har blitt mottatt, inkludert payload';
comment on column INNGAAENDE_HENDELSE.HENDELSE_ID is 'Unik identifikator for hendelsen innenfor angitt datakilde';
comment on column INNGAAENDE_HENDELSE.TYPE is 'Hendelsetype';
comment on column INNGAAENDE_HENDELSE.PAYLOAD is 'Innhold i hendelsen';
comment on column INNGAAENDE_HENDELSE.TIDLIGERE_HENDELSE_ID is 'Hendelsen er knyttet opp mot en tidligere hendelse som har denne verdien som HENDELSE_ID, aktuelt for eksempel ved korrigeringer';
comment on column INNGAAENDE_HENDELSE.HAANDTERES_ETTER is 'Angir tidligste tidspunkt for når hendelsen kan håndteres';
comment on column INNGAAENDE_HENDELSE.HAANDTERT_STATUS is 'Håndteringsstatusen på en hendelse';
comment on column INNGAAENDE_HENDELSE.SENDT_TID is 'Tidspunktet en hendelse ble sendt til FPSAK. Hendelsen har blitt kastet i grovsorteringen dersom SENDT_TID ikke er satt og hendelsen er ferdig håndtert.';


create index IDX_INNGAAENDE_HENDELSE_1 on INNGAAENDE_HENDELSE (HAANDTERT_STATUS);
create index IDX_INNGAAENDE_HENDELSE_2 on INNGAAENDE_HENDELSE (TYPE);
create index IDX_INNGAAENDE_HENDELSE_3 on INNGAAENDE_HENDELSE (TIDLIGERE_HENDELSE_ID);
