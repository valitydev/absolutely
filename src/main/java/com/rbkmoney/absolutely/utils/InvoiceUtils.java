package com.rbkmoney.absolutely.utils;

import com.rbkmoney.absolutely.exception.NotFoundException;
import com.rbkmoney.damsel.domain.InvoicePaymentAdjustment;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund;

public class InvoiceUtils {

    public static InvoicePayment extractPayment(com.rbkmoney.damsel.payment_processing.Invoice invoice, String paymentId) {
        return invoice.getPayments().stream()
                .filter(p -> p.getPayment().getId().equals(paymentId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Payment not found, invoiceId=%s, paymentId=%s", invoice.getInvoice().getId(), paymentId)));
    }

    public static InvoicePaymentRefund extractRefund(com.rbkmoney.damsel.payment_processing.InvoicePayment payment, String refundId) {
        return payment.getRefunds().stream()
                .filter(invoicePaymentRefund -> invoicePaymentRefund.getRefund().getId().equals(refundId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Refund not found, paymentId=%s, refundId=%s", payment.getPayment().getId(), refundId)));
    }

    public static InvoicePaymentAdjustment extractAdjustment(com.rbkmoney.damsel.payment_processing.InvoicePayment payment, String adjustmentId) {
        return payment.getAdjustments().stream()
                .filter(invoicePaymentAdjustment -> invoicePaymentAdjustment.getId().equals(adjustmentId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Adjustment not found, paymentId=%s, refundId=%s", payment.getPayment().getId(), adjustmentId)));
    }
}
