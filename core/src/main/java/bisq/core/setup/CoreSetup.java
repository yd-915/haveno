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

package bisq.core.setup;

import bisq.core.locale.CurrencyUtil;
import bisq.core.locale.Res;

import bisq.common.config.Config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CoreSetup {

    public static void setup(Config config) {
        CoreNetworkCapabilities.setSupportedCapabilities(config);
        Res.setup();
        CurrencyUtil.setup();
    }

}
