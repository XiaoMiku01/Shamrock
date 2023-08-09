package com.tencent.common.app;

import com.tencent.mobileqq.app.BusinessHandler;
import com.tencent.mobileqq.app.BusinessObserver;

import mqq.app.AppRuntime;

public abstract class AppInterface extends AppRuntime {
    public String getCurrentNickname() {
        return "";
    }

    public BusinessHandler getBusinessHandler(String className) {
        return null;
    }

    public void addObserver(BusinessObserver businessObserver) {
    }

    public void removeObserver(BusinessObserver businessObserver) {
    }
}
