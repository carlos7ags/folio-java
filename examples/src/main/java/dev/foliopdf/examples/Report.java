// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

/**
 * Multi-page business report built with the layout API — no HTML.
 *
 * <p>Demonstrates headings with auto-bookmarks, styled tables with headers
 * and right-aligned columns, lists, callout divs with borders and
 * background, line separators, page breaks, and text headers/footers with
 * {page} / {pages} placeholders.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Report
 * </pre>
 */
public final class Report {

    private static final Color NAVY = Color.of(0.06, 0.09, 0.16);
    private static final Color TEAL = Color.of(0.05, 0.61, 0.53);
    private static final Color GRAY = Color.of(0.39, 0.43, 0.47);
    private static final Color LIGHT_BLUE_BG = Color.of(0.97, 0.98, 1.0);
    private static final Color BORDER_GRAY = Color.of(0.85, 0.85, 0.85);

    public static void main(String[] args) {
        try (var doc = Document.builder()
                .letter()
                .margins(72)
                .title("Annual Report 2026")
                .author("Apex Capital Partners")
                .build()) {

            doc.autoBookmarks(true);

            Font helv = Font.helvetica();
            Font helvBold = Font.helveticaBold();

            // Header/footer text with automatic space reservation.
            // {page} and {pages} are placeholder tokens.
            doc.setHeaderText(
                    "Apex Capital Partners  —  Annual Report 2026",
                    helv, 9, Align.LEFT);
            doc.setFooterText(
                    "Confidential          Page {page} of {pages}",
                    helv, 8, Align.CENTER);

            // ===== PAGE 1: Cover =====
            doc.add(Paragraph.of(" ", helv, 1).spaceBefore(160));
            doc.add(Paragraph.of("Annual Report", helvBold, 36).align(Align.CENTER));
            doc.add(Paragraph.of("2026", helvBold, 48).align(Align.CENTER));
            doc.add(LineSeparator.of());
            doc.add(Paragraph.of("Apex Capital Partners", helv, 14).align(Align.CENTER));

            // ===== PAGE 2: Executive Summary =====
            doc.add(AreaBreak.of());
            doc.add(Heading.of("Executive Summary", HeadingLevel.H1));
            doc.add(body(
                    "Fiscal year 2026 marked a transformational period for Apex Capital " +
                    "Partners. Revenue grew 22% year-over-year to $28.3M, driven by strong " +
                    "performance in advisory services and a strategic expansion into the " +
                    "Asia-Pacific region.", helv));
            doc.add(body(
                    "Operating margin improved to 30%, reflecting disciplined cost " +
                    "management and technology-driven efficiency gains. Client retention " +
                    "remained strong at 97.2%, and we welcomed 23 new institutional clients " +
                    "during the year.", helv));

            // Callout box with left border accent.
            Div callout = Div.of()
                    .padding(10, 14, 10, 14)
                    .background(LIGHT_BLUE_BG)
                    .border(1, TEAL)
                    .spaceBefore(8)
                    .spaceAfter(12);
            callout.add(Paragraph.of(
                    "Highlight: Named \"Top Advisory Firm\" by the Financial Times " +
                    "for the third consecutive year.",
                    helv, 10));
            doc.add(callout);

            // Financial Highlights table.
            doc.add(Heading.of("Financial Highlights", HeadingLevel.H2));
            doc.add(financialTable(helv, helvBold));

            // ===== PAGE 3: Segments + Strategy =====
            doc.add(AreaBreak.of());
            doc.add(Heading.of("Revenue by Segment", HeadingLevel.H2));
            doc.add(body("Revenue is diversified across four business segments.", helv));
            doc.add(revenueTable(helv, helvBold));

            doc.add(Heading.of("Strategic Initiatives", HeadingLevel.H2));
            doc.add(body("Key accomplishments during 2026:", helv));

            ListElement list = ListElement.of(helv, 10)
                    .style(ListStyle.BULLET)
                    .leading(1.5)
                    .item("Launched digital asset custody platform ($2.1B AUM in first month)")
                    .item("Opened Singapore office with 12 analysts for Asia-Pacific expansion")
                    .item("Signed cross-border payment partnership with Deutsche Bank")
                    .item("Published inaugural ESG Impact Report")
                    .item("Achieved 97% client satisfaction in annual survey");
            doc.add(list);

            // ===== PAGE 4: Leadership Team =====
            doc.add(AreaBreak.of());
            doc.add(Heading.of("Leadership Team", HeadingLevel.H1));

            String[][] team = {
                    {"Sarah Chen", "Chief Financial Officer",
                     "20+ years in investment banking. Previously VP at Goldman Sachs. MBA from Wharton."},
                    {"Michael Torres", "Managing Director",
                     "Leads client advisory and M&A. Former McKinsey partner. CFA charterholder."},
                    {"Priya Patel", "Head of Research",
                     "PhD Economics, MIT. Published author on emerging market strategy."},
                    {"James Wu", "Chief Technology Officer",
                     "Led digital transformation at two Fortune 500 firms. MS Computer Science, Stanford."},
            };

            for (String[] t : team) {
                Div card = Div.of()
                        .padding(8, 12, 8, 12)
                        .border(0.5, BORDER_GRAY)
                        .spaceAfter(4);
                card.add(Paragraph.of(t[0] + "  —  " + t[1], helvBold, 11));
                card.add(Paragraph.of(t[2], helv, 9).leading(1.4).spaceBefore(2));
                doc.add(card);
            }

            doc.save("report.pdf");
            System.out.println("Created report.pdf");
        }
    }

    private static Paragraph body(String text, Font helv) {
        return Paragraph.of(text, helv, 10).leading(1.5).spaceAfter(6);
    }

    private static Table financialTable(Font helv, Font helvBold) {
        return Table.builder()
                .columnWidths(0.40, 0.20, 0.20, 0.20)
                .borderCollapse(true)
                .font(helv)
                .fontSize(9)
                .headerRow(row -> {
                    row.addCell("Metric", helvBold, 9).paddingSides(6, 8, 6, 8).border(2, NAVY);
                    row.addCell("2026", helvBold, 9).align(Align.RIGHT).paddingSides(6, 8, 6, 8).border(2, NAVY);
                    row.addCell("2025", helvBold, 9).align(Align.RIGHT).paddingSides(6, 8, 6, 8).border(2, NAVY);
                    row.addCell("Change", helvBold, 9).align(Align.RIGHT).paddingSides(6, 8, 6, 8).border(2, NAVY);
                })
                .row("Revenue", "$28.3M", "$23.2M", "+22.0%")
                .row("Gross Profit", "$17.0M", "$13.3M", "+27.8%")
                .row("Operating Expenses", "$8.5M", "$7.2M", "+18.1%")
                .row("Net Income", "$6.1M", "$5.2M", "+17.3%")
                .row("Operating Margin", "30.0%", "26.3%", "+3.7pp")
                .build();
    }

    private static Table revenueTable(Font helv, Font helvBold) {
        return Table.builder()
                .columnWidths(0.40, 0.30, 0.30)
                .borderCollapse(true)
                .font(helv)
                .fontSize(9)
                .headerRow("Segment", "Revenue", "% of Total")
                .row("Advisory Services", "$14.2M", "50%")
                .row("Asset Management", "$8.5M", "30%")
                .row("Research & Analytics", "$4.2M", "15%")
                .row("Other", "$1.4M", "5%")
                .build();
    }
}
