package com.happyprg.downloader.service.sftp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "downloader.sftp")
public class DownloaderSFTPConfig {

    private String localDiskRootPath;
    private int connectTimeout = 10000;
    private int readTimeout = 10000;
}
