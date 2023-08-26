// IQSigner.aidl
package moe.fuqiuluo.xposed.ipc;

import moe.fuqiuluo.xposed.ipc.IQSign;

interface IQSigner {
    IQSign sign(String cmd, int seq, String uin, in byte[] buffer);
}