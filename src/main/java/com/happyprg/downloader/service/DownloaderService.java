package com.happyprg.downloader.service;

import java.net.URI;

import org.apache.commons.lang3.tuple.Pair;

import reactor.core.publisher.Mono;

public interface DownloaderService {
    String ANONYMOUS = "anonymous";
    String ANONYMOUS_PASSWORD = "";
    String USER_INFO_SEPARATOR = ":";

    Mono<Pair<URI, String>> process(URI uri);

    boolean isSupportProtocol(String protocol);

}
