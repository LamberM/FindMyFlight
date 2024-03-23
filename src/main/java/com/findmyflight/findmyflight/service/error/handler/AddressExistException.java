package com.findmyflight.findmyflight.service.error.handler;

public class AddressExistException extends RuntimeException{
    public AddressExistException() {
        super("Address exists");
    }
}
