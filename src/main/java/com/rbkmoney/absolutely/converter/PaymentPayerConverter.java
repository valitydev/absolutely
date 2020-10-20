package com.rbkmoney.absolutely.converter;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain.DisposablePaymentResource;
import com.rbkmoney.damsel.domain.Payer;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.swag.adapter.abs.model.*;
import com.rbkmoney.swag.adapter.abs.model.BankCardPaymentSystem;
import com.rbkmoney.swag.adapter.abs.model.BankCardTokenProvider;
import com.rbkmoney.swag.adapter.abs.model.ClientInfo;
import com.rbkmoney.swag.adapter.abs.model.ContactInfo;
import com.rbkmoney.swag.adapter.abs.model.CustomerPayer;
import com.rbkmoney.swag.adapter.abs.model.PaymentResourcePayer;
import com.rbkmoney.swag.adapter.abs.model.RecurrentParentPayment;
import com.rbkmoney.swag.adapter.abs.model.RecurrentPayer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PaymentPayerConverter implements Converter<Payer, com.rbkmoney.swag.adapter.abs.model.Payer> {

    @Override
    public com.rbkmoney.swag.adapter.abs.model.Payer convert(Payer payer) {
        if (payer.isSetPaymentResource()) {
            return convertPaymentResourcePayer(payer.getPaymentResource());
        } else if (payer.isSetCustomer()) {
            return convertCustomerPayer(payer.getCustomer());
        } else if (payer.isSetRecurrent()) {
            return convertRecurrentPayer(payer.getRecurrent());
        } else {
            throw new IllegalStateException("Unknown payer type " + payer.getSetField().getFieldName());
        }
    }


    private com.rbkmoney.swag.adapter.abs.model.Payer convertRecurrentPayer(com.rbkmoney.damsel.domain.RecurrentPayer recurrentPayer) {
        return new RecurrentPayer()
                .contactInfo(convertContactInfo(recurrentPayer.getContactInfo()))
                .paymentTool(convertPaymentTool(recurrentPayer.getPaymentTool()))
                .recurrentParent(convertRecurrentParentPayment(recurrentPayer.getRecurrentParent()));
    }

    private RecurrentParentPayment convertRecurrentParentPayment(com.rbkmoney.damsel.domain.RecurrentParentPayment recurrentParent) {
        return new RecurrentParentPayment()
                .invoiceID(recurrentParent.getInvoiceId())
                .paymentID(recurrentParent.getPaymentId());
    }

    private ContactInfo convertContactInfo(com.rbkmoney.damsel.domain.ContactInfo contactInfo) {
        return new ContactInfo()
                .email(contactInfo.getEmail())
                .phoneNumber(contactInfo.getPhoneNumber());
    }

    private ClientInfo convertClientInfo(com.rbkmoney.damsel.domain.ClientInfo clientInfo) {
        return new ClientInfo()
                .ip(clientInfo.getIpAddress())
                .fingerprint(clientInfo.getFingerprint());
    }

    private com.rbkmoney.swag.adapter.abs.model.PaymentTool convertPaymentTool(PaymentTool paymentTool) {
        if (paymentTool.isSetBankCard()) {
            return convertPaymentToolBankCard(paymentTool.getBankCard());
        } else if (paymentTool.isSetCryptoCurrency()) {
            return convertPaymentToolCryptoWallet(paymentTool.getCryptoCurrency());
        } else if (paymentTool.isSetDigitalWallet()) {
            return convertPaymentToolDigitalWallet(paymentTool.getDigitalWallet());
        } else if (paymentTool.isSetMobileCommerce()) {
            return convertPaymentToolMobileCommerce(paymentTool.getMobileCommerce());
        } else if (paymentTool.isSetPaymentTerminal()) {
            return convertPaymentToolPaymentTerminal(paymentTool.getPaymentTerminal());
        } else {
            throw new IllegalStateException("Unknown payment tool " + paymentTool.getSetField().getFieldName());
        }
    }

    private com.rbkmoney.swag.adapter.abs.model.PaymentTool convertPaymentToolPaymentTerminal(PaymentTerminal paymentTerminal) {
        return new PaymentToolDetailsPaymentTerminal()
                .terminalType(PaymentTerminalProvider.fromValue(paymentTerminal.getTerminalType().name()));
    }

    private com.rbkmoney.swag.adapter.abs.model.PaymentTool convertPaymentToolMobileCommerce(MobileCommerce mobileCommerce) {
        return new PaymentToolDetailsMobileCommerce()
                .operator(PaymentToolDetailsMobileCommerce.OperatorEnum.fromValue(mobileCommerce.getOperator().name()))
                .phone(mobileCommerce.getPhone().getCc() + mobileCommerce.getPhone().getCtn());
    }

    private com.rbkmoney.swag.adapter.abs.model.PaymentTool convertPaymentToolDigitalWallet(DigitalWallet digitalWallet) {
        return new PaymentToolDetailsDigitalWallet()
                .id(digitalWallet.getId())
                .provider(PaymentToolDetailsDigitalWallet.ProviderEnum.fromValue(digitalWallet.getProvider().name()))
                .token(digitalWallet.getToken());
    }

    private com.rbkmoney.swag.adapter.abs.model.PaymentTool convertPaymentToolCryptoWallet(com.rbkmoney.damsel.domain.CryptoCurrency cryptoCurrency) {
        return new PaymentToolDetailsCryptoWallet()
                .cryptoCurrency(com.rbkmoney.swag.adapter.abs.model.CryptoCurrency.fromValue(cryptoCurrency.name()));
    }

    private com.rbkmoney.swag.adapter.abs.model.PaymentTool convertPaymentToolBankCard(BankCard bankCard) {
        return new PaymentToolDetailsBankCard()
                .token(bankCard.getToken())
                .bin(bankCard.getBin())
                .lastDigits(bankCard.getLastDigits())
                .paymentSystem(bankCard.isSetPaymentSystem() ? BankCardPaymentSystem.fromValue(bankCard.getPaymentSystem().name()) : null)
                .tokenProvider(BankCardTokenProvider.fromValue(bankCard.getTokenProvider().name()));
    }

    private com.rbkmoney.swag.adapter.abs.model.Payer convertCustomerPayer(com.rbkmoney.damsel.domain.CustomerPayer customerPayer) {
        return new CustomerPayer()
                .contactInfo(convertContactInfo(customerPayer.getContactInfo()))
                .customerBindingID(customerPayer.getCustomerBindingId())
                .customerID(customerPayer.getCustomerId())
                .paymentTool(convertPaymentTool(customerPayer.getPaymentTool()))
                .recurrentPaymentToolID(customerPayer.getRecPaymentToolId());
    }

    private com.rbkmoney.swag.adapter.abs.model.Payer convertPaymentResourcePayer(com.rbkmoney.damsel.domain.PaymentResourcePayer paymentResourcePayer) {
        DisposablePaymentResource resource = paymentResourcePayer.getResource();
        return new PaymentResourcePayer()
                .paymentSessionID(resource.getPaymentSessionId())
                .paymentTool(convertPaymentTool(resource.getPaymentTool()))
                .clientInfo(resource.isSetClientInfo() ? convertClientInfo(resource.getClientInfo()) : null)
                .contactInfo(convertContactInfo(paymentResourcePayer.getContactInfo()));
    }

}
