package com.happyprg.downloader.service.http;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "downloader.http")
public class DownloaderHTTPConfig {

    private String localDiskRootPath;
    private int connectionTimeout = 10000;
    private int readTimeOut = 10000;
}
