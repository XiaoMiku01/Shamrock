#include <stdexcept>
#include "cqcode.h"

inline void replace_string(std::string& str, const std::string& from, const std::string& to) {
    size_t startPos = 0;
    while ((startPos = str.find(from, startPos)) != std::string::npos) {
        str.replace(startPos, from.length(), to);
        startPos += to.length();
    }
}

void decode_cqcode(const std::string& code, std::vector<std::unordered_map<std::string, std::string>>& dest) {
    std::string cache;
    bool is_start = false;
    std::string key_tmp;
    std::unordered_map<std::string, std::string> kv;
    for(int i = 0; i < code.size(); i++) {
        auto c = code[i];
        if (c == '[') {
            if (is_start) {
                throw illegal_code();
            } else {
                if (!cache.empty()) {
                    std::unordered_map<std::string, std::string> kv;
                    kv.emplace("type", "text");
                    kv.emplace("text", cache);
                    dest.push_back(kv);
                    cache.clear();
                }
                auto c1 = code[++i];
                auto c2 = code[++i];
                auto c3 = code[++i];
                if (c1 == 'C' && c2 == 'Q' && c3 == ':') {
                    is_start = true;
                } else {
                    throw illegal_code();
                }
            }
        } else if (c == '=') {
            if (is_start) {
                if (cache.empty()) {
                    throw illegal_code();
                } else {
                    key_tmp.append(cache);
                    cache.clear();
                }
            }
        } else if (c == ',') {
            if (is_start) {
                if (!kv.contains("type") && !cache.empty()) {
                    kv.emplace("type", cache);
                    cache.clear();
                } else {
                    if (!key_tmp.empty()) {
                        replace_string(cache, "&amp;", "&");
                        replace_string(cache, "&#91;", "[");
                        replace_string(cache, "&#93;", "]");
                        replace_string(cache, "&#44;", ",");
                        kv.emplace(key_tmp, cache);
                        cache.clear();
                        key_tmp.clear();
                    }
                }
            }
        } else if (c == ']') {
            if (is_start) {
                if (!cache.empty()) {
                    if (!key_tmp.empty()) {
                        replace_string(cache, "&amp;", "&");
                        replace_string(cache, "&#91;", "[");
                        replace_string(cache, "&#93;", "]");
                        replace_string(cache, "&#44;", ",");
                        kv.emplace(key_tmp, cache);
                    }
                    dest.push_back(kv);
                    kv.clear();
                    key_tmp.clear();
                    cache.clear();
                }
            } else {
                throw illegal_code();
            }
        } else {
            cache += c;
        }
    }
    if (!cache.empty()) {
        std::unordered_map<std::string, std::string> kv;
        kv.emplace("type", "text");
        kv.emplace("text", cache);
        dest.push_back(kv);
    }
}

void encode_cqcode(const std::vector<std::unordered_map<std::string, std::string>> &segment, std::string &dest) {
    for (auto &msg: segment) {
        try {
            auto type = msg.at("type");
            if (type == "text") {
                dest.append(msg.at("text"));
            } else {
                dest.append("[CQ:");
                dest.append(type);
                dest.append(",");
                bool is_start = true;
                for (const auto &msg_data: msg) {
                    if (is_start) {
                        is_start = false;
                    } else {
                        dest.append(",");
                    }
                    dest.append(msg_data.first);
                    dest.append("=");
                    auto value = msg_data.second;
                    replace_string(value, "&", "&amp;");
                    replace_string(value, "[", "&#91;");
                    replace_string(value, "]", "&#93;");
                    replace_string(value, ",", "&#44;");
                    dest.append(value);
                }
                dest.append("]");
            }
        } catch (std::out_of_range& e) {
            throw illegal_code();
        }
    }
}
