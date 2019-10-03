package com.happyprg.downloader.logging;

import java.io.OutputStream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public final class LoggerOutputStream extends OutputStream {
    private final Loggable loggable;
    private String buffer = "";

    @Override
    public void write(int b) {
        final byte[] bytes = new byte[1];
        bytes[0] = (byte) (b & 0xff);
        buffer += new String(bytes);
        flush();
    }

    @Override
    public void write(byte[] b, int off, int len) {
        buffer += new String(b, off, len);
        flush();
    }

    @Override
    public void flush() {
        if (buffer.endsWith("\n")) {
            buffer = buffer.substring(0, buffer.length() - 1);
            loggable.log(buffer);
            buffer = "";
        }
    }
}