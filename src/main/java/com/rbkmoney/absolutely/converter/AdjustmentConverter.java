package com.rbkmoney.absolutely.converter;

import com.rbkmoney.absolutely.utils.CashFlowUtils;
import com.rbkmoney.absolutely.utils.InvoiceUtils;
import com.rbkmoney.absolutely.utils.TimeUtils;
import com.rbkmoney.damsel.domain.InvoicePaymentAdjustment;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.swag.adapter.abs.model.Adjustment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdjustmentConverter {

    public Adjustment convert(com.rbkmoney.damsel.payment_processing.Invoice invoice, String paymentId, String adjustmentId) {
        InvoicePayment invoicePayment = InvoiceUtils.extractPayment(invoice, paymentId);
        InvoicePaymentAdjustment adjustment = InvoiceUtils.extractAdjustment(invoicePayment, adjustmentId);
        var payment = invoicePayment.getPayment();

        Long oldAmount = CashFlowUtils.computeMerchantAmount(adjustment.getOldCashFlowInverse());
        Long newAmount = CashFlowUtils.computeMerchantAmount(adjustment.getNewCashFlow());
        long amount = newAmount + oldAmount;

        return new Adjustment()
                .id(adjustmentId)
                .paymentID(paymentId)
                .invoiceID(invoice.getInvoice().getId())
                .createdAt(TimeUtils.toOffsetDateTime(adjustment.getCreatedAt()))
                .domainRevision(adjustment.getDomainRevision())
                .partyRevision(adjustment.getPartyRevision())
                .partyID(payment.getOwnerId())
                .shopID(payment.getShopId())
                .amount(amount)
                .currency(payment.getCost().getCurrency().getSymbolicCode())
                .externalID(null);//TODO
    }
}
