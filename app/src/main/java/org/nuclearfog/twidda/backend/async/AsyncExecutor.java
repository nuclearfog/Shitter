package org.nuclearfog.twidda.backend.async;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executor implementation used to run tasks asynchronously
 *
 * @author nuclearfog
 */
public abstract class AsyncExecutor<Parameter, Result> {

	/**
	 * maximum task count to run in the background
	 */
	private static final int N_THREAD = 4;

	/**
	 * timeout for queued processes
	 */
	private static final long P_TIMEOUT = 5000L;

	/**
	 * thread pool executor
	 */
	private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(1, N_THREAD, P_TIMEOUT, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

	/**
	 * handler used to send result back to activity/fragment
	 */
	private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

	/**
	 * callback to activity/fragment
	 */
	private WeakReference<AsyncCallback<Result>> callback;

	/**
	 * contains all tasks used by an instance
	 */
	private Queue<Future<?>> queue = new LinkedBlockingQueue<>();

	/**
	 * start packground task
	 *
	 * @param parameter parameter to send to the background task
	 * @param callback  result from the background task
	 */
	public final void execute(final Parameter parameter, @Nullable AsyncCallback<Result> callback) {
		this.callback = new WeakReference<>(callback);
		Future<?> future = THREAD_POOL.submit(new Runnable() {
			@Override
			public void run() {
				Result result = doInBackground(parameter);
				onPostExecute(result);
			}
		});
		queue.add(future);
	}

	/**
	 * send signal to the tasks executed by this instance
	 */
	public final void cancel() {
		while (!queue.isEmpty()) {
			Future<?> future = queue.remove();
			future.cancel(true);
		}
	}

	/**
	 * check if this instance is executing a background task
	 *
	 * @return true if there aren't any tasks
	 */
	public final boolean isIdle() {
		return queue.isEmpty();
	}

	/**
	 * send result to main thread
	 *
	 * @param result result of the background task
	 */
	private void onPostExecute(final Result result) {
		UI_HANDLER.post(new Runnable() {
			@Override
			public void run() {
				if (!queue.isEmpty())
					queue.remove();
				AsyncCallback<Result> reference = callback.get();
				if (reference != null) {
					reference.onResult(result);
				}
			}
		});
	}

	/**
	 * This method is called in a background thread
	 *
	 * @param param parameter containing information for the background task
	 * @return result of the background task
	 */
	@NonNull
	@WorkerThread
	protected abstract Result doInBackground(@NonNull Parameter param);

	/**
	 * Callback used to send task result to main thread
	 */
	public interface AsyncCallback<Result> {

		/**
		 * @param result result of the task
		 */
		void onResult(Result result);
	}
}