package com.happyprg.downloader.service.sftp;

import static reactor.core.publisher.Mono.just;

import java.io.File;
import java.net.URI;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import com.happyprg.downloader.service.DownloaderInfo;
import com.happyprg.downloader.service.DownloaderService;
import com.happyprg.downloader.util.DownloaderFileService;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DownloaderSFTPService implements DownloaderService {

    private static final String SUPPORT_PROTOCOL = "sftp";
    private final DownloaderSFTPConfig downloaderSFTPConfig;
    private final DownloaderSFTPClient downloaderSFTPClient;
    private final DownloaderFileService downloaderFileService;

    public DownloaderSFTPService(DownloaderSFTPConfig downloaderSFTPConfig,
                                 DownloaderSFTPClient downloaderSFTPClient,
                                 DownloaderFileService downloaderFileService) {
        this.downloaderSFTPConfig = downloaderSFTPConfig;
        this.downloaderSFTPClient = downloaderSFTPClient;
        this.downloaderFileService = downloaderFileService;
    }

    @Override
    public boolean isSupportProtocol(String protocol) {
        return SUPPORT_PROTOCOL.equals(protocol);
    }

    @Override
    public Mono<Pair<URI, String>> process(URI uri) {
        return just(DownloaderInfo.builder().uri(uri)
                                  .localDiskRootPath(downloaderSFTPConfig.getLocalDiskRootPath())
                                  .build()).flatMap(
                downloaderInfo -> {
                    ChannelSftp channel = null;
                    try {
                        Session session = downloaderSFTPClient.connect(uri);
                        channel = downloaderSFTPClient.openSftp(session);

                        downloaderFileService.prepareForPersist(downloaderInfo);

                        downloaderSFTPClient.download(channel, uri.getPath(),
                                                      downloaderInfo.getTempLocalFileFullPath());

                        downloaderFileService.deleteQuietly(new File(downloaderInfo.getLocalFullPath()));

                        downloaderFileService.moveTempFileToFinalLocation(
                                downloaderInfo.getTempLocalFileFullPath(),
                                downloaderInfo
                                        .getLocalFileDirectory());
                        return just(Pair.of(uri, downloaderInfo.getLocalFullPath()));
                    } catch (Exception e) {
                        downloaderFileService.deleteIncompleteParentFile(downloaderInfo, e);
                        return Mono.error(e);
                    } finally {
                        downloaderSFTPClient.afterProcess(channel);
                    }
                });

    }

}
