package no.nav.foreldrepenger.mottak.leesah.domene;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import no.nav.foreldrepenger.mottak.fordel.BaseEntitet;

@Entity(name = "InngåendeHendelse")
@Table(name = "INNGAAENDE_HENDELSE")
public class InngåendeHendelse extends BaseEntitet {

    @Id
    @NaturalId
    @Column(name = "hendelse_id", nullable = false, updatable = false, unique = true)
    private String hendelseId;

    @Convert(converter = HendelseType.KodeverdiConverter.class)
    @Column(name = "type", nullable = false)
    private HendelseType hendelseType;

    @Lob
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "tidligere_hendelse_id")
    private String tidligereHendelseId;

    @Column(name = "haandteres_etter")
    private LocalDateTime håndteresEtterTidspunkt;

    @Convert(converter = HåndtertStatusType.KodeverdiConverter.class)
    @Column(name = "haandtert_status", nullable = false)
    private HåndtertStatusType håndtertStatus = HåndtertStatusType.MOTTATT;

    @Column(name = "sendt_tid")
    private LocalDateTime sendtTidspunkt;

    protected InngåendeHendelse() {
        // Hibernate
    }

    private InngåendeHendelse(Builder builder) {
        this.hendelseId = builder.hendelseId;
        this.tidligereHendelseId = builder.tidligereHendelseId;
        this.hendelseType = builder.hendelseType;
        this.payload = builder.payload;
        this.håndteresEtterTidspunkt = builder.håndteresEtterTidspunkt;
        this.håndtertStatus = builder.håndtertStatus;
        this.sendtTidspunkt = builder.sendtTidspunkt;
    }

    public String getHendelseId() {
        return hendelseId;
    }

    public UUID getHendelseIdAsUuid() {
        return UUID.fromString(hendelseId);
    }

    public String getTidligereHendelseId() {
        return tidligereHendelseId;
    }

    public HendelseType getHendelseType() {
        return hendelseType;
    }

    public LocalDateTime getSendtTidspunkt() {
        return sendtTidspunkt;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getHåndteresEtterTidspunkt() {
        return håndteresEtterTidspunkt;
    }

    public void setHåndteresEtterTidspunkt(LocalDateTime håndteresEtterTidspunkt) {
        this.håndteresEtterTidspunkt = håndteresEtterTidspunkt;
    }

    public HåndtertStatusType getHåndtertStatus() {
        return håndtertStatus;
    }

    public void setHåndtertStatus(HåndtertStatusType håndtertStatus) {
        this.håndtertStatus = håndtertStatus;
    }

    public void setSendtTidspunkt(LocalDateTime sendtTidspunkt) {
        this.sendtTidspunkt = sendtTidspunkt;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean erSendtTilFpsak() {
        return HåndtertStatusType.HÅNDTERT.equals(håndtertStatus) && sendtTidspunkt != null;
    }

    public boolean erFerdigbehandletMenIkkeSendtTilFpsak() {
        return HåndtertStatusType.HÅNDTERT.equals(håndtertStatus) && sendtTidspunkt == null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String hendelseId;
        private String tidligereHendelseId;
        private HendelseType hendelseType;
        private String payload;
        private LocalDateTime håndteresEtterTidspunkt;
        private HåndtertStatusType håndtertStatus;
        private LocalDateTime sendtTidspunkt;

        public Builder hendelseId(String hendelseId) {
            this.hendelseId = hendelseId;
            return this;
        }

        public Builder tidligereHendelseId(String tidligereHendelseId) {
            this.tidligereHendelseId = tidligereHendelseId;
            return this;
        }

        public Builder hendelseType(HendelseType hendelseType) {
            this.hendelseType = hendelseType;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder håndteresEtterTidspunkt(LocalDateTime håndteresEtterTidspunkt) {
            this.håndteresEtterTidspunkt = håndteresEtterTidspunkt;
            return this;
        }

        public Builder håndtertStatus(HåndtertStatusType håndtertStatus) {
            this.håndtertStatus = håndtertStatus;
            return this;
        }

        public Builder sendtTidspunkt(LocalDateTime sendtTidspunkt) {
            this.sendtTidspunkt = sendtTidspunkt;
            return this;
        }

        public InngåendeHendelse build() {
            return new InngåendeHendelse(this);
        }
    }
}
