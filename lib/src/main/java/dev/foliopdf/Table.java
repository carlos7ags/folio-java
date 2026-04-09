package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a PDF table. Build instances using {@link #builder()}.
 *
 * <pre>{@code
 * doc.add(Table.builder()
 *         .headerRow("Product", "Units", "Revenue")
 *         .row("Widget A", "1,200", "$48,000")
 *         .row("Widget B", "850", "$34,000")
 *         .build());
 * }</pre>
 *
 * <p>For styled rows, supply a {@link java.util.function.Consumer Consumer&lt;Row&gt;}
 * to {@link Builder#row(java.util.function.Consumer)} or {@link Builder#headerRow(java.util.function.Consumer)}.
 */
public final class Table implements Element {

    private final HandleRef handle;

    private Table(long handle) {
        this.handle = new HandleRef(handle, FolioNative::tableFree);
    }

    /**
     * Creates a simple table from string arrays. The first array is the header row,
     * the rest are data rows. Uses Helvetica 10pt.
     *
     * <pre>{@code
     * doc.add(Table.of(
     *     new String[]{"Name", "Price"},
     *     new String[]{"Widget A", "$10"},
     *     new String[]{"Widget B", "$20"}
     * ));
     * }</pre>
     *
     * @param rows the first array is used as the header row; subsequent arrays are data rows
     * @return a new {@link Table} instance
     * @throws FolioException if no rows are provided
     */
    public static Table of(String[]... rows) {
        if (rows.length == 0) throw new FolioException("Table requires at least one row");
        var b = builder().columns(rows[0].length);
        b.headerRow(rows[0]);
        for (int i = 1; i < rows.length; i++) {
            b.row(rows[i]);
        }
        return b.build();
    }

    /**
     * Returns a new builder for constructing a {@link Table}.
     *
     * @return a fresh {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the native handle for this table.
     *
     * @return the native handle value
     */
    public long handle() {
        return handle.get();
    }

    /**
     * Builder for creating a {@link Table}. Obtain an instance from {@link Table#builder()}.
     */
    public static final class Builder {
        private final List<String[]> rows = new ArrayList<>();
        private final List<Consumer<Row>> styledRows = new ArrayList<>();
        private String[] headerRow;
        private Consumer<Row> styledHeaderRow;
        private double[] columnWidths;
        private boolean borderCollapseEnabled = false;
        private boolean autoColumnWidthsEnabled = false;
        private double cellSpacingH = -1;
        private double cellSpacingV = -1;
        private double minWidth = -1;
        private String[] footerRow;
        private Consumer<Row> styledFooterRow;
        private Font font;
        private double fontSize = 10;

        Builder() {}

        /**
         * Hints at the number of columns; the actual column count is determined by the
         * cells added. Present for API symmetry with other layout builders.
         *
         * @param count the expected number of columns
         * @return this builder, for chaining
         */
        public Builder columns(int count) {
            return this;
        }

        /**
         * Sets explicit column widths in points.
         *
         * @param widths one width value per column
         * @return this builder, for chaining
         */
        public Builder columnWidths(double... widths) {
            this.columnWidths = widths;
            return this;
        }

        /**
         * Enables or disables border-collapse mode (adjacent borders share a single line).
         *
         * @param enabled {@code true} to collapse borders
         * @return this builder, for chaining
         */
        public Builder borderCollapse(boolean enabled) {
            // stored and applied at build time
            this.borderCollapseEnabled = enabled;
            return this;
        }

        /**
         * Sets the horizontal and vertical spacing between cells.
         *
         * @param h horizontal cell spacing in points
         * @param v vertical cell spacing in points
         * @return this builder, for chaining
         */
        public Builder cellSpacing(double h, double v) {
            this.cellSpacingH = h;
            this.cellSpacingV = v;
            return this;
        }

        /**
         * Enables automatic column width calculation based on cell content.
         *
         * @return this builder, for chaining
         */
        public Builder autoColumnWidths() {
            this.autoColumnWidthsEnabled = true;
            return this;
        }

        /**
         * Sets the minimum total table width in points.
         *
         * @param pts minimum width in points
         * @return this builder, for chaining
         */
        public Builder minWidth(double pts) {
            this.minWidth = pts;
            return this;
        }

        /**
         * Adds a plain-text footer row with one string per cell.
         *
         * @param cells the cell text values
         * @return this builder, for chaining
         */
        public Builder footerRow(String... cells) {
            this.footerRow = cells;
            return this;
        }

        /**
         * Adds a styled footer row using a {@link Row} builder callback.
         *
         * @param rowBuilder callback that populates and styles the footer row
         * @return this builder, for chaining
         */
        public Builder footerRow(Consumer<Row> rowBuilder) {
            this.styledFooterRow = rowBuilder;
            return this;
        }

        /**
         * Sets the default font used for all cells in this table.
         *
         * @param font the font to use
         * @return this builder, for chaining
         */
        public Builder font(Font font) {
            this.font = font;
            return this;
        }

        /**
         * Sets the default font size in points used for all cells in this table.
         *
         * @param size font size in points
         * @return this builder, for chaining
         */
        public Builder fontSize(double size) {
            this.fontSize = size;
            return this;
        }

        /**
         * Adds a plain-text header row with one string per cell.
         *
         * @param cells the cell text values
         * @return this builder, for chaining
         */
        public Builder headerRow(String... cells) {
            this.headerRow = cells;
            return this;
        }

        /**
         * Adds a styled header row using a {@link Row} builder callback.
         *
         * @param rowBuilder callback that populates and styles the header row
         * @return this builder, for chaining
         */
        public Builder headerRow(Consumer<Row> rowBuilder) {
            this.styledHeaderRow = rowBuilder;
            return this;
        }

        /**
         * Adds a plain-text body row with one string per cell.
         *
         * @param cells the cell text values
         * @return this builder, for chaining
         */
        public Builder row(String... cells) {
            rows.add(cells);
            return this;
        }

        /**
         * Adds a styled body row using a {@link Row} builder callback.
         *
         * @param rowBuilder callback that populates and styles the row
         * @return this builder, for chaining
         */
        public Builder row(Consumer<Row> rowBuilder) {
            rows.add(null);
            styledRows.add(rowBuilder);
            return this;
        }

        /**
         * Builds and returns the configured {@link Table}.
         *
         * @return a new {@link Table} instance
         * @throws FolioException if any native call fails during construction
         */
        public Table build() {
            long tableHandle = FolioNative.tableNew();
            if (tableHandle == 0) throw new FolioException("Failed to create table: " + FolioNative.lastError());

            if (columnWidths != null) {
                FolioNative.tableSetColumnWidths(tableHandle, columnWidths);
            }
            if (borderCollapseEnabled) {
                FolioNative.tableSetBorderCollapse(tableHandle, true);
            }
            if (autoColumnWidthsEnabled) {
                FolioNative.tableSetAutoColumnWidths(tableHandle);
            }
            if (cellSpacingH >= 0) {
                FolioNative.tableSetCellSpacing(tableHandle, cellSpacingH, cellSpacingV);
            }
            if (minWidth >= 0) {
                FolioNative.tableSetMinWidth(tableHandle, minWidth);
            }

            Font f = font != null ? font : Font.helvetica();
            long fontHandle = f.handle();

            if (styledHeaderRow != null) {
                long rowHandle = FolioNative.tableAddHeaderRow(tableHandle);
                if (rowHandle == 0) throw new FolioException("Failed to add header row: " + FolioNative.lastError());
                styledHeaderRow.accept(new Row(rowHandle, f, fontSize));
            } else if (headerRow != null) {
                long rowHandle = FolioNative.tableAddHeaderRow(tableHandle);
                if (rowHandle == 0) throw new FolioException("Failed to add header row: " + FolioNative.lastError());
                addCells(rowHandle, headerRow, fontHandle);
            }

            int styledIdx = 0;
            for (String[] row : rows) {
                long rowHandle = FolioNative.tableAddRow(tableHandle);
                if (rowHandle == 0) throw new FolioException("Failed to add table row: " + FolioNative.lastError());
                if (row == null) {
                    styledRows.get(styledIdx++).accept(new Row(rowHandle, f, fontSize));
                } else {
                    addCells(rowHandle, row, fontHandle);
                }
            }

            if (styledFooterRow != null) {
                long rowHandle = FolioNative.tableAddFooterRow(tableHandle);
                if (rowHandle == 0) throw new FolioException("Failed to add footer row: " + FolioNative.lastError());
                styledFooterRow.accept(new Row(rowHandle, f, fontSize));
            } else if (footerRow != null) {
                long rowHandle = FolioNative.tableAddFooterRow(tableHandle);
                if (rowHandle == 0) throw new FolioException("Failed to add footer row: " + FolioNative.lastError());
                addCells(rowHandle, footerRow, fontHandle);
            }

            return new Table(tableHandle);
        }

        private void addCells(long rowHandle, String[] cells, long fontHandle) {
            for (String cell : cells) {
                long cellHandle = FolioNative.rowAddCell(rowHandle, cell, fontHandle, fontSize);
                if (cellHandle == 0) throw new FolioException("Failed to add table cell: " + FolioNative.lastError());
            }
        }
    }
}
