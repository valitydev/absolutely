package com.rbkmoney.absolutely.converter;

import com.rbkmoney.absolutely.AbsolutelyApplication;
import com.rbkmoney.damsel.domain.InvoiceDetails;
import com.rbkmoney.damsel.domain.Payer;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.swag.adapter.abs.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = AbsolutelyApplication.class)
public class PaymentConverterTest {

    @Autowired
    private PaymentConverter paymentConverter;

    @Autowired
    private PaymentPayerConverter paymentPayerConverter;

    @Test
    public void convertTest() {

        String paymentId = "1";
        Invoice invoice = TestData.buildInvoicePayment(paymentId);

        Payment payment = (Payment) paymentConverter.convert(invoice, paymentId);
        assertEquals(invoice.getInvoice().getId(), payment.getInvoiceID());

        //check invoiceDetails equality
        InvoiceDetails detailsSource = invoice.getInvoice().getDetails();
        assertEquals(detailsSource.getProduct(), payment.getInvoiceDetails().getProduct());
        assertEquals(detailsSource.getDescription(), payment.getInvoiceDetails().getDescription());
        assertEquals(detailsSource.getCart().getLines().size(), payment.getInvoiceDetails().getCart().size());
        assertEquals(detailsSource.getCart().getLines().get(0).getPrice().getAmount(), (long) payment.getInvoiceDetails().getCart().get(0).getPrice());
        assertEquals(detailsSource.getCart().getLines().get(0).getProduct(), payment.getInvoiceDetails().getCart().get(0).getProduct());
        assertEquals(detailsSource.getCart().getLines().get(0).getQuantity(), (long) payment.getInvoiceDetails().getCart().get(0).getQuantity());
        assertNotNull(payment.getInvoiceDetails().getMetadata());
        assertTrue(payment.getInvoiceDetails().getMetadata() instanceof Map);
        assertEquals(((Map) payment.getInvoiceDetails().getMetadata()).get("paper"), "A4");

        //check payment equality
        InvoicePayment invoicePaymentSource = invoice.getPayments().get(0);
        com.rbkmoney.damsel.domain.InvoicePayment invoicePayment = invoicePaymentSource.getPayment();
        assertEquals(invoicePayment.getId(), payment.getId());
        assertEquals(Payment.StatusEnum.CAPTURED, payment.getStatus());
        assertEquals(invoicePayment.getCost().getAmount(), (long) payment.getAmount());
        assertEquals(invoicePayment.getCost().getCurrency().getSymbolicCode(), payment.getCurrency());
        assertEquals(invoicePayment.getDomainRevision(), (long)payment.getDomainRevision());
        assertEquals(invoicePayment.getPartyRevision(), (long)payment.getPartyRevision());
        assertTrue(payment.getFlow() instanceof PaymentFlowHold);
        assertEquals(invoicePayment.getFlow().getHold().getOnHoldExpiration().name(), ((PaymentFlowHold) payment.getFlow()).getOnHoldExpiration().getValue());
        assertEquals(invoicePayment.getOwnerId(), payment.getPartyID());
        assertEquals(invoicePayment.getShopId(), payment.getShopID());
        assertEquals(invoicePayment.isMakeRecurrent(), payment.getMakeRecurrent());
        assertEquals(invoicePayment.getExternalId(), payment.getExternalID());

        assertEquals(invoicePaymentSource.getRoute().getProvider().getId(), (int)payment.getPaymentRoute().getProviderID());
        assertEquals(invoicePaymentSource.getRoute().getTerminal().getId(), (int)payment.getPaymentRoute().getTerminalID());

        TransactionInfo transactionInfoSource = invoicePaymentSource.getSessions().get(0).getTransactionInfo();
        assertEquals(transactionInfoSource.getId(), payment.getTransactionInfo().getId());
        assertEquals(transactionInfoSource.getAdditionalInfo().getRrn(), payment.getTransactionInfo().getRrn());
    }

    @Test
    public void paymentPayerConverterTest() {
        Payer sourcePayer = TestData.buildPaymentResourcePayer(TestData.buildPaymentToolBankCard());
        var payer = paymentPayerConverter.convert(sourcePayer);
        var paymentResourcePayer = (com.rbkmoney.swag.adapter.abs.model.PaymentResourcePayer) payer;
        assertEquals(sourcePayer.getPaymentResource().getContactInfo().getEmail(), paymentResourcePayer.getContactInfo().getEmail());
        assertEquals(sourcePayer.getPaymentResource().getContactInfo().getPhoneNumber(), paymentResourcePayer.getContactInfo().getPhoneNumber());
        assertEquals(sourcePayer.getPaymentResource().getResource().getPaymentSessionId(), paymentResourcePayer.getPaymentSessionID());
        assertEquals(sourcePayer.getPaymentResource().getResource().getClientInfo().getIpAddress(), paymentResourcePayer.getClientInfo().getIp());
        assertEquals(sourcePayer.getPaymentResource().getResource().getClientInfo().getFingerprint(), paymentResourcePayer.getClientInfo().getFingerprint());
        assertTrue(paymentResourcePayer.getPaymentTool() instanceof PaymentToolDetailsBankCard);

        PaymentTool paymentTool = sourcePayer.getPaymentResource().getResource().getPaymentTool();
        PaymentToolDetailsBankCard paymentToolConverted = (PaymentToolDetailsBankCard) paymentResourcePayer.getPaymentTool();
        assertEquals(paymentTool.getBankCard().getToken(), paymentToolConverted.getToken());
        assertEquals(paymentTool.getBankCard().getTokenProvider().name(), paymentToolConverted.getTokenProvider().getValue());
        assertEquals(paymentTool.getBankCard().getBin(), paymentToolConverted.getBin());
        assertEquals(paymentTool.getBankCard().getLastDigits(), paymentToolConverted.getLastDigits());
        assertEquals(paymentTool.getBankCard().getPaymentSystem().name(), paymentToolConverted.getPaymentSystem().getValue());
    }

    @Test
    public void paymentPayerConverterCustomerTest() {
        Payer sourcePayer = TestData.buildCustomerPayer(TestData.buildPaymentToolDigitalWallet());
        var payer = paymentPayerConverter.convert(sourcePayer);
        var customerPayer = (com.rbkmoney.swag.adapter.abs.model.CustomerPayer) payer;
        assertEquals(sourcePayer.getCustomer().getContactInfo().getPhoneNumber(), customerPayer.getContactInfo().getPhoneNumber());
        assertEquals(sourcePayer.getCustomer().getContactInfo().getEmail(), customerPayer.getContactInfo().getEmail());
        assertEquals(sourcePayer.getCustomer().getCustomerId(), customerPayer.getCustomerID());
        assertEquals(sourcePayer.getCustomer().getCustomerBindingId(), customerPayer.getCustomerBindingID());
        assertEquals(sourcePayer.getCustomer().getRecPaymentToolId(), customerPayer.getRecurrentPaymentToolID());
        assertTrue(customerPayer.getPaymentTool() instanceof PaymentToolDetailsDigitalWallet);
        assertEquals(sourcePayer.getCustomer().getPaymentTool().getDigitalWallet().getId(), ((PaymentToolDetailsDigitalWallet) customerPayer.getPaymentTool()).getId());
        assertEquals(sourcePayer.getCustomer().getPaymentTool().getDigitalWallet().getToken(), ((PaymentToolDetailsDigitalWallet) customerPayer.getPaymentTool()).getToken());
        assertEquals(sourcePayer.getCustomer().getPaymentTool().getDigitalWallet().getProvider().name(), ((PaymentToolDetailsDigitalWallet) customerPayer.getPaymentTool()).getProvider().getValue());
    }

    @Test
    public void paymentPayerConverterRecurrntTest() {
        Payer sourcePayer = TestData.buildRecurrentPayer(TestData.buildPaymentToolMobileCommerce());
        var payer = paymentPayerConverter.convert(sourcePayer);
        var recurrentPayer = (com.rbkmoney.swag.adapter.abs.model.RecurrentPayer) payer;
        assertEquals(sourcePayer.getRecurrent().getContactInfo().getPhoneNumber(), recurrentPayer.getContactInfo().getPhoneNumber());
        assertEquals(sourcePayer.getRecurrent().getContactInfo().getEmail(), recurrentPayer.getContactInfo().getEmail());
        assertEquals(sourcePayer.getRecurrent().getRecurrentParent().getInvoiceId(), recurrentPayer.getRecurrentParent().getInvoiceID());
        assertEquals(sourcePayer.getRecurrent().getRecurrentParent().getPaymentId(), recurrentPayer.getRecurrentParent().getPaymentID());

        assertTrue(recurrentPayer.getPaymentTool() instanceof PaymentToolDetailsMobileCommerce);
        assertEquals(sourcePayer.getRecurrent().getPaymentTool().getMobileCommerce().getPhone().getCc() +
                        sourcePayer.getRecurrent().getPaymentTool().getMobileCommerce().getPhone().getCtn(),
                ((PaymentToolDetailsMobileCommerce) recurrentPayer.getPaymentTool()).getPhone());
        assertEquals(sourcePayer.getRecurrent().getPaymentTool().getMobileCommerce().getOperator().name(),
                ((PaymentToolDetailsMobileCommerce) recurrentPayer.getPaymentTool()).getOperator().getValue());

    }

}