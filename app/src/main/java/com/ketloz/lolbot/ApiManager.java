package com.ketloz.lolbot;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import no.stelar7.api.r4j.pojo.lol.match.Match;
import no.stelar7.api.r4j.pojo.lol.match.MatchReference;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

@SuppressWarnings("unused")
public class ApiManager {
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
                Context context = localView.getApplicationContext();
                switch (inputMessage.what) {
                    case DOWNLOAD_STARTED:
                        break;
                    case DOWNLOAD_COMPLETE:
                        recycleTask(apiTask);
                        break;
                    case DOWNLOAD_FAILED:
                        recycleTask(apiTask);
                        break;
                    default:
                        super.handleMessage(inputMessage);
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
                Thread thread = taskArray[taskArrayIndex].getCurrentThread();
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

    static public <T> void startDownload(Operation<T> operation, MainActivity view, MainActivityViewModel model) {
        ApiRequestTask task = new ApiRequestTask<>(operation);

        task.initializeDownloaderTask(ApiManager.instance, view, model);
        instance.downloadThreadPool.execute(task.getHTTPDownloadRunnable());
    }

    void recycleTask(ApiRequestTask downloadTask) {
        downloadTask.recycle();
        apiRequestTaskQueue.offer(downloadTask);
    }
}