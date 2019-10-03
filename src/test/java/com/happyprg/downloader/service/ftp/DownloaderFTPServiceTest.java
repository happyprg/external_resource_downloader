package com.happyprg.downloader.service.ftp;

import static java.net.URI.create;
import static org.apache.commons.io.FileUtils.getTempDirectory;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;
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
public class DownloaderFTPServiceTest {

    private static final URI EXPECTED_URI = create(
            "ftp://demo:password@test.rebex.net/pub/example/ConsoleClient.png");
    @Mock
    DownloaderFTPConfig downloaderFTPConfig;
    @Mock
    DownloaderFileService downloaderFileService;
    @Mock
    DownloaderFTPClient downloderFTPClient;
    @InjectMocks
    DownloaderFTPService downloaderFTPService;

    @Before
    public void before() {
        downloaderFTPService = new DownloaderFTPService(downloaderFTPConfig, downloaderFileService,
                                                        downloderFTPClient);
        when(downloaderFTPConfig.getLocalDiskRootPath()).thenReturn(getTempDirectory().getPath());
    }

    @Test
    public void isSupportProtocol() {
        assertTrue(downloaderFTPService.isSupportProtocol("ftp"));
    }

    @Test
    public void process() throws IOException {
        String expectedFilenameExtension = FilenameUtils.getExtension(EXPECTED_URI.getPath());
        FTPClient expectFTPClient = new FTPClient();
        when(downloderFTPClient.connect(EXPECTED_URI)).thenReturn(expectFTPClient);
        StepVerifier.create(downloaderFTPService.process(EXPECTED_URI))
                    .expectNextMatches(item -> item.getLeft().equals(EXPECTED_URI) && item.getRight().endsWith(
                            expectedFilenameExtension))
                    .verifyComplete();

        verify(downloaderFileService).prepareForPersist(any(DownloaderInfo.class));
        verify(downloderFTPClient).download(any(FTPClient.class), anyString(), anyString());
        verify(downloaderFileService).deleteQuietly(any(File.class));
        verify(downloaderFileService).moveTempFileToFinalLocation(anyString(), anyString());
        verify(downloderFTPClient).afterProcess(any(FTPClient.class));
    }

    @Test
    public void process_error() throws IOException {
        Class<IOException> expectExceptionClass = IOException.class;
        when(downloderFTPClient.connect(EXPECTED_URI)).thenThrow(expectExceptionClass);
        StepVerifier.create(downloaderFTPService.process(EXPECTED_URI))
                    .verifyError(expectExceptionClass);
        verify(downloderFTPClient).afterProcess(null);
    }

}