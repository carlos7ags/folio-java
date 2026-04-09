// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

/**
 * Professional invoice PDF built from HTML with modern CSS: Grid layout,
 * Flexbox, rounded table headers, and color-coded totals.
 *
 * <p>Demonstrates that the Folio Java SDK can produce polished, branded
 * invoices from a single HTML string — no manual layout API calls needed.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Invoice
 * </pre>
 */
public final class Invoice {

    private static final String INVOICE_HTML = """
            <!DOCTYPE html>
            <html>
            <head>
            <style>
              @page { size: Letter; margin: 40px; }
              body {
                font-family: Helvetica;
                font-size: 10px;
                color: #1f2937;
                margin: 0;
              }
              .header {
                display: grid;
                grid-template-columns: 1fr 1fr;
                align-items: start;
                margin-bottom: 32px;
                padding-bottom: 20px;
                border-bottom: 2px solid #1e40af;
              }
              .brand { font-size: 28px; font-weight: bold; color: #1e40af; }
              .brand-tag { color: #6b7280; font-size: 10px; margin-top: 4px; }
              .invoice-meta { text-align: right; }
              .invoice-meta h2 {
                font-size: 24px;
                color: #1e40af;
                margin: 0 0 8px 0;
                font-weight: bold;
              }
              .invoice-meta p { margin: 2px 0; font-size: 10px; }
              .invoice-meta .label { color: #6b7280; }
              .parties {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 24px;
                margin-bottom: 28px;
              }
              .party h3 {
                font-size: 10px;
                text-transform: uppercase;
                color: #6b7280;
                margin: 0 0 6px 0;
                letter-spacing: 0.5px;
              }
              .party .name { font-weight: bold; font-size: 12px; margin-bottom: 4px; }
              .party p { margin: 1px 0; font-size: 10px; color: #374151; }
              table.items {
                width: 100%%;
                border-collapse: separate;
                border-spacing: 0;
                margin-bottom: 24px;
              }
              table.items thead th {
                background-color: #1e40af;
                color: white;
                font-weight: bold;
                text-align: left;
                padding: 10px 12px;
                font-size: 10px;
                text-transform: uppercase;
                letter-spacing: 0.5px;
              }
              table.items thead th:first-child { border-top-left-radius: 6px; }
              table.items thead th:last-child { border-top-right-radius: 6px; }
              table.items tbody td {
                padding: 10px 12px;
                border-bottom: 1px solid #e5e7eb;
                font-size: 10px;
              }
              table.items tbody tr:last-child td { border-bottom: none; }
              .right { text-align: right; }
              .totals {
                display: flex;
                justify-content: flex-end;
                margin-bottom: 24px;
              }
              .totals-box {
                width: 280px;
                background-color: #f9fafb;
                border-radius: 6px;
                padding: 16px 20px;
              }
              .totals-row {
                display: flex;
                justify-content: space-between;
                padding: 4px 0;
                font-size: 11px;
              }
              .totals-row.grand {
                border-top: 2px solid #1e40af;
                margin-top: 8px;
                padding-top: 10px;
                font-size: 14px;
                font-weight: bold;
                color: #1e40af;
              }
              .footer {
                margin-top: 32px;
                padding: 14px 16px;
                background-color: #eff6ff;
                border-radius: 6px;
                font-size: 9px;
                color: #1e40af;
              }
              .footer strong { display: block; margin-bottom: 4px; }
            </style>
            </head>
            <body>

            <div class="header">
              <div>
                <div class="brand">FOLIO</div>
                <div class="brand-tag">Professional PDF Solutions</div>
              </div>
              <div class="invoice-meta">
                <h2>INVOICE</h2>
                <p><span class="label">Invoice #:</span> <b>INV-2026-0042</b></p>
                <p><span class="label">Issue Date:</span> April 8, 2026</p>
                <p><span class="label">Due Date:</span> May 8, 2026</p>
              </div>
            </div>

            <div class="parties">
              <div class="party">
                <h3>From</h3>
                <div class="name">Folio Solutions Ltd.</div>
                <p>742 Evergreen Terrace</p>
                <p>Springfield, IL 62704</p>
                <p>United States</p>
                <p>billing@foliopdf.dev</p>
              </div>
              <div class="party">
                <h3>Bill To</h3>
                <div class="name">Acme Corporation</div>
                <p>1 Acme Plaza, Suite 500</p>
                <p>New York, NY 10001</p>
                <p>United States</p>
                <p>accounts@acme.example</p>
              </div>
            </div>

            <table class="items">
              <thead>
                <tr>
                  <th>Description</th>
                  <th class="right">Qty</th>
                  <th class="right">Unit Price</th>
                  <th class="right">Amount</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Folio Enterprise License (annual)</td>
                  <td class="right">1</td>
                  <td class="right">$4,800.00</td>
                  <td class="right">$4,800.00</td>
                </tr>
                <tr>
                  <td>Premium Support — Q2 2026</td>
                  <td class="right">1</td>
                  <td class="right">$1,200.00</td>
                  <td class="right">$1,200.00</td>
                </tr>
                <tr>
                  <td>Integration Consulting</td>
                  <td class="right">12</td>
                  <td class="right">$180.00</td>
                  <td class="right">$2,160.00</td>
                </tr>
                <tr>
                  <td>Custom Template Design</td>
                  <td class="right">3</td>
                  <td class="right">$450.00</td>
                  <td class="right">$1,350.00</td>
                </tr>
              </tbody>
            </table>

            <div class="totals">
              <div class="totals-box">
                <div class="totals-row"><span>Subtotal</span><span>$9,510.00</span></div>
                <div class="totals-row"><span>Tax (8.875%%)</span><span>$843.76</span></div>
                <div class="totals-row grand"><span>Total Due</span><span>$10,353.76</span></div>
              </div>
            </div>

            <div class="footer">
              <strong>Payment Terms</strong>
              Net 30. Wire transfer to Folio Solutions Ltd.,
              Routing 021000021, Account 4812039501. Please reference
              invoice number INV-2026-0042 on your payment.
            </div>

            </body>
            </html>
            """;

    public static void main(String[] args) {
        System.out.println("Rendering invoice from HTML...");
        HtmlConverter.toPdf(INVOICE_HTML, "invoice.pdf");
        System.out.println("Created invoice.pdf");
    }
}
