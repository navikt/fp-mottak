package no.nav.foreldrepenger.mottak.journalføring.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum YtelseTypeDto {
    @JsonProperty("ES")
    ENGANGSTØNAD,
    @JsonProperty("FP")
    FORELDREPENGER,
    @JsonProperty("SVP")
    SVANGERSKAPSPENGER,
}
