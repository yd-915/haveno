/*
 * This file is part of Haveno.
 *
 * Haveno is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Haveno is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Haveno. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.support.dispute;

import bisq.core.support.messages.ChatMessage;

import bisq.common.proto.ProtoUtil;
import bisq.common.proto.network.NetworkPayload;
import bisq.common.util.Utilities;

import org.bitcoinj.core.Coin;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;
import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

@EqualsAndHashCode
@Getter
@Slf4j
public final class DisputeResult implements NetworkPayload {

    public enum Winner {
        BUYER,
        SELLER
    }

    public enum Reason {
        OTHER,
        BUG,
        USABILITY,
        SCAM,               // Not used anymore
        PROTOCOL_VIOLATION, // Not used anymore
        NO_REPLY,           // Not used anymore
        BANK_PROBLEMS,
        OPTION_TRADE,
        SELLER_NOT_RESPONDING,
        WRONG_SENDER_ACCOUNT,
        TRADE_ALREADY_SETTLED,
        PEER_WAS_LATE
    }

    private final String tradeId;
    private final int traderId;
    @Setter
    @Nullable
    private Winner winner;
    private int reasonOrdinal = Reason.OTHER.ordinal();
    private final BooleanProperty tamperProofEvidenceProperty = new SimpleBooleanProperty();
    private final BooleanProperty idVerificationProperty = new SimpleBooleanProperty();
    private final BooleanProperty screenCastProperty = new SimpleBooleanProperty();
    private final StringProperty summaryNotesProperty = new SimpleStringProperty("");
    @Setter
    @Nullable
    private ChatMessage chatMessage;
    @Setter
    @Nullable
    private byte[] arbitratorSignature;
    private long buyerPayoutAmount;
    private long sellerPayoutAmount;
    @Setter
    @Nullable
    private byte[] arbitratorPubKey;
    private long closeDate;

    public DisputeResult(String tradeId, int traderId) {
        this.tradeId = tradeId;
        this.traderId = traderId;
    }

    public DisputeResult(String tradeId,
                         int traderId,
                         @Nullable Winner winner,
                         int reasonOrdinal,
                         boolean tamperProofEvidence,
                         boolean idVerification,
                         boolean screenCast,
                         String summaryNotes,
                         @Nullable ChatMessage chatMessage,
                         @Nullable byte[] arbitratorSignature,
                         long buyerPayoutAmount,
                         long sellerPayoutAmount,
                         @Nullable byte[] arbitratorPubKey,
                         long closeDate) {
        this.tradeId = tradeId;
        this.traderId = traderId;
        this.winner = winner;
        this.reasonOrdinal = reasonOrdinal;
        this.tamperProofEvidenceProperty.set(tamperProofEvidence);
        this.idVerificationProperty.set(idVerification);
        this.screenCastProperty.set(screenCast);
        this.summaryNotesProperty.set(summaryNotes);
        this.chatMessage = chatMessage;
        this.arbitratorSignature = arbitratorSignature;
        this.buyerPayoutAmount = buyerPayoutAmount;
        this.sellerPayoutAmount = sellerPayoutAmount;
        this.arbitratorPubKey = arbitratorPubKey;
        this.closeDate = closeDate;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    public static DisputeResult fromProto(protobuf.DisputeResult proto) {
        return new DisputeResult(proto.getTradeId(),
                proto.getTraderId(),
                ProtoUtil.enumFromProto(DisputeResult.Winner.class, proto.getWinner().name()),
                proto.getReasonOrdinal(),
                proto.getTamperProofEvidence(),
                proto.getIdVerification(),
                proto.getScreenCast(),
                proto.getSummaryNotes(),
                proto.getChatMessage() == null ? null : ChatMessage.fromPayloadProto(proto.getChatMessage()),
                proto.getArbitratorSignature().toByteArray(),
                proto.getBuyerPayoutAmount(),
                proto.getSellerPayoutAmount(),
                proto.getArbitratorPubKey().toByteArray(),
                proto.getCloseDate());
    }

    @Override
    public protobuf.DisputeResult toProtoMessage() {
        final protobuf.DisputeResult.Builder builder = protobuf.DisputeResult.newBuilder()
                .setTradeId(tradeId)
                .setTraderId(traderId)
                .setReasonOrdinal(reasonOrdinal)
                .setTamperProofEvidence(tamperProofEvidenceProperty.get())
                .setIdVerification(idVerificationProperty.get())
                .setScreenCast(screenCastProperty.get())
                .setSummaryNotes(summaryNotesProperty.get())
                .setBuyerPayoutAmount(buyerPayoutAmount)
                .setSellerPayoutAmount(sellerPayoutAmount)
                .setCloseDate(closeDate);

        Optional.ofNullable(winner).ifPresent(result -> builder.setWinner(protobuf.DisputeResult.Winner.valueOf(winner.name())));
        Optional.ofNullable(chatMessage).ifPresent(chatMessage ->
                builder.setChatMessage(chatMessage.toProtoNetworkEnvelope().getChatMessage()));

        return builder.build();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    public BooleanProperty tamperProofEvidenceProperty() {
        return tamperProofEvidenceProperty;
    }

    public BooleanProperty idVerificationProperty() {
        return idVerificationProperty;
    }

    public BooleanProperty screenCastProperty() {
        return screenCastProperty;
    }

    public void setReason(Reason reason) {
        this.reasonOrdinal = reason.ordinal();
    }

    public Reason getReason() {
        if (reasonOrdinal < Reason.values().length)
            return Reason.values()[reasonOrdinal];
        else
            return Reason.OTHER;
    }

    public void setSummaryNotes(String summaryNotes) {
        this.summaryNotesProperty.set(summaryNotes);
    }

    public StringProperty summaryNotesProperty() {
        return summaryNotesProperty;
    }

    public void setBuyerPayoutAmount(Coin buyerPayoutAmount) {
        this.buyerPayoutAmount = buyerPayoutAmount.value;
    }

    public Coin getBuyerPayoutAmount() {
        return Coin.valueOf(buyerPayoutAmount);
    }

    public void setSellerPayoutAmount(Coin sellerPayoutAmount) {
        this.sellerPayoutAmount = sellerPayoutAmount.value;
    }

    public Coin getSellerPayoutAmount() {
        return Coin.valueOf(sellerPayoutAmount);
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate.getTime();
    }

    public Date getCloseDate() {
        return new Date(closeDate);
    }

    @Override
    public String toString() {
        return "DisputeResult{" +
                "\n     tradeId='" + tradeId + '\'' +
                ",\n     traderId=" + traderId +
                ",\n     winner=" + winner +
                ",\n     reasonOrdinal=" + reasonOrdinal +
                ",\n     tamperProofEvidenceProperty=" + tamperProofEvidenceProperty +
                ",\n     idVerificationProperty=" + idVerificationProperty +
                ",\n     screenCastProperty=" + screenCastProperty +
                ",\n     summaryNotesProperty=" + summaryNotesProperty +
                ",\n     chatMessage=" + chatMessage +
                ",\n     arbitratorSignature=" + Utilities.bytesAsHexString(arbitratorSignature) +
                ",\n     buyerPayoutAmount=" + buyerPayoutAmount +
                ",\n     sellerPayoutAmount=" + sellerPayoutAmount +
                ",\n     arbitratorPubKey=" + Utilities.bytesAsHexString(arbitratorPubKey) +
                ",\n     closeDate=" + closeDate +
                "\n}";
    }
}
