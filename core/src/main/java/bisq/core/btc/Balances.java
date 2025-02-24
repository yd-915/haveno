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

package bisq.core.btc;

import bisq.core.btc.listeners.XmrBalanceListener;
import bisq.core.btc.wallet.XmrWalletService;
import bisq.core.offer.OfferPayload;
import bisq.core.offer.OpenOffer;
import bisq.core.offer.OpenOfferManager;
import bisq.core.support.dispute.Dispute;
import bisq.core.support.dispute.refund.RefundManager;
import bisq.core.trade.ClosedTradableManager;
import bisq.core.trade.HavenoUtils;
import bisq.core.trade.Trade;
import bisq.core.trade.TradeManager;
import bisq.core.trade.failed.FailedTradesManager;
import bisq.network.p2p.P2PService;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import monero.common.MoneroError;
import monero.wallet.model.MoneroOutputQuery;
import monero.wallet.model.MoneroOutputWallet;
import monero.wallet.model.MoneroTxWallet;
import org.bitcoinj.core.Coin;

@Slf4j
public class Balances {
    private final TradeManager tradeManager;
    private final XmrWalletService xmrWalletService;
    private final OpenOfferManager openOfferManager;
    private final RefundManager refundManager;

    @Getter
    private final ObjectProperty<Coin> availableBalance = new SimpleObjectProperty<>();
    @Getter
    private final ObjectProperty<Coin> pendingBalance = new SimpleObjectProperty<>();
    @Getter
    private final ObjectProperty<Coin> reservedOfferBalance = new SimpleObjectProperty<>();
    @Getter
    private final ObjectProperty<Coin> reservedTradeBalance = new SimpleObjectProperty<>();
    @Getter
    private final ObjectProperty<Coin> reservedBalance = new SimpleObjectProperty<>(); // TODO (woodser): this balance is sum of reserved funds for offers and trade multisigs; remove?

    @Inject
    public Balances(TradeManager tradeManager,
                    XmrWalletService xmrWalletService,
                    OpenOfferManager openOfferManager,
                    ClosedTradableManager closedTradableManager,
                    FailedTradesManager failedTradesManager,
                    RefundManager refundManager) {
        this.tradeManager = tradeManager;
        this.xmrWalletService = xmrWalletService;
        this.openOfferManager = openOfferManager;
        this.refundManager = refundManager;
    }

    public void onAllServicesInitialized() {
        openOfferManager.getObservableList().addListener((ListChangeListener<OpenOffer>) c -> updatedBalances());
        tradeManager.getObservableList().addListener((ListChangeListener<Trade>) change -> updatedBalances());
        refundManager.getDisputesAsObservableList().addListener((ListChangeListener<Dispute>) c -> updatedBalances());
        xmrWalletService.addBalanceListener(new XmrBalanceListener() {
            @Override
            public void onBalanceChanged(BigInteger balance) {
                updatedBalances();
            }
        });
        updatedBalances();
    }

    private void updatedBalances() {
        if (!xmrWalletService.isWalletReady()) return;
        updateAvailableBalance();
        updatePendingBalance();
        updateReservedOfferBalance();
        updateReservedTradeBalance();
        updateReservedBalance();
    }

    // TODO (woodser): converting to long should generally be avoided since can lose precision, but in practice these amounts are below max value

    private void updateAvailableBalance() {
        availableBalance.set(Coin.valueOf(xmrWalletService.getWallet() == null ? 0 : xmrWalletService.getWallet().getUnlockedBalance(0).longValueExact()));
    }
    
    private void updatePendingBalance() {
        BigInteger balance = xmrWalletService.getWallet() == null ? new BigInteger("0") : xmrWalletService.getWallet().getBalance(0);
        BigInteger unlockedBalance = xmrWalletService.getWallet() == null ? new BigInteger("0") : xmrWalletService.getWallet().getUnlockedBalance(0);
        pendingBalance.set(Coin.valueOf(balance.subtract(unlockedBalance).longValueExact()));
    }
    
    private void updateReservedOfferBalance() {
        Coin sum = Coin.valueOf(0);
        if (xmrWalletService.getWallet() != null) {
            List<MoneroOutputWallet> frozenOutputs = xmrWalletService.getWallet().getOutputs(new MoneroOutputQuery().setIsFrozen(true).setIsSpent(false));
            for (MoneroOutputWallet frozenOutput : frozenOutputs) sum = sum.add(Coin.valueOf(frozenOutput.getAmount().longValueExact()));
        }
        reservedOfferBalance.set(sum);
    }
    
    private void updateReservedTradeBalance() {
        Coin sum = Coin.valueOf(0);
        List<Trade> openTrades = tradeManager.getTradesStreamWithFundsLockedIn().collect(Collectors.toList());
        for (Trade trade : openTrades) {
            try {
                MoneroTxWallet depositTx = xmrWalletService.getWallet().getTx(trade.getSelf().getDepositTxHash());
                if (!depositTx.isConfirmed()) continue; // outputs are frozen until confirmed by arbitrator's broadcast
            } catch (MoneroError e) {
                continue;
            }
            if (trade.getContract() == null) continue;
            Long reservedAmt;
            OfferPayload offerPayload = trade.getContract().getOfferPayload();
            if (trade.getArbitratorNodeAddress().equals(P2PService.getMyNodeAddress())) { // TODO (woodser): this only works if node address does not change
                reservedAmt = offerPayload.getAmount() + offerPayload.getBuyerSecurityDeposit() + offerPayload.getSellerSecurityDeposit(); // arbitrator reserved balance is sum of amounts sent to multisig
            } else {
                reservedAmt = trade.getContract().isMyRoleBuyer(tradeManager.getKeyRing().getPubKeyRing()) ? offerPayload.getBuyerSecurityDeposit() : offerPayload.getAmount() + offerPayload.getSellerSecurityDeposit();
            }
            sum = sum.add(Coin.valueOf(HavenoUtils.centinerosToAtomicUnits(reservedAmt).longValueExact()));
        }
        reservedTradeBalance.set(sum);
    }

    private void updateReservedBalance() {
        reservedBalance.set(reservedOfferBalance.get().add(reservedTradeBalance.get()));
    }
}
