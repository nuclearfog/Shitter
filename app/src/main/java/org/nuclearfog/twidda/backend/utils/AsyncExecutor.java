package org.nuclearfog.twidda.backend.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executor for tasks running in the bnackground
 *
 * @author nuclearfog
 */
public abstract class AsyncExecutor<Parameter, Result> {

	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

	private Handler uiHandler = new Handler(Looper.getMainLooper());

	private WeakReference<AsyncCallback<Result>> callback;

	private AtomicBoolean idle = new AtomicBoolean(true);
	private AtomicBoolean cancel = new AtomicBoolean(false);


	/**
	 * start packground task
	 *
	 * @param parameter parameter to send to the background task
	 * @param callback  result from the background task
	 */
	public final void execute(final Parameter parameter, AsyncCallback<Result> callback) {
		this.callback = new WeakReference<>(callback);

		EXECUTOR.submit(new Runnable() {
			@Override
			public void run() {
				idle.set(false);
				Result result = doInBackground(parameter);
				onPostExecute(result);
				idle.set(true);
			}
		});
	}

	/**
	 * stop running and scheduled tasks
	 */
	public final void cancel() {
		cancel.set(true);
	}

	/**
	 * check if there aren't any active tasks
	 *
	 * @return true if there aren't any tasks
	 */
	public final boolean isIdle() {
		return idle.get();
	}

	/**
	 * check if current instance's thread is cancelled
	 *
	 * @return true if the thread of the current instance is cancelled
	 */
	public boolean isCancelled() {
		return cancel.get();
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
	protected abstract Result doInBackground(Parameter param);

	/**
	 * Callback used to send task result to main thread
	 */
	public interface AsyncCallback<Result> {

		/**
		 *
		 * @param result result of the task
		 */
		void onResult(Result result);
	}
}