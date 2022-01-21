package org.nuclearfog.twidda.backend;

import org.nuclearfog.twidda.activities.VideoViewer;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class updates {@link VideoViewer}'s Seekbar while playing a video
 *
 * @author nuclearfog
 */
public class SeekbarUpdater implements Runnable {

    private ScheduledExecutorService updater;
    private WeakReference<VideoViewer> callback;

    private Runnable seekUpdate = new Runnable() {
        public void run() {
            VideoViewer mediaViewer = callback.get();
            if (mediaViewer != null) {
                mediaViewer.updateSeekBar();
            }
        }
    };


    public SeekbarUpdater(VideoViewer callback, int milliseconds) {
        this.callback = new WeakReference<>(callback);
        updater = Executors.newScheduledThreadPool(1);
        updater.scheduleWithFixedDelay(this, milliseconds, milliseconds, TimeUnit.MILLISECONDS);
    }


    @Override
    public void run() {
        VideoViewer mediaViewer = callback.get();
        if (mediaViewer != null) {
            mediaViewer.runOnUiThread(seekUpdate);
        }
    }

    /**
     * shutdown updater
     */
    public void shutdown() {
        updater.shutdown();
    }
}