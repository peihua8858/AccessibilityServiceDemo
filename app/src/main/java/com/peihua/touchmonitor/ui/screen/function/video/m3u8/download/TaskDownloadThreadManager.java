package com.peihua.touchmonitor.ui.screen.function.video.m3u8.download;

import com.peihua.touchmonitor.model.DownloadTask;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务下载线程管理器
 *
 * @author cloudgyb
 * @since 2.0.0
 */
public class TaskDownloadThreadManager {
    private static final TaskDownloadThreadManager instance = new TaskDownloadThreadManager();
    private final static ConcurrentHashMap<Long, Thread> downloadTaskThread = new ConcurrentHashMap<>();

    public static TaskDownloadThreadManager getInstance() {
        return instance;
    }

    public void startDownloadThread(DownloadTask task) {
        Long id = task.getId();
        Thread oldThread = downloadTaskThread.get(id);
        if (oldThread != null) {
            // 如果老的线程已经终止，创建一个新的
            if (oldThread.getState() == Thread.State.TERMINATED) {
                createDownloadThread(task);
                startDownloadThread(task);
            } else if (oldThread.getState() == Thread.State.NEW) {
                oldThread.start();
            }
        } else {
            createDownloadThread(task);
            startDownloadThread(task);
        }
    }

    public void stopDownloadThread(DownloadTask task) {
        Thread thread = downloadTaskThread.get(task.getId());
        if (thread == null) {
            return;
        }
        if (thread instanceof TaskDownloadThread) {
            TaskDownloadThread downloadThread = (TaskDownloadThread) thread;
            downloadThread.stopDownload();
            downloadTaskThread.remove(task.getId());
        }
    }

    public void createDownloadThread(DownloadTask task) {
        downloadTaskThread.put(task.getId(), new TaskDownloadThread(task));
    }
}
