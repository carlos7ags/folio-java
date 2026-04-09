// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

/**
 * Converts a rich HTML+CSS quarterly report into a multi-page PDF.
 * Demonstrates flexbox, tables, page breaks, gradient headers, and more.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.HtmlToPdf
 * </pre>
 */
public final class HtmlToPdf {

    private static final String REPORT_HTML = """
            <!DOCTYPE html>
            <html>
            <head>
              <title>Q4 2026 Quarterly Report</title>
              <meta name="author" content="Apex Capital Partners">
              <style>
                :root { --brand: #0f172a; --accent: #0d9488; --muted: #94a3b8; --border: #e2e8f0; }
                @page { size: A4; margin: 0 0 24px 0; }
                body { font-family: Helvetica, Arial, sans-serif; margin: 0; padding: 0; color: #2d3748; font-size: 10pt; }
                .header-band { background: linear-gradient(135deg, #0f172a, #4a6fa5); color: white; padding: 28px 2cm 24px; }
                .header-band h1 { font-size: 24pt; margin: 0 0 2px; font-weight: 700; }
                .header-band .sub { font-size: 10pt; color: var(--muted); }
                .body { padding: 24px 2cm 2cm; }
                .kpi-grid { display: flex; gap: 14px; margin-bottom: 28px; }
                .kpi { flex: 1; border: 1px solid var(--border); border-radius: 6px; padding: 14px; }
                .kpi-label { font-size: 7pt; text-transform: uppercase; color: var(--muted); margin-bottom: 6px; }
                .kpi-value { font-size: 22pt; font-weight: 700; color: var(--brand); }
                .kpi-change { font-size: 8.5pt; margin-top: 4px; }
                .up { color: #059669; } .down { color: #dc2626; }
                a { color: var(--accent); text-decoration: underline; }
                h2 { font-size: 12pt; font-weight: 700; color: var(--brand); margin: 24px 0 10px; padding-bottom: 6px; border-bottom: 1px solid var(--border); }
                table { width: 100%%; border-collapse: collapse; margin-bottom: 20px; }
                th { padding: 7px 10px; text-align: left; font-size: 7.5pt; text-transform: uppercase; color: #64748b; border-bottom: 2px solid #e2e8f0; }
                td { padding: 7px 10px; border-bottom: 1px solid #f1f5f9; font-size: 9pt; }
                .r { text-align: right; }
                .two-col { display: flex; gap: 24px; } .two-col > div { flex: 1; }
                .callout { padding: 12px 16px; background-color: #fffbeb; border-left: 3px solid #f59e0b; margin: 20px 0; font-size: 9pt; }
                .footer { margin-top: 28px; padding-top: 12px; border-top: 1px solid #e2e8f0; font-size: 7.5pt; color: #94a3b8; text-align: center; }
                .page-break { break-before: page; }
                .team-grid { display: flex; flex-wrap: wrap; gap: 16px; }
                .team-card { flex: 1; min-width: 200px; border: 1px solid var(--border); border-radius: 6px; padding: 14px; }
                .team-name { font-weight: 700; color: var(--brand); font-size: 10pt; }
                .team-role { font-size: 8pt; color: var(--accent); text-transform: uppercase; margin-bottom: 6px; }
                .team-bio { font-size: 8.5pt; color: #64748b; line-height: 1.5; }
                .milestone-list { padding-left: 18px; }
                .milestone-list li { margin-bottom: 6px; font-size: 9pt; color: #475569; }
                .milestone-list li strong { color: var(--brand); }
                .confidential { opacity: 0.4; font-size: 7pt; text-transform: uppercase; text-align: center; margin-top: 12px; }
              </style>
            </head>
            <body>
              <div class="header-band">
                <h1>Quarterly Report</h1>
                <div class="sub">Q4 2026 &mdash; Apex Capital Partners &mdash; Confidential</div>
              </div>
              <div class="body">
                <div class="kpi-grid">
                  <div class="kpi"><div class="kpi-label">Revenue</div><div class="kpi-value">$28.3M</div><div class="kpi-change up">+22%% YoY</div></div>
                  <div class="kpi"><div class="kpi-label">Net Income</div><div class="kpi-value">$6.1M</div><div class="kpi-change up">+18%% YoY</div></div>
                  <div class="kpi"><div class="kpi-label">Operating Margin</div><div class="kpi-value">30.0%%</div><div class="kpi-change up">+3.7pp YoY</div></div>
                  <div class="kpi"><div class="kpi-label">Client Retention</div><div class="kpi-value">97.2%%</div><div class="kpi-change down">-0.3%% QoQ</div></div>
                </div>
                <h2>Income Statement</h2>
                <table>
                  <thead><tr><th>Metric</th><th class="r">Q4 2026</th><th class="r">Q3 2026</th><th class="r">Q4 2025</th><th class="r">YoY</th></tr></thead>
                  <tbody>
                    <tr><td>Total Revenue</td><td class="r">$28.3M</td><td class="r">$25.1M</td><td class="r">$23.2M</td><td class="r up">+22.0%%</td></tr>
                    <tr><td>Net Income</td><td class="r">$6.1M</td><td class="r">$4.9M</td><td class="r">$5.2M</td><td class="r up">+17.3%%</td></tr>
                  </tbody>
                </table>
                <div class="callout"><strong>Outlook:</strong> Technology integration reduced operational costs by 15%%.</div>
                <div class="page-break"></div>
                <h2>Leadership Team</h2>
                <div class="team-grid">
                  <div class="team-card"><div class="team-name">Sarah Chen</div><div class="team-role">CFO</div><div class="team-bio">20+ years in investment banking. MBA from Wharton.</div></div>
                  <div class="team-card"><div class="team-name">Michael Torres</div><div class="team-role">Managing Director</div><div class="team-bio">Former McKinsey partner. CFA charterholder.</div></div>
                  <div class="team-card"><div class="team-name">Priya Patel</div><div class="team-role">Head of Research</div><div class="team-bio">PhD Economics, MIT.</div></div>
                </div>
                <div class="footer">Generated March 2026 &mdash; <a href="https://github.com/carlos7ags/folio">Built with Folio</a></div>
                <div class="confidential">Confidential &mdash; Do Not Distribute</div>
              </div>
            </body>
            </html>
            """;

    public static void main(String[] args) {
        HtmlConverter.toPdf(REPORT_HTML, "report.pdf");
        System.out.println("Created report.pdf");
    }
}
