package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Represents an interactive PDF form with fields such as text inputs, checkboxes,
 * dropdowns, and radio buttons. Attach to a document via {@link Document#form(Form)}.
 *
 * <pre>{@code
 * try (var form = Form.of()) {
 *     form.addTextField("name", 72, 700, 300, 720, 0);
 *     form.addCheckbox("agree", 72, 680, 90, 695, 0, false);
 *     doc.form(form);
 * }
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources to free the
 * underlying native handle.
 */
public final class Form implements AutoCloseable {

    private final HandleRef handle;

    private Form(long handle) {
        this.handle = new HandleRef(handle, FolioNative::formFree);
    }

    /**
     * Creates a new empty {@link Form}.
     *
     * @return a new {@link Form} instance
     * @throws FolioException if the native call fails
     */
    public static Form of() {
        long h = FolioNative.formNew();
        if (h == 0) throw new FolioException("Failed to create form: " + FolioNative.lastError());
        return new Form(h);
    }

    /**
     * Adds a single-line text field to the form.
     *
     * @param name      the unique field name
     * @param x1        left coordinate of the field rectangle in points
     * @param y1        bottom coordinate of the field rectangle in points
     * @param x2        right coordinate of the field rectangle in points
     * @param y2        top coordinate of the field rectangle in points
     * @param pageIndex zero-based page index where the field is placed
     * @return this form, for chaining
     */
    public Form addTextField(String name, double x1, double y1, double x2, double y2, int pageIndex) {
        FolioNative.formAddTextField(handle.get(), name, x1, y1, x2, y2, pageIndex);
        return this;
    }

    /**
     * Adds a checkbox field to the form.
     *
     * @param name      the unique field name
     * @param x1        left coordinate in points
     * @param y1        bottom coordinate in points
     * @param x2        right coordinate in points
     * @param y2        top coordinate in points
     * @param pageIndex zero-based page index
     * @param checked   initial checked state
     * @return this form, for chaining
     */
    public Form addCheckbox(String name, double x1, double y1, double x2, double y2, int pageIndex, boolean checked) {
        FolioNative.formAddCheckbox(handle.get(), name, x1, y1, x2, y2, pageIndex, checked);
        return this;
    }

    /**
     * Adds a dropdown (combo box) field with the given options to the form.
     *
     * @param name      the unique field name
     * @param x1        left coordinate in points
     * @param y1        bottom coordinate in points
     * @param x2        right coordinate in points
     * @param y2        top coordinate in points
     * @param pageIndex zero-based page index
     * @param options   the selectable option strings
     * @return this form, for chaining
     */
    public Form addDropdown(String name, double x1, double y1, double x2, double y2, int pageIndex, String... options) {
        FolioNative.formAddDropdown(handle.get(), name, x1, y1, x2, y2, pageIndex, options);
        return this;
    }

    /**
     * Adds a digital signature field to the form.
     *
     * @param name      the unique field name
     * @param x1        left coordinate in points
     * @param y1        bottom coordinate in points
     * @param x2        right coordinate in points
     * @param y2        top coordinate in points
     * @param pageIndex zero-based page index
     * @return this form, for chaining
     */
    public Form addSignature(String name, double x1, double y1, double x2, double y2, int pageIndex) {
        FolioNative.formAddSignature(handle.get(), name, x1, y1, x2, y2, pageIndex);
        return this;
    }

    /**
     * Adds a multi-line text area field to the form.
     *
     * @param name      the unique field name
     * @param x1        left coordinate in points
     * @param y1        bottom coordinate in points
     * @param x2        right coordinate in points
     * @param y2        top coordinate in points
     * @param pageIndex zero-based page index
     * @return this form, for chaining
     */
    public Form addMultilineTextField(String name, double x1, double y1, double x2, double y2, int pageIndex) {
        FolioNative.formAddMultilineTextField(handle.get(), name, x1, y1, x2, y2, pageIndex);
        return this;
    }

    /**
     * Adds a password input field (masked text) to the form.
     *
     * @param name      the unique field name
     * @param x1        left coordinate in points
     * @param y1        bottom coordinate in points
     * @param x2        right coordinate in points
     * @param y2        top coordinate in points
     * @param pageIndex zero-based page index
     * @return this form, for chaining
     */
    public Form addPasswordField(String name, double x1, double y1, double x2, double y2, int pageIndex) {
        FolioNative.formAddPasswordField(handle.get(), name, x1, y1, x2, y2, pageIndex);
        return this;
    }

    /**
     * Adds a scrollable list box field with the given options to the form.
     *
     * @param name      the unique field name
     * @param x1        left coordinate in points
     * @param y1        bottom coordinate in points
     * @param x2        right coordinate in points
     * @param y2        top coordinate in points
     * @param pageIndex zero-based page index
     * @param options   the selectable option strings
     * @return this form, for chaining
     */
    public Form addListbox(String name, double x1, double y1, double x2, double y2, int pageIndex, String... options) {
        FolioNative.formAddListbox(handle.get(), name, x1, y1, x2, y2, pageIndex, options);
        return this;
    }

    /**
     * Adds a radio button group to the form. Each radio button is defined by a
     * value, a bounding rectangle (four doubles in {@code rects}), and a page index.
     *
     * @param name        the shared group field name
     * @param values      the value associated with each radio button
     * @param rects       flat array of {@code [x1, y1, x2, y2]} per button
     * @param pageIndices zero-based page index for each button
     * @return this form, for chaining
     */
    public Form addRadioGroup(String name, String[] values, double[] rects, int[] pageIndices) {
        FolioNative.formAddRadioGroup(handle.get(), name, values, rects, pageIndices);
        return this;
    }

    /**
     * Adds a pre-configured {@link FormField} to this form.
     *
     * @param field the field to add
     * @return this form, for chaining
     */
    public Form addField(FormField field) {
        FolioNative.formAddField(handle.get(), field.handle());
        return this;
    }

    /**
     * Returns the native handle for this form.
     *
     * @return the native handle value
     */
    public long handle() {
        return handle.get();
    }

    /**
     * Frees the underlying native form handle. Called automatically by
     * try-with-resources.
     */
    @Override
    public void close() {
        handle.close();
    }
}
