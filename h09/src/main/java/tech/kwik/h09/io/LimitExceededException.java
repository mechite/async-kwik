package tech.kwik.h09.io;

import java.io.IOException;

public class LimitExceededException extends IOException {
    public LimitExceededException(long limit) {
        super("Limit of " + limit + " bytes is exceeded");
    }
}