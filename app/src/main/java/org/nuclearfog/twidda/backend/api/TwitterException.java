package org.nuclearfog.twidda.backend.api;

import java.io.IOException;


public class TwitterException extends Exception {

    public static final int TOKEN_NOT_SET = 600;

    private int httpCode;

    TwitterException(Exception e) {
        super(e);
        httpCode = -1;
    }

    TwitterException(int httpCode) {
        this.httpCode = httpCode;
    }

    public int getCode() {
        return httpCode;
    }
}