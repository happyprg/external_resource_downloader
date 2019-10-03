package com.happyprg.downloader.service;

import static java.io.File.separator;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.apache.commons.io.FileUtils.getTempDirectory;
import static org.apache.commons.io.FilenameUtils.EXTENSION_SEPARATOR;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.net.URI;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Getter
@ToString
public class DownloaderInfo {
    private final URI uri;
    private final String localFileDirectory;
    private final String localFileName;
    private final String tempLocalFileFullPath;
    private final String localFullPath;

    public static FileHandleInfoBuilder builder() {return new FileHandleInfoBuilder();}

    public String getLocalFullPath() {
        return this.localFileDirectory + this.localFileName;
    }

    @Slf4j
    public static class FileHandleInfoBuilder {
        private URI url;
        private String localDiskRootPath;

        FileHandleInfoBuilder() {}

        public FileHandleInfoBuilder uri(URI uri) {
            this.url = uri;
            return this;
        }

        public FileHandleInfoBuilder localDiskRootPath(String localDiskRootPath) {
            this.localDiskRootPath = localDiskRootPath;
            return this;
        }

        public DownloaderInfo build() {
            if (StringUtils.isBlank(localDiskRootPath)) {
                throw new IllegalArgumentException("the value for localDiskRootPath is blank");
            }
            String localFileParentPath = sha256Hex(
                    this.url.getHost() + this.url.getPath() + UUID.randomUUID().toString());
            String localFileDirectory = this.localDiskRootPath + localFileParentPath + separator;
            String fileName = sha256Hex(this.url.getPath()) + EXTENSION_SEPARATOR
                                   + getExtension(this.url.getPath());
            String tempLocalFullPath = getTempDirectory() + separator + localFileParentPath + separator
                                       + fileName;
            log.info("Start downloading remoteFile to temp - " + tempLocalFullPath);

            String localFileFullPath = localFileDirectory + fileName;

            return new DownloaderInfo(this.url, localFileDirectory, fileName, tempLocalFullPath,
                                      localFileFullPath);
        }

    }

}
