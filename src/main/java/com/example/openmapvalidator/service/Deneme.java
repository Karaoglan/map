package com.example.openmapvalidator.service;

import com.google.maps.PendingResult;
import com.google.maps.errors.ApiException;

import java.io.IOException;

public class Deneme implements PendingResult {
    @Override
    public void setCallback(Callback callback) {

    }

    @Override
    public Object await() throws ApiException, InterruptedException, IOException {
        return null;
    }

    @Override
    public Object awaitIgnoreError() {
        return null;
    }

    @Override
    public void cancel() {

    }
}
