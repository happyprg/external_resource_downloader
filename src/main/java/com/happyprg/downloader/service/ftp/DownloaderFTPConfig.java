package com.happyprg.downloader.service.ftp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "downloader.ftp")
public class DownloaderFTPConfig {

    private String localDiskRootPath;
    private int connectTimeout = 10000;
    private int readTimeout = 10000;
    private int dataTimeout = 10000;

}
