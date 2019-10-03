package com.happyprg.downloader.controller;

import static com.google.common.collect.Sets.intersection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.apache.commons.lang3.StringUtils.join;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ListBodySpec;

import com.google.common.collect.ImmutableSet;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@AutoConfigureWebTestClient(timeout = "10000")//10 seconds
public class DownloaderControllerTest {

    private final static String NOT_FOUND_URI = "ftp://speedtest.tele2.net/NOT_FOUND.zip";
    private final static Set<String> EXPECTED_URIS = ImmutableSet.of(
            "https://file-examples.com/wp-content/uploads/2017/10/file_example_JPG_100kB.jpg",
            "ftp://speedtest.tele2.net/1KB.zip",
            NOT_FOUND_URI,
            "sftp://demo:password@test.rebex.net:22/pub/example/WinFormClient.png",
            "http://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_700KB.mp3");
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void download() {
        ListBodySpec<DownloaderResponse> actual = webTestClient.get()
                                                               .uri(uriBuilder -> uriBuilder.path("/download")
                                                                                            .queryParam("uris",
                                                                                                        join(EXPECTED_URIS
                                                                                                                     .toArray(),
                                                                                                             ","))
                                                                                            .build()).exchange()
                                                               .expectStatus().isOk()
                                                               .expectHeader().contentType(
                        APPLICATION_JSON_UTF8)
                                                               .expectBodyList(DownloaderResponse.class);
        assertEquals(EXPECTED_URIS.size(), actual.returnResult().getResponseBody().size());
        Set<String> succeedUris = actual.returnResult().getResponseBody().stream()
                                        .filter(item -> item.getCode() == OK.value()).map(
                        DownloaderResponse::getUri)
                                        .collect(toSet());
        assertEquals(4, intersection(succeedUris, EXPECTED_URIS).size());
        assertTrue(!succeedUris.contains(NOT_FOUND_URI));
        assertEquals(0, (int) actual.returnResult().getResponseBody().stream()
                                    .filter(item -> item.getCode() == OK.value()).map(
                        DownloaderResponse::getLocalFileFullPath)
                                    .collect(toList()).parallelStream()
                                    .filter(item -> !FileUtils.getFile(item).exists()).count());
    }
}