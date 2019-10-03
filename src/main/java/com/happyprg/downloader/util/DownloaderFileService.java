package com.happyprg.downloader.util;

import static java.lang.System.lineSeparator;
import static org.apache.commons.io.FileUtils.forceMkdirParent;
import static org.apache.commons.io.FileUtils.moveFileToDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.happyprg.downloader.service.DownloaderInfo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DownloaderFileService {
    private static final boolean CREATE_DEST_DIR = true;

    public void prepareForPersist(DownloaderInfo downloaderInfo) throws IOException {
        forceMkdirParent(new File(downloaderInfo.getTempLocalFileFullPath()));
    }

    public void deleteIncompleteParentFile(DownloaderInfo downloaderInfo, Exception e) {
        if (Objects.isNull(downloaderInfo)) {
            return;
        }
        deleteQuietly(new File(downloaderInfo.getTempLocalFileFullPath()).getParentFile());
        log.error("Could not process the remote file. localFileFullPath - " + downloaderInfo.getLocalFullPath()
                  + ", And clean up temporary file. tempLocalFullPath - " + downloaderInfo
                          .getTempLocalFileFullPath(), e);
    }

    public void moveTempFileToFinalLocation(String tempLocalFileFullPath,
                                            String localFileDirectory) throws IOException {
        if (StringUtils.isBlank(tempLocalFileFullPath)) {
            log.error("tempLocalFileFullPath is blank");
            return;
        }
        if (StringUtils.isBlank(localFileDirectory)) {
            log.error("localFileDirectory is blank");
        }
        moveFileToDirectory(new File(tempLocalFileFullPath),
                            new File(localFileDirectory), CREATE_DEST_DIR);
        log.info("Move tempLocalFileFullPath - " + tempLocalFileFullPath + lineSeparator()
                 + ", to localFileDirectory - "
                 + localFileDirectory);
    }

    public void copyURLToFile(URL url, File destFile, int connectionTimeout, int readTimeOut)
            throws IOException {
        FileUtils.copyURLToFile(url, destFile, connectionTimeout, readTimeOut);
    }

    public void deleteQuietly(File file) {
        FileUtils.deleteQuietly(file);
    }
}
