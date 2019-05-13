package com.nilangpatel.worldgdp.services;

import com.agapsys.rcf.exceptions.BadRequestException;

public class InvalidDataException extends BadRequestException {

    public InvalidDataException() {}

    public InvalidDataException(Integer appStatus) {
        super(appStatus);
    }

    public InvalidDataException(String msg, Object... msgArgs) {
        super(msg, msgArgs);
    }

    public InvalidDataException(Integer appStatus, String msg, Object... msgArgs) {
        super(appStatus, msg, msgArgs);
    }
    
}
