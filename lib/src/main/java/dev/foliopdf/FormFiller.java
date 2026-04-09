package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Fills interactive form fields in an existing PDF. Create an instance from an open
 * {@link PdfReader}, then use {@link #setValue(String, String)} and
 * {@link #setCheckbox(String, boolean)} to populate fields.
 *
 * <pre>{@code
 * try (var reader = PdfReader.open("form_template.pdf");
 *      var filler = FormFiller.of(reader)) {
 *     filler.setValue("name", "Jane Doe")
 *           .setCheckbox("agree", true);
 * }
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources to free the
 * underlying native handle.
 */
public final class FormFiller implements AutoCloseable {

    private final HandleRef handle;

    private FormFiller(long handle) {
        this.handle = new HandleRef(handle, FolioNative::formFillerFree);
    }

    /**
     * Creates a {@link FormFiller} bound to the given open PDF reader.
     *
     * @param reader an open {@link PdfReader} whose form fields will be filled
     * @return a new {@link FormFiller} for the PDF
     * @throws FolioException if the native call fails
     */
    public static FormFiller of(PdfReader reader) {
        long h = FolioNative.formFillerNew(reader.readerHandle());
        if (h == 0) throw new FolioException("Failed to create form filler: " + FolioNative.lastError());
        return new FormFiller(h);
    }

    /**
     * Returns a newline-separated list of all form field names in the PDF.
     *
     * @return field names as a single string
     */
    public String fieldNames() {
        long buf = FolioNative.formFillerFieldNames(handle.get());
        return FolioNative.bufferToString(buf);
    }

    /**
     * Returns the current value of the named form field.
     *
     * @param fieldName the form field name
     * @return the current value, or an empty string if the field is empty
     */
    public String getValue(String fieldName) {
        long buf = FolioNative.formFillerGetValue(handle.get(), fieldName);
        return FolioNative.bufferToString(buf);
    }

    /**
     * Sets the value of a text form field.
     *
     * @param fieldName the form field name
     * @param value     the new value to set
     * @return this filler, for chaining
     */
    public FormFiller setValue(String fieldName, String value) {
        FolioNative.formFillerSetValue(handle.get(), fieldName, value);
        return this;
    }

    /**
     * Sets the checked state of a checkbox form field.
     *
     * @param fieldName the checkbox field name
     * @param checked   {@code true} to check, {@code false} to uncheck
     * @return this filler, for chaining
     */
    public FormFiller setCheckbox(String fieldName, boolean checked) {
        FolioNative.formFillerSetCheckbox(handle.get(), fieldName, checked);
        return this;
    }

    /**
     * Frees the underlying native filler handle. Called automatically by
     * try-with-resources.
     */
    @Override
    public void close() {
        handle.close();
    }
}
