package com.niks.filelog.network;

import com.niks.filelog.FileLogHelper;

public class HttpCustomLogger implements HttpLoggingInterceptor.Logger {
    @Override
    public void log(String message) {
        FileLogHelper.INSTANCE.log(message, "Network");
    }
}