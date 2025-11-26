package tech.kwik.core.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

final class ListenerThreadPool {

    // Static initializers

    private static final List<QuicStreamImpl> pending = new ArrayList<>();
    private static final ThreadPoolExecutor executor;

    static {
        executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "kwik-listener");
                thread.setDaemon(true);

                return thread;
            }
        });
    }

    public static void execute(QuicStreamImpl stream, Runnable runnable) {
        if (pending.contains(stream)) {
            return;
        }

        executor.execute(() -> {
            try {
                runnable.run();
            } finally {
                pending.remove(stream);
            }
        });
    }

    // Object

    private ListenerThreadPool() {
        throw new UnsupportedOperationException("this class cannot be instantiated");
    }

}
