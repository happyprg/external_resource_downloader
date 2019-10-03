package com.happyprg.downloader.service.sftp;

import static com.happyprg.downloader.service.DownloaderService.USER_INFO_SEPARATOR;
import static java.util.Objects.isNull;

import java.net.URI;

import org.springframework.stereotype.Service;

import com.happyprg.downloader.service.DownloaderClient;
import com.happyprg.downloader.util.DownloaderFileService;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DownloaderSFTPClient implements DownloaderClient<ChannelSftp> {

    private static final String NO = "no";
    private static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
    private static final String SUPPORT_PROTOCOL = "sftp";
    private static final int DEFAULT_PORT = 22;
    private final DownloaderSFTPConfig downloaderSFTPConfig;
    private final DownloaderFileService downloaderFileService;

    public DownloaderSFTPClient(DownloaderSFTPConfig downloaderSFTPConfig,
                                DownloaderFileService downloaderFileService) {
        this.downloaderSFTPConfig = downloaderSFTPConfig;
        this.downloaderFileService = downloaderFileService;
    }

    public Session connect(URI uri) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(uri.getUserInfo().split(USER_INFO_SEPARATOR)[0],
                                          uri.getHost(),
                                          uri.getPort() <= 0 ? DEFAULT_PORT : uri.getPort());
        session.setPassword(uri.getUserInfo().split(USER_INFO_SEPARATOR)[1]);
        session.setConfig(STRICT_HOST_KEY_CHECKING, NO);

        session.connect(downloaderSFTPConfig.getConnectTimeout());
        session.setTimeout(downloaderSFTPConfig.getReadTimeout());
        if (!session.isConnected()) {
            throw new JSchException("not connected session.");
        }
        return session;
    }

    public ChannelSftp openSftp(Session session) throws JSchException {
        ChannelSftp channel = (ChannelSftp) session.openChannel(SUPPORT_PROTOCOL);
        channel.connect(downloaderSFTPConfig.getConnectTimeout());

        if (!channel.isConnected()) {
            throw new JSchException("not connected channel.");
        }

        return channel;
    }

    @Override
    public void download(ChannelSftp channel, String path, String tempLocalFullPath) throws SftpException {
        channel.get(path, tempLocalFullPath);
    }

    @Override
    public void afterProcess(ChannelSftp client) {

        if (!isNull(client)) {
            client.disconnect();
        }
    }

}
