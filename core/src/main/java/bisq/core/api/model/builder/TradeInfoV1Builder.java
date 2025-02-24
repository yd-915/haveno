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

package bisq.core.api.model.builder;

import bisq.core.api.model.ContractInfo;
import bisq.core.api.model.OfferInfo;
import bisq.core.api.model.TradeInfo;

import lombok.Getter;

/**
 * A builder helps avoid bungling use of a large TradeInfo constructor
 * argument list.  If consecutive argument values of the same type are not
 * ordered correctly, the compiler won't complain but the resulting bugs could
 * be hard to find and fix.
 */
@Getter
public final class TradeInfoV1Builder {

    private OfferInfo offer;
    private String tradeId;
    private String shortId;
    private long date;
    private String role;
    private boolean isCurrencyForTakerFeeBtc;
    private long txFeeAsLong;
    private long takerFeeAsLong;
    private String makerDepositTxId;
    private String takerDepositTxId;
    private String payoutTxId;
    private long amountAsLong;
    private String price;
    private String volume;
    private String arbitratorNodeAddress;
    private String tradingPeerNodeAddress;
    private String state;
    private String phase;
    private String periodState;
    private String payoutState;
    private String disputeState;
    private boolean isDepositPublished;
    private boolean isDepositConfirmed;
    private boolean isDepositUnlocked;
    private boolean isPaymentSent;
    private boolean isPaymentReceived;
    private boolean isPayoutPublished;
    private boolean isPayoutConfirmed;
    private boolean isPayoutUnlocked;
    private boolean isCompleted;
    private String contractAsJson;
    private ContractInfo contract;
    private String closingStatus;

    public TradeInfoV1Builder withOffer(OfferInfo offer) {
        this.offer = offer;
        return this;
    }

    public TradeInfoV1Builder withTradeId(String tradeId) {
        this.tradeId = tradeId;
        return this;
    }

    public TradeInfoV1Builder withShortId(String shortId) {
        this.shortId = shortId;
        return this;
    }

    public TradeInfoV1Builder withDate(long date) {
        this.date = date;
        return this;
    }

    public TradeInfoV1Builder withRole(String role) {
        this.role = role;
        return this;
    }

    public TradeInfoV1Builder withIsCurrencyForTakerFeeBtc(boolean isCurrencyForTakerFeeBtc) {
        this.isCurrencyForTakerFeeBtc = isCurrencyForTakerFeeBtc;
        return this;
    }

    public TradeInfoV1Builder withTxFeeAsLong(long txFeeAsLong) {
        this.txFeeAsLong = txFeeAsLong;
        return this;
    }

    public TradeInfoV1Builder withTakerFeeAsLong(long takerFeeAsLong) {
        this.takerFeeAsLong = takerFeeAsLong;
        return this;
    }

    public TradeInfoV1Builder withMakerDepositTxId(String makerDepositTxId) {
        this.makerDepositTxId = makerDepositTxId;
        return this;
    }

    public TradeInfoV1Builder withTakerDepositTxId(String takerDepositTxId) {
        this.takerDepositTxId = takerDepositTxId;
        return this;
    }

    public TradeInfoV1Builder withPayoutTxId(String payoutTxId) {
        this.payoutTxId = payoutTxId;
        return this;
    }

    public TradeInfoV1Builder withAmountAsLong(long amountAsLong) {
        this.amountAsLong = amountAsLong;
        return this;
    }

    public TradeInfoV1Builder withPrice(String price) {
        this.price = price;
        return this;
    }

    public TradeInfoV1Builder withVolume(String volume) {
        this.volume = volume;
        return this;
    }
    
    public TradeInfoV1Builder withState(String state) {
        this.state = state;
        return this;
    }

    public TradeInfoV1Builder withPhase(String phase) {
        this.phase = phase;
        return this;
    }

    public TradeInfoV1Builder withPeriodState(String periodState) {
        this.periodState = periodState;
        return this;
    }

    public TradeInfoV1Builder withPayoutState(String payoutState) {
        this.payoutState = payoutState;
        return this;
    }

    public TradeInfoV1Builder withDisputeState(String disputeState) {
        this.disputeState = disputeState;
        return this;
    }

    public TradeInfoV1Builder withArbitratorNodeAddress(String arbitratorNodeAddress) {
        this.arbitratorNodeAddress = arbitratorNodeAddress;
        return this;
    }

    public TradeInfoV1Builder withTradingPeerNodeAddress(String tradingPeerNodeAddress) {
        this.tradingPeerNodeAddress = tradingPeerNodeAddress;
        return this;
    }

    public TradeInfoV1Builder withIsDepositPublished(boolean isDepositPublished) {
        this.isDepositPublished = isDepositPublished;
        return this;
    }

    public TradeInfoV1Builder withIsDepositConfirmed(boolean isDepositConfirmed) {
        this.isDepositConfirmed = isDepositConfirmed;
        return this;
    }

    public TradeInfoV1Builder withIsDepositUnlocked(boolean isDepositUnlocked) {
        this.isDepositUnlocked = isDepositUnlocked;
        return this;
    }

    public TradeInfoV1Builder withIsPaymentSent(boolean isPaymentSent) {
        this.isPaymentSent = isPaymentSent;
        return this;
    }

    public TradeInfoV1Builder withIsPaymentReceived(boolean isPaymentReceived) {
        this.isPaymentReceived = isPaymentReceived;
        return this;
    }

    public TradeInfoV1Builder withIsPayoutPublished(boolean isPayoutPublished) {
        this.isPayoutPublished = isPayoutPublished;
        return this;
    }

    public TradeInfoV1Builder withIsPayoutConfirmed(boolean isPayoutConfirmed) {
        this.isPayoutConfirmed = isPayoutConfirmed;
        return this;
    }

    public TradeInfoV1Builder withIsPayoutUnlocked(boolean isPayoutUnlocked) {
        this.isPayoutUnlocked = isPayoutUnlocked;
        return this;
    }

    public TradeInfoV1Builder withIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
        return this;
    }

    public TradeInfoV1Builder withContractAsJson(String contractAsJson) {
        this.contractAsJson = contractAsJson;
        return this;
    }

    public TradeInfoV1Builder withContract(ContractInfo contract) {
        this.contract = contract;
        return this;
    }

    public TradeInfoV1Builder withClosingStatus(String closingStatus) {
        this.closingStatus = closingStatus;
        return this;
    }

    public TradeInfo build() {
        return new TradeInfo(this);
    }
}
