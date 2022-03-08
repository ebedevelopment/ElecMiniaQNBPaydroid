package com.ebe.miniaelec.http;

public interface RequestListener {
    void onSuccess(String response);

    void onFailure(String failureMsg);
}
