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

package bisq.core.payment;

import bisq.core.api.model.PaymentAccountFormField;
import bisq.core.locale.CurrencyUtil;
import bisq.core.locale.TradeCurrency;
import bisq.core.payment.payload.BankAccountPayload;
import bisq.core.payment.payload.NationalBankAccountPayload;
import bisq.core.payment.payload.PaymentAccountPayload;
import bisq.core.payment.payload.PaymentMethod;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
public final class NationalBankAccount extends CountryBasedPaymentAccount implements SameCountryRestrictedBankAccount {

    public static final List<TradeCurrency> SUPPORTED_CURRENCIES = CurrencyUtil.getAllFiatCurrencies();

    public NationalBankAccount() {
        super(PaymentMethod.NATIONAL_BANK);
    }

    @Override
    protected PaymentAccountPayload createPayload() {
        return new NationalBankAccountPayload(paymentMethod.getId(), id);
    }

    @Override
    public @NonNull List<TradeCurrency> getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES;
    }

    @Override
    public @NonNull List<PaymentAccountFormField.FieldId> getInputFieldIds() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getBankId() {
        return ((BankAccountPayload) paymentAccountPayload).getBankId();
    }

    @Override
    public String getCountryCode() {
        return getCountry() != null ? getCountry().code : "";
    }
}
