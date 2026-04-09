package dev.foliopdf.internal;

import java.lang.ref.Cleaner;
import java.util.function.LongConsumer;

public final class HandleRef implements AutoCloseable {

    private static final Cleaner CLEANER = Cleaner.create();

    private final long handle;
    private final Cleaner.Cleanable cleanable;
    private volatile boolean closed = false;

    public HandleRef(long handle, LongConsumer freeFunction) {
        if (handle == 0) {
            throw new IllegalArgumentException("Invalid handle: 0");
        }
        this.handle = handle;
        this.cleanable = CLEANER.register(this, new CleanAction(handle, freeFunction));
    }

    public long get() {
        if (closed) {
            throw new IllegalStateException(
                "Cannot access native handle: resource has been closed. " +
                "Use try-with-resources to ensure proper lifecycle.");
        }
        return handle;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            cleanable.clean();
        }
    }

    public boolean isClosed() {
        return closed;
    }

    private record CleanAction(long handle, LongConsumer freeFunction) implements Runnable {
        @Override
        public void run() {
            freeFunction.accept(handle);
        }
    }
}
