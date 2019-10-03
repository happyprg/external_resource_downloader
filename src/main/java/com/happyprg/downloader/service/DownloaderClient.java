package com.happyprg.downloader.service;

public interface DownloaderClient<T> {

    void download(T client, String downloadPath, String tempLocalFullPath) throws Exception;

    void afterProcess(T client);

}
