package com.ketloz.lolbot;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import no.stelar7.api.r4j.basic.constants.api.Platform;
import no.stelar7.api.r4j.impl.R4J;
import no.stelar7.api.r4j.pojo.lol.match.Match;
import no.stelar7.api.r4j.pojo.lol.match.MatchReference;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

public class MainActivityViewModel extends ViewModel {
    public MutableLiveData<List<Match>> matches = new MutableLiveData<>();
    public Platform platform = Platform.NA1;
    public R4J api;
    public String summoner;
}

@SuppressWarnings("unused")
class ApiManager {
    static final int DOWNLOAD_FAILED = -1;
    static final int DOWNLOAD_STARTED = 1;
    static final int DOWNLOAD_COMPLETE = 2;
    static final int TASK_COMPLETE = 4;

    private static final int KEEP_ALIVE_TIME = 1;

    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    private static final int CORE_POOL_SIZE = 8;

    private static final int MAXIMUM_POOL_SIZE = 8;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final BlockingQueue<Runnable> downloadWorkQueue;

    private final Queue<ApiRequestTask> apiRequestTaskQueue;

    private final ThreadPoolExecutor downloadThreadPool;

    private Handler handler;

    private static final ApiManager instance;

    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        instance = new ApiManager();
    }

    private ApiManager() {
        downloadWorkQueue = new LinkedBlockingQueue<>();

        apiRequestTaskQueue = new LinkedBlockingQueue<>();

        downloadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, downloadWorkQueue);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                ApiRequestTask apiTask = (ApiRequestTask) inputMessage.obj;

                MainActivity localView = apiTask.getView();

                if (localView != null) {
                    switch (inputMessage.what) {
                        case DOWNLOAD_STARTED:
                            break;
                        case DOWNLOAD_COMPLETE:
                            break;
                        case DOWNLOAD_FAILED:
                            recycleTask(apiTask);
                            break;
                        default:
                            super.handleMessage(inputMessage);
                    }
                }
            }
        };
    }

    public static ApiManager getInstance() {
        return instance;
    }

    @SuppressLint("HandlerLeak")
    public void handleState(ApiRequestTask apiRequestTask, int state) {
        switch (state) {
            case DOWNLOAD_COMPLETE:
            case DOWNLOAD_FAILED:
            case DOWNLOAD_STARTED:
                Message completeMessage = handler.obtainMessage(state, apiRequestTask);
                completeMessage.sendToTarget();
                break;
            default:
                handler.obtainMessage(state, apiRequestTask).sendToTarget();
                break;
        }

    }

    public static void cancelAll() {
        ApiRequestTask[] taskArray = new ApiRequestTask[instance.downloadWorkQueue.size()];

        instance.downloadWorkQueue.toArray(taskArray);

        int taskArraylen = taskArray.length;

        synchronized (instance) {
            for (int taskArrayIndex = 0; taskArrayIndex < taskArraylen; taskArrayIndex++) {
                Thread thread = taskArray[taskArrayIndex].thread;
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }
    }

    static public void removeDownload(ApiRequestTask downloaderTask) {
        if (downloaderTask != null) {
            synchronized (instance) {
                Thread thread = downloaderTask.getCurrentThread();

                if (null != thread)
                    thread.interrupt();
            }

            instance.downloadThreadPool.remove(downloaderTask.getHTTPDownloadRunnable());
        }
    }

    static public ApiRequestTask startDownload(MainActivity view) {
        ApiRequestTask downloadTask = instance.apiRequestTaskQueue.poll();

        if (null == downloadTask) {
            downloadTask = new ApiRequestTask<List<Match>>(() -> {
                MainActivityViewModel model = ViewModelProviders.of(view).get(MainActivityViewModel.class);
                Summoner summoner = model.api.getLoLAPI().getSummonerAPI().getSummonerByName(model.platform, model.summoner);
                List<MatchReference> matchRefs = model.api.getLoLAPI().getMatchAPI().getMatchList(model.platform, summoner.getSummonerId());
                List<Match> matches = new ArrayList<>();

                for (MatchReference ref : matchRefs) {
                    matches.add(ref.getFullMatch());
                }

                return matches;
            });
        }

        downloadTask.initializeDownloaderTask(ApiManager.instance, view);
        instance.downloadThreadPool.execute(downloadTask.getHTTPDownloadRunnable());

        // Sets the display to show that the image is queued for downloading and decoding.
        // TODO: Add something to tell user that donwload has started

        return downloadTask;
    }

    void recycleTask(ApiRequestTask downloadTask) {
        downloadTask.recycle();
        apiRequestTaskQueue.offer(downloadTask);
    }
}

class ApiRequestTask<T> implements ApiRequestRunnable.TaskRunnableDownloadMethods {
    private WeakReference<MainActivity> mainActivityRef;
    Thread thread;
    private Runnable downloadRunnable;
    private Thread currentThread;
    private static ApiManager apiManager;

    ApiRequestTask(Operation<T> operation) {
        downloadRunnable = new ApiRequestRunnable<>(this, operation);
        apiManager = ApiManager.getInstance();
    }

    void initializeDownloaderTask(
            ApiManager manager,
            MainActivity view)
    {
        apiManager = manager;
        mainActivityRef = new WeakReference<>(view);
    }

    void recycle() {
        if ( null != mainActivityRef) {
            mainActivityRef.clear();
            mainActivityRef = null;
        }
    }

    void handleState(int state) {
        apiManager.handleState(this, state);
    }

    Runnable getHTTPDownloadRunnable() {
        return downloadRunnable;
    }

    public MainActivity getView() {
        if ( null != mainActivityRef) {
            return mainActivityRef.get();
        }
        return null;
    }

    public Thread getCurrentThread() {
        synchronized(apiManager) {
            return currentThread;
        }
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

interface Operation<T> {
    T perform();
}

class ApiRequestRunnable<T> implements Runnable {
    @SuppressWarnings("unused")
    private static final String LOG_TAG = "ApiRequestRunnable";

    static final int HTTP_STATE_FAILED = -1;
    static final int HTTP_STATE_STARTED = 0;
    static final int HTTP_STATE_COMPLETED = 1;

    private final TaskRunnableDownloadMethods task;

    interface TaskRunnableDownloadMethods {

        void setCurrentThread(Thread currentThread);

        void handleDownloadState(int state);
    }

    private Operation<T> operation;

    ApiRequestRunnable(TaskRunnableDownloadMethods apiTask, Operation<T> operation) {
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
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            task.handleDownloadState(HTTP_STATE_STARTED);

            result = this.operation.perform();

            task.handleDownloadState(HTTP_STATE_COMPLETED);

        } catch (InterruptedException e1) {

        } finally {
            if (null == result) {
                task.handleDownloadState(HTTP_STATE_FAILED);
            }

            task.setCurrentThread(null);
            Thread.interrupted();
        }
    }
}
