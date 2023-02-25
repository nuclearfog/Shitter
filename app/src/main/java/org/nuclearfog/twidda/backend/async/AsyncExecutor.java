package org.nuclearfog.twidda.backend.async;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Executor implementation used to run tasks asynchronously
 *
 * @author nuclearfog
 */
public abstract class AsyncExecutor<Parameter, Result> {

	/**
	 * maximum task count to run in the background
	 */
	private static final int N_THREAD = 2;

	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(N_THREAD);

	private Handler uiHandler = new Handler(Looper.getMainLooper());
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
	public final void execute(final Parameter parameter, AsyncCallback<Result> callback) {
		this.callback = new WeakReference<>(callback);
		Future<?> future = EXECUTOR.submit(new Runnable() {
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
		uiHandler.post(new Runnable() {
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