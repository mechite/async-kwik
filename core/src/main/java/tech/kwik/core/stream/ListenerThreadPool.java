package tech.kwik.core.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;

final class ListenerThreadPool implements AutoCloseable {

    private final List<QuicStreamImpl> pending;
    private final ExecutorService executor;

    ListenerThreadPool() {
        this.pending = new ArrayList<>();
        this.executor = new ThreadPoolExecutor(1, 1, 1,
                SECONDS, new LinkedBlockingQueue<>(), runnable -> {
			Thread thread = new Thread(runnable, "kwik-listener");
			thread.setDaemon(true);

			return thread;
		});
    }

    void execute(QuicStreamImpl stream, Runnable runnable) {
        if (pending.contains(stream)) return;
        executor.execute(() -> {
            try {
                runnable.run();
            } finally {
                pending.remove(stream);
            }
        });
    }
    
    /**
     * Implementation of {@link AutoCloseable#close()} that performs an
     * orderly shutdown of {@link #executor}.
     *
     * @implNote This is a clone of OpenJDK 19+ default close method
     * available directly on the newer {@code ExecutorService} interface.
     */
    @Override
    public void close() {
        boolean terminated = this.executor.isTerminated();
		if (terminated) return;
  
		this.executor.shutdown();
		boolean interrupted = false;
		while (!terminated) {
			try {
				terminated = this.executor.awaitTermination(1L, DAYS);
			} catch (InterruptedException e) {
				if (interrupted) continue;
				this.executor.shutdownNow();
				interrupted = true;
			}
		}
		if (!interrupted) return;
		currentThread().interrupt();
	}
}
