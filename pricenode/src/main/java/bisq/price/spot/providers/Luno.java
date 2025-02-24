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

package bisq.price.spot.providers;

import bisq.price.spot.ExchangeRate;
import bisq.price.spot.ExchangeRateProvider;

import org.knowm.xchange.luno.LunoExchange;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;

import java.util.Set;

@Component
class Luno extends ExchangeRateProvider {

    public Luno(Environment env) {
        super(env, "LUNO", "luno", Duration.ofMinutes(1));
    }

    @Override
    public Set<ExchangeRate> doGet() {
        // Supported fiat: IDR (Indonesian rupiah), MYR (Malaysian ringgit),
        // NGN (Nigerian Naira), ZAR (South African rand)
        // Supported alts: -
        return doGet(LunoExchange.class);
    }

    @Override
    protected long getMarketDataCallDelay() {
        // Luno allows only 1 MarketData call per second
        // (see https://www.luno.com/en/developers/api )
        return 1000;
    }
}
