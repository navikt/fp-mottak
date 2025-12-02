FP-MOTTAK
===============
[![Bygg og deploy](https://github.com/navikt/fp-mottak/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/navikt/fp-mottak/actions/workflows/build.yml)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-mottak&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=navikt_fp-mottak)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-mottak&metric=coverage)](https://sonarcloud.io/summary/new_code?id=navikt_fp-mottak)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-mottak&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fp-mottak)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-mottak&metric=bugs)](https://sonarcloud.io/dashboard?id=navikt_fp-mottak)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-mottak&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=navikt_fp-mottak)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-mottak&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=navikt_fp-mottak)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-mottak&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=navikt_fp-mottak)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-mottak&metric=sqale_index)](https://sonarcloud.io/dashboard?id=navikt_fp-mottak)

Dette er kildekode som dekker applikasjonen for mottak og fordeling av søknader fra Selvbetjening mellom Gosys, Infotrygd og FPSAK/FPTIBAKE.
Løsningen prøver å journalføre andre dokumenter også - om det ikke er mulig blir de sendt til manuell journalføring.

### Struktur

Dette er løsning for mottak og fordeling av søknader (og inntektsmeldinger).

### Kontekst
#### Automatisk dokument behandling
![journalføring-diagram](docs/journalføring-diagram-Automatisk_journalføring.png)
#### Manuell dokument behandling
![manuell_journalføring-diagram-Manuell_journalføring.png](docs%2Fmanuell_journalføring-diagram-Manuell_journalføring.png)

### Utviklingshåndbok

[Utviklingoppsett](https://confluence.adeo.no/display/LVF/60+Utviklingsoppsett)
[Utviklerhåndbok, Kodestandard, osv](https://confluence.adeo.no/pages/viewpage.action?pageId=190254327)

### Sikkerhet

Det er mulig å kalle tjenesten med bruk av følgende tokens

- Azure CC
- Azure OBO med følgende rettigheter:
    - fpsak-saksbehandler - manuell journalføring
    - fpsak-veileder
    - fpsak-drift
- TokenX
- STS (fases ut)
- SAML (fases ut)

### Docker

```bash
mvn -B -Dfile.encoding=UTF-8 -DskipTests clean install

docker build -t fpmottak .
```

### Protip for å kjøre tester raskere
Finn filen .testcontainers.properties, ligger ofte på hjemmeområdet ditt eks:

```~/.testcontainers.properties```

legg til denne verdien

```testcontainers.reuse.enable=true```
