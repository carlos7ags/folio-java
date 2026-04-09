// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Creates a PDF/A-3B compliant invoice with an embedded Factur-X/ZUGFeRD XML
 * file attachment. Demonstrates PDF/A compliance, file attachments, and
 * HTML-based invoice rendering.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.ZugferdInvoice
 * </pre>
 */
public final class ZugferdInvoice {

    private static final String INVOICE_HTML = """
            <html><head><style>
            body { font-family: Helvetica; font-size: 10px; margin: 0; }
            h1 { font-size: 22px; color: #1a1a2e; text-align: center; margin-bottom: 4px; }
            h2 { font-size: 13px; color: #2c3e50; margin-top: 12px; margin-bottom: 4px; }
            p { margin-bottom: 3px; line-height: 1.3; }
            table { width: 100%%; border-collapse: collapse; margin-top: 10px; }
            th { background-color: #f0f0f0; text-align: left; padding: 6px 8px; border-bottom: 2px solid #333; }
            td { padding: 5px 8px; border-bottom: 1px solid #ddd; }
            .right { text-align: right; }
            .total td { border-top: 2px solid #333; font-weight: bold; }
            .meta { color: #555; }
            hr { margin: 8px 0; border: none; border-top: 1px solid #ccc; }
            </style></head><body>
            <h1>INVOICE</h1>
            <hr/>
            <p><b>Invoice Number:</b> 2024-001</p>
            <p><b>Date:</b> 2024-01-15</p>
            <p><b>Due Date:</b> 2024-02-15</p>
            <h2>From</h2>
            <p>ACME Corp<br/>123 Main Street<br/>Berlin, 10115, Germany<br/>VAT: DE123456789</p>
            <h2>Bill To</h2>
            <p>Example GmbH<br/>456 Business Ave<br/>Munich, 80331, Germany<br/>VAT: DE987654321</p>
            <table>
            <tr><th>Description</th><th class="right">Qty</th><th class="right">Unit Price</th><th class="right">Total</th></tr>
            <tr><td>Widget A - Standard</td><td class="right">10</td><td class="right">5.00 EUR</td><td class="right">50.00 EUR</td></tr>
            <tr><td>Widget B - Premium</td><td class="right">3</td><td class="right">12.50 EUR</td><td class="right">37.50 EUR</td></tr>
            <tr><td>Consulting Service</td><td class="right">1</td><td class="right">250.00 EUR</td><td class="right">250.00 EUR</td></tr>
            <tr class="total"><td></td><td></td><td class="right">Subtotal:</td><td class="right">337.50 EUR</td></tr>
            <tr class="total"><td></td><td></td><td class="right">VAT (19%%):</td><td class="right">64.13 EUR</td></tr>
            <tr class="total"><td></td><td></td><td class="right"><b>Total:</b></td><td class="right"><b>401.63 EUR</b></td></tr>
            </table>
            <p class="meta" style="margin-top: 14px;">Payment terms: 30 days net. Bank: Deutsche Bank, IBAN: DE89 3704 0044 0532 0130 00</p>
            <p class="meta">This invoice contains an embedded Factur-X XML for automated processing.</p>
            </body></html>
            """;

    private static final String FACTURX_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rsm:CrossIndustryInvoice
              xmlns:rsm="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100"
              xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"
              xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100">
              <rsm:ExchangedDocument>
                <ram:ID>2024-001</ram:ID>
                <ram:TypeCode>380</ram:TypeCode>
                <ram:IssueDateTime>
                  <udt:DateTimeString format="102">20240115</udt:DateTimeString>
                </ram:IssueDateTime>
              </rsm:ExchangedDocument>
              <rsm:SupplyChainTradeTransaction>
                <ram:ApplicableHeaderTradeAgreement>
                  <ram:SellerTradeParty><ram:Name>ACME Corp</ram:Name></ram:SellerTradeParty>
                  <ram:BuyerTradeParty><ram:Name>Example GmbH</ram:Name></ram:BuyerTradeParty>
                </ram:ApplicableHeaderTradeAgreement>
                <ram:ApplicableHeaderTradeSettlement>
                  <ram:InvoiceCurrencyCode>EUR</ram:InvoiceCurrencyCode>
                  <ram:SpecifiedTradeSettlementHeaderMonetarySummation>
                    <ram:GrandTotalAmount>401.63</ram:GrandTotalAmount>
                    <ram:DuePayableAmount>401.63</ram:DuePayableAmount>
                  </ram:SpecifiedTradeSettlementHeaderMonetarySummation>
                </ram:ApplicableHeaderTradeSettlement>
              </rsm:SupplyChainTradeTransaction>
            </rsm:CrossIndustryInvoice>
            """;

    public static void main(String[] args) {
        // PDF/A requires all fonts to be embedded — discover a system TTF
        String fontPath = findSystemFont();
        if (fontPath == null) {
            System.err.println("No suitable system font found for PDF/A embedding");
            System.exit(1);
        }

        // Inject @font-face into the invoice HTML so all text uses the embedded font
        String fontCss = """
            @font-face { font-family: 'Inv'; src: url('%s'); }
            @font-face { font-family: 'InvBold'; font-weight: bold; src: url('%s'); }
            body { font-family: 'Inv'; }
            b, strong, th { font-family: 'InvBold'; }
            """.formatted(fontPath, fontPath);
        String html = INVOICE_HTML.replace(
            "body { font-family: Helvetica;",
            fontCss + "\nbody { font-family: 'Inv';");

        try (var doc = Document.builder()
                .pageSize(PageSize.A4)
                .title("Invoice 2024-001")
                .author("ACME Corp")
                .margins(40, 40, 40, 40)
                .build()) {

            // Render invoice content via HTML with embedded font
            doc.addHtml(html);

            // Enable PDF/A-3B for e-invoice compliance
            doc.pdfA(PdfALevel.PDF_A_3B);

            // Attach Factur-X XML
            doc.attachFile(
                FACTURX_XML.getBytes(StandardCharsets.UTF_8),
                "factur-x.xml",
                "application/xml",
                "Factur-X XML Invoice Data (BASIC profile)",
                "Alternative"
            );

            doc.save("zugferd-invoice.pdf");
            System.out.println("Created zugferd-invoice.pdf");
        }
    }

    private static String findSystemFont() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String[] candidates;
        if (os.contains("mac")) {
            candidates = new String[] {
                "/System/Library/Fonts/Supplemental/Arial.ttf",
                "/System/Library/Fonts/Supplemental/Verdana.ttf",
                "/System/Library/Fonts/Supplemental/Georgia.ttf",
            };
        } else if (os.contains("linux")) {
            candidates = new String[] {
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf",
            };
        } else {
            candidates = new String[] {
                "C:\\Windows\\Fonts\\arial.ttf",
                "C:\\Windows\\Fonts\\verdana.ttf",
            };
        }
        for (String path : candidates) {
            if (new File(path).exists()) return path;
        }
        return null;
    }
}
