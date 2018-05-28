package com.example.moneybox;

/**
 * Created by mumumushi on 18-3-10.
 */

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
