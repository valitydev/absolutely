package com.rbkmoney.absolutely.converter;

import com.rbkmoney.absolutely.AbsolutelyApplication;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund;
import com.rbkmoney.swag.adapter.abs.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = AbsolutelyApplication.class)
public class RefundConverterTest {

    @Autowired
    private RefundConverter refundConverter;

    @Test
    public void convertTest() {
        String paymentId = "1";
        String refundId = "2";
        Invoice invoiceSource = TestData.buildInvoiceRefund(paymentId, refundId);
        Refund refund = (Refund) refundConverter.convert(invoiceSource, paymentId, refundId);

        InvoicePayment invoicePaymentSource = invoiceSource.getPayments().get(0);
        InvoicePaymentRefund invoicePaymentRefundSource = invoicePaymentSource.getRefunds().get(0);
        com.rbkmoney.damsel.domain.InvoicePaymentRefund refundSource = invoicePaymentRefundSource.getRefund();
        assertEquals(refundSource.getId(), refund.getId());
        assertEquals(invoicePaymentSource.getPayment().getId(), refund.getPaymentID());
        assertEquals(refundSource.getReason(), refund.getReason());
        assertEquals(Refund.StatusEnum.SUCCEEDED, refund.getStatus());
        assertEquals(invoicePaymentRefundSource.getSessions().get(0).getTransactionInfo().getId(),
                refund.getTransactionInfo().getId());
        assertEquals(invoicePaymentRefundSource.getSessions().get(0).getTransactionInfo().getAdditionalInfo().getRrn(),
                refund.getTransactionInfo().getRrn());
        assertEquals(invoiceSource.getInvoice().getId(), refund.getInvoiceID());
        assertEquals(refundSource.getCash().getAmount(), (long)refund.getAmount());
        assertEquals(refundSource.getCash().getCurrency().getSymbolicCode(), refund.getCurrency());
        assertEquals(refundSource.getDomainRevision(), (long)refund.getDomainRevision());
        assertEquals(refundSource.getPartyRevision(), (long)refund.getPartyRevision());
        assertEquals(invoicePaymentSource.getPayment().getOwnerId(), refund.getPartyID());
        assertEquals(invoicePaymentSource.getPayment().getShopId(), refund.getShopID());

    }
}