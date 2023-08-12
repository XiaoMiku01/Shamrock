package com.tencent.qqnt.kernel.nativeinterface;

import java.util.ArrayList;

public interface IMsgOperateCallback {
    void onResult(int i2, String str, ArrayList<MsgRecord> arrayList);
}