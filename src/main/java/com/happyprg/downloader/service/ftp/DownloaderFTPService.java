package com.happyprg.downloader.service.ftp;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;

import com.happyprg.downloader.service.DownloaderInfo;
import com.happyprg.downloader.service.DownloaderService;
import com.happyprg.downloader.util.DownloaderFileService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DownloaderFTPService implements DownloaderService {

    private static final String SUPPORT_PROTOCOL = "ftp";
    private final DownloaderFTPConfig downloaderFTPConfig;
    private final DownloaderFileService downloaderFileService;
    private final DownloaderFTPClient downloaderFtpClient;

    public DownloaderFTPService(DownloaderFTPConfig downloaderFTPConfig,
                                DownloaderFileService downloaderFileService,
                                DownloaderFTPClient downloaderFtpClient) {
        this.downloaderFTPConfig = downloaderFTPConfig;
        this.downloaderFileService = downloaderFileService;
        this.downloaderFtpClient = downloaderFtpClient;
    }

    @Override
    public boolean isSupportProtocol(String protocol) {
        return SUPPORT_PROTOCOL.equals(protocol);
    }

    @Override
    public Mono<Pair<URI, String>> process(URI uri) {
        return just(uri).map(item -> DownloaderInfo.builder()
                                                   .uri(item)
                                                   .localDiskRootPath(downloaderFTPConfig
                                                                              .getLocalDiskRootPath())
                                                   .build()).flatMap(
                downloaderInfo -> {
                    FTPClient ftpClient = null;
                    try {
                        downloaderFileService.prepareForPersist(downloaderInfo);

                        ftpClient = downloaderFtpClient.connect(uri);

                        downloaderFtpClient.download(ftpClient, downloaderInfo.getUri().getPath()
                                , downloaderInfo.getTempLocalFileFullPath());

                        downloaderFileService.deleteQuietly(new File(downloaderInfo.getLocalFullPath()));

                        downloaderFileService.moveTempFileToFinalLocation(
                                downloaderInfo.getTempLocalFileFullPath(),
                                downloaderInfo
                                        .getLocalFileDirectory());

                        return just(Pair.of(uri, downloaderInfo.getLocalFullPath()));
                    } catch (IOException e) {
                        downloaderFileService.deleteIncompleteParentFile(downloaderInfo, e);
                        return error(e);
                    } finally {
                        downloaderFtpClient.afterProcess(ftpClient);
                    }
                });
    }

}
