package org.nuclearfog.twidda.backend.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executor for tasks running in the bnackground
 *
 * @param <Parameter>
 * @param <Result>
 */
public abstract class AsyncExecutor<Parameter, Result> {

	private ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
	private Handler uiHandler = new Handler(Looper.getMainLooper());

	private WeakReference<AsyncCallback<Result>> callback;
	private AtomicInteger processCount = new AtomicInteger(0);


	public final void execute(final Parameter parameter, AsyncCallback<Result> callback) {
		this.callback = new WeakReference<>(callback);

		singleExecutor.submit(new Runnable() {
			@Override
			public void run() {
				processCount.getAndIncrement();
				Result result = doInBackground(parameter);
				onPostExecute(result);
				processCount.getAndDecrement();
			}
		});
	}


	public final void kill() {
		singleExecutor.shutdown();
		processCount.set(0);
	}


	public final boolean idle() {
		return processCount.get() == 0;
	}


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


	@NonNull
	protected abstract Result doInBackground(Parameter request);


	public interface AsyncCallback<Result> {

		void onResult(Result res);
	}
}