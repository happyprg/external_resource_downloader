package com.happyprg.downloader.service.ftp;

import static com.happyprg.downloader.service.ftp.DownloaderFTPClient.DEFAULT_FTP_PORT;
import static java.net.URI.create;
import static org.apache.commons.net.ftp.FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloaderFTPClientTest {

    private static final URI EXPECTED_URI = create(
            "ftp://demo:password@test.rebex.net/pub/example/ConsoleClient.png");
    @Mock
    DownloaderFTPConfig downloaderFTPConfig;
    @InjectMocks
    DownloaderFTPClient downloaderFtpClient;

    @Before
    public void before() {
        downloaderFtpClient = new DownloaderFTPClient(downloaderFTPConfig);
    }

    @Test
    public void createFtpClient() {
    }

    @Test
    public void checkConnectReplyCode() {
    }

    @Test
    public void login() {
    }

    @Test
    public void afterProcess() {
    }

    @Test
    public void connect() throws IOException {
        FTPClient actual = downloaderFtpClient.connect(EXPECTED_URI);
        assertEquals(actual.getConnectTimeout(), downloaderFTPConfig.getConnectTimeout());
        assertEquals(actual.getDefaultTimeout(), downloaderFTPConfig.getReadTimeout());
        assertEquals(actual.getRemotePort(), DEFAULT_FTP_PORT);
        assertEquals(true, downloaderFtpClient.checkConnectReplyCode(actual, EXPECTED_URI.toString()));
        assertEquals(true, downloaderFtpClient.login(actual, EXPECTED_URI));
        assertEquals(actual.getDataConnectionMode(), PASSIVE_LOCAL_DATA_CONNECTION_MODE);
    }

    @Test(expected = IOException.class)
    public void checkLoginStatus() throws IOException {
        downloaderFtpClient.checkLoginStatus(EXPECTED_URI, new FTPClient());

    }

}