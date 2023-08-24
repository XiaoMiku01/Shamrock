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
                    kv.emplace("_type", "text");
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
                if (!kv.contains("_type") && !cache.empty()) {
                    kv.emplace("_type", cache);
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
        kv.emplace("_type", "text");
        kv.emplace("text", cache);
        dest.push_back(kv);
    }
}
