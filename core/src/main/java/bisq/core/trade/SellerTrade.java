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

package bisq.core.trade;

import bisq.core.btc.wallet.XmrWalletService;
import bisq.core.locale.CurrencyUtil;
import bisq.core.offer.Offer;
import bisq.core.trade.protocol.ProcessModel;

import bisq.network.p2p.NodeAddress;

import org.bitcoinj.core.Coin;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public abstract class SellerTrade extends Trade {
    SellerTrade(Offer offer,
                Coin tradeAmount,
                Coin takerFee,
                long tradePrice,
                XmrWalletService xmrWalletService,
                ProcessModel processModel,
                String uid,
                @Nullable NodeAddress makerNodeAddress,
                @Nullable NodeAddress takerNodeAddress,
                @Nullable NodeAddress arbitratorNodeAddress) {
        super(offer,
                tradeAmount,
                takerFee,
                tradePrice,
                xmrWalletService,
                processModel,
                uid,
                makerNodeAddress,
                takerNodeAddress,
                arbitratorNodeAddress);
    }

    @Override
    public Coin getPayoutAmount() {
        return checkNotNull(getOffer()).getSellerSecurityDeposit();
    }

    @Override
    public boolean confirmPermitted() {
        // For altcoin there is no reason to delay BTC release as no chargeback risk
        if (CurrencyUtil.isCryptoCurrency(getOffer().getCurrencyCode())) {
            return true;
        }

        switch (getDisputeState()) {
            case NO_DISPUTE:
                return true;

            case DISPUTE_REQUESTED:
            case DISPUTE_OPENED:
            case ARBITRATOR_SENT_DISPUTE_CLOSED_MSG:
            case ARBITRATOR_SEND_FAILED_DISPUTE_CLOSED_MSG:
            case ARBITRATOR_STORED_IN_MAILBOX_DISPUTE_CLOSED_MSG:
            case ARBITRATOR_SAW_ARRIVED_DISPUTE_CLOSED_MSG:
            case DISPUTE_CLOSED:
            case MEDIATION_REQUESTED:
            case MEDIATION_STARTED_BY_PEER:
                return false;

            case MEDIATION_CLOSED:
                return !mediationResultAppliedPenaltyToSeller();

            case REFUND_REQUESTED:
            case REFUND_REQUEST_STARTED_BY_PEER:
            case REFUND_REQUEST_CLOSED:
            default:
                return false;
        }
    }
}

