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

package bisq.core.api;

import bisq.core.monetary.Altcoin;
import bisq.core.monetary.Price;
import bisq.core.offer.CreateOfferService;
import bisq.core.offer.Offer;
import bisq.core.offer.OfferBookService;
import bisq.core.offer.OfferDirection;
import bisq.core.offer.OfferFilterService;
import bisq.core.offer.OfferFilterService.Result;
import bisq.core.offer.OfferUtil;
import bisq.core.offer.OpenOffer;
import bisq.core.offer.OpenOfferManager;
import bisq.core.payment.PaymentAccount;
import bisq.core.user.User;
import bisq.core.util.PriceUtil;
import bisq.common.crypto.KeyRing;
import bisq.common.handlers.ErrorMessageHandler;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.utils.Fiat;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import static bisq.common.util.MathUtils.exactMultiply;
import static bisq.common.util.MathUtils.roundDoubleToLong;
import static bisq.common.util.MathUtils.scaleUpByPowerOf10;
import static bisq.core.locale.CurrencyUtil.isCryptoCurrency;
import static bisq.core.offer.OfferDirection.BUY;
import static bisq.core.payment.PaymentAccountUtil.isPaymentAccountValidForOffer;
import static java.lang.String.format;
import static java.util.Comparator.comparing;

@Singleton
@Slf4j
public class CoreOffersService {

    private final Supplier<Comparator<Offer>> priceComparator = () -> comparing(Offer::getPrice);
    private final Supplier<Comparator<Offer>> reversePriceComparator = () -> comparing(Offer::getPrice).reversed();

    private final CoreContext coreContext;
    private final KeyRing keyRing;
    // Dependencies on core api services in this package must be kept to an absolute
    // minimum, but some trading functions require an unlocked wallet's key, so an
    // exception is made in this case.
    private final CoreWalletsService coreWalletsService;
    private final CreateOfferService createOfferService;
    private final OfferBookService offerBookService;
    private final OfferFilterService offerFilter;
    private final OpenOfferManager openOfferManager;
    private final User user;
    
    @Inject
    public CoreOffersService(CoreContext coreContext,
                             KeyRing keyRing,
                             CoreWalletsService coreWalletsService,
                             CreateOfferService createOfferService,
                             OfferBookService offerBookService,
                             OfferFilterService offerFilter,
                             OpenOfferManager openOfferManager,
                             OfferUtil offerUtil,
                             User user) {
        this.coreContext = coreContext;
        this.keyRing = keyRing;
        this.coreWalletsService = coreWalletsService;
        this.createOfferService = createOfferService;
        this.offerBookService = offerBookService;
        this.offerFilter = offerFilter;
        this.openOfferManager = openOfferManager;
        this.user = user;
    }

    // excludes my offers
    List<Offer> getOffers() {
        List<Offer> offers = new ArrayList<>(offerBookService.getOffers()).stream()
                .filter(o -> !o.isMyOffer(keyRing))
                .filter(o -> {
                    Result result = offerFilter.canTakeOffer(o, coreContext.isApiUser());
                    return result.isValid() || result == Result.HAS_NO_PAYMENT_ACCOUNT_VALID_FOR_OFFER;
                })
                .collect(Collectors.toList());
        offers.removeAll(getOffersWithDuplicateKeyImages(offers));
        return offers;
    }

    List<Offer> getOffers(String direction, String currencyCode) {
        return getOffers().stream()
                .filter(o -> offerMatchesDirectionAndCurrency(o, direction, currencyCode))
                .sorted(priceComparator(direction))
                .collect(Collectors.toList());
    }

    Offer getOffer(String id) {
        return getOffers().stream()
                .filter(o -> o.getId().equals(id))
                .findAny().orElseThrow(() ->
                        new IllegalStateException(format("offer with id '%s' not found", id)));
    }

    List<Offer> getMyOffers() {
        List<Offer> offers = openOfferManager.getOpenOffers().stream()
                .map(OpenOffer::getOffer)
                .filter(o -> o.isMyOffer(keyRing))
                .collect(Collectors.toList());
        offers.removeAll(getOffersWithDuplicateKeyImages(offers));
        return offers;
    };

    List<Offer> getMyOffers(String direction, String currencyCode) {
        return getMyOffers().stream()
                .filter(o -> offerMatchesDirectionAndCurrency(o, direction, currencyCode))
                .sorted(priceComparator(direction))
                .collect(Collectors.toList());
    }

    Offer getMyOffer(String id) {
        return getMyOffers().stream()
                .filter(o -> o.getId().equals(id))
                .findAny().orElseThrow(() ->
                        new IllegalStateException(format("offer with id '%s' not found", id)));
    }

    OpenOffer getMyOpenOffer(String id) {
        getMyOffer(id); // ensure offer is valid
        return openOfferManager.getOpenOfferById(id)
                .filter(open -> open.getOffer().isMyOffer(keyRing))
                .orElseThrow(() ->
                        new IllegalStateException(format("openoffer with id '%s' not found", id)));
    }

    void postOffer(String currencyCode,
                             String directionAsString,
                             String priceAsString,
                             boolean useMarketBasedPrice,
                             double marketPriceMargin,
                             long amountAsLong,
                             long minAmountAsLong,
                             double buyerSecurityDeposit,
                             String triggerPriceAsString,
                             String paymentAccountId,
                             Consumer<Offer> resultHandler,
                             ErrorMessageHandler errorMessageHandler) {
        coreWalletsService.verifyWalletsAreAvailable();
        coreWalletsService.verifyEncryptedWalletIsUnlocked();

        PaymentAccount paymentAccount = user.getPaymentAccount(paymentAccountId);
        if (paymentAccount == null)
            throw new IllegalArgumentException(format("payment account with id %s not found", paymentAccountId));

        String upperCaseCurrencyCode = currencyCode.toUpperCase();
        String offerId = createOfferService.getRandomOfferId();
        OfferDirection direction = OfferDirection.valueOf(directionAsString.toUpperCase());
        Price price = priceAsString.isEmpty() ? null : Price.valueOf(upperCaseCurrencyCode, priceStringToLong(priceAsString, upperCaseCurrencyCode));
        Coin amount = Coin.valueOf(amountAsLong);
        Coin minAmount = Coin.valueOf(minAmountAsLong);
        Coin useDefaultTxFee = Coin.ZERO;
        Offer offer = createOfferService.createAndGetOffer(offerId,
                direction,
                upperCaseCurrencyCode,
                amount,
                minAmount,
                price,
                useDefaultTxFee,
                useMarketBasedPrice,
                exactMultiply(marketPriceMargin, 0.01),
                buyerSecurityDeposit,
                paymentAccount);

        verifyPaymentAccountIsValidForNewOffer(offer, paymentAccount);

        // We don't support atm funding from external wallet to keep it simple.
        boolean useSavingsWallet = true;
        //noinspection ConstantConditions
        placeOffer(offer,
                triggerPriceAsString,
                useSavingsWallet,
                transaction -> resultHandler.accept(offer),
                errorMessageHandler);
    }

    Offer editOffer(String offerId,
                    String currencyCode,
                    OfferDirection direction,
                    Price price,
                    boolean useMarketBasedPrice,
                    double marketPriceMargin,
                    Coin amount,
                    Coin minAmount,
                    double buyerSecurityDeposit,
                    PaymentAccount paymentAccount) {
        Coin useDefaultTxFee = Coin.ZERO;
        return createOfferService.createAndGetOffer(offerId,
                direction,
                currencyCode.toUpperCase(),
                amount,
                minAmount,
                price,
                useDefaultTxFee,
                useMarketBasedPrice,
                exactMultiply(marketPriceMargin, 0.01),
                buyerSecurityDeposit,
                paymentAccount);
    }

    void cancelOffer(String id) {
        Offer offer = getMyOffer(id);
        openOfferManager.removeOffer(offer,
                () -> {
                },
                errorMessage -> {
                    throw new IllegalStateException(errorMessage);
                });
    }

    // -------------------------- PRIVATE HELPERS -----------------------------

    private Set<Offer> getOffersWithDuplicateKeyImages(List<Offer> offers) {
        Set<Offer> duplicateFundedOffers = new HashSet<Offer>();
        Set<String> seenKeyImages = new HashSet<String>();
        for (Offer offer : offers) {
            if (offer.getOfferPayload().getReserveTxKeyImages() == null) continue;
            for (String keyImage : offer.getOfferPayload().getReserveTxKeyImages()) {
                if (!seenKeyImages.add(keyImage)) {
                    for (Offer offer2 : offers) {
                        if (offer2.getOfferPayload().getReserveTxKeyImages().contains(keyImage)) {
                            log.warn("Key image {} belongs to multiple offers, removing offer {}", keyImage, offer2.getId());
                            duplicateFundedOffers.add(offer2);
                        }
                    }
                }
            }
        }
        return duplicateFundedOffers;
    }

    private void verifyPaymentAccountIsValidForNewOffer(Offer offer, PaymentAccount paymentAccount) {
        if (!isPaymentAccountValidForOffer(offer, paymentAccount)) {
            String error = format("cannot create %s offer with payment account %s",
                    offer.getOfferPayload().getCounterCurrencyCode(),
                    paymentAccount.getId());
            throw new IllegalStateException(error);
        }
    }

    private void placeOffer(Offer offer,
                            String triggerPriceAsString,
                            boolean useSavingsWallet,
                            Consumer<Transaction> resultHandler,
                            ErrorMessageHandler errorMessageHandler) {
        long triggerPriceAsLong = PriceUtil.getMarketPriceAsLong(triggerPriceAsString, offer.getCurrencyCode());
        openOfferManager.placeOffer(offer,
                useSavingsWallet,
                triggerPriceAsLong,
                resultHandler::accept,
                errorMessageHandler);
    }

    private boolean offerMatchesDirectionAndCurrency(Offer offer,
                                                     String direction,
                                                     String currencyCode) {
        var offerOfWantedDirection = offer.getDirection().name().equalsIgnoreCase(direction);
        var counterAssetCode = isCryptoCurrency(currencyCode) ? offer.getOfferPayload().getBaseCurrencyCode() : offer.getOfferPayload().getCounterCurrencyCode(); // TODO: crypto pairs invert base and counter currencies
        var offerInWantedCurrency = counterAssetCode.equalsIgnoreCase(currencyCode);
        return offerOfWantedDirection && offerInWantedCurrency;
    }

    private Comparator<Offer> priceComparator(String direction) {
        // A buyer probably wants to see sell orders in price ascending order.
        // A seller probably wants to see buy orders in price descending order.
        return direction.equalsIgnoreCase(BUY.name())
                ? reversePriceComparator.get()
                : priceComparator.get();
    }

    private long priceStringToLong(String priceAsString, String currencyCode) {
        int precision = isCryptoCurrency(currencyCode) ? Altcoin.SMALLEST_UNIT_EXPONENT : Fiat.SMALLEST_UNIT_EXPONENT;
        double priceAsDouble = new BigDecimal(priceAsString).doubleValue();
        double scaled = scaleUpByPowerOf10(priceAsDouble, precision);
        return roundDoubleToLong(scaled);
    }
}
