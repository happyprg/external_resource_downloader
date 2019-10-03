package com.happyprg.downloader.service.sftp;

import static java.net.URI.create;
import static org.apache.commons.io.FileUtils.getTempDirectory;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.happyprg.downloader.service.DownloaderInfo;
import com.happyprg.downloader.util.DownloaderFileService;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class DownloaderSFTPServiceTest {

    private static final URI EXPECTED_URI = create(
            "sftp://demo:password@test.rebex.net:22/pub/example/WinFormClient.png");
    @Mock
    DownloaderSFTPConfig downloaderSFTPConfig;
    @Mock
    DownloaderSFTPClient downloaderSFTPClient;
    @InjectMocks
    DownloaderSFTPService downloaderSFTPService;
    @Mock
    private DownloaderFileService downloaderFileService;

    @Before
    public void before() {
        downloaderSFTPService = new DownloaderSFTPService(downloaderSFTPConfig, downloaderSFTPClient,
                                                          downloaderFileService);
        when(downloaderSFTPConfig.getLocalDiskRootPath()).thenReturn(getTempDirectory().getPath());
    }

    @Test
    public void isSupportProtocol() {
        assertTrue(downloaderSFTPService.isSupportProtocol("sftp"));
    }

    @Test
    public void process() throws IOException, SftpException, JSchException {
        String expectedFilenameExtension = FilenameUtils.getExtension(EXPECTED_URI.getPath());
        Session mockSession = mock(Session.class);
        when(downloaderSFTPClient.connect(EXPECTED_URI)).thenReturn(mockSession);
        ChannelSftp mockChannel = mock(ChannelSftp.class);
        when(downloaderSFTPClient.openSftp(mockSession)).thenReturn(mockChannel);
        StepVerifier.create(downloaderSFTPService.process(EXPECTED_URI))
                    .expectNextMatches(item -> item.getLeft().equals(EXPECTED_URI) && item.getRight().endsWith(
                            expectedFilenameExtension)).verifyComplete();

        verify(downloaderFileService).prepareForPersist(any(DownloaderInfo.class));
        verify(downloaderSFTPClient).download(eq(mockChannel), anyString(), anyString());
        verify(downloaderFileService).deleteQuietly(any(File.class));
        verify(downloaderFileService).moveTempFileToFinalLocation(anyString(), anyString());
        verify(downloaderSFTPClient).afterProcess(eq(mockChannel));
    }

    @Test
    public void process_error() throws JSchException {
        Class<JSchException> expectExceptionClass = JSchException.class;
        when(downloaderSFTPClient.connect(EXPECTED_URI)).thenThrow(expectExceptionClass);
        StepVerifier.create(downloaderSFTPService.process(EXPECTED_URI))
                    .verifyError(expectExceptionClass);
        verify(downloaderSFTPClient).afterProcess(null);
    }
}