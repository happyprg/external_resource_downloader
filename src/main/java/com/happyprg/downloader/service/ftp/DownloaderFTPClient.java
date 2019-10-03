package com.happyprg.downloader.service.ftp;

import static com.happyprg.downloader.service.DownloaderService.ANONYMOUS;
import static com.happyprg.downloader.service.DownloaderService.ANONYMOUS_PASSWORD;
import static com.happyprg.downloader.service.DownloaderService.USER_INFO_SEPARATOR;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;
import static org.apache.commons.net.ftp.FTPReply.isPositiveCompletion;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;

import com.happyprg.downloader.service.DownloaderClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DownloaderFTPClient implements DownloaderClient<FTPClient> {
    static final int DEFAULT_FTP_PORT = 21;
    private static final boolean RESUMABLE = true;

    private FTPClient createFtpClient(DownloaderFTPConfig downloaderFTPConfig, URI uri) throws
                                                                                        IOException {
        final FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(downloaderFTPConfig.getConnectTimeout());
        ftpClient.setDefaultTimeout(downloaderFTPConfig.getReadTimeout());
        ftpClient.setDataTimeout(downloaderFTPConfig.getDataTimeout());
        ftpClient.connect(uri.getHost(), uri.getPort() <= 0 ? DEFAULT_FTP_PORT : uri.getPort());
        return ftpClient;
    }

    private final DownloaderFTPConfig downloaderFTPConfig;

    public DownloaderFTPClient(DownloaderFTPConfig downloaderFTPConfig) {
        this.downloaderFTPConfig = downloaderFTPConfig;
    }

    public FTPClient connect(URI uri) throws IOException {
        final FTPClient ftpClient = createFtpClient(downloaderFTPConfig, uri);

        checkConnectReplyCode(ftpClient, uri.toString());

        checkLoginStatus(uri, ftpClient);

        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(BINARY_FILE_TYPE);
        return ftpClient;
    }

    boolean checkLoginStatus(URI uri, FTPClient ftpClient) throws IOException {
        if (!login(ftpClient, uri)) {
            throw new IllegalArgumentException(("Could not login to the server"));
        }
        return true;
    }

    boolean checkConnectReplyCode(FTPClient ftpClient, String uri) {
        if (!isPositiveCompletion(ftpClient.getReplyCode())) {
            throw new RuntimeException("Could not connect to the server. uri - " + uri
                                       + "downloaderConfig.getConnectTimeout() - "
                                       + downloaderFTPConfig.getConnectTimeout()
            );
        }
        return true;
    }

    boolean login(FTPClient ftpClient, URI uri) throws IOException {

        if (isBlank(uri.getUserInfo())) {
            return ftpClient.login(ANONYMOUS, ANONYMOUS_PASSWORD);
        }
        return ftpClient.login(uri.getUserInfo().split(USER_INFO_SEPARATOR)[0],
                               uri.getUserInfo().split(USER_INFO_SEPARATOR)[1]);
    }

    @Override
    public void afterProcess(FTPClient ftpClient) {
        if (!isNull(ftpClient) && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException ee) {
                log.error(ee.getMessage());
            }
        }
    }

    @Override
    public void download(FTPClient ftpClient, String downloadPath, String tempLocalFullPath)
            throws IOException {

        try (OutputStream outputStream = new FileOutputStream(tempLocalFullPath,
                                                              RESUMABLE)) {
            boolean result = ftpClient.retrieveFile(downloadPath, outputStream);
            if (!result) {
                throw new RuntimeException(
                        "Could not download the file at - " + ftpClient.getRemoteAddress().getHostName()
                        + downloadPath);
            }
        } catch (IOException e) {
            throw e;
        }
    }
}
