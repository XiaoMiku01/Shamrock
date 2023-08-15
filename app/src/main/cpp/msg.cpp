#include <random>
#include <jni.h>

#define NT_CHAT_TYPE_C2C 1
#define NT_CHAT_TYPE_GROUP 2
#define NT_CHAT_TYPE_GROUP_LESS 133
#define NT_CHAT_TYPE_DIS 3
#define NT_CHAT_TYPE_GUILD 4
#define NT_CHAT_TYPE_GROUP_GUILD 9
#define NT_CHAT_TYPE_MATCH_FRIEND 104
#define NT_CHAT_TYPE_LESS_FROM_GROUP 100
#define NT_CHAT_TYPE_LESS_FROM_UNKNOWN 99
#define NT_CHAT_TYPE_TMP_FRIEND_VERIFY 101

static uint8_t MSG_TYPE_GROUP =   0;
static uint8_t MSG_TYPE_PRIVATE = 1;
static uint8_t MSG_TYPE_GUILD =   2;
static uint8_t MSG_TYPE_LESS =    3;
static uint8_t MSG_TYPE_DIS =     4;

struct MessageId {
    uint32_t peer_id : 32 = 0;
    uint8_t msg_type : 3 = 0;
    int32_t random : 28 = 0;
    uint8_t unknown : 1 = 0;
};

bool isGroupMsg(MessageId msgId);
bool isPrivateMsg(MessageId msgId);
bool isLessMsg(MessageId msgId);
bool isGuildMsg(MessageId msgId);

MessageId calcUniqueMsgId(int32_t chatType, int64_t peerId);

/*
extern "C"
JNIEXPORT jlong JNICALL
Java_moe_fuqiuluo_http_action_helper_MessageHelper_createMessageUniseq(JNIEnv *env, jobject thiz,
                                                                       jint chat_type,
                                                                       jlong peer_id) {
    MessageId id = calcUniqueMsgId(chat_type, peer_id);
    return *(int64_t*) &id;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_moe_fuqiuluo_http_action_helper_MessageHelper_getPeerId(JNIEnv *env, jobject thiz,
                                                             jlong msg_id) {
    MessageId id = *(MessageId*) &msg_id;
    return id.peer_id;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_moe_fuqiuluo_http_action_helper_MessageHelper_isGroup(JNIEnv *env, jobject thiz,
                                                           jlong msg_id) {
    MessageId id = *(MessageId*) &msg_id;
    return isGroupMsg(id);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_moe_fuqiuluo_http_action_helper_MessageHelper_isLess(JNIEnv *env, jobject thiz,
                                                           jlong msg_id) {
    MessageId id = *(MessageId*) &msg_id;
    return isLessMsg(id);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_moe_fuqiuluo_http_action_helper_MessageHelper_isGuild(JNIEnv *env, jobject thiz,
                                                           jlong msg_id) {
    MessageId id = *(MessageId*) &msg_id;
    return isGuildMsg(id);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_moe_fuqiuluo_http_action_helper_MessageHelper_isPrivate(JNIEnv *env, jobject thiz,
                                                           jlong msg_id) {
    MessageId id = *(MessageId*) &msg_id;
    return isPrivateMsg(id);
}
 */

MessageId calcUniqueMsgId(int32_t chatType, int64_t peerId) {
    MessageId id{};
    uint8_t msgType = MSG_TYPE_GROUP;
    if (
            chatType == NT_CHAT_TYPE_C2C ||
            chatType == NT_CHAT_TYPE_GROUP_LESS ||
            chatType == NT_CHAT_TYPE_MATCH_FRIEND ||
            chatType == NT_CHAT_TYPE_TMP_FRIEND_VERIFY
            ) {
        msgType = MSG_TYPE_PRIVATE;
    } else if (chatType == NT_CHAT_TYPE_GROUP) {
        msgType = MSG_TYPE_GROUP;
    } else if (msgType == NT_CHAT_TYPE_DIS) {
        msgType = MSG_TYPE_DIS;
    } else if (msgType == NT_CHAT_TYPE_GUILD || msgType == NT_CHAT_TYPE_GROUP_GUILD) {
        msgType = MSG_TYPE_GUILD;
    } else if (chatType == NT_CHAT_TYPE_LESS_FROM_GROUP || chatType == NT_CHAT_TYPE_LESS_FROM_UNKNOWN) {
        msgType = MSG_TYPE_LESS;
    }
    id.unknown = 0;
    std::random_device rd;
    std::mt19937 generator(rd());
    std::uniform_int_distribution<int> distribution(0, 134217727);
    id.random = distribution(generator);
    id.msg_type = msgType;
    id.peer_id = peerId;
    return id;
}

bool isGroupMsg(MessageId msgId) {
    return msgId.msg_type == MSG_TYPE_GROUP;
}

bool isPrivateMsg(MessageId msgId) {
    return msgId.msg_type == MSG_TYPE_PRIVATE;
}

bool isLessMsg(MessageId msgId) {
    return msgId.msg_type == MSG_TYPE_LESS;
}

bool isGuildMsg(MessageId msgId) {
    return msgId.msg_type == MSG_TYPE_GUILD;
}
