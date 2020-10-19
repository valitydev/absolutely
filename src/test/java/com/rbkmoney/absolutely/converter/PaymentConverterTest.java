package com.rbkmoney.absolutely.converter;

import com.rbkmoney.absolutely.AbsolutelyApplication;
import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain.BankCardPaymentSystem;
import com.rbkmoney.damsel.domain.BankCardTokenProvider;
import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.ClientInfo;
import com.rbkmoney.damsel.domain.ContactInfo;
import com.rbkmoney.damsel.domain.CustomerPayer;
import com.rbkmoney.damsel.domain.DisposablePaymentResource;
import com.rbkmoney.damsel.domain.InvoiceCart;
import com.rbkmoney.damsel.domain.InvoiceDetails;
import com.rbkmoney.damsel.domain.InvoiceLine;
import com.rbkmoney.damsel.domain.Payer;
import com.rbkmoney.damsel.domain.PaymentResourcePayer;
import com.rbkmoney.damsel.domain.PaymentRoute;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.domain.RecurrentParentPayment;
import com.rbkmoney.damsel.domain.RecurrentPayer;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentSession;
import com.rbkmoney.swag.adapter.abs.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
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
        Invoice invoice = buildBankCardPayerInvoice(paymentId);

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
        Payer sourcePayer = buildPaymentResourcePayer(buildPaymentToolBankCard());
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
        Payer sourcePayer = buildCustomerPayer(buildPaymentToolDigitalWallet());
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
        Payer sourcePayer = buildRecurrentPayer(buildPaymentToolMobileCommerce());
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

    private Invoice buildBankCardPayerInvoice(String paymentId) {
        return new Invoice()
                    .setInvoice(new com.rbkmoney.damsel.domain.Invoice()
                            .setId("invoice_id_444")
                            .setDetails(new InvoiceDetails()
                                    .setProduct("product_kek")
                                    .setDescription("description_lol")
                                    .setCart(new InvoiceCart()
                                            .setLines(List.of(new InvoiceLine()
                                                    .setPrice(new Cash().setAmount(133))
                                                    .setProduct("line_product")
                                                    .setQuantity(2)))))
                            .setContext(new Content().setData("{\"paper\": \"A4\", \"count\": 5}".getBytes())))
                    .setPayments(List.of(new InvoicePayment()
                            .setPayment(new com.rbkmoney.damsel.domain.InvoicePayment()
                                    .setId(paymentId)
                                    .setCreatedAt("2016-03-22T06:12:27Z")
                                    .setStatus(InvoicePaymentStatus.captured(new InvoicePaymentCaptured()))
                                    .setCost(new Cash()
                                            .setAmount(1245)
                                            .setCurrency(new CurrencyRef()
                                                    .setSymbolicCode("RUB")))
                                    .setDomainRevision(3)
                                    .setFlow(InvoicePaymentFlow.hold(new InvoicePaymentFlowHold()
                                            .setHeldUntil("2016-03-22T06:12:27Z")
                                            .setOnHoldExpiration(OnHoldExpiration.cancel)))
                                    .setPayer(buildRecurrentPayer(buildPaymentToolMobileCommerce()))
                                    .setPartyRevision(13)
                                    .setOwnerId("party_id_33")
                                    .setShopId("shop_id_88")
                                    .setMakeRecurrent(true)
                                    .setExternalId("45343262362362"))
                            .setRoute(new PaymentRoute()
                                    .setProvider(new ProviderRef(1))
                                    .setTerminal(new TerminalRef(3)))
                            .setSessions(List.of(new InvoicePaymentSession()
                                    .setTransactionInfo(new TransactionInfo()
                                            .setId("trx_id_t5")
                                            .setAdditionalInfo(new AdditionalTransactionInfo()
                                                    .setRrn("rrn555")))))));
    }

    private Payer buildPaymentResourcePayer(PaymentTool paymentTool) {
        return Payer.payment_resource(new PaymentResourcePayer()
                .setContactInfo(buildContactInfo())
                .setResource(new DisposablePaymentResource()
                        .setClientInfo(buildClientInfo())
                        .setPaymentTool(paymentTool)));
    }

    private PaymentTool buildPaymentToolBankCard() {
        return PaymentTool.bank_card(new BankCard()
                .setToken("token_kek")
                .setLastDigits("1245")
                .setBin("458899")
                .setPaymentSystem(BankCardPaymentSystem.amex)
                .setTokenProvider(BankCardTokenProvider.applepay));
    }

    private PaymentTool buildPaymentToolMobileCommerce() {
        return PaymentTool.mobile_commerce(new MobileCommerce()
                .setPhone(new MobilePhone().setCc("8").setCtn("9087775544"))
                .setOperator(MobileOperator.beeline));
    }

    private PaymentTool buildPaymentToolDigitalWallet() {
        return PaymentTool.digital_wallet(new DigitalWallet()
                .setId("dig_wal_1")
                .setProvider(DigitalWalletProvider.qiwi)
                .setToken("kekek_token"));
    }

    private ClientInfo buildClientInfo() {
        return new ClientInfo()
                .setIpAddress("127.0.0.1")
                .setFingerprint("sdjfdvbserweirbi4b2");
    }

    private ContactInfo buildContactInfo() {
        return new ContactInfo()
                .setEmail("kek@kek.ru")
                .setPhoneNumber("898889888483");
    }

    private Payer buildCustomerPayer(PaymentTool paymentTool) {
        return Payer.customer(new CustomerPayer()
                .setContactInfo(buildContactInfo())
                .setCustomerBindingId("cust_bing_id")
                .setCustomerId("cust_id")
                .setRecPaymentToolId("ejjeje")
                .setPaymentTool(paymentTool));
    }

    private Payer buildRecurrentPayer(PaymentTool paymentTool) {
        return Payer.recurrent(new RecurrentPayer()
                .setContactInfo(buildContactInfo())
                .setRecurrentParent(new RecurrentParentPayment()
                        .setInvoiceId("oinv_id")
                        .setPaymentId("ppapa_id"))
                .setPaymentTool(paymentTool));
    }
}