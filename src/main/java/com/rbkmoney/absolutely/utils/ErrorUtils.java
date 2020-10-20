package com.rbkmoney.absolutely.utils;

import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.domain.SubFailure;
import com.rbkmoney.swag.adapter.abs.model.Error;
import com.rbkmoney.swag.adapter.abs.model.SubError;

public class ErrorUtils {

    public static Error getError(OperationFailure operationFailure) {
        if (operationFailure.isSetFailure()) {
            Failure failure = operationFailure.getFailure();
            Error paymentError = new Error();
            paymentError.setCode(failure.getCode());
            if (failure.isSetSub()) {
                SubFailure sub = failure.getSub();
                paymentError.setSubError(getSubError(sub));
            }
            return paymentError;
        } else if (operationFailure.isSetOperationTimeout()) {
            Error paymentError = new Error();
            paymentError.setCode("408");
            return paymentError;
        } else {
            throw new IllegalStateException("Unknown failure type " + operationFailure.getSetField().getFieldName());
        }
    }


    private static SubError getSubError(SubFailure sub) {
        SubError paymentErrorSubError = new SubError();
        paymentErrorSubError.setCode(sub.getCode());
        if (sub.isSetSub()) {
            paymentErrorSubError.setSubError(getSubError(sub.getSub()));
        }
        return paymentErrorSubError;
    }
}
