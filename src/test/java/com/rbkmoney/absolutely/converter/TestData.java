package com.rbkmoney.absolutely.converter;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentSession;
import com.rbkmoney.damsel.payment_processing.InvoiceRefundSession;

import java.util.List;

public class TestData {

    public static Invoice buildInvoicePayment(String paymentId) {
        return new Invoice()
                .setInvoice(buildInvoicePayment())
                .setPayments(List.of(new InvoicePayment()
                        .setPayment(buildPayment(paymentId))
                        .setCashFlow(buildCashFlow())
                        .setRoute(buildRoute())
                        .setSessions(buildPaymentSessions())));
    }

    private static List<FinalCashFlowPosting> buildCashFlow() {
        return List.of(new FinalCashFlowPosting()
                .setSource(new FinalCashFlowAccount()
                        .setAccountType(CashFlowAccount.merchant(MerchantCashFlowAccount.settlement))
                        .setAccountId(111))
                .setDestination(new FinalCashFlowAccount()
                        .setAccountType(CashFlowAccount.system(SystemCashFlowAccount.settlement))
                        .setAccountId(3444))
                .setVolume(new Cash()
                        .setAmount(1111)
                        .setCurrency(new CurrencyRef()
                                .setSymbolicCode("RUB"))));
    }

    public static Invoice buildInvoiceRefund(String paymentId, String refundId) {
        Invoice invoice = buildInvoicePayment(paymentId);
        InvoicePayment invoicePayment = invoice.getPayments().get(0);
        invoicePayment.setRefunds(List.of(
                new com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund()
                        .setRefund(buildRefund(refundId))
                        .setSessions(buildRefundSessions())));
        return invoice;
    }

    public static Invoice buildInvoiceAdjustment(String paymentId, String adjustmentId) {
        Invoice invoice = buildInvoicePayment(paymentId);
        InvoicePayment invoicePayment = invoice.getPayments().get(0);
        invoicePayment.setAdjustments(List.of(buildAdjustment(adjustmentId)));
        return invoice;
    }

    private static InvoicePaymentAdjustment buildAdjustment(String adjustmentId) {
        return new InvoicePaymentAdjustment()
                .setId(adjustmentId)
                .setStatus(InvoicePaymentAdjustmentStatus.captured(new InvoicePaymentAdjustmentCaptured()))
                .setCreatedAt("2016-03-22T06:12:27Z")
                .setDomainRevision(1)
                .setPartyRevision(3)
                .setReason("reason")
                .setNewCashFlow(buildCashFlow())
                .setOldCashFlowInverse(buildCashFlow());
    }


    private static InvoicePaymentRefund buildRefund(String refundId) {
        return new InvoicePaymentRefund()
                .setId(refundId)
                .setStatus(InvoicePaymentRefundStatus.succeeded(new InvoicePaymentRefundSucceeded()))
                .setCreatedAt("2016-03-22T06:12:27Z")
                .setDomainRevision(1)
                .setPartyRevision(3)
                .setCash(new Cash()
                        .setAmount(1245)
                        .setCurrency(new CurrencyRef()
                                .setSymbolicCode("RUB")))
                .setReason("reason")
                .setCart(buildInvoiceCart())
                .setExternalId("113");
    }

    private static List<InvoicePaymentSession> buildPaymentSessions() {
        return List.of(new InvoicePaymentSession()
                .setTransactionInfo(new TransactionInfo()
                        .setId("trx_id_t5")
                        .setAdditionalInfo(new AdditionalTransactionInfo()
                                .setRrn("rrn555"))));
    }

    private static List<InvoiceRefundSession> buildRefundSessions() {
        return List.of(new InvoiceRefundSession()
                .setTransactionInfo(new TransactionInfo()
                        .setId("trx_id_t5")
                        .setAdditionalInfo(new AdditionalTransactionInfo()
                                .setRrn("rrn555"))));
    }

    private static PaymentRoute buildRoute() {
        return new PaymentRoute()
                .setProvider(new ProviderRef(1))
                .setTerminal(new TerminalRef(3));
    }

    private static com.rbkmoney.damsel.domain.InvoicePayment buildPayment(String paymentId) {
        return new com.rbkmoney.damsel.domain.InvoicePayment()
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
                .setExternalId("45343262362362");
    }

    private static com.rbkmoney.damsel.domain.Invoice buildInvoicePayment() {
        return new com.rbkmoney.damsel.domain.Invoice()
                .setId("invoice_id_444")
                .setDetails(new InvoiceDetails()
                        .setProduct("product_kek")
                        .setDescription("description_lol")
                        .setCart(buildInvoiceCart()))
                .setContext(new Content().setData("{\"paper\": \"A4\", \"count\": 5}".getBytes()));
    }

    private static InvoiceCart buildInvoiceCart() {
        return new InvoiceCart()
                .setLines(List.of(new InvoiceLine()
                        .setPrice(new Cash().setAmount(133))
                        .setProduct("line_product")
                        .setQuantity(2)));
    }

    public static Payer buildPaymentResourcePayer(PaymentTool paymentTool) {
        return Payer.payment_resource(new PaymentResourcePayer()
                .setContactInfo(buildContactInfo())
                .setResource(new DisposablePaymentResource()
                        .setClientInfo(buildClientInfo())
                        .setPaymentTool(paymentTool)));
    }

    public static PaymentTool buildPaymentToolBankCard() {
        return PaymentTool.bank_card(new BankCard()
                .setToken("token_kek")
                .setLastDigits("1245")
                .setBin("458899")
                .setPaymentSystem(BankCardPaymentSystem.amex)
                .setTokenProvider(BankCardTokenProvider.applepay));
    }

    public static PaymentTool buildPaymentToolMobileCommerce() {
        return PaymentTool.mobile_commerce(new MobileCommerce()
                .setPhone(new MobilePhone().setCc("8").setCtn("9087775544"))
                .setOperator(MobileOperator.beeline));
    }

    public static PaymentTool buildPaymentToolDigitalWallet() {
        return PaymentTool.digital_wallet(new DigitalWallet()
                .setId("dig_wal_1")
                .setProvider(DigitalWalletProvider.qiwi)
                .setToken("kekek_token"));
    }

    private static ClientInfo buildClientInfo() {
        return new ClientInfo()
                .setIpAddress("127.0.0.1")
                .setFingerprint("sdjfdvbserweirbi4b2");
    }

    private static ContactInfo buildContactInfo() {
        return new ContactInfo()
                .setEmail("kek@kek.ru")
                .setPhoneNumber("898889888483");
    }

    public static Payer buildCustomerPayer(PaymentTool paymentTool) {
        return Payer.customer(new CustomerPayer()
                .setContactInfo(buildContactInfo())
                .setCustomerBindingId("cust_bing_id")
                .setCustomerId("cust_id")
                .setRecPaymentToolId("ejjeje")
                .setPaymentTool(paymentTool));
    }

    public static Payer buildRecurrentPayer(PaymentTool paymentTool) {
        return Payer.recurrent(new RecurrentPayer()
                .setContactInfo(buildContactInfo())
                .setRecurrentParent(new RecurrentParentPayment()
                        .setInvoiceId("oinv_id")
                        .setPaymentId("ppapa_id"))
                .setPaymentTool(paymentTool));
    }
}
