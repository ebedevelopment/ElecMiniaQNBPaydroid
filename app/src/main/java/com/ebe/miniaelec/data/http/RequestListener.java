package com.ebe.miniaelec.data.http;

public interface RequestListener {
    void onSuccess(String response);

    void onFailure(String failureMsg);
}
