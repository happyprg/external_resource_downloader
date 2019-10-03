package com.happyprg.downloader.service.http;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

import java.io.File;
import java.net.URI;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.happyprg.downloader.service.DownloaderInfo;
import com.happyprg.downloader.service.DownloaderService;
import com.happyprg.downloader.util.DownloaderFileService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DownloaderHTTPService implements DownloaderService {

    private static final Set<String> SUPPORT_PROTOCOL = Sets.newHashSet("http", "https");
    private final DownloaderHTTPConfig downloaderHTTPConfig;
    private final DownloaderFileService downloaderFileService;

    public DownloaderHTTPService(DownloaderHTTPConfig downloaderHTTPConfig,
                                 DownloaderFileService downloaderFileService) {
        this.downloaderHTTPConfig = downloaderHTTPConfig;
        this.downloaderFileService = downloaderFileService;
    }

    @Override
    public boolean isSupportProtocol(String protocol) {
        return SUPPORT_PROTOCOL.contains(protocol);
    }

    @Override
    public Mono<Pair<URI, String>> process(URI uri) {

        return just(DownloaderInfo.builder().uri(uri)
                                  .localDiskRootPath(downloaderHTTPConfig.getLocalDiskRootPath())
                                  .build()).flatMap(downloaderInfo -> {
            try {
                downloaderFileService.prepareForPersist(downloaderInfo);

                downloaderFileService.copyURLToFile(uri.toURL(),
                                                    new File(downloaderInfo.getTempLocalFileFullPath()),
                                                    downloaderHTTPConfig.getConnectionTimeout(),
                                                    downloaderHTTPConfig.getReadTimeOut());
                downloaderFileService.deleteQuietly(new File(downloaderInfo.getLocalFullPath()));

                downloaderFileService.moveTempFileToFinalLocation(downloaderInfo.getTempLocalFileFullPath(),
                                                                  downloaderInfo.getLocalFileDirectory());
                return just(Pair.of(uri, downloaderInfo.getLocalFullPath()));
            } catch (Exception e) {
                downloaderFileService.deleteIncompleteParentFile(downloaderInfo, e);
                return error(e);
            }
        });
    }
}
