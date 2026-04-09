package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Represents a configurable form field that can be added to a {@link Form} via
 * {@link Form#addField(FormField)}. Use the static factory methods to create a
 * specific field type, then chain fluent setters before adding it to the form.
 *
 * <pre>{@code
 * try (var field = FormField.textField("email", 72, 700, 300, 720, 0)
 *         .value("user@example.com")
 *         .required()) {
 *     form.addField(field);
 * }
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources to free the
 * underlying native handle.
 */
public final class FormField implements AutoCloseable {

    private final HandleRef handle;

    private FormField(long handle) {
        this.handle = new HandleRef(handle, FolioNative::formFieldFree);
    }

    /**
     * Creates a single-line text field.
     *
     * @param name      the unique field name
     * @param x1        left coordinate in points
     * @param y1        bottom coordinate in points
     * @param x2        right coordinate in points
     * @param y2        top coordinate in points
     * @param pageIndex zero-based page index
     * @return a new {@link FormField} representing the text field
     * @throws FolioException if the native call fails
     */
    public static FormField textField(String name, double x1, double y1, double x2, double y2, int pageIndex) {
        long h = FolioNative.formCreateTextField(name, x1, y1, x2, y2, pageIndex);
        if (h == 0) throw new FolioException("Failed to create text field: " + FolioNative.lastError());
        return new FormField(h);
    }

    /**
     * Creates a checkbox field.
     *
     * @param name      the unique field name
     * @param x1        left coordinate in points
     * @param y1        bottom coordinate in points
     * @param x2        right coordinate in points
     * @param y2        top coordinate in points
     * @param pageIndex zero-based page index
     * @param checked   initial checked state
     * @return a new {@link FormField} representing the checkbox
     * @throws FolioException if the native call fails
     */
    public static FormField checkbox(String name, double x1, double y1, double x2, double y2, int pageIndex, boolean checked) {
        long h = FolioNative.formCreateCheckbox(name, x1, y1, x2, y2, pageIndex, checked);
        if (h == 0) throw new FolioException("Failed to create checkbox: " + FolioNative.lastError());
        return new FormField(h);
    }

    /**
     * Sets the default value for this field.
     *
     * @param value the initial field value
     * @return this field, for chaining
     */
    public FormField value(String value) {
        FolioNative.formFieldSetValue(handle.get(), value);
        return this;
    }

    /**
     * Marks this field as read-only so users cannot edit it.
     *
     * @return this field, for chaining
     */
    public FormField readOnly() {
        FolioNative.formFieldSetReadOnly(handle.get());
        return this;
    }

    /**
     * Marks this field as required (must be filled before submission).
     *
     * @return this field, for chaining
     */
    public FormField required() {
        FolioNative.formFieldSetRequired(handle.get());
        return this;
    }

    /**
     * Sets the background fill color of this field widget.
     *
     * @param color the background {@link Color}
     * @return this field, for chaining
     */
    public FormField backgroundColor(Color color) {
        FolioNative.formFieldSetBackgroundColor(handle.get(), color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Sets the border color of this field widget.
     *
     * @param color the border {@link Color}
     * @return this field, for chaining
     */
    public FormField borderColor(Color color) {
        FolioNative.formFieldSetBorderColor(handle.get(), color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Returns the native handle for this form field.
     *
     * @return the native handle value
     */
    public long handle() {
        return handle.get();
    }

    /**
     * Frees the underlying native field handle. Called automatically by
     * try-with-resources.
     */
    @Override
    public void close() {
        handle.close();
    }
}
