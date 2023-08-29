package com.tencent.qqnt.kernel.nativeinterface;

import java.util.ArrayList;

public interface IQQNTWrapperSession {
    //IKernelAvatarService getAvatarService();

    //IKernelBuddyService getBuddyService();

    IKernelUixConvertService getUixConvertService();

    IKernelGroupService getGroupService();

    ArrayList<String> getCacheErrLog();

    //IKernelConfigMgrService getConfigMgrService();

    //IKernelDirectSessionService getDirectSessionService();

    //IKernelFeedService getFeedChannelService();

    //IKernelGroupService getGroupService();

    //IKernelGuildService getGuildService();

    IKernelMsgService getMsgService();

    //IKernelProfileService getProfileService();

    //IKernelRDeliveryService getRDeliveryService();

    //IKernelRecentContactService getRecentContactService();

    IKernelRichMediaService getRichMediaService();

   // IKernelSearchService getSearchService();

    String getSessionId();

    //IKernelSettingService getSettingService();

    ArrayList<String> getShortLinkBlacklist();

    //IKernelStorageCleanService getStorageCleanService();

   // IKernelTestPerformanceService getTestPerformanceService();

   // IKernelTicketService getTicketService();

    //IKernelTipOffService getTipOffService();

    //IKernelUnitedConfigService getUnitedConfigService();

    //IKernelYellowFaceService getYellowFaceService();

    boolean offLineSync(boolean z);

    void onDispatchPush(int i2, byte[] bArr);

    void onDispatchRequestReply(long j2, int i2, byte[] bArr);

    void switchToBackGround();

    void switchToFront();
}