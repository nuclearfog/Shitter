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
	private static final long P_TIMEOUT = 4L;

	/**
	 * thread pool executor
	 */
	private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(N_THREAD, N_THREAD, P_TIMEOUT, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

	/**
	 * handler used to send result back to activity/fragment
	 */
	private Handler uiHandler = new Handler(Looper.getMainLooper());

	/**
	 * contains all tasks used by an instance
	 */
	private Queue<Future<?>> futureTasks = new LinkedBlockingQueue<>();

	/**
	 * start packground task
	 *
	 * @param parameter parameter to send to the background task
	 * @param callback  result from the background task
	 */
	public final void execute(final Parameter parameter, @Nullable AsyncCallback<Result> callback) {
		final WeakReference<AsyncCallback<Result>> callbackReference = new WeakReference<>(callback);
		Future<?> future = THREAD_POOL.submit(new Runnable() {
			@Override
			public void run() {
				Result result = doInBackground(parameter);
				onPostExecute(result, callbackReference);
			}
		});
		futureTasks.add(future);
	}

	/**
	 * send signal to the tasks executed by this instance
	 */
	public final void cancel() {
		while (!futureTasks.isEmpty()) {
			Future<?> future = futureTasks.remove();
			future.cancel(true);
		}
	}

	/**
	 * check if this instance is executing a background task
	 *
	 * @return true if there aren't any tasks
	 */
	public final boolean isIdle() {
		return futureTasks.isEmpty();
	}

	/**
	 * send result to main thread
	 *
	 * @param result result of the background task
	 */
	private synchronized void onPostExecute(@Nullable final Result result, WeakReference<AsyncCallback<Result>> callbackReference) {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				if (!futureTasks.isEmpty())
					futureTasks.remove();
				AsyncCallback<Result> reference = callbackReference.get();
				if (reference != null && result != null) {
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
	@WorkerThread
	protected abstract Result doInBackground(@NonNull Parameter param);

	/**
	 * Callback used to send task result to main thread
	 */
	public interface AsyncCallback<Result> {

		/**
		 * @param result result of the task
		 */
		void onResult(@NonNull Result result);
	}
}