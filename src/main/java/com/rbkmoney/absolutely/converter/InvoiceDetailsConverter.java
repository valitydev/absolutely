package com.rbkmoney.absolutely.converter;

import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.domain.InvoiceDetails;
import com.rbkmoney.swag.adapter.abs.model.InvoiceCart;
import com.rbkmoney.swag.adapter.abs.model.InvoiceLine;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceDetailsConverter implements Converter<Invoice, com.rbkmoney.swag.adapter.abs.model.InvoiceDetails> {

    private final MetadataDeserializer deserializer;

    @Override
    public com.rbkmoney.swag.adapter.abs.model.InvoiceDetails convert(Invoice invoice) {
            var invoiceDetails = invoice.getDetails();
            InvoiceCart cart = new InvoiceCart();
            invoiceDetails.getCart().getLines().forEach(l ->
                    cart.add(new InvoiceLine()
                            .product(l.getProduct())
                            .price(l.getPrice().getAmount())
                            .quantity(l.getQuantity())));

            return new com.rbkmoney.swag.adapter.abs.model.InvoiceDetails()
                    .product(invoiceDetails.getProduct())
                    .description(invoiceDetails.getDescription())
                    .metadata(getMetadata(invoice))
                    .cart(cart);
    }

    private Object getMetadata(com.rbkmoney.damsel.domain.Invoice source) {
        return source.isSetContext() ? deserializer.deserialize(source.getContext().getData()) : null;
    }

}
