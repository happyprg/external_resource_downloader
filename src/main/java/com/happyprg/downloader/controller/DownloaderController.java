package com.happyprg.downloader.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Flux.fromIterable;
import static reactor.core.publisher.Mono.just;
import static reactor.core.scheduler.Schedulers.parallel;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happyprg.downloader.service.DownloaderService;

import lombok.extern.java.Log;
import reactor.core.publisher.ParallelFlux;

@RestController
@Log
public class DownloaderController {
    private final List<DownloaderService> downloaderServices;

    public DownloaderController(List<DownloaderService> downloaderServices) {
        this.downloaderServices = downloaderServices;
    }

    @GetMapping
    public ParallelFlux<DownloaderResponse> download(
            @RequestParam() Set<String> uris) {
        return fromIterable(uris)
                .parallel()
                .runOn(parallel())
                .flatMap(item ->
                         {
                             URI uri = URI.create(item);
                             return just(Pair.of(downloaderServices.stream()
                                                                   .filter(service -> service
                                                                           .isSupportProtocol(
                                                                                   uri.getScheme())).findFirst()
                                                                   .orElseThrow(IllegalAccessError::new), uri));
                         }
                ).flatMap(serviceURIPair ->
                                  serviceURIPair.getLeft()
                                                .process(serviceURIPair.getRight())
                                                .flatMap(uriStringPair -> just(
                                                        new DownloaderResponse()
                                                                .setCode(OK.value())
                                                                .setUri(uriStringPair.getLeft().toString())
                                                                .setLocalFileFullPath(
                                                                        uriStringPair.getRight())))
                                                .onErrorResume(throwable -> {
                                                    return just(
                                                            new DownloaderResponse()
                                                                    .setErrorMsg(throwable.toString())
                                                                    .setCode(INTERNAL_SERVER_ERROR.value()));

                                                }));
    }
}
