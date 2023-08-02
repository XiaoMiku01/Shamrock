package com.tencent.mobileqq.transfile;

import com.tencent.mobileqq.transfile.api.ITransFileController;

import java.util.concurrent.atomic.AtomicBoolean;

import mqq.app.AppRuntime;

public class BaseTransFileController implements ITransFileController {
    @Override
    public AtomicBoolean isWorking() {
        return null;
    }

    @Override
    public synchronized boolean transferAsync(TransferRequest transferRequest) {
        return false;
    }

    @Override
    public void onCreate(AppRuntime appRuntime) {

    }

    @Override
    public void onDestroy() {

    }
}
