package com.findmyflight.findmyflight.service.error.handler;

public class IdNotExistException extends RuntimeException{
    public IdNotExistException() {
        super("ID does not exist");
    }
}
