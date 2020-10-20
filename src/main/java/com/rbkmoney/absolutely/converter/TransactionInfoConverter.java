package com.rbkmoney.absolutely.converter;

import com.rbkmoney.damsel.domain.TransactionInfo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TransactionInfoConverter implements Converter<TransactionInfo, com.rbkmoney.swag.adapter.abs.model.TransactionInfo> {

    @Override
    public com.rbkmoney.swag.adapter.abs.model.TransactionInfo convert(TransactionInfo transactionInfo) {
        if (transactionInfo == null) {
            return null;
        }
        return new com.rbkmoney.swag.adapter.abs.model.TransactionInfo()
                .id(transactionInfo.getId())
                .rrn(transactionInfo.isSetAdditionalInfo() ? transactionInfo.getAdditionalInfo().getRrn() : null);
    }
}
