package com.ketloz.lolbot;

import android.support.v4.app.FragmentActivity;

import java.lang.ref.WeakReference;

public class ApiRequestTask<T> implements ApiRequestRunnable.TaskRunnableDownloadMethods {
    private WeakReference<MainActivity> activityRef;
    private Runnable downloadRunnable;
    private Thread currentThread;
    private static ApiManager apiManager;
    private MainActivityViewModel viewModel;

    public T result = null;

    ApiRequestTask(Operation<T> operation) {
        downloadRunnable = new ApiRequestRunnable<>(this, operation);
        apiManager = ApiManager.getInstance();
    }

    void initializeDownloaderTask(
            ApiManager manager,
            MainActivity view,
            MainActivityViewModel model)
    {
        viewModel = model;
        apiManager = manager;
        activityRef = new WeakReference<>(view);
    }

    void recycle() {
        if ( null != activityRef) {
            activityRef.clear();
            activityRef = null;
        }

        viewModel = null;
        result = null;
    }

    void handleState(int state) {
        apiManager.handleState(this, state);
    }

    Runnable getHTTPDownloadRunnable() {
        return downloadRunnable;
    }

    public MainActivity getView() {
        if ( null != activityRef) {
            return activityRef.get();
        }
        return null;
    }

    public Thread getCurrentThread() {
        synchronized(apiManager) {
            return currentThread;
        }
    }

    public MainActivityViewModel getViewModel() {
        return viewModel;
    }

    public void setCurrentThread(Thread thread) {
        synchronized(apiManager) {
            currentThread = thread;
        }
    }

    @Override
    public void handleDownloadState(int state) {
        int outState;

        switch(state) {
            case ApiRequestRunnable.HTTP_STATE_COMPLETED:
                outState = ApiManager.DOWNLOAD_COMPLETE;
                break;
            case ApiRequestRunnable.HTTP_STATE_FAILED:
                outState = ApiManager.DOWNLOAD_FAILED;
                break;
            default:
                outState = ApiManager.DOWNLOAD_STARTED;
                break;
        }

        handleState(outState);
    }
}