package dev.foliopdf.internal;

import dev.foliopdf.FolioException;
import dev.foliopdf.FolioIOException;
import dev.foliopdf.Page;
import dev.foliopdf.PageDecorator;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Low-level Panama FFI bindings to the Folio native library.
 *
 * <p>All methods in this class are thread-safe. The underlying C library is
 * <b>not</b> thread-safe, so every call into native code is serialized through
 * a global lock. Callers do not need to perform external synchronization.
 *
 * <p>This class is internal — use the public API classes in {@code dev.foliopdf}
 * instead.
 */
public final class FolioNative {

    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LIB;
    private static final ReentrantLock NATIVE_LOCK = new ReentrantLock();

    static {
        NativeLoader.load();
        LIB = SymbolLookup.loaderLookup();
    }

    private FolioNative() {}

    /**
     * Acquires the native lock, executes the given operation, and releases it.
     * All public methods must route through this to ensure thread safety.
     */
    static <T> T locked(NativeCall<T> call) {
        NATIVE_LOCK.lock();
        try {
            return call.invoke();
        } finally {
            NATIVE_LOCK.unlock();
        }
    }

    static void lockedVoid(NativeCallVoid call) {
        NATIVE_LOCK.lock();
        try {
            call.invoke();
        } finally {
            NATIVE_LOCK.unlock();
        }
    }

    @FunctionalInterface
    interface NativeCall<T> { T invoke(); }

    @FunctionalInterface
    interface NativeCallVoid { void invoke(); }

    private static MethodHandle downcall(String name, FunctionDescriptor desc) {
        return LINKER.downcallHandle(
            LIB.find(name).orElseThrow(() ->
                new FolioException("Symbol not found: " + name)),
            desc
        );
    }

    // ── Error handling ──────────────────────────────────────────────

    private static final MethodHandle last_error = downcall("folio_last_error",
        FunctionDescriptor.of(ValueLayout.ADDRESS));

    public static String lastError() {
        return locked(() -> {
            try {
                MemorySegment ptr = (MemorySegment) last_error.invokeExact();
                if (ptr.equals(MemorySegment.NULL)) return "Unknown error";
                return ptr.reinterpret(4096).getString(0);
            } catch (Throwable t) {
                throw new FolioException("lastError failed", t);
            }
        });
    }

    private static final MethodHandle version = downcall("folio_version",
        FunctionDescriptor.of(ValueLayout.ADDRESS));

    public static String version() {
        return locked(() -> {
            try {
                MemorySegment ptr = (MemorySegment) version.invokeExact();
                if (ptr.equals(MemorySegment.NULL)) return "unknown";
                return ptr.reinterpret(256).getString(0);
            } catch (Throwable t) {
                throw new FolioException("version failed", t);
            }
        });
    }

    static void checkResult(int result, String operation) {
        if (result != 0) {
            String msg = operation + ": " + lastError();
            if (result == -3) {
                throw new FolioIOException(msg);
            }
            throw new FolioException(result, msg);
        }
    }

    // ── Document ────────────────────────────────────────────────────

    private static final MethodHandle document_new = downcall("folio_document_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static long documentNew(double width, double height) {
        return locked(() -> {
            try {
                return (long) document_new.invokeExact(width, height);
            } catch (Throwable t) {
                throw new FolioException("documentNew failed", t);
            }
        });
    }

    private static final MethodHandle document_new_letter = downcall("folio_document_new_letter",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long documentNewLetter() {
        return locked(() -> {
            try {
                return (long) document_new_letter.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("documentNewLetter failed", t);
            }
        });
    }

    private static final MethodHandle document_new_a4 = downcall("folio_document_new_a4",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long documentNewA4() {
        return locked(() -> {
            try {
                return (long) document_new_a4.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("documentNewA4 failed", t);
            }
        });
    }

    private static final MethodHandle document_set_title = downcall("folio_document_set_title",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    public static void documentSetTitle(long doc, String title) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) document_set_title.invokeExact(doc, arena.allocateFrom(title));
                checkResult(rc, "documentSetTitle");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentSetTitle failed", t);
            }
        });
    }

    private static final MethodHandle document_set_author = downcall("folio_document_set_author",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    public static void documentSetAuthor(long doc, String author) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) document_set_author.invokeExact(doc, arena.allocateFrom(author));
                checkResult(rc, "documentSetAuthor");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentSetAuthor failed", t);
            }
        });
    }

    private static final MethodHandle document_set_margins = downcall("folio_document_set_margins",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static void documentSetMargins(long doc, double top, double right, double bottom, double left) {
        lockedVoid(() -> {
            try {
                int rc = (int) document_set_margins.invokeExact(doc, top, right, bottom, left);
                checkResult(rc, "documentSetMargins");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentSetMargins failed", t);
            }
        });
    }

    private static final MethodHandle document_add = downcall("folio_document_add",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static void documentAdd(long doc, long element) {
        lockedVoid(() -> {
            try {
                int rc = (int) document_add.invokeExact(doc, element);
                checkResult(rc, "documentAdd");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentAdd failed", t);
            }
        });
    }

    private static final MethodHandle document_save = downcall("folio_document_save",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    public static void documentSave(long doc, String path) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) document_save.invokeExact(doc, arena.allocateFrom(path));
                checkResult(rc, "documentSave");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentSave failed", t);
            }
        });
    }

    private static final MethodHandle document_write_to_buffer = downcall("folio_document_write_to_buffer",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static long documentWriteToBuffer(long doc) {
        return locked(() -> {
            try {
                return (long) document_write_to_buffer.invokeExact(doc);
            } catch (Throwable t) {
                throw new FolioException("documentWriteToBuffer failed", t);
            }
        });
    }

    private static final MethodHandle document_free = downcall("folio_document_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void documentFree(long doc) {
        lockedVoid(() -> {
            try {
                document_free.invokeExact(doc);
            } catch (Throwable t) {
                throw new FolioException("documentFree failed", t);
            }
        });
    }

    private static final MethodHandle document_page_count = downcall("folio_document_page_count",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));

    public static int documentPageCount(long doc) {
        return locked(() -> {
            try {
                return (int) document_page_count.invokeExact(doc);
            } catch (Throwable t) {
                throw new FolioException("documentPageCount failed", t);
            }
        });
    }

    private static final MethodHandle document_add_page = downcall("folio_document_add_page",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static long documentAddPage(long doc) {
        return locked(() -> {
            try {
                return (long) document_add_page.invokeExact(doc);
            } catch (Throwable t) {
                throw new FolioException("documentAddPage failed", t);
            }
        });
    }

    private static final MethodHandle document_set_tagged = downcall("folio_document_set_tagged",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static void documentSetTagged(long doc, boolean enabled) {
        lockedVoid(() -> {
            try {
                int rc = (int) document_set_tagged.invokeExact(doc, enabled ? 1 : 0);
                checkResult(rc, "documentSetTagged");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentSetTagged failed", t);
            }
        });
    }

    private static final MethodHandle document_set_pdfa = downcall("folio_document_set_pdfa",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static void documentSetPdfA(long doc, int level) {
        lockedVoid(() -> {
            try {
                int rc = (int) document_set_pdfa.invokeExact(doc, level);
                checkResult(rc, "documentSetPdfA");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentSetPdfA failed", t);
            }
        });
    }

    private static final MethodHandle document_set_encryption = downcall("folio_document_set_encryption",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    public static void documentSetEncryption(long doc, String userPw, String ownerPw, int algorithm) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) document_set_encryption.invokeExact(doc,
                    arena.allocateFrom(userPw), arena.allocateFrom(ownerPw), algorithm);
                checkResult(rc, "documentSetEncryption");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentSetEncryption failed", t);
            }
        });
    }

    private static final MethodHandle document_set_auto_bookmarks = downcall("folio_document_set_auto_bookmarks",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static void documentSetAutoBookmarks(long doc, boolean enabled) {
        lockedVoid(() -> {
            try {
                int rc = (int) document_set_auto_bookmarks.invokeExact(doc, enabled ? 1 : 0);
                checkResult(rc, "documentSetAutoBookmarks");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentSetAutoBookmarks failed", t);
            }
        });
    }

    private static final MethodHandle document_set_form = downcall("folio_document_set_form",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static void documentSetForm(long doc, long form) {
        lockedVoid(() -> {
            try {
                int rc = (int) document_set_form.invokeExact(doc, form);
                checkResult(rc, "documentSetForm");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentSetForm failed", t);
            }
        });
    }

    // ── Page ────────────────────────────────────────────────────────

    private static final MethodHandle page_add_text = downcall("folio_page_add_text",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static void pageAddText(long page, String text, long font, double size, double x, double y) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) page_add_text.invokeExact(page, arena.allocateFrom(text), font, size, x, y);
                checkResult(rc, "pageAddText");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("pageAddText failed", t);
            }
        });
    }

    private static final MethodHandle page_add_text_embedded = downcall("folio_page_add_text_embedded",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static void pageAddTextEmbedded(long page, String text, long font, double size, double x, double y) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) page_add_text_embedded.invokeExact(page, arena.allocateFrom(text), font, size, x, y);
                checkResult(rc, "pageAddTextEmbedded");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("pageAddTextEmbedded failed", t);
            }
        });
    }

    private static final MethodHandle page_add_image = downcall("folio_page_add_image",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static void pageAddImage(long page, long img, double x, double y, double w, double h) {
        lockedVoid(() -> {
            try {
                int rc = (int) page_add_image.invokeExact(page, img, x, y, w, h);
                checkResult(rc, "pageAddImage");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("pageAddImage failed", t);
            }
        });
    }

    private static final MethodHandle page_add_link = downcall("folio_page_add_link",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS));

    public static void pageAddLink(long page, double x1, double y1, double x2, double y2, String uri) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) page_add_link.invokeExact(page, x1, y1, x2, y2, arena.allocateFrom(uri));
                checkResult(rc, "pageAddLink");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("pageAddLink failed", t);
            }
        });
    }

    private static final MethodHandle page_set_opacity = downcall("folio_page_set_opacity",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static void pageSetOpacity(long page, double alpha) {
        lockedVoid(() -> {
            try {
                int rc = (int) page_set_opacity.invokeExact(page, alpha);
                checkResult(rc, "pageSetOpacity");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("pageSetOpacity failed", t);
            }
        });
    }

    private static final MethodHandle page_set_rotate = downcall("folio_page_set_rotate",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static void pageSetRotate(long page, int degrees) {
        lockedVoid(() -> {
            try {
                int rc = (int) page_set_rotate.invokeExact(page, degrees);
                checkResult(rc, "pageSetRotate");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("pageSetRotate failed", t);
            }
        });
    }

    // ── Paragraph ───────────────────────────────────────────────────

    private static final MethodHandle paragraph_new = downcall("folio_paragraph_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static long paragraphNew(String text, long font, double fontSize) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) paragraph_new.invokeExact(arena.allocateFrom(text), font, fontSize);
            } catch (Throwable t) {
                throw new FolioException("paragraphNew failed", t);
            }
        });
    }

    private static final MethodHandle paragraph_new_embedded = downcall("folio_paragraph_new_embedded",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static long paragraphNewEmbedded(String text, long font, double fontSize) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) paragraph_new_embedded.invokeExact(arena.allocateFrom(text), font, fontSize);
            } catch (Throwable t) {
                throw new FolioException("paragraphNewEmbedded failed", t);
            }
        });
    }

    private static final MethodHandle paragraph_set_align = downcall("folio_paragraph_set_align",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static void paragraphSetAlign(long para, int align) {
        lockedVoid(() -> {
            try {
                int rc = (int) paragraph_set_align.invokeExact(para, align);
                checkResult(rc, "paragraphSetAlign");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("paragraphSetAlign failed", t);
            }
        });
    }

    private static final MethodHandle paragraph_set_leading = downcall("folio_paragraph_set_leading",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static void paragraphSetLeading(long para, double leading) {
        lockedVoid(() -> {
            try {
                int rc = (int) paragraph_set_leading.invokeExact(para, leading);
                checkResult(rc, "paragraphSetLeading");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("paragraphSetLeading failed", t);
            }
        });
    }

    private static final MethodHandle paragraph_set_space_before = downcall("folio_paragraph_set_space_before",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static void paragraphSetSpaceBefore(long para, double pts) {
        lockedVoid(() -> {
            try {
                int rc = (int) paragraph_set_space_before.invokeExact(para, pts);
                checkResult(rc, "paragraphSetSpaceBefore");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("paragraphSetSpaceBefore failed", t);
            }
        });
    }

    private static final MethodHandle paragraph_set_space_after = downcall("folio_paragraph_set_space_after",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static void paragraphSetSpaceAfter(long para, double pts) {
        lockedVoid(() -> {
            try {
                int rc = (int) paragraph_set_space_after.invokeExact(para, pts);
                checkResult(rc, "paragraphSetSpaceAfter");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("paragraphSetSpaceAfter failed", t);
            }
        });
    }

    private static final MethodHandle paragraph_set_background = downcall("folio_paragraph_set_background",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static void paragraphSetBackground(long para, double r, double g, double b) {
        lockedVoid(() -> {
            try {
                int rc = (int) paragraph_set_background.invokeExact(para, r, g, b);
                checkResult(rc, "paragraphSetBackground");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("paragraphSetBackground failed", t);
            }
        });
    }

    private static final MethodHandle paragraph_set_first_indent = downcall("folio_paragraph_set_first_indent",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static void paragraphSetFirstIndent(long para, double pts) {
        lockedVoid(() -> {
            try {
                int rc = (int) paragraph_set_first_indent.invokeExact(para, pts);
                checkResult(rc, "paragraphSetFirstIndent");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("paragraphSetFirstIndent failed", t);
            }
        });
    }

    private static final MethodHandle paragraph_add_run = downcall("folio_paragraph_add_run",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static void paragraphAddRun(long para, String text, long font, double fontSize, double r, double g, double b) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) paragraph_add_run.invokeExact(para, arena.allocateFrom(text), font, fontSize, r, g, b);
                checkResult(rc, "paragraphAddRun");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("paragraphAddRun failed", t);
            }
        });
    }

    private static final MethodHandle paragraph_free = downcall("folio_paragraph_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void paragraphFree(long para) {
        lockedVoid(() -> {
            try {
                paragraph_free.invokeExact(para);
            } catch (Throwable t) {
                throw new FolioException("paragraphFree failed", t);
            }
        });
    }

    // ── Heading ─────────────────────────────────────────────────────

    private static final MethodHandle heading_new = downcall("folio_heading_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    public static long headingNew(String text, int level) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) heading_new.invokeExact(arena.allocateFrom(text), level);
            } catch (Throwable t) {
                throw new FolioException("headingNew failed", t);
            }
        });
    }

    private static final MethodHandle heading_new_with_font = downcall("folio_heading_new_with_font",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static long headingNewWithFont(String text, int level, long font, double fontSize) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) heading_new_with_font.invokeExact(arena.allocateFrom(text), level, font, fontSize);
            } catch (Throwable t) {
                throw new FolioException("headingNewWithFont failed", t);
            }
        });
    }

    private static final MethodHandle heading_new_embedded = downcall("folio_heading_new_embedded",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));

    public static long headingNewEmbedded(String text, int level, long font) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) heading_new_embedded.invokeExact(arena.allocateFrom(text), level, font);
            } catch (Throwable t) {
                throw new FolioException("headingNewEmbedded failed", t);
            }
        });
    }

    private static final MethodHandle heading_set_align = downcall("folio_heading_set_align",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static void headingSetAlign(long heading, int align) {
        lockedVoid(() -> {
            try {
                int rc = (int) heading_set_align.invokeExact(heading, align);
                checkResult(rc, "headingSetAlign");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("headingSetAlign failed", t);
            }
        });
    }

    private static final MethodHandle heading_free = downcall("folio_heading_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void headingFree(long heading) {
        lockedVoid(() -> {
            try {
                heading_free.invokeExact(heading);
            } catch (Throwable t) {
                throw new FolioException("headingFree failed", t);
            }
        });
    }

    // ── Table ───────────────────────────────────────────────────────

    private static final MethodHandle table_new = downcall("folio_table_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long tableNew() {
        return locked(() -> {
            try {
                return (long) table_new.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("tableNew failed", t);
            }
        });
    }

    private static final MethodHandle table_set_column_widths = downcall("folio_table_set_column_widths",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    public static void tableSetColumnWidths(long table, double[] widths) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                MemorySegment seg = arena.allocate(ValueLayout.JAVA_DOUBLE, widths.length);
                for (int i = 0; i < widths.length; i++) {
                    seg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, widths[i]);
                }
                int rc = (int) table_set_column_widths.invokeExact(table, seg, widths.length);
                checkResult(rc, "tableSetColumnWidths");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("tableSetColumnWidths failed", t);
            }
        });
    }

    private static final MethodHandle table_set_border_collapse = downcall("folio_table_set_border_collapse",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static void tableSetBorderCollapse(long table, boolean enabled) {
        lockedVoid(() -> {
            try {
                int rc = (int) table_set_border_collapse.invokeExact(table, enabled ? 1 : 0);
                checkResult(rc, "tableSetBorderCollapse");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("tableSetBorderCollapse failed", t);
            }
        });
    }

    private static final MethodHandle table_add_row = downcall("folio_table_add_row",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static long tableAddRow(long table) {
        return locked(() -> {
            try {
                return (long) table_add_row.invokeExact(table);
            } catch (Throwable t) {
                throw new FolioException("tableAddRow failed", t);
            }
        });
    }

    private static final MethodHandle table_add_header_row = downcall("folio_table_add_header_row",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static long tableAddHeaderRow(long table) {
        return locked(() -> {
            try {
                return (long) table_add_header_row.invokeExact(table);
            } catch (Throwable t) {
                throw new FolioException("tableAddHeaderRow failed", t);
            }
        });
    }

    private static final MethodHandle row_add_cell = downcall("folio_row_add_cell",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static long rowAddCell(long row, String text, long font, double fontSize) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) row_add_cell.invokeExact(row, arena.allocateFrom(text), font, fontSize);
            } catch (Throwable t) {
                throw new FolioException("rowAddCell failed", t);
            }
        });
    }

    private static final MethodHandle row_add_cell_embedded = downcall("folio_row_add_cell_embedded",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static long rowAddCellEmbedded(long row, String text, long font, double fontSize) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) row_add_cell_embedded.invokeExact(row, arena.allocateFrom(text), font, fontSize);
            } catch (Throwable t) {
                throw new FolioException("rowAddCellEmbedded failed", t);
            }
        });
    }

    private static final MethodHandle row_add_cell_element = downcall("folio_row_add_cell_element",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static long rowAddCellElement(long row, long element) {
        return locked(() -> {
            try {
                return (long) row_add_cell_element.invokeExact(row, element);
            } catch (Throwable t) {
                throw new FolioException("rowAddCellElement failed", t);
            }
        });
    }

    private static final MethodHandle row_free = downcall("folio_row_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void rowFree(long row) {
        lockedVoid(() -> {
            try {
                row_free.invokeExact(row);
            } catch (Throwable t) {
                throw new FolioException("rowFree failed", t);
            }
        });
    }

    private static final MethodHandle cell_free = downcall("folio_cell_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void cellFree(long cell) {
        lockedVoid(() -> {
            try {
                cell_free.invokeExact(cell);
            } catch (Throwable t) {
                throw new FolioException("cellFree failed", t);
            }
        });
    }

    private static final MethodHandle cell_set_align = downcall("folio_cell_set_align",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static void cellSetAlign(long cell, int align) {
        lockedVoid(() -> {
            try {
                int rc = (int) cell_set_align.invokeExact(cell, align);
                checkResult(rc, "cellSetAlign");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("cellSetAlign failed", t);
            }
        });
    }

    private static final MethodHandle cell_set_padding = downcall("folio_cell_set_padding",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static void cellSetPadding(long cell, double padding) {
        lockedVoid(() -> {
            try {
                int rc = (int) cell_set_padding.invokeExact(cell, padding);
                checkResult(rc, "cellSetPadding");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("cellSetPadding failed", t);
            }
        });
    }

    private static final MethodHandle cell_set_background = downcall("folio_cell_set_background",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static void cellSetBackground(long cell, double r, double g, double b) {
        lockedVoid(() -> {
            try {
                int rc = (int) cell_set_background.invokeExact(cell, r, g, b);
                checkResult(rc, "cellSetBackground");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("cellSetBackground failed", t);
            }
        });
    }

    private static final MethodHandle cell_set_colspan = downcall("folio_cell_set_colspan",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static void cellSetColspan(long cell, int n) {
        lockedVoid(() -> {
            try {
                int rc = (int) cell_set_colspan.invokeExact(cell, n);
                checkResult(rc, "cellSetColspan");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("cellSetColspan failed", t);
            }
        });
    }

    private static final MethodHandle cell_set_rowspan = downcall("folio_cell_set_rowspan",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static void cellSetRowspan(long cell, int n) {
        lockedVoid(() -> {
            try {
                int rc = (int) cell_set_rowspan.invokeExact(cell, n);
                checkResult(rc, "cellSetRowspan");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("cellSetRowspan failed", t);
            }
        });
    }

    private static final MethodHandle table_free = downcall("folio_table_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void tableFree(long table) {
        lockedVoid(() -> {
            try {
                table_free.invokeExact(table);
            } catch (Throwable t) {
                throw new FolioException("tableFree failed", t);
            }
        });
    }

    // ── Font ────────────────────────────────────────────────────────

    private static final MethodHandle font_standard = downcall("folio_font_standard",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    public static long fontStandard(String name) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) font_standard.invokeExact(arena.allocateFrom(name));
            } catch (Throwable t) {
                throw new FolioException("fontStandard failed", t);
            }
        });
    }

    private static final MethodHandle font_helvetica = downcall("folio_font_helvetica",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long fontHelvetica() {
        return locked(() -> {
            try {
                return (long) font_helvetica.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("fontHelvetica failed", t);
            }
        });
    }

    private static final MethodHandle font_helvetica_bold = downcall("folio_font_helvetica_bold",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long fontHelveticaBold() {
        return locked(() -> {
            try {
                return (long) font_helvetica_bold.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("fontHelveticaBold failed", t);
            }
        });
    }

    private static final MethodHandle font_times_roman = downcall("folio_font_times_roman",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long fontTimesRoman() {
        return locked(() -> {
            try {
                return (long) font_times_roman.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("fontTimesRoman failed", t);
            }
        });
    }

    private static final MethodHandle font_times_bold = downcall("folio_font_times_bold",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long fontTimesBold() {
        return locked(() -> {
            try {
                return (long) font_times_bold.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("fontTimesBold failed", t);
            }
        });
    }

    private static final MethodHandle font_courier = downcall("folio_font_courier",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long fontCourier() {
        return locked(() -> {
            try {
                return (long) font_courier.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("fontCourier failed", t);
            }
        });
    }

    private static final MethodHandle font_load_ttf = downcall("folio_font_load_ttf",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    public static long fontLoadTTF(String path) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) font_load_ttf.invokeExact(arena.allocateFrom(path));
            } catch (Throwable t) {
                throw new FolioException("fontLoadTTF failed", t);
            }
        });
    }

    private static final MethodHandle font_parse_ttf = downcall("folio_font_parse_ttf",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    public static long fontParseTTF(byte[] data) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                MemorySegment seg = arena.allocate(ValueLayout.JAVA_BYTE, data.length);
                MemorySegment.copy(data, 0, seg, ValueLayout.JAVA_BYTE, 0, data.length);
                return (long) font_parse_ttf.invokeExact(seg, data.length);
            } catch (Throwable t) {
                throw new FolioException("fontParseTTF failed", t);
            }
        });
    }

    private static final MethodHandle font_free = downcall("folio_font_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void fontFree(long font) {
        lockedVoid(() -> {
            try {
                font_free.invokeExact(font);
            } catch (Throwable t) {
                throw new FolioException("fontFree failed", t);
            }
        });
    }

    // ── Image ───────────────────────────────────────────────────────

    private static final MethodHandle image_load_jpeg = downcall("folio_image_load_jpeg",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    public static long imageLoadJpeg(String path) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) image_load_jpeg.invokeExact(arena.allocateFrom(path));
            } catch (Throwable t) {
                throw new FolioException("imageLoadJpeg failed", t);
            }
        });
    }

    private static final MethodHandle image_load_png = downcall("folio_image_load_png",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    public static long imageLoadPng(String path) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) image_load_png.invokeExact(arena.allocateFrom(path));
            } catch (Throwable t) {
                throw new FolioException("imageLoadPng failed", t);
            }
        });
    }

    private static final MethodHandle image_parse_jpeg = downcall("folio_image_parse_jpeg",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    public static long imageParseJpeg(byte[] data) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                MemorySegment seg = arena.allocate(ValueLayout.JAVA_BYTE, data.length);
                MemorySegment.copy(data, 0, seg, ValueLayout.JAVA_BYTE, 0, data.length);
                return (long) image_parse_jpeg.invokeExact(seg, data.length);
            } catch (Throwable t) {
                throw new FolioException("imageParseJpeg failed", t);
            }
        });
    }

    private static final MethodHandle image_parse_png = downcall("folio_image_parse_png",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    public static long imageParsePng(byte[] data) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                MemorySegment seg = arena.allocate(ValueLayout.JAVA_BYTE, data.length);
                MemorySegment.copy(data, 0, seg, ValueLayout.JAVA_BYTE, 0, data.length);
                return (long) image_parse_png.invokeExact(seg, data.length);
            } catch (Throwable t) {
                throw new FolioException("imageParsePng failed", t);
            }
        });
    }

    private static final MethodHandle image_width = downcall("folio_image_width",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));

    public static int imageWidth(long img) {
        return locked(() -> {
            try {
                return (int) image_width.invokeExact(img);
            } catch (Throwable t) {
                throw new FolioException("imageWidth failed", t);
            }
        });
    }

    private static final MethodHandle image_height = downcall("folio_image_height",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));

    public static int imageHeight(long img) {
        return locked(() -> {
            try {
                return (int) image_height.invokeExact(img);
            } catch (Throwable t) {
                throw new FolioException("imageHeight failed", t);
            }
        });
    }

    private static final MethodHandle image_free = downcall("folio_image_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void imageFree(long img) {
        lockedVoid(() -> {
            try {
                image_free.invokeExact(img);
            } catch (Throwable t) {
                throw new FolioException("imageFree failed", t);
            }
        });
    }

    private static final MethodHandle image_element_new = downcall("folio_image_element_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static long imageElementNew(long img) {
        return locked(() -> {
            try {
                return (long) image_element_new.invokeExact(img);
            } catch (Throwable t) {
                throw new FolioException("imageElementNew failed", t);
            }
        });
    }

    private static final MethodHandle image_element_set_size = downcall("folio_image_element_set_size",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static void imageElementSetSize(long elem, double w, double h) {
        lockedVoid(() -> {
            try {
                int rc = (int) image_element_set_size.invokeExact(elem, w, h);
                checkResult(rc, "imageElementSetSize");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("imageElementSetSize failed", t);
            }
        });
    }

    private static final MethodHandle image_element_free = downcall("folio_image_element_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void imageElementFree(long elem) {
        lockedVoid(() -> {
            try {
                image_element_free.invokeExact(elem);
            } catch (Throwable t) {
                throw new FolioException("imageElementFree failed", t);
            }
        });
    }

    // ── Div ─────────────────────────────────────────────────────────

    private static final MethodHandle div_new = downcall("folio_div_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long divNew() {
        return locked(() -> {
            try {
                return (long) div_new.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("divNew failed", t);
            }
        });
    }

    private static final MethodHandle div_free = downcall("folio_div_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void divFree(long div) {
        lockedVoid(() -> {
            try {
                div_free.invokeExact(div);
            } catch (Throwable t) {
                throw new FolioException("divFree failed", t);
            }
        });
    }

    private static final MethodHandle div_add = downcall("folio_div_add",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static void divAdd(long div, long element) {
        lockedVoid(() -> {
            try {
                int rc = (int) div_add.invokeExact(div, element);
                checkResult(rc, "divAdd");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("divAdd failed", t);
            }
        });
    }

    private static final MethodHandle div_set_padding = downcall("folio_div_set_padding",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static void divSetPadding(long div, double top, double right, double bottom, double left) {
        lockedVoid(() -> {
            try {
                int rc = (int) div_set_padding.invokeExact(div, top, right, bottom, left);
                checkResult(rc, "divSetPadding");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("divSetPadding failed", t);
            }
        });
    }

    private static final MethodHandle div_set_background = downcall("folio_div_set_background",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static void divSetBackground(long div, double r, double g, double b) {
        lockedVoid(() -> {
            try {
                int rc = (int) div_set_background.invokeExact(div, r, g, b);
                checkResult(rc, "divSetBackground");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("divSetBackground failed", t);
            }
        });
    }

    private static final MethodHandle div_set_border = downcall("folio_div_set_border",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static void divSetBorder(long div, double width, double r, double g, double b) {
        lockedVoid(() -> {
            try {
                int rc = (int) div_set_border.invokeExact(div, width, r, g, b);
                checkResult(rc, "divSetBorder");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("divSetBorder failed", t);
            }
        });
    }

    private static final MethodHandle div_set_width = downcall("folio_div_set_width",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static void divSetWidth(long div, double pts) {
        lockedVoid(() -> {
            try {
                int rc = (int) div_set_width.invokeExact(div, pts);
                checkResult(rc, "divSetWidth");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("divSetWidth failed", t);
            }
        });
    }

    private static final MethodHandle div_set_min_height = downcall("folio_div_set_min_height",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static void divSetMinHeight(long div, double pts) {
        lockedVoid(() -> {
            try {
                int rc = (int) div_set_min_height.invokeExact(div, pts);
                checkResult(rc, "divSetMinHeight");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("divSetMinHeight failed", t);
            }
        });
    }

    private static final MethodHandle div_set_space_before = downcall("folio_div_set_space_before",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static void divSetSpaceBefore(long div, double pts) {
        lockedVoid(() -> {
            try {
                int rc = (int) div_set_space_before.invokeExact(div, pts);
                checkResult(rc, "divSetSpaceBefore");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("divSetSpaceBefore failed", t);
            }
        });
    }

    private static final MethodHandle div_set_space_after = downcall("folio_div_set_space_after",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static void divSetSpaceAfter(long div, double pts) {
        lockedVoid(() -> {
            try {
                int rc = (int) div_set_space_after.invokeExact(div, pts);
                checkResult(rc, "divSetSpaceAfter");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("divSetSpaceAfter failed", t);
            }
        });
    }

    // ── List ────────────────────────────────────────────────────────

    private static final MethodHandle list_new = downcall("folio_list_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static long listNew(long font, double fontSize) {
        return locked(() -> {
            try {
                return (long) list_new.invokeExact(font, fontSize);
            } catch (Throwable t) {
                throw new FolioException("listNew failed", t);
            }
        });
    }

    private static final MethodHandle list_new_embedded = downcall("folio_list_new_embedded",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static long listNewEmbedded(long font, double fontSize) {
        return locked(() -> {
            try {
                return (long) list_new_embedded.invokeExact(font, fontSize);
            } catch (Throwable t) {
                throw new FolioException("listNewEmbedded failed", t);
            }
        });
    }

    private static final MethodHandle list_free = downcall("folio_list_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void listFree(long list) {
        lockedVoid(() -> {
            try {
                list_free.invokeExact(list);
            } catch (Throwable t) {
                throw new FolioException("listFree failed", t);
            }
        });
    }

    private static final MethodHandle list_set_style = downcall("folio_list_set_style",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static void listSetStyle(long list, int style) {
        lockedVoid(() -> {
            try {
                int rc = (int) list_set_style.invokeExact(list, style);
                checkResult(rc, "listSetStyle");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("listSetStyle failed", t);
            }
        });
    }

    private static final MethodHandle list_set_indent = downcall("folio_list_set_indent",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));

    public static void listSetIndent(long list, double indent) {
        lockedVoid(() -> {
            try {
                int rc = (int) list_set_indent.invokeExact(list, indent);
                checkResult(rc, "listSetIndent");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("listSetIndent failed", t);
            }
        });
    }

    private static final MethodHandle list_add_item = downcall("folio_list_add_item",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    public static void listAddItem(long list, String text) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) list_add_item.invokeExact(list, arena.allocateFrom(text));
                checkResult(rc, "listAddItem");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("listAddItem failed", t);
            }
        });
    }

    // ── Line Separator / Area Break ─────────────────────────────────

    private static final MethodHandle line_separator_new = downcall("folio_line_separator_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long lineSeparatorNew() {
        return locked(() -> {
            try {
                return (long) line_separator_new.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("lineSeparatorNew failed", t);
            }
        });
    }

    private static final MethodHandle area_break_new = downcall("folio_area_break_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long areaBreakNew() {
        return locked(() -> {
            try {
                return (long) area_break_new.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("areaBreakNew failed", t);
            }
        });
    }

    // ── Reader ──────────────────────────────────────────────────────

    private static final MethodHandle reader_open = downcall("folio_reader_open",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    public static long readerOpen(String path) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) reader_open.invokeExact(arena.allocateFrom(path));
            } catch (Throwable t) {
                throw new FolioException("readerOpen failed", t);
            }
        });
    }

    private static final MethodHandle reader_free = downcall("folio_reader_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void readerFree(long reader) {
        lockedVoid(() -> {
            try {
                reader_free.invokeExact(reader);
            } catch (Throwable t) {
                throw new FolioException("readerFree failed", t);
            }
        });
    }

    private static final MethodHandle reader_page_count = downcall("folio_reader_page_count",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));

    public static int readerPageCount(long reader) {
        return locked(() -> {
            try {
                return (int) reader_page_count.invokeExact(reader);
            } catch (Throwable t) {
                throw new FolioException("readerPageCount failed", t);
            }
        });
    }

    private static final MethodHandle reader_extract_text = downcall("folio_reader_extract_text",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static long readerExtractText(long reader, int pageIndex) {
        return locked(() -> {
            try {
                return (long) reader_extract_text.invokeExact(reader, pageIndex);
            } catch (Throwable t) {
                throw new FolioException("readerExtractText failed", t);
            }
        });
    }

    private static final MethodHandle reader_info_title = downcall("folio_reader_info_title",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static long readerInfoTitle(long reader) {
        return locked(() -> {
            try {
                return (long) reader_info_title.invokeExact(reader);
            } catch (Throwable t) {
                throw new FolioException("readerInfoTitle failed", t);
            }
        });
    }

    private static final MethodHandle reader_info_author = downcall("folio_reader_info_author",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static long readerInfoAuthor(long reader) {
        return locked(() -> {
            try {
                return (long) reader_info_author.invokeExact(reader);
            } catch (Throwable t) {
                throw new FolioException("readerInfoAuthor failed", t);
            }
        });
    }

    private static final MethodHandle reader_page_width = downcall("folio_reader_page_width",
        FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static double readerPageWidth(long reader, int pageIndex) {
        return locked(() -> {
            try {
                return (double) reader_page_width.invokeExact(reader, pageIndex);
            } catch (Throwable t) {
                throw new FolioException("readerPageWidth failed", t);
            }
        });
    }

    private static final MethodHandle reader_page_height = downcall("folio_reader_page_height",
        FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));

    public static double readerPageHeight(long reader, int pageIndex) {
        return locked(() -> {
            try {
                return (double) reader_page_height.invokeExact(reader, pageIndex);
            } catch (Throwable t) {
                throw new FolioException("readerPageHeight failed", t);
            }
        });
    }

    // ── Buffer ──────────────────────────────────────────────────────

    private static final MethodHandle buffer_data = downcall("folio_buffer_data",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));

    public static MemorySegment bufferData(long buf) {
        return locked(() -> {
            try {
                return (MemorySegment) buffer_data.invokeExact(buf);
            } catch (Throwable t) {
                throw new FolioException("bufferData failed", t);
            }
        });
    }

    private static final MethodHandle buffer_len = downcall("folio_buffer_len",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));

    public static int bufferLen(long buf) {
        return locked(() -> {
            try {
                return (int) buffer_len.invokeExact(buf);
            } catch (Throwable t) {
                throw new FolioException("bufferLen failed", t);
            }
        });
    }

    private static final MethodHandle buffer_free = downcall("folio_buffer_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void bufferFree(long buf) {
        lockedVoid(() -> {
            try {
                buffer_free.invokeExact(buf);
            } catch (Throwable t) {
                throw new FolioException("bufferFree failed", t);
            }
        });
    }

    public static String bufferToString(long buf) {
        return locked(() -> {
            if (buf == 0) return null;
            try {
                int len = bufferLen(buf);
                if (len <= 0) return "";
                MemorySegment data = bufferData(buf);
                byte[] bytes = data.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
                return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            } finally {
                bufferFree(buf);
            }
        });
    }

    // ── HTML ────────────────────────────────────────────────────────

    private static final MethodHandle html_to_pdf = downcall("folio_html_to_pdf",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

    public static void htmlToPdf(String html, String outputPath) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) html_to_pdf.invokeExact(arena.allocateFrom(html), arena.allocateFrom(outputPath));
                checkResult(rc, "htmlToPdf");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("htmlToPdf failed", t);
            }
        });
    }

    private static final MethodHandle html_to_buffer = downcall("folio_html_to_buffer",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static long htmlToBuffer(String html, double pageWidth, double pageHeight) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) html_to_buffer.invokeExact(arena.allocateFrom(html), pageWidth, pageHeight);
            } catch (Throwable t) {
                throw new FolioException("htmlToBuffer failed", t);
            }
        });
    }

    private static final MethodHandle html_convert = downcall("folio_html_convert",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

    public static long htmlConvert(String html, double pageWidth, double pageHeight) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) html_convert.invokeExact(arena.allocateFrom(html), pageWidth, pageHeight);
            } catch (Throwable t) {
                throw new FolioException("htmlConvert failed", t);
            }
        });
    }

    // ── Reader (additional) ────────────────────────────────────────

    private static final MethodHandle reader_parse = downcall("folio_reader_parse",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    public static long readerParse(byte[] data) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                MemorySegment seg = arena.allocate(ValueLayout.JAVA_BYTE, data.length);
                MemorySegment.copy(data, 0, seg, ValueLayout.JAVA_BYTE, 0, data.length);
                return (long) reader_parse.invokeExact(seg, data.length);
            } catch (Throwable t) {
                throw new FolioException("readerParse failed", t);
            }
        });
    }

    private static final MethodHandle reader_version = downcall("folio_reader_version",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));

    public static long readerVersion(long reader) {
        return locked(() -> {
            try {
                return (long) reader_version.invokeExact(reader);
            } catch (Throwable t) {
                throw new FolioException("readerVersion failed", t);
            }
        });
    }

    // ── Forms ──────────────────────────────────────────────────────

    private static final MethodHandle form_new = downcall("folio_form_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));

    public static long formNew() {
        return locked(() -> {
            try {
                return (long) form_new.invokeExact();
            } catch (Throwable t) {
                throw new FolioException("formNew failed", t);
            }
        });
    }

    private static final MethodHandle form_free = downcall("folio_form_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));

    public static void formFree(long form) {
        lockedVoid(() -> {
            try {
                form_free.invokeExact(form);
            } catch (Throwable t) {
                throw new FolioException("formFree failed", t);
            }
        });
    }

    private static final MethodHandle form_add_text_field = downcall("folio_form_add_text_field",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT));

    public static void formAddTextField(long form, String name, double x1, double y1, double x2, double y2, int pageIndex) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) form_add_text_field.invokeExact(form, arena.allocateFrom(name), x1, y1, x2, y2, pageIndex);
                checkResult(rc, "formAddTextField");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("formAddTextField failed", t);
            }
        });
    }

    private static final MethodHandle form_add_checkbox = downcall("folio_form_add_checkbox",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));

    public static void formAddCheckbox(long form, String name, double x1, double y1, double x2, double y2, int pageIndex, boolean checked) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) form_add_checkbox.invokeExact(form, arena.allocateFrom(name), x1, y1, x2, y2, pageIndex, checked ? 1 : 0);
                checkResult(rc, "formAddCheckbox");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("formAddCheckbox failed", t);
            }
        });
    }

    private static final MethodHandle form_add_dropdown = downcall("folio_form_add_dropdown",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    public static void formAddDropdown(long form, String name, double x1, double y1, double x2, double y2, int pageIndex, String[] options) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                MemorySegment optArray = arena.allocate(ValueLayout.ADDRESS, options.length);
                for (int i = 0; i < options.length; i++) {
                    optArray.setAtIndex(ValueLayout.ADDRESS, i, arena.allocateFrom(options[i]));
                }
                int rc = (int) form_add_dropdown.invokeExact(form, arena.allocateFrom(name), x1, y1, x2, y2, pageIndex, optArray, options.length);
                checkResult(rc, "formAddDropdown");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("formAddDropdown failed", t);
            }
        });
    }

    private static final MethodHandle form_add_signature = downcall("folio_form_add_signature",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT));

    public static void formAddSignature(long form, String name, double x1, double y1, double x2, double y2, int pageIndex) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) form_add_signature.invokeExact(form, arena.allocateFrom(name), x1, y1, x2, y2, pageIndex);
                checkResult(rc, "formAddSignature");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("formAddSignature failed", t);
            }
        });
    }

    // ── Additional Fonts ────────────────────────────────────────────

    private static final MethodHandle font_courier_bold = downcall("folio_font_courier_bold", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long fontCourierBold() { return locked(() -> { try { return (long) font_courier_bold.invokeExact(); } catch (Throwable t) { throw new FolioException("fontCourierBold failed", t); } }); }

    private static final MethodHandle font_courier_oblique = downcall("folio_font_courier_oblique", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long fontCourierOblique() { return locked(() -> { try { return (long) font_courier_oblique.invokeExact(); } catch (Throwable t) { throw new FolioException("fontCourierOblique failed", t); } }); }

    private static final MethodHandle font_courier_bold_oblique = downcall("folio_font_courier_bold_oblique", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long fontCourierBoldOblique() { return locked(() -> { try { return (long) font_courier_bold_oblique.invokeExact(); } catch (Throwable t) { throw new FolioException("fontCourierBoldOblique failed", t); } }); }

    private static final MethodHandle font_helvetica_oblique = downcall("folio_font_helvetica_oblique", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long fontHelveticaOblique() { return locked(() -> { try { return (long) font_helvetica_oblique.invokeExact(); } catch (Throwable t) { throw new FolioException("fontHelveticaOblique failed", t); } }); }

    private static final MethodHandle font_helvetica_bold_oblique = downcall("folio_font_helvetica_bold_oblique", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long fontHelveticaBoldOblique() { return locked(() -> { try { return (long) font_helvetica_bold_oblique.invokeExact(); } catch (Throwable t) { throw new FolioException("fontHelveticaBoldOblique failed", t); } }); }

    private static final MethodHandle font_times_italic = downcall("folio_font_times_italic", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long fontTimesItalic() { return locked(() -> { try { return (long) font_times_italic.invokeExact(); } catch (Throwable t) { throw new FolioException("fontTimesItalic failed", t); } }); }

    private static final MethodHandle font_times_bold_italic = downcall("folio_font_times_bold_italic", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long fontTimesBoldItalic() { return locked(() -> { try { return (long) font_times_bold_italic.invokeExact(); } catch (Throwable t) { throw new FolioException("fontTimesBoldItalic failed", t); } }); }

    private static final MethodHandle font_symbol = downcall("folio_font_symbol", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long fontSymbol() { return locked(() -> { try { return (long) font_symbol.invokeExact(); } catch (Throwable t) { throw new FolioException("fontSymbol failed", t); } }); }

    private static final MethodHandle font_zapf_dingbats = downcall("folio_font_zapf_dingbats", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long fontZapfDingbats() { return locked(() -> { try { return (long) font_zapf_dingbats.invokeExact(); } catch (Throwable t) { throw new FolioException("fontZapfDingbats failed", t); } }); }

    // ── Additional Paragraph ────────────────────────────────────────

    private static final MethodHandle paragraph_set_orphans = downcall("folio_paragraph_set_orphans",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void paragraphSetOrphans(long para, int n) { lockedVoid(() -> { try { int rc = (int) paragraph_set_orphans.invokeExact(para, n); checkResult(rc, "paragraphSetOrphans"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("paragraphSetOrphans failed", t); } }); }

    private static final MethodHandle paragraph_set_widows = downcall("folio_paragraph_set_widows",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void paragraphSetWidows(long para, int n) { lockedVoid(() -> { try { int rc = (int) paragraph_set_widows.invokeExact(para, n); checkResult(rc, "paragraphSetWidows"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("paragraphSetWidows failed", t); } }); }

    private static final MethodHandle paragraph_set_ellipsis = downcall("folio_paragraph_set_ellipsis",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void paragraphSetEllipsis(long para, boolean enabled) { lockedVoid(() -> { try { int rc = (int) paragraph_set_ellipsis.invokeExact(para, enabled ? 1 : 0); checkResult(rc, "paragraphSetEllipsis"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("paragraphSetEllipsis failed", t); } }); }

    private static final MethodHandle paragraph_set_word_break = downcall("folio_paragraph_set_word_break",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void paragraphSetWordBreak(long para, String mode) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) paragraph_set_word_break.invokeExact(para, arena.allocateFrom(mode)); checkResult(rc, "paragraphSetWordBreak"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("paragraphSetWordBreak failed", t); } }); }

    private static final MethodHandle paragraph_set_hyphens = downcall("folio_paragraph_set_hyphens",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void paragraphSetHyphens(long para, String mode) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) paragraph_set_hyphens.invokeExact(para, arena.allocateFrom(mode)); checkResult(rc, "paragraphSetHyphens"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("paragraphSetHyphens failed", t); } }); }

    // ── Additional Table ────────────────────────────────────────────

    private static final MethodHandle table_add_footer_row = downcall("folio_table_add_footer_row",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static long tableAddFooterRow(long table) { return locked(() -> { try { return (long) table_add_footer_row.invokeExact(table); } catch (Throwable t) { throw new FolioException("tableAddFooterRow failed", t); } }); }

    private static final MethodHandle table_set_cell_spacing = downcall("folio_table_set_cell_spacing",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void tableSetCellSpacing(long table, double h, double v) { lockedVoid(() -> { try { int rc = (int) table_set_cell_spacing.invokeExact(table, h, v); checkResult(rc, "tableSetCellSpacing"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("tableSetCellSpacing failed", t); } }); }

    private static final MethodHandle table_set_auto_column_widths = downcall("folio_table_set_auto_column_widths",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static void tableSetAutoColumnWidths(long table) { lockedVoid(() -> { try { int rc = (int) table_set_auto_column_widths.invokeExact(table); checkResult(rc, "tableSetAutoColumnWidths"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("tableSetAutoColumnWidths failed", t); } }); }

    private static final MethodHandle table_set_min_width = downcall("folio_table_set_min_width",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void tableSetMinWidth(long table, double pts) { lockedVoid(() -> { try { int rc = (int) table_set_min_width.invokeExact(table, pts); checkResult(rc, "tableSetMinWidth"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("tableSetMinWidth failed", t); } }); }

    // ── Additional Cell ─────────────────────────────────────────────

    private static final MethodHandle cell_set_padding_sides = downcall("folio_cell_set_padding_sides",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void cellSetPaddingSides(long cell, double top, double right, double bottom, double left) { lockedVoid(() -> { try { int rc = (int) cell_set_padding_sides.invokeExact(cell, top, right, bottom, left); checkResult(rc, "cellSetPaddingSides"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("cellSetPaddingSides failed", t); } }); }

    private static final MethodHandle cell_set_valign = downcall("folio_cell_set_valign",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void cellSetValign(long cell, int valign) { lockedVoid(() -> { try { int rc = (int) cell_set_valign.invokeExact(cell, valign); checkResult(rc, "cellSetValign"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("cellSetValign failed", t); } }); }

    private static final MethodHandle cell_set_border = downcall("folio_cell_set_border",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void cellSetBorder(long cell, double width, double r, double g, double b) { lockedVoid(() -> { try { int rc = (int) cell_set_border.invokeExact(cell, width, r, g, b); checkResult(rc, "cellSetBorder"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("cellSetBorder failed", t); } }); }

    private static final MethodHandle cell_set_borders = downcall("folio_cell_set_borders",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void cellSetBorders(long cell, double topW, double topR, double topG, double topB, double rightW, double rightR, double rightG, double rightB, double bottomW, double bottomR, double bottomG, double bottomB, double leftW, double leftR, double leftG, double leftB) { lockedVoid(() -> { try { int rc = (int) cell_set_borders.invokeExact(cell, topW, topR, topG, topB, rightW, rightR, rightG, rightB, bottomW, bottomR, bottomG, bottomB, leftW, leftR, leftG, leftB); checkResult(rc, "cellSetBorders"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("cellSetBorders failed", t); } }); }

    private static final MethodHandle cell_set_width_hint = downcall("folio_cell_set_width_hint",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void cellSetWidthHint(long cell, double pts) { lockedVoid(() -> { try { int rc = (int) cell_set_width_hint.invokeExact(cell, pts); checkResult(rc, "cellSetWidthHint"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("cellSetWidthHint failed", t); } }); }

    // ── Additional Div ──────────────────────────────────────────────

    private static final MethodHandle div_set_border_radius = downcall("folio_div_set_border_radius",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void divSetBorderRadius(long div, double radius) { lockedVoid(() -> { try { int rc = (int) div_set_border_radius.invokeExact(div, radius); checkResult(rc, "divSetBorderRadius"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("divSetBorderRadius failed", t); } }); }

    private static final MethodHandle div_set_opacity = downcall("folio_div_set_opacity",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void divSetOpacity(long div, double opacity) { lockedVoid(() -> { try { int rc = (int) div_set_opacity.invokeExact(div, opacity); checkResult(rc, "divSetOpacity"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("divSetOpacity failed", t); } }); }

    private static final MethodHandle div_set_overflow = downcall("folio_div_set_overflow",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void divSetOverflow(long div, String mode) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) div_set_overflow.invokeExact(div, arena.allocateFrom(mode)); checkResult(rc, "divSetOverflow"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("divSetOverflow failed", t); } }); }

    private static final MethodHandle div_set_tag = downcall("folio_div_set_tag", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void divSetTag(long div, String tag) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) div_set_tag.invokeExact(div, arena.allocateFrom(tag)); checkResult(rc, "divSetTag"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("divSetTag failed", t); } }); }

    private static final MethodHandle div_set_max_width = downcall("folio_div_set_max_width",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void divSetMaxWidth(long div, double pts) { lockedVoid(() -> { try { int rc = (int) div_set_max_width.invokeExact(div, pts); checkResult(rc, "divSetMaxWidth"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("divSetMaxWidth failed", t); } }); }

    private static final MethodHandle div_set_min_width = downcall("folio_div_set_min_width",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void divSetMinWidth(long div, double pts) { lockedVoid(() -> { try { int rc = (int) div_set_min_width.invokeExact(div, pts); checkResult(rc, "divSetMinWidth"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("divSetMinWidth failed", t); } }); }

    private static final MethodHandle div_set_box_shadow = downcall("folio_div_set_box_shadow",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void divSetBoxShadow(long div, double offsetX, double offsetY, double blur, double spread, double r, double g, double b) { lockedVoid(() -> { try { int rc = (int) div_set_box_shadow.invokeExact(div, offsetX, offsetY, blur, spread, r, g, b); checkResult(rc, "divSetBoxShadow"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("divSetBoxShadow failed", t); } }); }

    private static final MethodHandle div_set_max_height = downcall("folio_div_set_max_height",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void divSetMaxHeight(long div, double pts) { lockedVoid(() -> { try { int rc = (int) div_set_max_height.invokeExact(div, pts); checkResult(rc, "divSetMaxHeight"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("divSetMaxHeight failed", t); } }); }

    // ── Additional List ─────────────────────────────────────────────

    private static final MethodHandle list_set_leading = downcall("folio_list_set_leading",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void listSetLeading(long list, double leading) { lockedVoid(() -> { try { int rc = (int) list_set_leading.invokeExact(list, leading); checkResult(rc, "listSetLeading"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("listSetLeading failed", t); } }); }

    private static final MethodHandle list_add_nested_item = downcall("folio_list_add_nested_item",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static long listAddNestedItem(long list, String text) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) list_add_nested_item.invokeExact(list, arena.allocateFrom(text)); } catch (Throwable t) { throw new FolioException("listAddNestedItem failed", t); } }); }

    // ── Additional Image ────────────────────────────────────────────

    private static final MethodHandle image_load_tiff = downcall("folio_image_load_tiff",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static long imageLoadTiff(String path) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) image_load_tiff.invokeExact(arena.allocateFrom(path)); } catch (Throwable t) { throw new FolioException("imageLoadTiff failed", t); } }); }

    private static final MethodHandle image_element_set_align = downcall("folio_image_element_set_align",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void imageElementSetAlign(long elem, int align) { lockedVoid(() -> { try { int rc = (int) image_element_set_align.invokeExact(elem, align); checkResult(rc, "imageElementSetAlign"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("imageElementSetAlign failed", t); } }); }

    private static final MethodHandle image_element_set_alt_text = downcall("folio_image_element_set_alt_text", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void imageElementSetAltText(long elem, String text) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) image_element_set_alt_text.invokeExact(elem, arena.allocateFrom(text)); checkResult(rc, "imageElementSetAltText"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("imageElementSetAltText failed", t); } }); }

    // ── Link ────────────────────────────────────────────────────────

    private static final MethodHandle link_new = downcall("folio_link_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static long linkNew(String text, String uri, long font, double fontSize) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) link_new.invokeExact(arena.allocateFrom(text), arena.allocateFrom(uri), font, fontSize); } catch (Throwable t) { throw new FolioException("linkNew failed", t); } }); }

    private static final MethodHandle link_new_embedded = downcall("folio_link_new_embedded",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static long linkNewEmbedded(String text, String uri, long font, double fontSize) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) link_new_embedded.invokeExact(arena.allocateFrom(text), arena.allocateFrom(uri), font, fontSize); } catch (Throwable t) { throw new FolioException("linkNewEmbedded failed", t); } }); }

    private static final MethodHandle link_new_internal = downcall("folio_link_new_internal",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static long linkNewInternal(String text, String destName, long font, double fontSize) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) link_new_internal.invokeExact(arena.allocateFrom(text), arena.allocateFrom(destName), font, fontSize); } catch (Throwable t) { throw new FolioException("linkNewInternal failed", t); } }); }

    private static final MethodHandle link_free = downcall("folio_link_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void linkFree(long link) { lockedVoid(() -> { try { link_free.invokeExact(link); } catch (Throwable t) { throw new FolioException("linkFree failed", t); } }); }

    private static final MethodHandle link_set_color = downcall("folio_link_set_color",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void linkSetColor(long link, double r, double g, double b) { lockedVoid(() -> { try { int rc = (int) link_set_color.invokeExact(link, r, g, b); checkResult(rc, "linkSetColor"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("linkSetColor failed", t); } }); }

    private static final MethodHandle link_set_underline = downcall("folio_link_set_underline",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static void linkSetUnderline(long link) { lockedVoid(() -> { try { int rc = (int) link_set_underline.invokeExact(link); checkResult(rc, "linkSetUnderline"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("linkSetUnderline failed", t); } }); }

    private static final MethodHandle link_set_align = downcall("folio_link_set_align",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void linkSetAlign(long link, int align) { lockedVoid(() -> { try { int rc = (int) link_set_align.invokeExact(link, align); checkResult(rc, "linkSetAlign"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("linkSetAlign failed", t); } }); }

    // ── Barcode ─────────────────────────────────────────────────────

    private static final MethodHandle barcode_qr = downcall("folio_barcode_qr", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static long barcodeQr(String data) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) barcode_qr.invokeExact(arena.allocateFrom(data)); } catch (Throwable t) { throw new FolioException("barcodeQr failed", t); } }); }

    private static final MethodHandle barcode_qr_ecc = downcall("folio_barcode_qr_ecc", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static long barcodeQrEcc(String data, int level) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) barcode_qr_ecc.invokeExact(arena.allocateFrom(data), level); } catch (Throwable t) { throw new FolioException("barcodeQrEcc failed", t); } }); }

    private static final MethodHandle barcode_code128 = downcall("folio_barcode_code128", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static long barcodeCode128(String data) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) barcode_code128.invokeExact(arena.allocateFrom(data)); } catch (Throwable t) { throw new FolioException("barcodeCode128 failed", t); } }); }

    private static final MethodHandle barcode_ean13 = downcall("folio_barcode_ean13", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static long barcodeEan13(String data) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) barcode_ean13.invokeExact(arena.allocateFrom(data)); } catch (Throwable t) { throw new FolioException("barcodeEan13 failed", t); } }); }

    private static final MethodHandle barcode_width = downcall("folio_barcode_width", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static int barcodeWidth(long bc) { return locked(() -> { try { return (int) barcode_width.invokeExact(bc); } catch (Throwable t) { throw new FolioException("barcodeWidth failed", t); } }); }

    private static final MethodHandle barcode_height = downcall("folio_barcode_height", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static int barcodeHeight(long bc) { return locked(() -> { try { return (int) barcode_height.invokeExact(bc); } catch (Throwable t) { throw new FolioException("barcodeHeight failed", t); } }); }

    private static final MethodHandle barcode_free = downcall("folio_barcode_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void barcodeFree(long bc) { lockedVoid(() -> { try { barcode_free.invokeExact(bc); } catch (Throwable t) { throw new FolioException("barcodeFree failed", t); } }); }

    private static final MethodHandle barcode_element_new = downcall("folio_barcode_element_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static long barcodeElementNew(long bc, double width) { return locked(() -> { try { return (long) barcode_element_new.invokeExact(bc, width); } catch (Throwable t) { throw new FolioException("barcodeElementNew failed", t); } }); }

    private static final MethodHandle barcode_element_set_height = downcall("folio_barcode_element_set_height", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void barcodeElementSetHeight(long elem, double height) { lockedVoid(() -> { try { int rc = (int) barcode_element_set_height.invokeExact(elem, height); checkResult(rc, "barcodeElementSetHeight"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("barcodeElementSetHeight failed", t); } }); }

    private static final MethodHandle barcode_element_set_align = downcall("folio_barcode_element_set_align", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void barcodeElementSetAlign(long elem, int align) { lockedVoid(() -> { try { int rc = (int) barcode_element_set_align.invokeExact(elem, align); checkResult(rc, "barcodeElementSetAlign"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("barcodeElementSetAlign failed", t); } }); }

    private static final MethodHandle barcode_element_set_alt_text = downcall("folio_barcode_element_set_alt_text", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void barcodeElementSetAltText(long elem, String text) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) barcode_element_set_alt_text.invokeExact(elem, arena.allocateFrom(text)); checkResult(rc, "barcodeElementSetAltText"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("barcodeElementSetAltText failed", t); } }); }

    private static final MethodHandle barcode_element_free = downcall("folio_barcode_element_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void barcodeElementFree(long elem) { lockedVoid(() -> { try { barcode_element_free.invokeExact(elem); } catch (Throwable t) { throw new FolioException("barcodeElementFree failed", t); } }); }

    // ── SVG ─────────────────────────────────────────────────────────

    private static final MethodHandle svg_parse = downcall("folio_svg_parse", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static long svgParse(String svgXml) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) svg_parse.invokeExact(arena.allocateFrom(svgXml)); } catch (Throwable t) { throw new FolioException("svgParse failed", t); } }); }

    private static final MethodHandle svg_parse_bytes = downcall("folio_svg_parse_bytes", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static long svgParseBytes(byte[] data) { return locked(() -> { try (var arena = Arena.ofConfined()) { MemorySegment seg = arena.allocate(ValueLayout.JAVA_BYTE, data.length); MemorySegment.copy(data, 0, seg, ValueLayout.JAVA_BYTE, 0, data.length); return (long) svg_parse_bytes.invokeExact(seg, data.length); } catch (Throwable t) { throw new FolioException("svgParseBytes failed", t); } }); }

    private static final MethodHandle svg_width = downcall("folio_svg_width", FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_LONG));
    public static double svgWidth(long svg) { return locked(() -> { try { return (double) svg_width.invokeExact(svg); } catch (Throwable t) { throw new FolioException("svgWidth failed", t); } }); }

    private static final MethodHandle svg_height = downcall("folio_svg_height", FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_LONG));
    public static double svgHeight(long svg) { return locked(() -> { try { return (double) svg_height.invokeExact(svg); } catch (Throwable t) { throw new FolioException("svgHeight failed", t); } }); }

    private static final MethodHandle svg_free = downcall("folio_svg_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void svgFree(long svg) { lockedVoid(() -> { try { svg_free.invokeExact(svg); } catch (Throwable t) { throw new FolioException("svgFree failed", t); } }); }

    private static final MethodHandle svg_element_new = downcall("folio_svg_element_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static long svgElementNew(long svg) { return locked(() -> { try { return (long) svg_element_new.invokeExact(svg); } catch (Throwable t) { throw new FolioException("svgElementNew failed", t); } }); }

    private static final MethodHandle svg_element_set_size = downcall("folio_svg_element_set_size", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void svgElementSetSize(long elem, double w, double h) { lockedVoid(() -> { try { int rc = (int) svg_element_set_size.invokeExact(elem, w, h); checkResult(rc, "svgElementSetSize"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("svgElementSetSize failed", t); } }); }

    private static final MethodHandle svg_element_set_align = downcall("folio_svg_element_set_align", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void svgElementSetAlign(long elem, int align) { lockedVoid(() -> { try { int rc = (int) svg_element_set_align.invokeExact(elem, align); checkResult(rc, "svgElementSetAlign"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("svgElementSetAlign failed", t); } }); }

    private static final MethodHandle svg_element_set_alt_text = downcall("folio_svg_element_set_alt_text", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void svgElementSetAltText(long elem, String text) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) svg_element_set_alt_text.invokeExact(elem, arena.allocateFrom(text)); checkResult(rc, "svgElementSetAltText"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("svgElementSetAltText failed", t); } }); }

    private static final MethodHandle svg_element_free = downcall("folio_svg_element_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void svgElementFree(long elem) { lockedVoid(() -> { try { svg_element_free.invokeExact(elem); } catch (Throwable t) { throw new FolioException("svgElementFree failed", t); } }); }

    // ── Flex ────────────────────────────────────────────────────────

    private static final MethodHandle flex_new = downcall("folio_flex_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long flexNew() { return locked(() -> { try { return (long) flex_new.invokeExact(); } catch (Throwable t) { throw new FolioException("flexNew failed", t); } }); }

    private static final MethodHandle flex_free = downcall("folio_flex_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void flexFree(long flex) { lockedVoid(() -> { try { flex_free.invokeExact(flex); } catch (Throwable t) { throw new FolioException("flexFree failed", t); } }); }

    private static final MethodHandle flex_add = downcall("folio_flex_add", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static void flexAdd(long flex, long element) { lockedVoid(() -> { try { int rc = (int) flex_add.invokeExact(flex, element); checkResult(rc, "flexAdd"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexAdd failed", t); } }); }

    private static final MethodHandle flex_add_item = downcall("folio_flex_add_item", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static void flexAddItem(long flex, long item) { lockedVoid(() -> { try { int rc = (int) flex_add_item.invokeExact(flex, item); checkResult(rc, "flexAddItem"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexAddItem failed", t); } }); }

    private static final MethodHandle flex_set_direction = downcall("folio_flex_set_direction", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void flexSetDirection(long flex, int dir) { lockedVoid(() -> { try { int rc = (int) flex_set_direction.invokeExact(flex, dir); checkResult(rc, "flexSetDirection"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetDirection failed", t); } }); }

    private static final MethodHandle flex_set_justify_content = downcall("folio_flex_set_justify_content", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void flexSetJustifyContent(long flex, int justify) { lockedVoid(() -> { try { int rc = (int) flex_set_justify_content.invokeExact(flex, justify); checkResult(rc, "flexSetJustifyContent"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetJustifyContent failed", t); } }); }

    private static final MethodHandle flex_set_align_items = downcall("folio_flex_set_align_items", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void flexSetAlignItems(long flex, int align) { lockedVoid(() -> { try { int rc = (int) flex_set_align_items.invokeExact(flex, align); checkResult(rc, "flexSetAlignItems"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetAlignItems failed", t); } }); }

    private static final MethodHandle flex_set_wrap = downcall("folio_flex_set_wrap", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void flexSetWrap(long flex, int wrap) { lockedVoid(() -> { try { int rc = (int) flex_set_wrap.invokeExact(flex, wrap); checkResult(rc, "flexSetWrap"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetWrap failed", t); } }); }

    private static final MethodHandle flex_set_gap = downcall("folio_flex_set_gap", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void flexSetGap(long flex, double gap) { lockedVoid(() -> { try { int rc = (int) flex_set_gap.invokeExact(flex, gap); checkResult(rc, "flexSetGap"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetGap failed", t); } }); }

    private static final MethodHandle flex_set_row_gap = downcall("folio_flex_set_row_gap", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void flexSetRowGap(long flex, double gap) { lockedVoid(() -> { try { int rc = (int) flex_set_row_gap.invokeExact(flex, gap); checkResult(rc, "flexSetRowGap"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetRowGap failed", t); } }); }

    private static final MethodHandle flex_set_column_gap = downcall("folio_flex_set_column_gap", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void flexSetColumnGap(long flex, double gap) { lockedVoid(() -> { try { int rc = (int) flex_set_column_gap.invokeExact(flex, gap); checkResult(rc, "flexSetColumnGap"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetColumnGap failed", t); } }); }

    private static final MethodHandle flex_set_padding = downcall("folio_flex_set_padding", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void flexSetPadding(long flex, double padding) { lockedVoid(() -> { try { int rc = (int) flex_set_padding.invokeExact(flex, padding); checkResult(rc, "flexSetPadding"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetPadding failed", t); } }); }

    private static final MethodHandle flex_set_padding_all = downcall("folio_flex_set_padding_all", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void flexSetPaddingAll(long flex, double top, double right, double bottom, double left) { lockedVoid(() -> { try { int rc = (int) flex_set_padding_all.invokeExact(flex, top, right, bottom, left); checkResult(rc, "flexSetPaddingAll"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetPaddingAll failed", t); } }); }

    private static final MethodHandle flex_set_background = downcall("folio_flex_set_background", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void flexSetBackground(long flex, double r, double g, double b) { lockedVoid(() -> { try { int rc = (int) flex_set_background.invokeExact(flex, r, g, b); checkResult(rc, "flexSetBackground"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetBackground failed", t); } }); }

    private static final MethodHandle flex_set_border = downcall("folio_flex_set_border", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void flexSetBorder(long flex, double width, double r, double g, double b) { lockedVoid(() -> { try { int rc = (int) flex_set_border.invokeExact(flex, width, r, g, b); checkResult(rc, "flexSetBorder"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetBorder failed", t); } }); }

    private static final MethodHandle flex_set_space_before = downcall("folio_flex_set_space_before", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void flexSetSpaceBefore(long flex, double pts) { lockedVoid(() -> { try { int rc = (int) flex_set_space_before.invokeExact(flex, pts); checkResult(rc, "flexSetSpaceBefore"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetSpaceBefore failed", t); } }); }

    private static final MethodHandle flex_set_space_after = downcall("folio_flex_set_space_after", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void flexSetSpaceAfter(long flex, double pts) { lockedVoid(() -> { try { int rc = (int) flex_set_space_after.invokeExact(flex, pts); checkResult(rc, "flexSetSpaceAfter"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexSetSpaceAfter failed", t); } }); }

    private static final MethodHandle flex_item_new = downcall("folio_flex_item_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static long flexItemNew(long element) { return locked(() -> { try { return (long) flex_item_new.invokeExact(element); } catch (Throwable t) { throw new FolioException("flexItemNew failed", t); } }); }

    private static final MethodHandle flex_item_free = downcall("folio_flex_item_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void flexItemFree(long item) { lockedVoid(() -> { try { flex_item_free.invokeExact(item); } catch (Throwable t) { throw new FolioException("flexItemFree failed", t); } }); }

    private static final MethodHandle flex_item_set_grow = downcall("folio_flex_item_set_grow", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void flexItemSetGrow(long item, double grow) { lockedVoid(() -> { try { int rc = (int) flex_item_set_grow.invokeExact(item, grow); checkResult(rc, "flexItemSetGrow"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexItemSetGrow failed", t); } }); }

    private static final MethodHandle flex_item_set_shrink = downcall("folio_flex_item_set_shrink", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void flexItemSetShrink(long item, double shrink) { lockedVoid(() -> { try { int rc = (int) flex_item_set_shrink.invokeExact(item, shrink); checkResult(rc, "flexItemSetShrink"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexItemSetShrink failed", t); } }); }

    private static final MethodHandle flex_item_set_basis = downcall("folio_flex_item_set_basis", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void flexItemSetBasis(long item, double basis) { lockedVoid(() -> { try { int rc = (int) flex_item_set_basis.invokeExact(item, basis); checkResult(rc, "flexItemSetBasis"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexItemSetBasis failed", t); } }); }

    private static final MethodHandle flex_item_set_align_self = downcall("folio_flex_item_set_align_self", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void flexItemSetAlignSelf(long item, int align) { lockedVoid(() -> { try { int rc = (int) flex_item_set_align_self.invokeExact(item, align); checkResult(rc, "flexItemSetAlignSelf"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexItemSetAlignSelf failed", t); } }); }

    private static final MethodHandle flex_item_set_margins = downcall("folio_flex_item_set_margins", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void flexItemSetMargins(long item, double top, double right, double bottom, double left) { lockedVoid(() -> { try { int rc = (int) flex_item_set_margins.invokeExact(item, top, right, bottom, left); checkResult(rc, "flexItemSetMargins"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("flexItemSetMargins failed", t); } }); }

    // ── Grid ────────────────────────────────────────────────────────

    private static final MethodHandle grid_new = downcall("folio_grid_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long gridNew() { return locked(() -> { try { return (long) grid_new.invokeExact(); } catch (Throwable t) { throw new FolioException("gridNew failed", t); } }); }

    private static final MethodHandle grid_free = downcall("folio_grid_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void gridFree(long grid) { lockedVoid(() -> { try { grid_free.invokeExact(grid); } catch (Throwable t) { throw new FolioException("gridFree failed", t); } }); }

    private static final MethodHandle grid_add_child = downcall("folio_grid_add_child", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static void gridAddChild(long grid, long element) { lockedVoid(() -> { try { int rc = (int) grid_add_child.invokeExact(grid, element); checkResult(rc, "gridAddChild"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridAddChild failed", t); } }); }

    private static final MethodHandle grid_set_template_columns = downcall("folio_grid_set_template_columns", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void gridSetTemplateColumns(long grid, int[] types, double[] values) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment tSeg = arena.allocate(ValueLayout.JAVA_INT, types.length); for (int i = 0; i < types.length; i++) tSeg.setAtIndex(ValueLayout.JAVA_INT, i, types[i]); MemorySegment vSeg = arena.allocate(ValueLayout.JAVA_DOUBLE, values.length); for (int i = 0; i < values.length; i++) vSeg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, values[i]); int rc = (int) grid_set_template_columns.invokeExact(grid, tSeg, vSeg, types.length); checkResult(rc, "gridSetTemplateColumns"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetTemplateColumns failed", t); } }); }

    private static final MethodHandle grid_set_template_rows = downcall("folio_grid_set_template_rows", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void gridSetTemplateRows(long grid, int[] types, double[] values) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment tSeg = arena.allocate(ValueLayout.JAVA_INT, types.length); for (int i = 0; i < types.length; i++) tSeg.setAtIndex(ValueLayout.JAVA_INT, i, types[i]); MemorySegment vSeg = arena.allocate(ValueLayout.JAVA_DOUBLE, values.length); for (int i = 0; i < values.length; i++) vSeg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, values[i]); int rc = (int) grid_set_template_rows.invokeExact(grid, tSeg, vSeg, types.length); checkResult(rc, "gridSetTemplateRows"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetTemplateRows failed", t); } }); }

    private static final MethodHandle grid_set_auto_rows = downcall("folio_grid_set_auto_rows", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void gridSetAutoRows(long grid, int[] types, double[] values) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment tSeg = arena.allocate(ValueLayout.JAVA_INT, types.length); for (int i = 0; i < types.length; i++) tSeg.setAtIndex(ValueLayout.JAVA_INT, i, types[i]); MemorySegment vSeg = arena.allocate(ValueLayout.JAVA_DOUBLE, values.length); for (int i = 0; i < values.length; i++) vSeg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, values[i]); int rc = (int) grid_set_auto_rows.invokeExact(grid, tSeg, vSeg, types.length); checkResult(rc, "gridSetAutoRows"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetAutoRows failed", t); } }); }

    private static final MethodHandle grid_set_gap = downcall("folio_grid_set_gap", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void gridSetGap(long grid, double rowGap, double colGap) { lockedVoid(() -> { try { int rc = (int) grid_set_gap.invokeExact(grid, rowGap, colGap); checkResult(rc, "gridSetGap"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetGap failed", t); } }); }

    private static final MethodHandle grid_set_placement = downcall("folio_grid_set_placement", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    public static void gridSetPlacement(long grid, int childIndex, int colStart, int colEnd, int rowStart, int rowEnd) { lockedVoid(() -> { try { int rc = (int) grid_set_placement.invokeExact(grid, childIndex, colStart, colEnd, rowStart, rowEnd); checkResult(rc, "gridSetPlacement"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetPlacement failed", t); } }); }

    private static final MethodHandle grid_set_padding = downcall("folio_grid_set_padding", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void gridSetPadding(long grid, double padding) { lockedVoid(() -> { try { int rc = (int) grid_set_padding.invokeExact(grid, padding); checkResult(rc, "gridSetPadding"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetPadding failed", t); } }); }

    private static final MethodHandle grid_set_background = downcall("folio_grid_set_background", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void gridSetBackground(long grid, double r, double g, double b) { lockedVoid(() -> { try { int rc = (int) grid_set_background.invokeExact(grid, r, g, b); checkResult(rc, "gridSetBackground"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetBackground failed", t); } }); }

    private static final MethodHandle grid_set_justify_items = downcall("folio_grid_set_justify_items", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void gridSetJustifyItems(long grid, int align) { lockedVoid(() -> { try { int rc = (int) grid_set_justify_items.invokeExact(grid, align); checkResult(rc, "gridSetJustifyItems"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetJustifyItems failed", t); } }); }

    private static final MethodHandle grid_set_align_items = downcall("folio_grid_set_align_items", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void gridSetAlignItems(long grid, int align) { lockedVoid(() -> { try { int rc = (int) grid_set_align_items.invokeExact(grid, align); checkResult(rc, "gridSetAlignItems"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetAlignItems failed", t); } }); }

    private static final MethodHandle grid_set_justify_content = downcall("folio_grid_set_justify_content", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void gridSetJustifyContent(long grid, int justify) { lockedVoid(() -> { try { int rc = (int) grid_set_justify_content.invokeExact(grid, justify); checkResult(rc, "gridSetJustifyContent"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetJustifyContent failed", t); } }); }

    private static final MethodHandle grid_set_align_content = downcall("folio_grid_set_align_content", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void gridSetAlignContent(long grid, int align) { lockedVoid(() -> { try { int rc = (int) grid_set_align_content.invokeExact(grid, align); checkResult(rc, "gridSetAlignContent"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetAlignContent failed", t); } }); }

    private static final MethodHandle grid_set_space_before = downcall("folio_grid_set_space_before", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void gridSetSpaceBefore(long grid, double pts) { lockedVoid(() -> { try { int rc = (int) grid_set_space_before.invokeExact(grid, pts); checkResult(rc, "gridSetSpaceBefore"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetSpaceBefore failed", t); } }); }

    private static final MethodHandle grid_set_space_after = downcall("folio_grid_set_space_after", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void gridSetSpaceAfter(long grid, double pts) { lockedVoid(() -> { try { int rc = (int) grid_set_space_after.invokeExact(grid, pts); checkResult(rc, "gridSetSpaceAfter"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("gridSetSpaceAfter failed", t); } }); }

    // ── Columns ─────────────────────────────────────────────────────

    private static final MethodHandle columns_new = downcall("folio_columns_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static long columnsNew(int cols) { return locked(() -> { try { return (long) columns_new.invokeExact(cols); } catch (Throwable t) { throw new FolioException("columnsNew failed", t); } }); }

    private static final MethodHandle columns_free = downcall("folio_columns_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void columnsFree(long columns) { lockedVoid(() -> { try { columns_free.invokeExact(columns); } catch (Throwable t) { throw new FolioException("columnsFree failed", t); } }); }

    private static final MethodHandle columns_set_gap = downcall("folio_columns_set_gap", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void columnsSetGap(long columns, double gap) { lockedVoid(() -> { try { int rc = (int) columns_set_gap.invokeExact(columns, gap); checkResult(rc, "columnsSetGap"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("columnsSetGap failed", t); } }); }

    private static final MethodHandle columns_set_widths = downcall("folio_columns_set_widths", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void columnsSetWidths(long columns, double[] widths) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment seg = arena.allocate(ValueLayout.JAVA_DOUBLE, widths.length); for (int i = 0; i < widths.length; i++) seg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, widths[i]); int rc = (int) columns_set_widths.invokeExact(columns, seg, widths.length); checkResult(rc, "columnsSetWidths"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("columnsSetWidths failed", t); } }); }

    private static final MethodHandle columns_add = downcall("folio_columns_add", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static void columnsAdd(long columns, int colIndex, long element) { lockedVoid(() -> { try { int rc = (int) columns_add.invokeExact(columns, colIndex, element); checkResult(rc, "columnsAdd"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("columnsAdd failed", t); } }); }

    // ── Float ───────────────────────────────────────────────────────

    private static final MethodHandle float_new = downcall("folio_float_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static long floatNew(int side, long element) { return locked(() -> { try { return (long) float_new.invokeExact(side, element); } catch (Throwable t) { throw new FolioException("floatNew failed", t); } }); }

    private static final MethodHandle float_free = downcall("folio_float_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void floatFree(long flt) { lockedVoid(() -> { try { float_free.invokeExact(flt); } catch (Throwable t) { throw new FolioException("floatFree failed", t); } }); }

    private static final MethodHandle float_set_margin = downcall("folio_float_set_margin", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void floatSetMargin(long flt, double margin) { lockedVoid(() -> { try { int rc = (int) float_set_margin.invokeExact(flt, margin); checkResult(rc, "floatSetMargin"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("floatSetMargin failed", t); } }); }

    // ── TabbedLine ──────────────────────────────────────────────────

    private static final MethodHandle tabbed_line_new = downcall("folio_tabbed_line_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static long tabbedLineNew(long font, double fontSize, double[] positions, int[] aligns, int[] leaders) { return locked(() -> { try (var arena = Arena.ofConfined()) { MemorySegment pSeg = arena.allocate(ValueLayout.JAVA_DOUBLE, positions.length); for (int i = 0; i < positions.length; i++) pSeg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, positions[i]); MemorySegment aSeg = arena.allocate(ValueLayout.JAVA_INT, aligns.length); for (int i = 0; i < aligns.length; i++) aSeg.setAtIndex(ValueLayout.JAVA_INT, i, aligns[i]); MemorySegment lSeg = arena.allocate(ValueLayout.JAVA_INT, leaders.length); for (int i = 0; i < leaders.length; i++) lSeg.setAtIndex(ValueLayout.JAVA_INT, i, leaders[i]); return (long) tabbed_line_new.invokeExact(font, fontSize, pSeg, aSeg, lSeg, positions.length); } catch (Throwable t) { throw new FolioException("tabbedLineNew failed", t); } }); }

    private static final MethodHandle tabbed_line_new_embedded = downcall("folio_tabbed_line_new_embedded", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static long tabbedLineNewEmbedded(long font, double fontSize, double[] positions, int[] aligns, int[] leaders) { return locked(() -> { try (var arena = Arena.ofConfined()) { MemorySegment pSeg = arena.allocate(ValueLayout.JAVA_DOUBLE, positions.length); for (int i = 0; i < positions.length; i++) pSeg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, positions[i]); MemorySegment aSeg = arena.allocate(ValueLayout.JAVA_INT, aligns.length); for (int i = 0; i < aligns.length; i++) aSeg.setAtIndex(ValueLayout.JAVA_INT, i, aligns[i]); MemorySegment lSeg = arena.allocate(ValueLayout.JAVA_INT, leaders.length); for (int i = 0; i < leaders.length; i++) lSeg.setAtIndex(ValueLayout.JAVA_INT, i, leaders[i]); return (long) tabbed_line_new_embedded.invokeExact(font, fontSize, pSeg, aSeg, lSeg, positions.length); } catch (Throwable t) { throw new FolioException("tabbedLineNewEmbedded failed", t); } }); }

    private static final MethodHandle tabbed_line_free = downcall("folio_tabbed_line_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void tabbedLineFree(long tl) { lockedVoid(() -> { try { tabbed_line_free.invokeExact(tl); } catch (Throwable t) { throw new FolioException("tabbedLineFree failed", t); } }); }

    private static final MethodHandle tabbed_line_set_segments = downcall("folio_tabbed_line_set_segments", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void tabbedLineSetSegments(long tl, String[] segments) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment arr = arena.allocate(ValueLayout.ADDRESS, segments.length); for (int i = 0; i < segments.length; i++) arr.setAtIndex(ValueLayout.ADDRESS, i, arena.allocateFrom(segments[i])); int rc = (int) tabbed_line_set_segments.invokeExact(tl, arr, segments.length); checkResult(rc, "tabbedLineSetSegments"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("tabbedLineSetSegments failed", t); } }); }

    private static final MethodHandle tabbed_line_set_color = downcall("folio_tabbed_line_set_color", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void tabbedLineSetColor(long tl, double r, double g, double b) { lockedVoid(() -> { try { int rc = (int) tabbed_line_set_color.invokeExact(tl, r, g, b); checkResult(rc, "tabbedLineSetColor"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("tabbedLineSetColor failed", t); } }); }

    private static final MethodHandle tabbed_line_set_leading = downcall("folio_tabbed_line_set_leading", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void tabbedLineSetLeading(long tl, double leading) { lockedVoid(() -> { try { int rc = (int) tabbed_line_set_leading.invokeExact(tl, leading); checkResult(rc, "tabbedLineSetLeading"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("tabbedLineSetLeading failed", t); } }); }

    // ── Additional Document ─────────────────────────────────────────

    private static final MethodHandle document_set_watermark = downcall("folio_document_set_watermark", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void documentSetWatermark(long doc, String text) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) document_set_watermark.invokeExact(doc, arena.allocateFrom(text)); checkResult(rc, "documentSetWatermark"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentSetWatermark failed", t); } }); }

    private static final MethodHandle document_set_watermark_config = downcall("folio_document_set_watermark_config", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void documentSetWatermarkConfig(long doc, String text, double fontSize, double r, double g, double b, double angle, double opacity) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) document_set_watermark_config.invokeExact(doc, arena.allocateFrom(text), fontSize, r, g, b, angle, opacity); checkResult(rc, "documentSetWatermarkConfig"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentSetWatermarkConfig failed", t); } }); }

    private static final MethodHandle document_add_outline = downcall("folio_document_add_outline", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static long documentAddOutline(long doc, String title, int pageIndex) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) document_add_outline.invokeExact(doc, arena.allocateFrom(title), pageIndex); } catch (Throwable t) { throw new FolioException("documentAddOutline failed", t); } }); }

    private static final MethodHandle document_add_outline_xyz = downcall("folio_document_add_outline_xyz", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static long documentAddOutlineXyz(long doc, String title, int pageIndex, double left, double top, double zoom) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) document_add_outline_xyz.invokeExact(doc, arena.allocateFrom(title), pageIndex, left, top, zoom); } catch (Throwable t) { throw new FolioException("documentAddOutlineXyz failed", t); } }); }

    private static final MethodHandle outline_add_child = downcall("folio_outline_add_child", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static long outlineAddChild(long outline, String title, int pageIndex) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) outline_add_child.invokeExact(outline, arena.allocateFrom(title), pageIndex); } catch (Throwable t) { throw new FolioException("outlineAddChild failed", t); } }); }

    private static final MethodHandle outline_add_child_xyz = downcall("folio_outline_add_child_xyz", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static long outlineAddChildXyz(long outline, String title, int pageIndex, double left, double top, double zoom) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) outline_add_child_xyz.invokeExact(outline, arena.allocateFrom(title), pageIndex, left, top, zoom); } catch (Throwable t) { throw new FolioException("outlineAddChildXyz failed", t); } }); }

    private static final MethodHandle document_add_named_dest = downcall("folio_document_add_named_dest", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void documentAddNamedDest(long doc, String name, int pageIndex, String fitType, double top, double left, double zoom) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) document_add_named_dest.invokeExact(doc, arena.allocateFrom(name), pageIndex, arena.allocateFrom(fitType), top, left, zoom); checkResult(rc, "documentAddNamedDest"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentAddNamedDest failed", t); } }); }

    private static final MethodHandle document_set_viewer_preferences = downcall("folio_document_set_viewer_preferences", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    public static void documentSetViewerPreferences(long doc, String pageLayout, String pageMode, boolean hideToolbar, boolean hideMenubar, boolean hideWindowUI, boolean fitWindow, boolean centerWindow, boolean displayDocTitle) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) document_set_viewer_preferences.invokeExact(doc, arena.allocateFrom(pageLayout), arena.allocateFrom(pageMode), hideToolbar ? 1 : 0, hideMenubar ? 1 : 0, hideWindowUI ? 1 : 0, fitWindow ? 1 : 0, centerWindow ? 1 : 0, displayDocTitle ? 1 : 0); checkResult(rc, "documentSetViewerPreferences"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentSetViewerPreferences failed", t); } }); }

    private static final MethodHandle document_add_page_label = downcall("folio_document_add_page_label", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void documentAddPageLabel(long doc, int pageIndex, String style, String prefix, int start) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) document_add_page_label.invokeExact(doc, pageIndex, arena.allocateFrom(style), arena.allocateFrom(prefix), start); checkResult(rc, "documentAddPageLabel"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentAddPageLabel failed", t); } }); }

    private static final MethodHandle document_remove_page = downcall("folio_document_remove_page", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void documentRemovePage(long doc, int index) { lockedVoid(() -> { try { int rc = (int) document_remove_page.invokeExact(doc, index); checkResult(rc, "documentRemovePage"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentRemovePage failed", t); } }); }

    private static final MethodHandle document_add_absolute = downcall("folio_document_add_absolute", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void documentAddAbsolute(long doc, long element, double x, double y, double width) { lockedVoid(() -> { try { int rc = (int) document_add_absolute.invokeExact(doc, element, x, y, width); checkResult(rc, "documentAddAbsolute"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentAddAbsolute failed", t); } }); }

    private static final MethodHandle document_attach_file = downcall("folio_document_attach_file", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    public static void documentAttachFile(long doc, byte[] data, String fileName, String mimeType, String description, String afRelationship) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment seg = arena.allocate(ValueLayout.JAVA_BYTE, data.length); MemorySegment.copy(data, 0, seg, ValueLayout.JAVA_BYTE, 0, data.length); int rc = (int) document_attach_file.invokeExact(doc, seg, data.length, arena.allocateFrom(fileName), arena.allocateFrom(mimeType), arena.allocateFrom(description), arena.allocateFrom(afRelationship)); checkResult(rc, "documentAttachFile"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentAttachFile failed", t); } }); }

    private static final MethodHandle document_add_html = downcall("folio_document_add_html", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void documentAddHtml(long doc, String html) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) document_add_html.invokeExact(doc, arena.allocateFrom(html)); checkResult(rc, "documentAddHtml"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentAddHtml failed", t); } }); }

    private static final MethodHandle document_add_html_with_options = downcall("folio_document_add_html_with_options", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    public static void documentAddHtmlWithOptions(long doc, String html, double defaultFontSize, double pageWidth, double pageHeight, String basePath, String fallbackFontPath) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) document_add_html_with_options.invokeExact(doc, arena.allocateFrom(html), defaultFontSize, pageWidth, pageHeight, arena.allocateFrom(basePath), arena.allocateFrom(fallbackFontPath)); checkResult(rc, "documentAddHtmlWithOptions"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentAddHtmlWithOptions failed", t); } }); }

    private static final MethodHandle document_set_first_margins = downcall("folio_document_set_first_margins", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void documentSetFirstMargins(long doc, double top, double right, double bottom, double left) { lockedVoid(() -> { try { int rc = (int) document_set_first_margins.invokeExact(doc, top, right, bottom, left); checkResult(rc, "documentSetFirstMargins"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentSetFirstMargins failed", t); } }); }

    private static final MethodHandle document_set_left_margins = downcall("folio_document_set_left_margins", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void documentSetLeftMargins(long doc, double top, double right, double bottom, double left) { lockedVoid(() -> { try { int rc = (int) document_set_left_margins.invokeExact(doc, top, right, bottom, left); checkResult(rc, "documentSetLeftMargins"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentSetLeftMargins failed", t); } }); }

    private static final MethodHandle document_set_right_margins = downcall("folio_document_set_right_margins", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void documentSetRightMargins(long doc, double top, double right, double bottom, double left) { lockedVoid(() -> { try { int rc = (int) document_set_right_margins.invokeExact(doc, top, right, bottom, left); checkResult(rc, "documentSetRightMargins"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentSetRightMargins failed", t); } }); }

    // ── Additional Page ─────────────────────────────────────────────

    private static final MethodHandle page_set_crop_box = downcall("folio_page_set_crop_box", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void pageSetCropBox(long page, double x1, double y1, double x2, double y2) { lockedVoid(() -> { try { int rc = (int) page_set_crop_box.invokeExact(page, x1, y1, x2, y2); checkResult(rc, "pageSetCropBox"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageSetCropBox failed", t); } }); }

    private static final MethodHandle page_set_trim_box = downcall("folio_page_set_trim_box", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void pageSetTrimBox(long page, double x1, double y1, double x2, double y2) { lockedVoid(() -> { try { int rc = (int) page_set_trim_box.invokeExact(page, x1, y1, x2, y2); checkResult(rc, "pageSetTrimBox"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageSetTrimBox failed", t); } }); }

    private static final MethodHandle page_set_bleed_box = downcall("folio_page_set_bleed_box", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void pageSetBleedBox(long page, double x1, double y1, double x2, double y2) { lockedVoid(() -> { try { int rc = (int) page_set_bleed_box.invokeExact(page, x1, y1, x2, y2); checkResult(rc, "pageSetBleedBox"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageSetBleedBox failed", t); } }); }

    private static final MethodHandle page_set_art_box = downcall("folio_page_set_art_box", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void pageSetArtBox(long page, double x1, double y1, double x2, double y2) { lockedVoid(() -> { try { int rc = (int) page_set_art_box.invokeExact(page, x1, y1, x2, y2); checkResult(rc, "pageSetArtBox"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageSetArtBox failed", t); } }); }

    private static final MethodHandle page_set_size = downcall("folio_page_set_size", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void pageSetSize(long page, double width, double height) { lockedVoid(() -> { try { int rc = (int) page_set_size.invokeExact(page, width, height); checkResult(rc, "pageSetSize"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageSetSize failed", t); } }); }

    private static final MethodHandle page_add_page_link = downcall("folio_page_add_page_link", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT));
    public static void pageAddPageLink(long page, double x1, double y1, double x2, double y2, int targetPage) { lockedVoid(() -> { try { int rc = (int) page_add_page_link.invokeExact(page, x1, y1, x2, y2, targetPage); checkResult(rc, "pageAddPageLink"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageAddPageLink failed", t); } }); }

    private static final MethodHandle page_add_internal_link = downcall("folio_page_add_internal_link", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS));
    public static void pageAddInternalLink(long page, double x1, double y1, double x2, double y2, String destName) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) page_add_internal_link.invokeExact(page, x1, y1, x2, y2, arena.allocateFrom(destName)); checkResult(rc, "pageAddInternalLink"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageAddInternalLink failed", t); } }); }

    private static final MethodHandle page_add_text_annotation = downcall("folio_page_add_text_annotation", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    public static void pageAddTextAnnotation(long page, double x1, double y1, double x2, double y2, String text, String icon) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) page_add_text_annotation.invokeExact(page, x1, y1, x2, y2, arena.allocateFrom(text), arena.allocateFrom(icon)); checkResult(rc, "pageAddTextAnnotation"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageAddTextAnnotation failed", t); } }); }

    private static final MethodHandle page_set_opacity_fill_stroke = downcall("folio_page_set_opacity_fill_stroke", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void pageSetOpacityFillStroke(long page, double fillAlpha, double strokeAlpha) { lockedVoid(() -> { try { int rc = (int) page_set_opacity_fill_stroke.invokeExact(page, fillAlpha, strokeAlpha); checkResult(rc, "pageSetOpacityFillStroke"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageSetOpacityFillStroke failed", t); } }); }

    private static final MethodHandle page_add_highlight = downcall("folio_page_add_highlight", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void pageAddHighlight(long page, double x1, double y1, double x2, double y2, double r, double g, double b, double[] quadPoints) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment seg = arena.allocate(ValueLayout.JAVA_DOUBLE, quadPoints.length); for (int i = 0; i < quadPoints.length; i++) seg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, quadPoints[i]); int rc = (int) page_add_highlight.invokeExact(page, x1, y1, x2, y2, r, g, b, seg, quadPoints.length); checkResult(rc, "pageAddHighlight"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageAddHighlight failed", t); } }); }

    private static final MethodHandle page_add_underline_annotation = downcall("folio_page_add_underline_annotation", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void pageAddUnderlineAnnotation(long page, double x1, double y1, double x2, double y2, double r, double g, double b, double[] quadPoints) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment seg = arena.allocate(ValueLayout.JAVA_DOUBLE, quadPoints.length); for (int i = 0; i < quadPoints.length; i++) seg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, quadPoints[i]); int rc = (int) page_add_underline_annotation.invokeExact(page, x1, y1, x2, y2, r, g, b, seg, quadPoints.length); checkResult(rc, "pageAddUnderlineAnnotation"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageAddUnderlineAnnotation failed", t); } }); }

    private static final MethodHandle page_add_squiggly = downcall("folio_page_add_squiggly", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void pageAddSquiggly(long page, double x1, double y1, double x2, double y2, double r, double g, double b, double[] quadPoints) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment seg = arena.allocate(ValueLayout.JAVA_DOUBLE, quadPoints.length); for (int i = 0; i < quadPoints.length; i++) seg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, quadPoints[i]); int rc = (int) page_add_squiggly.invokeExact(page, x1, y1, x2, y2, r, g, b, seg, quadPoints.length); checkResult(rc, "pageAddSquiggly"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageAddSquiggly failed", t); } }); }

    private static final MethodHandle page_add_strikeout = downcall("folio_page_add_strikeout", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void pageAddStrikeout(long page, double x1, double y1, double x2, double y2, double r, double g, double b, double[] quadPoints) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment seg = arena.allocate(ValueLayout.JAVA_DOUBLE, quadPoints.length); for (int i = 0; i < quadPoints.length; i++) seg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, quadPoints[i]); int rc = (int) page_add_strikeout.invokeExact(page, x1, y1, x2, y2, r, g, b, seg, quadPoints.length); checkResult(rc, "pageAddStrikeout"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("pageAddStrikeout failed", t); } }); }

    // ── Additional Forms ────────────────────────────────────────────

    private static final MethodHandle form_add_multiline_text_field = downcall("folio_form_add_multiline_text_field", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT));
    public static void formAddMultilineTextField(long form, String name, double x1, double y1, double x2, double y2, int pageIndex) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) form_add_multiline_text_field.invokeExact(form, arena.allocateFrom(name), x1, y1, x2, y2, pageIndex); checkResult(rc, "formAddMultilineTextField"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formAddMultilineTextField failed", t); } }); }

    private static final MethodHandle form_add_password_field = downcall("folio_form_add_password_field", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT));
    public static void formAddPasswordField(long form, String name, double x1, double y1, double x2, double y2, int pageIndex) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) form_add_password_field.invokeExact(form, arena.allocateFrom(name), x1, y1, x2, y2, pageIndex); checkResult(rc, "formAddPasswordField"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formAddPasswordField failed", t); } }); }

    private static final MethodHandle form_add_listbox = downcall("folio_form_add_listbox", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void formAddListbox(long form, String name, double x1, double y1, double x2, double y2, int pageIndex, String[] options) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment optArray = arena.allocate(ValueLayout.ADDRESS, options.length); for (int i = 0; i < options.length; i++) optArray.setAtIndex(ValueLayout.ADDRESS, i, arena.allocateFrom(options[i])); int rc = (int) form_add_listbox.invokeExact(form, arena.allocateFrom(name), x1, y1, x2, y2, pageIndex, optArray, options.length); checkResult(rc, "formAddListbox"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formAddListbox failed", t); } }); }

    private static final MethodHandle form_add_radio_group = downcall("folio_form_add_radio_group", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void formAddRadioGroup(long form, String name, String[] values, double[] rects, int[] pageIndices) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment valArray = arena.allocate(ValueLayout.ADDRESS, values.length); for (int i = 0; i < values.length; i++) valArray.setAtIndex(ValueLayout.ADDRESS, i, arena.allocateFrom(values[i])); MemorySegment rectSeg = arena.allocate(ValueLayout.JAVA_DOUBLE, rects.length); for (int i = 0; i < rects.length; i++) rectSeg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, rects[i]); MemorySegment pageSeg = arena.allocate(ValueLayout.JAVA_INT, pageIndices.length); for (int i = 0; i < pageIndices.length; i++) pageSeg.setAtIndex(ValueLayout.JAVA_INT, i, pageIndices[i]); int rc = (int) form_add_radio_group.invokeExact(form, arena.allocateFrom(name), valArray, rectSeg, pageSeg, values.length); checkResult(rc, "formAddRadioGroup"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formAddRadioGroup failed", t); } }); }

    private static final MethodHandle form_create_text_field = downcall("folio_form_create_text_field", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT));
    public static long formCreateTextField(String name, double x1, double y1, double x2, double y2, int pageIndex) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) form_create_text_field.invokeExact(arena.allocateFrom(name), x1, y1, x2, y2, pageIndex); } catch (Throwable t) { throw new FolioException("formCreateTextField failed", t); } }); }

    private static final MethodHandle form_create_checkbox = downcall("folio_form_create_checkbox", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    public static long formCreateCheckbox(String name, double x1, double y1, double x2, double y2, int pageIndex, boolean checked) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) form_create_checkbox.invokeExact(arena.allocateFrom(name), x1, y1, x2, y2, pageIndex, checked ? 1 : 0); } catch (Throwable t) { throw new FolioException("formCreateCheckbox failed", t); } }); }

    private static final MethodHandle form_add_field = downcall("folio_form_add_field", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static void formAddField(long form, long field) { lockedVoid(() -> { try { int rc = (int) form_add_field.invokeExact(form, field); checkResult(rc, "formAddField"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formAddField failed", t); } }); }

    private static final MethodHandle form_field_free = downcall("folio_form_field_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void formFieldFree(long field) { lockedVoid(() -> { try { form_field_free.invokeExact(field); } catch (Throwable t) { throw new FolioException("formFieldFree failed", t); } }); }

    private static final MethodHandle form_field_set_value = downcall("folio_form_field_set_value", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void formFieldSetValue(long field, String value) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) form_field_set_value.invokeExact(field, arena.allocateFrom(value)); checkResult(rc, "formFieldSetValue"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formFieldSetValue failed", t); } }); }

    private static final MethodHandle form_field_set_read_only = downcall("folio_form_field_set_read_only", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static void formFieldSetReadOnly(long field) { lockedVoid(() -> { try { int rc = (int) form_field_set_read_only.invokeExact(field); checkResult(rc, "formFieldSetReadOnly"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formFieldSetReadOnly failed", t); } }); }

    private static final MethodHandle form_field_set_required = downcall("folio_form_field_set_required", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static void formFieldSetRequired(long field) { lockedVoid(() -> { try { int rc = (int) form_field_set_required.invokeExact(field); checkResult(rc, "formFieldSetRequired"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formFieldSetRequired failed", t); } }); }

    private static final MethodHandle form_field_set_background_color = downcall("folio_form_field_set_background_color", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void formFieldSetBackgroundColor(long field, double r, double g, double b) { lockedVoid(() -> { try { int rc = (int) form_field_set_background_color.invokeExact(field, r, g, b); checkResult(rc, "formFieldSetBackgroundColor"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formFieldSetBackgroundColor failed", t); } }); }

    private static final MethodHandle form_field_set_border_color = downcall("folio_form_field_set_border_color", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void formFieldSetBorderColor(long field, double r, double g, double b) { lockedVoid(() -> { try { int rc = (int) form_field_set_border_color.invokeExact(field, r, g, b); checkResult(rc, "formFieldSetBorderColor"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formFieldSetBorderColor failed", t); } }); }

    private static final MethodHandle form_filler_new = downcall("folio_form_filler_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static long formFillerNew(long reader) { return locked(() -> { try { return (long) form_filler_new.invokeExact(reader); } catch (Throwable t) { throw new FolioException("formFillerNew failed", t); } }); }

    private static final MethodHandle form_filler_free = downcall("folio_form_filler_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void formFillerFree(long filler) { lockedVoid(() -> { try { form_filler_free.invokeExact(filler); } catch (Throwable t) { throw new FolioException("formFillerFree failed", t); } }); }

    private static final MethodHandle form_filler_field_names = downcall("folio_form_filler_field_names", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static long formFillerFieldNames(long filler) { return locked(() -> { try { return (long) form_filler_field_names.invokeExact(filler); } catch (Throwable t) { throw new FolioException("formFillerFieldNames failed", t); } }); }

    private static final MethodHandle form_filler_get_value = downcall("folio_form_filler_get_value", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static long formFillerGetValue(long filler, String fieldName) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) form_filler_get_value.invokeExact(filler, arena.allocateFrom(fieldName)); } catch (Throwable t) { throw new FolioException("formFillerGetValue failed", t); } }); }

    private static final MethodHandle form_filler_set_value = downcall("folio_form_filler_set_value", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    public static void formFillerSetValue(long filler, String fieldName, String value) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) form_filler_set_value.invokeExact(filler, arena.allocateFrom(fieldName), arena.allocateFrom(value)); checkResult(rc, "formFillerSetValue"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formFillerSetValue failed", t); } }); }

    private static final MethodHandle form_filler_set_checkbox = downcall("folio_form_filler_set_checkbox", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void formFillerSetCheckbox(long filler, String fieldName, boolean checked) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) form_filler_set_checkbox.invokeExact(filler, arena.allocateFrom(fieldName), checked ? 1 : 0); checkResult(rc, "formFillerSetCheckbox"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("formFillerSetCheckbox failed", t); } }); }

    // ── Page Decorator Callbacks (upcalls) ──────────────────────────

    // C signature: void (*folio_page_decorator_fn)(int32_t page_index, int32_t total_pages, uint64_t page_handle, void *user_data)
    private static final FunctionDescriptor DECORATOR_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);

    private static final MethodHandle document_set_header = downcall("folio_document_set_header",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

    private static final MethodHandle document_set_footer = downcall("folio_document_set_footer",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

    // Target method for upcall — called from C
    private static void decoratorCallback(PageDecorator decorator, int pageIndex, int totalPages, long pageHandle, MemorySegment userData) {
        decorator.decorate(pageIndex, totalPages, new Page(pageHandle));
    }

    public static MemorySegment createDecoratorStub(PageDecorator decorator, Arena arena) {
        return locked(() -> {
            try {
                MethodHandle target = MethodHandles.lookup().findStatic(
                    FolioNative.class, "decoratorCallback",
                    MethodType.methodType(void.class, PageDecorator.class, int.class, int.class, long.class, MemorySegment.class));
                target = MethodHandles.insertArguments(target, 0, decorator);
                return LINKER.upcallStub(target, DECORATOR_DESC, arena);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new FolioException("Failed to create decorator upcall stub", e);
            }
        });
    }

    public static void documentSetHeader(long doc, MemorySegment stub) {
        lockedVoid(() -> {
            try {
                int rc = (int) document_set_header.invokeExact(doc, stub, MemorySegment.NULL);
                checkResult(rc, "documentSetHeader");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentSetHeader failed", t);
            }
        });
    }

    public static void documentSetFooter(long doc, MemorySegment stub) {
        lockedVoid(() -> {
            try {
                int rc = (int) document_set_footer.invokeExact(doc, stub, MemorySegment.NULL);
                checkResult(rc, "documentSetFooter");
            } catch (FolioException e) {
                throw e;
            } catch (Throwable t) {
                throw new FolioException("documentSetFooter failed", t);
            }
        });
    }

    // ── TextRun builder ─────────────────────────────────────────────

    private static final MethodHandle run_list_new = downcall("folio_run_list_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long runListNew() { return locked(() -> { try { return (long) run_list_new.invokeExact(); } catch (Throwable t) { throw new FolioException("runListNew failed", t); } }); }

    private static final MethodHandle run_list_free = downcall("folio_run_list_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void runListFree(long rl) { lockedVoid(() -> { try { run_list_free.invokeExact(rl); } catch (Throwable t) { throw new FolioException("runListFree failed", t); } }); }

    private static final MethodHandle run_list_add = downcall("folio_run_list_add", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void runListAdd(long rl, String text, long font, double fontSize, double r, double g, double b) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) run_list_add.invokeExact(rl, arena.allocateFrom(text), font, fontSize, r, g, b); checkResult(rc, "runListAdd"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("runListAdd failed", t); } }); }

    private static final MethodHandle run_list_add_embedded = downcall("folio_run_list_add_embedded", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void runListAddEmbedded(long rl, String text, long font, double fontSize, double r, double g, double b) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) run_list_add_embedded.invokeExact(rl, arena.allocateFrom(text), font, fontSize, r, g, b); checkResult(rc, "runListAddEmbedded"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("runListAddEmbedded failed", t); } }); }

    private static final MethodHandle run_list_add_link = downcall("folio_run_list_add_link", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void runListAddLink(long rl, String text, long font, double fontSize, double r, double g, double b, String uri, boolean underline) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) run_list_add_link.invokeExact(rl, arena.allocateFrom(text), font, fontSize, r, g, b, arena.allocateFrom(uri), underline ? 1 : 0); checkResult(rc, "runListAddLink"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("runListAddLink failed", t); } }); }

    private static final MethodHandle run_list_last_set_underline = downcall("folio_run_list_last_set_underline", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static void runListLastSetUnderline(long rl) { lockedVoid(() -> { try { int rc = (int) run_list_last_set_underline.invokeExact(rl); checkResult(rc, "runListLastSetUnderline"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("runListLastSetUnderline failed", t); } }); }

    private static final MethodHandle run_list_last_set_strikethrough = downcall("folio_run_list_last_set_strikethrough", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static void runListLastSetStrikethrough(long rl) { lockedVoid(() -> { try { int rc = (int) run_list_last_set_strikethrough.invokeExact(rl); checkResult(rc, "runListLastSetStrikethrough"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("runListLastSetStrikethrough failed", t); } }); }

    private static final MethodHandle run_list_last_set_letter_spacing = downcall("folio_run_list_last_set_letter_spacing", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void runListLastSetLetterSpacing(long rl, double spacing) { lockedVoid(() -> { try { int rc = (int) run_list_last_set_letter_spacing.invokeExact(rl, spacing); checkResult(rc, "runListLastSetLetterSpacing"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("runListLastSetLetterSpacing failed", t); } }); }

    private static final MethodHandle heading_set_runs = downcall("folio_heading_set_runs", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static void headingSetRuns(long heading, long runList) { lockedVoid(() -> { try { int rc = (int) heading_set_runs.invokeExact(heading, runList); checkResult(rc, "headingSetRuns"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("headingSetRuns failed", t); } }); }

    private static final MethodHandle list_add_item_runs = downcall("folio_list_add_item_runs", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static void listAddItemRuns(long list, long runList) { lockedVoid(() -> { try { int rc = (int) list_add_item_runs.invokeExact(list, runList); checkResult(rc, "listAddItemRuns"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("listAddItemRuns failed", t); } }); }

    private static final MethodHandle list_add_item_runs_with_sublist = downcall("folio_list_add_item_runs_with_sublist", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static long listAddItemRunsWithSublist(long list, long runList) { return locked(() -> { try { return (long) list_add_item_runs_with_sublist.invokeExact(list, runList); } catch (Throwable t) { throw new FolioException("listAddItemRunsWithSublist failed", t); } }); }

    // ── Merge ───────────────────────────────────────────────────────

    private static final MethodHandle reader_merge = downcall("folio_reader_merge", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static long readerMerge(long[] readers) { return locked(() -> { try (var arena = Arena.ofConfined()) { MemorySegment seg = arena.allocate(ValueLayout.JAVA_LONG, readers.length); for (int i = 0; i < readers.length; i++) seg.setAtIndex(ValueLayout.JAVA_LONG, i, readers[i]); return (long) reader_merge.invokeExact(seg, readers.length); } catch (Throwable t) { throw new FolioException("readerMerge failed", t); } }); }

    private static final MethodHandle merge_files = downcall("folio_merge_files", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static long mergeFiles(String[] paths) { return locked(() -> { try (var arena = Arena.ofConfined()) { MemorySegment arr = arena.allocate(ValueLayout.ADDRESS, paths.length); for (int i = 0; i < paths.length; i++) arr.setAtIndex(ValueLayout.ADDRESS, i, arena.allocateFrom(paths[i])); return (long) merge_files.invokeExact(arr, paths.length); } catch (Throwable t) { throw new FolioException("mergeFiles failed", t); } }); }

    private static final MethodHandle merge_set_info = downcall("folio_merge_set_info", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    public static void mergeSetInfo(long merged, String title, String author) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) merge_set_info.invokeExact(merged, arena.allocateFrom(title), arena.allocateFrom(author)); checkResult(rc, "mergeSetInfo"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("mergeSetInfo failed", t); } }); }

    private static final MethodHandle merge_add_blank_page = downcall("folio_merge_add_blank_page", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void mergeAddBlankPage(long merged, double width, double height) { lockedVoid(() -> { try { int rc = (int) merge_add_blank_page.invokeExact(merged, width, height); checkResult(rc, "mergeAddBlankPage"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("mergeAddBlankPage failed", t); } }); }

    private static final MethodHandle merge_add_page_with_text = downcall("folio_merge_add_page_with_text", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void mergeAddPageWithText(long merged, double width, double height, String text, long font, double fontSize, double x, double y) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) merge_add_page_with_text.invokeExact(merged, width, height, arena.allocateFrom(text), font, fontSize, x, y); checkResult(rc, "mergeAddPageWithText"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("mergeAddPageWithText failed", t); } }); }

    private static final MethodHandle merge_save = downcall("folio_merge_save", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void mergeSave(long merged, String path) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) merge_save.invokeExact(merged, arena.allocateFrom(path)); checkResult(rc, "mergeSave"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("mergeSave failed", t); } }); }

    private static final MethodHandle merge_write_to_buffer = downcall("folio_merge_write_to_buffer", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static long mergeWriteToBuffer(long merged) { return locked(() -> { try { return (long) merge_write_to_buffer.invokeExact(merged); } catch (Throwable t) { throw new FolioException("mergeWriteToBuffer failed", t); } }); }

    private static final MethodHandle merge_free = downcall("folio_merge_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void mergeFree(long merged) { lockedVoid(() -> { try { merge_free.invokeExact(merged); } catch (Throwable t) { throw new FolioException("mergeFree failed", t); } }); }

    // ── Additional Merge ────────────────────────────────────────────

    private static final MethodHandle merge_page_count = downcall("folio_merge_page_count", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static int mergePageCount(long merged) { return locked(() -> { try { return (int) merge_page_count.invokeExact(merged); } catch (Throwable t) { throw new FolioException("mergePageCount failed", t); } }); }

    private static final MethodHandle merge_remove_page = downcall("folio_merge_remove_page", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void mergeRemovePage(long merged, int index) { lockedVoid(() -> { try { int rc = (int) merge_remove_page.invokeExact(merged, index); checkResult(rc, "mergeRemovePage"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("mergeRemovePage failed", t); } }); }

    private static final MethodHandle merge_rotate_page = downcall("folio_merge_rotate_page", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    public static void mergeRotatePage(long merged, int index, int degrees) { lockedVoid(() -> { try { int rc = (int) merge_rotate_page.invokeExact(merged, index, degrees); checkResult(rc, "mergeRotatePage"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("mergeRotatePage failed", t); } }); }

    private static final MethodHandle merge_reorder_pages = downcall("folio_merge_reorder_pages", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void mergeReorderPages(long merged, int[] order) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { MemorySegment seg = arena.allocate(ValueLayout.JAVA_INT, order.length); for (int i = 0; i < order.length; i++) seg.setAtIndex(ValueLayout.JAVA_INT, i, order[i]); int rc = (int) merge_reorder_pages.invokeExact(merged, seg, order.length); checkResult(rc, "mergeReorderPages"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("mergeReorderPages failed", t); } }); }

    private static final MethodHandle merge_crop_page = downcall("folio_merge_crop_page", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void mergeCropPage(long merged, int index, double x1, double y1, double x2, double y2) { lockedVoid(() -> { try { int rc = (int) merge_crop_page.invokeExact(merged, index, x1, y1, x2, y2); checkResult(rc, "mergeCropPage"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("mergeCropPage failed", t); } }); }

    private static final MethodHandle merge_flatten_forms = downcall("folio_merge_flatten_forms", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static void mergeFlattenForms(long merged) { lockedVoid(() -> { try { int rc = (int) merge_flatten_forms.invokeExact(merged); checkResult(rc, "mergeFlattenForms"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("mergeFlattenForms failed", t); } }); }

    // ── Encryption with permissions ─────────────────────────────────

    private static final MethodHandle document_set_encryption_with_permissions = downcall("folio_document_set_encryption_with_permissions", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    public static void documentSetEncryptionWithPermissions(long doc, String userPw, String ownerPw, int algorithm, int permissions) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) document_set_encryption_with_permissions.invokeExact(doc, arena.allocateFrom(userPw), arena.allocateFrom(ownerPw), algorithm, permissions); checkResult(rc, "documentSetEncryptionWithPermissions"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("documentSetEncryptionWithPermissions failed", t); } }); }

    // ── Reader content extraction ───────────────────────────────────

    private static final MethodHandle reader_text_spans = downcall("folio_reader_text_spans", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    private static final MethodHandle reader_structure_tree = downcall("folio_reader_structure_tree", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static long readerStructureTree(long reader) { return locked(() -> { try { return (long) reader_structure_tree.invokeExact(reader); } catch (Throwable t) { throw new FolioException("readerStructureTree failed", t); } }); }

    public static long readerTextSpans(long reader, int pageIndex) { return locked(() -> { try { return (long) reader_text_spans.invokeExact(reader, pageIndex); } catch (Throwable t) { throw new FolioException("readerTextSpans failed", t); } }); }

    private static final MethodHandle reader_images = downcall("folio_reader_images", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static long readerImages(long reader, int pageIndex) { return locked(() -> { try { return (long) reader_images.invokeExact(reader, pageIndex); } catch (Throwable t) { throw new FolioException("readerImages failed", t); } }); }

    private static final MethodHandle reader_paths = downcall("folio_reader_paths", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static long readerPaths(long reader, int pageIndex) { return locked(() -> { try { return (long) reader_paths.invokeExact(reader, pageIndex); } catch (Throwable t) { throw new FolioException("readerPaths failed", t); } }); }

    // ── Digital Signatures ──────────────────────────────────────────

    private static final MethodHandle signer_new_pem = downcall("folio_signer_new_pem", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static long signerNewPem(byte[] keyPem, byte[] certPem) { return locked(() -> { try (var arena = Arena.ofConfined()) { MemorySegment keySeg = arena.allocate(ValueLayout.JAVA_BYTE, keyPem.length); MemorySegment.copy(keyPem, 0, keySeg, ValueLayout.JAVA_BYTE, 0, keyPem.length); MemorySegment certSeg = arena.allocate(ValueLayout.JAVA_BYTE, certPem.length); MemorySegment.copy(certPem, 0, certSeg, ValueLayout.JAVA_BYTE, 0, certPem.length); return (long) signer_new_pem.invokeExact(keySeg, keyPem.length, certSeg, certPem.length); } catch (Throwable t) { throw new FolioException("signerNewPem failed", t); } }); }

    private static final MethodHandle signer_free = downcall("folio_signer_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void signerFree(long signer) { lockedVoid(() -> { try { signer_free.invokeExact(signer); } catch (Throwable t) { throw new FolioException("signerFree failed", t); } }); }

    private static final MethodHandle tsa_client_new = downcall("folio_tsa_client_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static long tsaClientNew(String url) { return locked(() -> { try (var arena = Arena.ofConfined()) { return (long) tsa_client_new.invokeExact(arena.allocateFrom(url)); } catch (Throwable t) { throw new FolioException("tsaClientNew failed", t); } }); }

    private static final MethodHandle tsa_client_free = downcall("folio_tsa_client_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void tsaClientFree(long tsa) { lockedVoid(() -> { try { tsa_client_free.invokeExact(tsa); } catch (Throwable t) { throw new FolioException("tsaClientFree failed", t); } }); }

    private static final MethodHandle ocsp_client_new = downcall("folio_ocsp_client_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long ocspClientNew() { return locked(() -> { try { return (long) ocsp_client_new.invokeExact(); } catch (Throwable t) { throw new FolioException("ocspClientNew failed", t); } }); }

    private static final MethodHandle ocsp_client_free = downcall("folio_ocsp_client_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void ocspClientFree(long ocsp) { lockedVoid(() -> { try { ocsp_client_free.invokeExact(ocsp); } catch (Throwable t) { throw new FolioException("ocspClientFree failed", t); } }); }

    private static final MethodHandle sign_opts_new = downcall("folio_sign_opts_new", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static long signOptsNew(long signer, int level) { return locked(() -> { try { return (long) sign_opts_new.invokeExact(signer, level); } catch (Throwable t) { throw new FolioException("signOptsNew failed", t); } }); }

    private static final MethodHandle sign_opts_set_name = downcall("folio_sign_opts_set_name", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void signOptsSetName(long opts, String name) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) sign_opts_set_name.invokeExact(opts, arena.allocateFrom(name)); checkResult(rc, "signOptsSetName"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("signOptsSetName failed", t); } }); }

    private static final MethodHandle sign_opts_set_reason = downcall("folio_sign_opts_set_reason", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void signOptsSetReason(long opts, String reason) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) sign_opts_set_reason.invokeExact(opts, arena.allocateFrom(reason)); checkResult(rc, "signOptsSetReason"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("signOptsSetReason failed", t); } }); }

    private static final MethodHandle sign_opts_set_location = downcall("folio_sign_opts_set_location", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void signOptsSetLocation(long opts, String location) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) sign_opts_set_location.invokeExact(opts, arena.allocateFrom(location)); checkResult(rc, "signOptsSetLocation"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("signOptsSetLocation failed", t); } }); }

    private static final MethodHandle sign_opts_set_contact_info = downcall("folio_sign_opts_set_contact_info", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void signOptsSetContactInfo(long opts, String info) { lockedVoid(() -> { try (var arena = Arena.ofConfined()) { int rc = (int) sign_opts_set_contact_info.invokeExact(opts, arena.allocateFrom(info)); checkResult(rc, "signOptsSetContactInfo"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("signOptsSetContactInfo failed", t); } }); }

    private static final MethodHandle sign_opts_set_tsa = downcall("folio_sign_opts_set_tsa", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static void signOptsSetTsa(long opts, long tsa) { lockedVoid(() -> { try { int rc = (int) sign_opts_set_tsa.invokeExact(opts, tsa); checkResult(rc, "signOptsSetTsa"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("signOptsSetTsa failed", t); } }); }

    private static final MethodHandle sign_opts_set_ocsp = downcall("folio_sign_opts_set_ocsp", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static void signOptsSetOcsp(long opts, long ocsp) { lockedVoid(() -> { try { int rc = (int) sign_opts_set_ocsp.invokeExact(opts, ocsp); checkResult(rc, "signOptsSetOcsp"); } catch (FolioException e) { throw e; } catch (Throwable t) { throw new FolioException("signOptsSetOcsp failed", t); } }); }

    private static final MethodHandle sign_opts_free = downcall("folio_sign_opts_free", FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void signOptsFree(long opts) { lockedVoid(() -> { try { sign_opts_free.invokeExact(opts); } catch (Throwable t) { throw new FolioException("signOptsFree failed", t); } }); }

    private static final MethodHandle sign_pdf = downcall("folio_sign_pdf", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static long signPdf(byte[] pdfData, long opts) { return locked(() -> { try (var arena = Arena.ofConfined()) { MemorySegment seg = arena.allocate(ValueLayout.JAVA_BYTE, pdfData.length); MemorySegment.copy(pdfData, 0, seg, ValueLayout.JAVA_BYTE, 0, pdfData.length); return (long) sign_pdf.invokeExact(seg, pdfData.length, opts); } catch (Throwable t) { throw new FolioException("signPdf failed", t); } }); }

    // ── v0.6.2: Document convenience ─────────────────────────────────

    private static final MethodHandle document_to_bytes = downcall("folio_document_to_bytes",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static long documentToBytes(long doc) {
        return locked(() -> {
            try { return (long) document_to_bytes.invokeExact(doc); }
            catch (Throwable t) { throw new FolioException("documentToBytes failed", t); }
        });
    }

    private static final MethodHandle document_validate_pdfa = downcall("folio_document_validate_pdfa",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static void documentValidatePdfA(long doc) {
        lockedVoid(() -> {
            try { int rc = (int) document_validate_pdfa.invokeExact(doc); checkResult(rc, "documentValidatePdfA"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("documentValidatePdfA failed", t); }
        });
    }

    // ── v0.6.2: Document headers/footers ────────────────────────────

    private static final MethodHandle document_set_header_text = downcall("folio_document_set_header_text",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT));
    public static void documentSetHeaderText(long doc, String text, long font, double size, int align) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) document_set_header_text.invokeExact(doc, arena.allocateFrom(text), font, size, align);
                checkResult(rc, "documentSetHeaderText");
            } catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("documentSetHeaderText failed", t); }
        });
    }

    private static final MethodHandle document_set_footer_text = downcall("folio_document_set_footer_text",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT));
    public static void documentSetFooterText(long doc, String text, long font, double size, int align) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) document_set_footer_text.invokeExact(doc, arena.allocateFrom(text), font, size, align);
                checkResult(rc, "documentSetFooterText");
            } catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("documentSetFooterText failed", t); }
        });
    }

    // ── v0.6.2: Div extensions ──────────────────────────────────────

    private static final MethodHandle div_set_width_percent = downcall("folio_div_set_width_percent",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void divSetWidthPercent(long div, double pct) {
        lockedVoid(() -> {
            try { int rc = (int) div_set_width_percent.invokeExact(div, pct); checkResult(rc, "divSetWidthPercent"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("divSetWidthPercent failed", t); }
        });
    }

    private static final MethodHandle div_set_aspect_ratio = downcall("folio_div_set_aspect_ratio",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void divSetAspectRatio(long div, double ratio) {
        lockedVoid(() -> {
            try { int rc = (int) div_set_aspect_ratio.invokeExact(div, ratio); checkResult(rc, "divSetAspectRatio"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("divSetAspectRatio failed", t); }
        });
    }

    private static final MethodHandle div_set_keep_together = downcall("folio_div_set_keep_together",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void divSetKeepTogether(long div, boolean enabled) {
        lockedVoid(() -> {
            try { int rc = (int) div_set_keep_together.invokeExact(div, enabled ? 1 : 0); checkResult(rc, "divSetKeepTogether"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("divSetKeepTogether failed", t); }
        });
    }

    private static final MethodHandle div_set_border_radius_per_corner = downcall("folio_div_set_border_radius_per_corner",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void divSetBorderRadiusPerCorner(long div, double tl, double tr, double br, double bl) {
        lockedVoid(() -> {
            try { int rc = (int) div_set_border_radius_per_corner.invokeExact(div, tl, tr, br, bl); checkResult(rc, "divSetBorderRadiusPerCorner"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("divSetBorderRadiusPerCorner failed", t); }
        });
    }

    private static final MethodHandle div_set_hcenter = downcall("folio_div_set_hcenter",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void divSetHCenter(long div, boolean enabled) {
        lockedVoid(() -> {
            try { int rc = (int) div_set_hcenter.invokeExact(div, enabled ? 1 : 0); checkResult(rc, "divSetHCenter"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("divSetHCenter failed", t); }
        });
    }

    private static final MethodHandle div_set_hright = downcall("folio_div_set_hright",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void divSetHRight(long div, boolean enabled) {
        lockedVoid(() -> {
            try { int rc = (int) div_set_hright.invokeExact(div, enabled ? 1 : 0); checkResult(rc, "divSetHRight"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("divSetHRight failed", t); }
        });
    }

    private static final MethodHandle div_set_clear = downcall("folio_div_set_clear",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void divSetClear(long div, String value) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) div_set_clear.invokeExact(div, arena.allocateFrom(value));
                checkResult(rc, "divSetClear");
            } catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("divSetClear failed", t); }
        });
    }

    private static final MethodHandle div_set_outline = downcall("folio_div_set_outline",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE));
    public static void divSetOutline(long div, double width, String style, double r, double g, double b, double offset) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) div_set_outline.invokeExact(div, width, arena.allocateFrom(style), r, g, b, offset);
                checkResult(rc, "divSetOutline");
            } catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("divSetOutline failed", t); }
        });
    }

    private static final MethodHandle div_add_box_shadow = downcall("folio_div_add_box_shadow",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void divAddBoxShadow(long div, double ox, double oy, double blur, double spread, double r, double g, double b) {
        lockedVoid(() -> {
            try { int rc = (int) div_add_box_shadow.invokeExact(div, ox, oy, blur, spread, r, g, b); checkResult(rc, "divAddBoxShadow"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("divAddBoxShadow failed", t); }
        });
    }

    // ── v0.6.2: Cell border radius ──────────────────────────────────

    private static final MethodHandle cell_set_border_radius = downcall("folio_cell_set_border_radius",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE));
    public static void cellSetBorderRadius(long cell, double radius) {
        lockedVoid(() -> {
            try { int rc = (int) cell_set_border_radius.invokeExact(cell, radius); checkResult(rc, "cellSetBorderRadius"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("cellSetBorderRadius failed", t); }
        });
    }

    private static final MethodHandle cell_set_border_radius_per_corner = downcall("folio_cell_set_border_radius_per_corner",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void cellSetBorderRadiusPerCorner(long cell, double tl, double tr, double br, double bl) {
        lockedVoid(() -> {
            try { int rc = (int) cell_set_border_radius_per_corner.invokeExact(cell, tl, tr, br, bl); checkResult(rc, "cellSetBorderRadiusPerCorner"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("cellSetBorderRadiusPerCorner failed", t); }
        });
    }

    // ── v0.6.2: Grid extensions ─────────────────────────────────────

    private static final MethodHandle grid_set_border = downcall("folio_grid_set_border",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void gridSetBorder(long grid, double width, double r, double g, double b) {
        lockedVoid(() -> {
            try { int rc = (int) grid_set_border.invokeExact(grid, width, r, g, b); checkResult(rc, "gridSetBorder"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("gridSetBorder failed", t); }
        });
    }

    private static final MethodHandle grid_set_borders = downcall("folio_grid_set_borders",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void gridSetBorders(long grid,
            double topW, double topR, double topG, double topB,
            double rightW, double rightR, double rightG, double rightB,
            double bottomW, double bottomR, double bottomG, double bottomB,
            double leftW, double leftR, double leftG, double leftB) {
        lockedVoid(() -> {
            try {
                int rc = (int) grid_set_borders.invokeExact(grid,
                    topW, topR, topG, topB,
                    rightW, rightR, rightG, rightB,
                    bottomW, bottomR, bottomG, bottomB,
                    leftW, leftR, leftG, leftB);
                checkResult(rc, "gridSetBorders");
            } catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("gridSetBorders failed", t); }
        });
    }

    private static final MethodHandle grid_set_template_areas = downcall("folio_grid_set_template_areas",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static void gridSetTemplateAreas(long grid, String[] rows, int[] cols) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                MemorySegment rowsArr = arena.allocate(ValueLayout.ADDRESS, rows.length);
                for (int i = 0; i < rows.length; i++) {
                    rowsArr.setAtIndex(ValueLayout.ADDRESS, i, arena.allocateFrom(rows[i]));
                }
                MemorySegment colsArr = arena.allocate(ValueLayout.JAVA_INT, cols.length);
                for (int i = 0; i < cols.length; i++) {
                    colsArr.setAtIndex(ValueLayout.JAVA_INT, i, cols[i]);
                }
                int rc = (int) grid_set_template_areas.invokeExact(grid, rowsArr, colsArr, rows.length);
                checkResult(rc, "gridSetTemplateAreas");
            } catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("gridSetTemplateAreas failed", t); }
        });
    }

    // ── v0.6.2: Flex extensions ─────────────────────────────────────

    private static final MethodHandle flex_set_align_content = downcall("folio_flex_set_align_content",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void flexSetAlignContent(long flex, int align) {
        lockedVoid(() -> {
            try { int rc = (int) flex_set_align_content.invokeExact(flex, align); checkResult(rc, "flexSetAlignContent"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("flexSetAlignContent failed", t); }
        });
    }

    private static final MethodHandle flex_set_borders = downcall("folio_flex_set_borders",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void flexSetBorders(long flex,
            double topW, double topR, double topG, double topB,
            double rightW, double rightR, double rightG, double rightB,
            double bottomW, double bottomR, double bottomG, double bottomB,
            double leftW, double leftR, double leftG, double leftB) {
        lockedVoid(() -> {
            try {
                int rc = (int) flex_set_borders.invokeExact(flex,
                    topW, topR, topG, topB,
                    rightW, rightR, rightG, rightB,
                    bottomW, bottomR, bottomG, bottomB,
                    leftW, leftR, leftG, leftB);
                checkResult(rc, "flexSetBorders");
            } catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("flexSetBorders failed", t); }
        });
    }

    // ── v0.6.2: Paragraph ───────────────────────────────────────────

    private static final MethodHandle paragraph_set_text_align_last = downcall("folio_paragraph_set_text_align_last",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void paragraphSetTextAlignLast(long para, int align) {
        lockedVoid(() -> {
            try { int rc = (int) paragraph_set_text_align_last.invokeExact(para, align); checkResult(rc, "paragraphSetTextAlignLast"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("paragraphSetTextAlignLast failed", t); }
        });
    }

    // ── v0.6.2: Image element ───────────────────────────────────────

    private static final MethodHandle image_element_set_object_fit = downcall("folio_image_element_set_object_fit",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void imageElementSetObjectFit(long elem, String fit) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) image_element_set_object_fit.invokeExact(elem, arena.allocateFrom(fit));
                checkResult(rc, "imageElementSetObjectFit");
            } catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("imageElementSetObjectFit failed", t); }
        });
    }

    private static final MethodHandle image_element_set_object_position = downcall("folio_image_element_set_object_position",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    public static void imageElementSetObjectPosition(long elem, String pos) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) image_element_set_object_position.invokeExact(elem, arena.allocateFrom(pos));
                checkResult(rc, "imageElementSetObjectPosition");
            } catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("imageElementSetObjectPosition failed", t); }
        });
    }

    // ── v0.6.2: RunList highlight ───────────────────────────────────

    private static final MethodHandle run_list_last_set_background_color = downcall("folio_run_list_last_set_background_color",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void runListLastSetBackgroundColor(long rl, double r, double g, double b) {
        lockedVoid(() -> {
            try { int rc = (int) run_list_last_set_background_color.invokeExact(rl, r, g, b); checkResult(rc, "runListLastSetBackgroundColor"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("runListLastSetBackgroundColor failed", t); }
        });
    }

    // ── v0.6.2: PKCS#12 signer ──────────────────────────────────────

    private static final MethodHandle signer_new_pkcs12 = downcall("folio_signer_new_pkcs12",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    public static long signerNewPkcs12(byte[] data, String password) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                MemorySegment seg = arena.allocate(ValueLayout.JAVA_BYTE, data.length);
                MemorySegment.copy(data, 0, seg, ValueLayout.JAVA_BYTE, 0, data.length);
                return (long) signer_new_pkcs12.invokeExact(seg, data.length, arena.allocateFrom(password));
            } catch (Throwable t) { throw new FolioException("signerNewPkcs12 failed", t); }
        });
    }

    // ── v0.6.2: HTML CSS length parser ──────────────────────────────

    private static final MethodHandle html_parse_css_length = downcall("folio_html_parse_css_length",
        FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static double htmlParseCssLength(String s, double fontSize, double relativeTo) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (double) html_parse_css_length.invokeExact(arena.allocateFrom(s), fontSize, relativeTo);
            } catch (Throwable t) { throw new FolioException("htmlParseCssLength failed", t); }
        });
    }

    // ── v0.6.2: Page drawing primitives ─────────────────────────────

    private static final MethodHandle page_add_line = downcall("folio_page_add_line",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void pageAddLine(long page, double x1, double y1, double x2, double y2, double width, double r, double g, double b) {
        lockedVoid(() -> {
            try { int rc = (int) page_add_line.invokeExact(page, x1, y1, x2, y2, width, r, g, b); checkResult(rc, "pageAddLine"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("pageAddLine failed", t); }
        });
    }

    private static final MethodHandle page_add_rect = downcall("folio_page_add_rect",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void pageAddRect(long page, double x, double y, double w, double h, double strokeWidth, double r, double g, double b) {
        lockedVoid(() -> {
            try { int rc = (int) page_add_rect.invokeExact(page, x, y, w, h, strokeWidth, r, g, b); checkResult(rc, "pageAddRect"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("pageAddRect failed", t); }
        });
    }

    private static final MethodHandle page_add_rect_filled = downcall("folio_page_add_rect_filled",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void pageAddRectFilled(long page, double x, double y, double w, double h, double r, double g, double b) {
        lockedVoid(() -> {
            try { int rc = (int) page_add_rect_filled.invokeExact(page, x, y, w, h, r, g, b); checkResult(rc, "pageAddRectFilled"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("pageAddRectFilled failed", t); }
        });
    }

    // ── v0.6.2: Page import / template workflows ────────────────────

    private static final MethodHandle extract_page_import = downcall("folio_extract_page_import",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static long extractPageImport(long reader, int pageIndex) {
        return locked(() -> {
            try { return (long) extract_page_import.invokeExact(reader, pageIndex); }
            catch (Throwable t) { throw new FolioException("extractPageImport failed", t); }
        });
    }

    private static final MethodHandle page_import_free = downcall("folio_page_import_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void pageImportFree(long imp) {
        lockedVoid(() -> {
            try { page_import_free.invokeExact(imp); }
            catch (Throwable t) { throw new FolioException("pageImportFree failed", t); }
        });
    }

    private static final MethodHandle page_import_width = downcall("folio_page_import_width",
        FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_LONG));
    public static double pageImportWidth(long imp) {
        return locked(() -> {
            try { return (double) page_import_width.invokeExact(imp); }
            catch (Throwable t) { throw new FolioException("pageImportWidth failed", t); }
        });
    }

    private static final MethodHandle page_import_height = downcall("folio_page_import_height",
        FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_LONG));
    public static double pageImportHeight(long imp) {
        return locked(() -> {
            try { return (double) page_import_height.invokeExact(imp); }
            catch (Throwable t) { throw new FolioException("pageImportHeight failed", t); }
        });
    }

    private static final MethodHandle page_import_apply = downcall("folio_page_import_apply",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
    public static void pageImportApply(long page, long imp) {
        lockedVoid(() -> {
            try { int rc = (int) page_import_apply.invokeExact(page, imp); checkResult(rc, "pageImportApply"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("pageImportApply failed", t); }
        });
    }

    // ── v0.6.2: Redaction ───────────────────────────────────────────

    private static final MethodHandle redact_opts_new = downcall("folio_redact_opts_new",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    public static long redactOptsNew() {
        return locked(() -> {
            try { return (long) redact_opts_new.invokeExact(); }
            catch (Throwable t) { throw new FolioException("redactOptsNew failed", t); }
        });
    }

    private static final MethodHandle redact_opts_free = downcall("folio_redact_opts_free",
        FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
    public static void redactOptsFree(long opts) {
        lockedVoid(() -> {
            try { redact_opts_free.invokeExact(opts); }
            catch (Throwable t) { throw new FolioException("redactOptsFree failed", t); }
        });
    }

    private static final MethodHandle redact_opts_set_fill_color = downcall("folio_redact_opts_set_fill_color",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void redactOptsSetFillColor(long opts, double r, double g, double b) {
        lockedVoid(() -> {
            try { int rc = (int) redact_opts_set_fill_color.invokeExact(opts, r, g, b); checkResult(rc, "redactOptsSetFillColor"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("redactOptsSetFillColor failed", t); }
        });
    }

    private static final MethodHandle redact_opts_set_overlay = downcall("folio_redact_opts_set_overlay",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
    public static void redactOptsSetOverlay(long opts, String text, double fontSize, double r, double g, double b) {
        lockedVoid(() -> {
            try (var arena = Arena.ofConfined()) {
                int rc = (int) redact_opts_set_overlay.invokeExact(opts, arena.allocateFrom(text), fontSize, r, g, b);
                checkResult(rc, "redactOptsSetOverlay");
            } catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("redactOptsSetOverlay failed", t); }
        });
    }

    private static final MethodHandle redact_opts_set_strip_metadata = downcall("folio_redact_opts_set_strip_metadata",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
    public static void redactOptsSetStripMetadata(long opts, boolean strip) {
        lockedVoid(() -> {
            try { int rc = (int) redact_opts_set_strip_metadata.invokeExact(opts, strip ? 1 : 0); checkResult(rc, "redactOptsSetStripMetadata"); }
            catch (FolioException e) { throw e; }
            catch (Throwable t) { throw new FolioException("redactOptsSetStripMetadata failed", t); }
        });
    }

    private static final MethodHandle redact_text = downcall("folio_redact_text",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static long redactText(long reader, String[] targets, long opts) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                MemorySegment arr = arena.allocate(ValueLayout.ADDRESS, targets.length);
                for (int i = 0; i < targets.length; i++) {
                    arr.setAtIndex(ValueLayout.ADDRESS, i, arena.allocateFrom(targets[i]));
                }
                return (long) redact_text.invokeExact(reader, arr, targets.length, opts);
            } catch (Throwable t) { throw new FolioException("redactText failed", t); }
        });
    }

    private static final MethodHandle redact_pattern = downcall("folio_redact_pattern",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
    public static long redactPattern(long reader, String pattern, long opts) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                return (long) redact_pattern.invokeExact(reader, arena.allocateFrom(pattern), opts);
            } catch (Throwable t) { throw new FolioException("redactPattern failed", t); }
        });
    }

    private static final MethodHandle redact_regions = downcall("folio_redact_regions",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    public static long redactRegions(long reader, int[] pages, double[] x1s, double[] y1s, double[] x2s, double[] y2s, long opts) {
        return locked(() -> {
            try (var arena = Arena.ofConfined()) {
                int n = pages.length;
                MemorySegment pagesSeg = arena.allocate(ValueLayout.JAVA_INT, n);
                MemorySegment x1Seg = arena.allocate(ValueLayout.JAVA_DOUBLE, n);
                MemorySegment y1Seg = arena.allocate(ValueLayout.JAVA_DOUBLE, n);
                MemorySegment x2Seg = arena.allocate(ValueLayout.JAVA_DOUBLE, n);
                MemorySegment y2Seg = arena.allocate(ValueLayout.JAVA_DOUBLE, n);
                for (int i = 0; i < n; i++) {
                    pagesSeg.setAtIndex(ValueLayout.JAVA_INT, i, pages[i]);
                    x1Seg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, x1s[i]);
                    y1Seg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, y1s[i]);
                    x2Seg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, x2s[i]);
                    y2Seg.setAtIndex(ValueLayout.JAVA_DOUBLE, i, y2s[i]);
                }
                return (long) redact_regions.invokeExact(reader, pagesSeg, x1Seg, y1Seg, x2Seg, y2Seg, n, opts);
            } catch (Throwable t) { throw new FolioException("redactRegions failed", t); }
        });
    }

    /** Helper that extracts a buffer handle to a byte array and frees the buffer. */
    public static byte[] bufferToByteArray(long buf) {
        return locked(() -> {
            if (buf == 0) throw new FolioException("Buffer is null: " + lastError());
            try {
                int len = bufferLen(buf);
                if (len <= 0) return new byte[0];
                MemorySegment data = bufferData(buf);
                return data.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
            } finally {
                bufferFree(buf);
            }
        });
    }
}
