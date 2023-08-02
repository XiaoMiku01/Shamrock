package com.tencent.qqnt.kernel.nativeinterface;

import java.util.ArrayList;

/* compiled from: P */
/* loaded from: classes2.dex */
public final class MarketFaceElement {
    ArrayList<MarketFaceSupportSize> apngSupportSize;
    String backColor;
    String dynamicFacePath;
    String emojiId;
    int emojiPackageId;
    Integer emojiType;
    Integer endTime;
    int faceInfo;
    String faceName;
    Integer hasIpProduct;
    int imageHeight;
    int imageWidth;
    int itemType;
    String key;
    int mediaType;
    byte[] mobileParam;
    byte[] param;
    String sourceJumpUrl;
    String sourceName;
    Integer sourceType;
    String sourceTypeName;
    Integer startTime;
    String staticFacePath;
    int subType;
    ArrayList<MarketFaceSupportSize> supportSize;
    ArrayList<Integer> voiceItemHeightArr;
    String volumeColor;

    public MarketFaceElement() {
    }

    public ArrayList<MarketFaceSupportSize> getApngSupportSize() {
        return this.apngSupportSize;
    }

    public String getBackColor() {
        return this.backColor;
    }

    public String getDynamicFacePath() {
        return this.dynamicFacePath;
    }

    public String getEmojiId() {
        return this.emojiId;
    }

    public int getEmojiPackageId() {
        return this.emojiPackageId;
    }

    public Integer getEmojiType() {
        return this.emojiType;
    }

    public Integer getEndTime() {
        return this.endTime;
    }

    public int getFaceInfo() {
        return this.faceInfo;
    }

    public String getFaceName() {
        return this.faceName;
    }

    public Integer getHasIpProduct() {
        return this.hasIpProduct;
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public int getItemType() {
        return this.itemType;
    }

    public String getKey() {
        return this.key;
    }

    public int getMediaType() {
        return this.mediaType;
    }

    public byte[] getMobileParam() {
        return this.mobileParam;
    }

    public byte[] getParam() {
        return this.param;
    }

    public String getSourceJumpUrl() {
        return this.sourceJumpUrl;
    }

    public String getSourceName() {
        return this.sourceName;
    }

    public Integer getSourceType() {
        return this.sourceType;
    }

    public String getSourceTypeName() {
        return this.sourceTypeName;
    }

    public Integer getStartTime() {
        return this.startTime;
    }

    public String getStaticFacePath() {
        return this.staticFacePath;
    }

    public int getSubType() {
        return this.subType;
    }

    public ArrayList<MarketFaceSupportSize> getSupportSize() {
        return this.supportSize;
    }

    public ArrayList<Integer> getVoiceItemHeightArr() {
        return this.voiceItemHeightArr;
    }

    public String getVolumeColor() {
        return this.volumeColor;
    }

    public String toString() {
        return "MarketFaceElement{itemType=" + this.itemType + ",faceInfo=" + this.faceInfo + ",emojiPackageId=" + this.emojiPackageId + ",subType=" + this.subType + ",mediaType=" + this.mediaType + ",imageWidth=" + this.imageWidth + ",imageHeight=" + this.imageHeight + ",faceName=" + this.faceName + ",emojiId=" + this.emojiId + ",key=" + this.key + ",param=" + this.param + ",mobileParam=" + this.mobileParam + ",sourceType=" + this.sourceType + ",startTime=" + this.startTime + ",endTime=" + this.endTime + ",emojiType=" + this.emojiType + ",hasIpProduct=" + this.hasIpProduct + ",voiceItemHeightArr=" + this.voiceItemHeightArr + ",sourceName=" + this.sourceName + ",sourceJumpUrl=" + this.sourceJumpUrl + ",sourceTypeName=" + this.sourceTypeName + ",backColor=" + this.backColor + ",volumeColor=" + this.volumeColor + ",staticFacePath=" + this.staticFacePath + ",dynamicFacePath=" + this.dynamicFacePath + ",supportSize=" + this.supportSize + ",apngSupportSize=" + this.apngSupportSize + ",}";
    }

    public MarketFaceElement(int i2, int i3, int i4, int i5, int i6, int i7, int i8, String str, String str2, String str3, byte[] bArr, byte[] bArr2, Integer num, Integer num2, Integer num3, Integer num4, Integer num5, ArrayList<Integer> arrayList, String str4, String str5, String str6, String str7, String str8, String str9, String str10, ArrayList<MarketFaceSupportSize> arrayList2, ArrayList<MarketFaceSupportSize> arrayList3) {
        this.itemType = i2;
        this.faceInfo = i3;
        this.emojiPackageId = i4;
        this.subType = i5;
        this.mediaType = i6;
        this.imageWidth = i7;
        this.imageHeight = i8;
        this.faceName = str;
        this.emojiId = str2;
        this.key = str3;
        this.param = bArr;
        this.mobileParam = bArr2;
        this.sourceType = num;
        this.startTime = num2;
        this.endTime = num3;
        this.emojiType = num4;
        this.hasIpProduct = num5;
        this.voiceItemHeightArr = arrayList;
        this.sourceName = str4;
        this.sourceJumpUrl = str5;
        this.sourceTypeName = str6;
        this.backColor = str7;
        this.volumeColor = str8;
        this.staticFacePath = str9;
        this.dynamicFacePath = str10;
        this.supportSize = arrayList2;
        this.apngSupportSize = arrayList3;
    }
}
