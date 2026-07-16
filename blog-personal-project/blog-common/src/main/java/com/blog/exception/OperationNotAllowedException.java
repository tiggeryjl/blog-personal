package com.blog.exception;

public class OperationNotAllowedException extends BaseException {

    public OperationNotAllowedException() {
    }

    public OperationNotAllowedException(String msg) {
        super(msg);
    }

}
