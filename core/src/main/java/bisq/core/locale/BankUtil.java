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

package bisq.core.locale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankUtil {

    private static final Logger log = LoggerFactory.getLogger(BankUtil.class);

    // BankName
    @SuppressWarnings("SameReturnValue")
    public static boolean isBankNameRequired(@SuppressWarnings("unused") String countryCode) {
        // Currently we always return true but let's keep that method to be more flexible in case we want to not show
        // it at some new payment method.
        return true;
         /*
        switch (countryCode) {
            // We show always the bank name as it is needed in specific banks.
            // Though that handling should be optimized in futures.
            case "GB":
            case "US":
            case "NZ":
            case "AU":
            case "CA":
            case "SE":
            case "HK":
                return false;
            case "MX":
            case "BR":
            case "AR":
                return true;
            default:
                return true;
        }*/
    }

    public static String getBankNameLabel(String countryCode) {
        switch (countryCode) {
            case "BR":
                return Res.get("payment.bank.name");
            default:
                return isBankNameRequired(countryCode) ? Res.get("payment.bank.name") : Res.get("payment.bank.nameOptional");
        }
    }

    // BankId
    public static boolean isBankIdRequired(String countryCode) {
        switch (countryCode) {
            case "GB":
            case "US":
            case "BR":
            case "NZ":
            case "AU":
            case "SE":
            case "CL":
            case "NO":
            case "AR":
                return false;
            case "CA":
            case "MX":
            case "HK":
                return true;
            default:
                return true;
        }
    }

    public static String getBankIdLabel(String countryCode) {
        switch (countryCode) {
            case "CA":
                return "Institution Number";// do not translate as it is used in English only
            case "MX":
            case "HK":
                return Res.get("payment.bankCode");
            default:
                return isBankIdRequired(countryCode) ? Res.get("payment.bankId") : Res.get("payment.bankIdOptional");
        }

    }

    // BranchId
    public static boolean isBranchIdRequired(String countryCode) {
        switch (countryCode) {
            case "GB":
            case "US":
            case "BR":
            case "AU":
            case "CA":
                return true;
            case "NZ":
            case "MX":
            case "HK":
            case "SE":
            case "NO":
                return false;
            default:
                return true;
        }
    }

    public static String getBranchIdLabel(String countryCode) {
        switch (countryCode) {
            case "GB":
                return "UK sort code"; // do not translate as it is used in English only
            case "US":
                return "Routing Number"; // do not translate as it is used in English only
            case "BR":
                return "Código da Agência"; // do not translate as it is used in Portuguese only
            case "AU":
                return "BSB code"; // do not translate as it is used in English only
            case "CA":
                return "Transit Number"; // do not translate as it is used in English only
            default:
                return isBranchIdRequired(countryCode) ? Res.get("payment.branchNr") : Res.get("payment.branchNrOptional");
        }
    }


    // AccountNr
    @SuppressWarnings("SameReturnValue")
    public static boolean isAccountNrRequired(String countryCode) {
        switch (countryCode) {
            default:
                return true;
        }
    }

    public static String getAccountNrLabel(String countryCode) {
        switch (countryCode) {
            case "GB":
            case "US":
            case "BR":
            case "NZ":
            case "AU":
            case "CA":
            case "HK":
                return Res.get("payment.accountNr");
            case "NO":
                return "Kontonummer"; // do not translate as it is used in Norwegian and Swedish only
            case "SE":
                return "Kontonummer"; // do not translate as it is used in Norwegian and Swedish only
            case "MX":
                return "CLABE"; // do not translate as it is used in Spanish only
            case "CL":
                return "Cuenta"; // do not translate as it is used in Spanish only
            case "AR":
                return "Número de cuenta"; // do not translate as it is used in Spanish only
            default:
                return Res.get("payment.accountNrLabel");
        }
    }

    // AccountType
    public static boolean isAccountTypeRequired(String countryCode) {
        switch (countryCode) {
            case "US":
            case "BR":
            case "CA":
                return true;
            default:
                return false;
        }
    }

    public static String getAccountTypeLabel(String countryCode) {
        switch (countryCode) {
            case "US":
            case "BR":
            case "CA":
                return Res.get("payment.accountType");
            default:
                return "";
        }
    }

    public static List<String> getAccountTypeValues(String countryCode) {
        switch (countryCode) {
            case "US":
            case "BR":
            case "CA":
                return Arrays.asList(Res.get("payment.checking"), Res.get("payment.savings"));
            default:
                return new ArrayList<>();
        }
    }


    // HolderId
    public static boolean isHolderIdRequired(String countryCode) {
        switch (countryCode) {
            case "BR":
            case "CL":
            case "AR":
                return true;
            default:
                return false;
        }
    }

    public static String getHolderIdLabel(String countryCode) {
        switch (countryCode) {
            case "BR":
                return "Cadastro de Pessoas Físicas (CPF)"; // do not translate as it is used in Portuguese only
            case "CL":
                return "Rol Único Tributario (RUT)";  // do not translate as it is used in Spanish only
            case "AR":
                return "CUIL/CUIT"; // do not translate as it is used in Spanish only
            default:
                return Res.get("payment.personalId");
        }
    }

    public static String getHolderIdLabelShort(String countryCode) {
        switch (countryCode) {
            case "BR":
                return "CPF"; // do not translate as it is used in portuguese only
            case "CL":
                return "RUT";  // do not translate as it is used in spanish only
            case "AR":
                return "CUIT";
            default:
                return "ID";
        }
    }

    // Validation
    public static boolean useValidation(String countryCode) {
        switch (countryCode) {
            case "GB":
            case "US":
            case "BR":
            case "AU":
            case "CA":
            case "NZ":
            case "MX":
            case "HK":
            case "SE":
            case "NO":
            case "AR":
                return true;
            default:
                return false;
        }
    }

    public static List<Country> getAllStateRequiredCountries() {
        List<String> codes = List.of("US", "CA", "AU", "MY", "MX", "CN");
        List<Country> list = CountryUtil.getCountries(codes);
        list.sort((a, b) -> a.name.compareTo(b.name));
        return list;
    }

    public static boolean isStateRequired(String countryCode) {
        return getAllStateRequiredCountries().stream().map(country -> country.code).collect(Collectors.toList()).contains(countryCode);
    }

    public static boolean isNationalAccountIdRequired(String countryCode) {
        switch (countryCode) {
            case "AR":
                return true;
            default:
                return false;
        }
    }

    public static String getNationalAccountIdLabel(String countryCode) {
        switch (countryCode) {
            case "AR":
                return Res.get("payment.national.account.id.AR");
            default:
                return "";
        }
    }
}
