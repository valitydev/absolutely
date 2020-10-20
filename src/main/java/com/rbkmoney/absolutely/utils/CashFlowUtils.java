package com.rbkmoney.absolutely.utils;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.absolutely.model.FeeType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CashFlowUtils {

    public static Map<FeeType, Long> getFees(List<FinalCashFlowPosting> cashFlowPostings) {
        if (cashFlowPostings != null && !cashFlowPostings.isEmpty()) {
            return cashFlowPostings.stream()
                    .collect(
                            Collectors.groupingBy(
                                    CashFlowUtils::getFeeType,
                                    Collectors.summingLong(posting -> posting.getVolume().getAmount())
                            )
                    );
        }
        return Map.of();
    }

    public static FeeType getFeeType(FinalCashFlowPosting cashFlowPosting) {
        CashFlowAccount source = cashFlowPosting.getSource().getAccountType();
        CashFlowAccount destination = cashFlowPosting.getDestination().getAccountType();

        if (source.isSetProvider() && source.getProvider() == ProviderCashFlowAccount.settlement
                && destination.isSetMerchant() && destination.getMerchant() == MerchantCashFlowAccount.settlement) {
            return FeeType.AMOUNT;
        }

        if (source.isSetMerchant()
                && source.getMerchant() == MerchantCashFlowAccount.settlement
                && destination.isSetSystem()) {
            return FeeType.FEE;
        }

        if (source.isSetSystem()
                && destination.isSetExternal()) {
            return FeeType.EXTERNAL_FEE;
        }

        if (source.isSetSystem()
                && destination.isSetProvider()) {
            return FeeType.PROVIDER_FEE;
        }

        return FeeType.UNKNOWN;
    }

    public static Long getFee(Map<FeeType, Long> fees) {
        return fees != null ? fees.getOrDefault(FeeType.FEE, 0L) : 0L;
    }

    public static Long getProviderFee(Map<FeeType, Long> fees) {
        return fees != null ? fees.getOrDefault(FeeType.PROVIDER_FEE, 0L) : 0L;
    }

    public static Long getExternalFee(Map<FeeType, Long> fees) {
        return fees != null ? fees.getOrDefault(FeeType.EXTERNAL_FEE, 0L) : 0L;
    }

    public static Long getMerchantAmount(List<FinalCashFlowPosting> finalCashFlow) {
        long amountSource = getAmount(finalCashFlow, FinalCashFlowPosting::getSource);
        long amountDest = getAmount(finalCashFlow, FinalCashFlowPosting::getDestination);
        return amountDest - amountSource;
    }

    private static long getAmount(List<FinalCashFlowPosting> finalCashFlow,
                                  Function<FinalCashFlowPosting, FinalCashFlowAccount> func) {
        return finalCashFlow.stream()
                .filter(f -> isMerchantSettlement(func.apply(f).getAccountType()))
                .mapToLong(cashFlow -> cashFlow.getVolume().getAmount())
                .sum();
    }

    private static boolean isMerchantSettlement(com.rbkmoney.damsel.domain.CashFlowAccount cashFlowAccount) {
        return cashFlowAccount.isSetMerchant() &&
                cashFlowAccount.getMerchant() == MerchantCashFlowAccount.settlement;
    }

}
