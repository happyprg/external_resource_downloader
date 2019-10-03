package com.happyprg.downloader.service.http;

import static org.apache.commons.io.FileUtils.getTempDirectory;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.happyprg.downloader.service.DownloaderInfo;
import com.happyprg.downloader.util.DownloaderFileService;

import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class DownloaderHTTPServiceTest {

    @Mock
    DownloaderHTTPConfig downloaderHTTPConfig;

    @Mock
    DownloaderFileService downloaderFileService;

    @InjectMocks
    DownloaderHTTPService downloaderHTTPService;
    private static final URI EXPECTED_URI = URI.create(
            "https://file-examples.com/wp-content/uploads/2018/04/file_example_AVI_1280_1_5MG.avi");

    @Before
    public void before() {
        downloaderHTTPService = new DownloaderHTTPService(downloaderHTTPConfig, downloaderFileService);
        when(downloaderHTTPConfig.getLocalDiskRootPath()).thenReturn(getTempDirectory().getPath());
    }

    @Test
    public void isSupportProtocol() {
        assertTrue(downloaderHTTPService.isSupportProtocol("http"));
        assertTrue(downloaderHTTPService.isSupportProtocol("https"));
    }

    @Test
    public void process() throws IOException {
        String expectedFilenameExtension = FilenameUtils.getExtension(EXPECTED_URI.getPath());
        StepVerifier.create(downloaderHTTPService.process(EXPECTED_URI))
                    .expectNextMatches(item -> item.getLeft().equals(EXPECTED_URI) && item.getRight().endsWith(
                            expectedFilenameExtension))
                    .verifyComplete();

        verify(downloaderFileService).prepareForPersist(any(DownloaderInfo.class));
        verify(downloaderFileService).copyURLToFile(any(URL.class), any(File.class), anyInt(), anyInt());
        verify(downloaderFileService).deleteQuietly(any(File.class));
        verify(downloaderFileService).moveTempFileToFinalLocation(anyString(),
                                                                  anyString());
    }

    @Test
    public void process_error() throws IOException {
        Class<IOException> expectedError = IOException.class;
        doThrow(expectedError).when(downloaderFileService).prepareForPersist(any(DownloaderInfo.class));

        StepVerifier.create(downloaderHTTPService.process(EXPECTED_URI))
                    .verifyError(expectedError);

    }
}