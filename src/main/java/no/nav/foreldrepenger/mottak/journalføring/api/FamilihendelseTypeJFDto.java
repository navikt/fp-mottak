package no.nav.foreldrepenger.mottak.journalføring.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FamilihendelseTypeJFDto {
    @JsonProperty("FODSL")
    FØDSEL,
    @JsonProperty("TERM")
    TERMIN,
    @JsonProperty("ADPSJN")
    ADOPSJON,
    @JsonProperty("OMSRGO")
    OMSORG
}
