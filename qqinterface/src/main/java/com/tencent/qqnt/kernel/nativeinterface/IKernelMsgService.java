package com.tencent.qqnt.kernel.nativeinterface;

import java.util.ArrayList;

public interface IKernelMsgService {
    void deleteMsg(Contact contact, ArrayList<Long> msgIdList, IOperateCallback callback);

    void fetchLongMsg(Contact contact, long msgId);

    void getRecallMsgsByMsgId(Contact contact, ArrayList<Long> arrayList, IMsgOperateCallback iMsgOperateCallback);

    void recallMsg(Contact contact, ArrayList<Long> msgIdList, IOperateCallback callback);

    //void recallMsgs(Contact contact, ArrayList<GProRecallReqItem> arrayList, IGProRecallCallback iGProRecallCallback);
}
