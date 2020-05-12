package com.ketloz.lolbot;

import android.util.Log;

public class ApiRequestRunnable<T> implements Runnable {
    @SuppressWarnings("unused")
    private static final String LOG_TAG = "ApiRequestRunnable";

    static final int HTTP_STATE_FAILED = -1;
    static final int HTTP_STATE_STARTED = 0;
    static final int HTTP_STATE_COMPLETED = 1;

    private final ApiRequestTask task;

    public interface TaskRunnableDownloadMethods {
        void setCurrentThread(Thread currentThread);
        void handleDownloadState(int state);
    }

    private Operation<T> operation;

    ApiRequestRunnable(ApiRequestTask<T> apiTask, Operation<T> operation) {
        task = apiTask;
        this.operation = operation;
    }

    @SuppressWarnings("resource")
    @Override
    public void run() {
        task.setCurrentThread(Thread.currentThread());
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        T result = null;

        try {
            task.handleDownloadState(HTTP_STATE_STARTED);

            result = this.operation.perform(task.getViewModel());

            if (result == null) throw new Exception("Issue with operation");

            task.handleDownloadState(HTTP_STATE_COMPLETED);

        } catch (Throwable e) {
            // Logs an error
            Log.e(LOG_TAG, "Some error has occurred" + e.getMessage());

            /*
             * Tells the system that garbage collection is
             * necessary. Notice that collection may or may not
             * occur.
             */
            java.lang.System.gc();

            if (Thread.interrupted()) {
                return;
            }
            /*
             * Tries to pause the thread for 250 milliseconds,
             * and catches an Exception if something tries to
             * activate the thread before it wakes up.
             */
            try {
                Thread.sleep(250);
            } catch (java.lang.InterruptedException interruptException) {
                return;
            }
        } finally {
            if (null == result) {
                task.handleDownloadState(HTTP_STATE_FAILED);
            } else {
                task.result = result;
            }

            task.setCurrentThread(null);
            Thread.interrupted();
        }
    }
}
