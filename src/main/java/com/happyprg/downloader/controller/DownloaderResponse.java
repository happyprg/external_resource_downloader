package com.happyprg.downloader.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JsonInclude(Include.NON_NULL)
@Accessors(chain = true)
public class DownloaderResponse {
    int code;
    String uri;
    String localFileFullPath;
    String errorMsg;
}