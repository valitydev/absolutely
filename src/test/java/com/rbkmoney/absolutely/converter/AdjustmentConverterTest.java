package com.rbkmoney.absolutely.converter;

import com.rbkmoney.absolutely.AbsolutelyApplication;
import com.rbkmoney.damsel.domain.InvoicePaymentAdjustment;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund;
import com.rbkmoney.swag.adapter.abs.model.Adjustment;
import com.rbkmoney.swag.adapter.abs.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = AbsolutelyApplication.class)
public class AdjustmentConverterTest {

    @Autowired
    private AdjustmentConverter adjustmentConverter;

    @Test
    public void convertTest() {
        String paymentId = "1";
        String adjId = "2";
        Invoice invoiceSource = TestData.buildInvoiceAdjustment(paymentId, adjId);
        Adjustment adjustment = (Adjustment) adjustmentConverter.convert(invoiceSource, paymentId, adjId);

        InvoicePayment invoicePaymentSource = invoiceSource.getPayments().get(0);
        InvoicePaymentAdjustment adjustmentSource = invoicePaymentSource.getAdjustments().get(0);
        assertEquals(adjustmentSource.getId(), adjustment.getId());
        assertEquals(invoicePaymentSource.getPayment().getId(), adjustment.getPaymentID());
        assertEquals(invoiceSource.getInvoice().getId(), adjustment.getInvoiceID());
        assertEquals(adjustmentSource.getDomainRevision(), (long)adjustment.getDomainRevision());
        assertEquals(adjustmentSource.getPartyRevision(), (long)adjustment.getPartyRevision());
        assertEquals(invoicePaymentSource.getPayment().getOwnerId(), adjustment.getPartyID());
        assertEquals(invoicePaymentSource.getPayment().getShopId(), adjustment.getShopID());

    }
}