# Shamrock

☘ 基于Xposed实现的Onebot11/12标准QQBot框架

> 本项目仅提供学习与交流用途，请在24小时内删除，本项目目的是研究Xposed和Lsposed框架的使用，以及Epic框架开发相关知识，如有违反法律，请联系删除。

# 权限声明

未在此处声明的权限，请警惕是否为正版Shamrock。

- 联网权限: 为了让Shamrock进程使用HTTP API进行一些操作。
- [Hook**系统框架**](https://github.com/fuqiuluo/Shamrock/wiki/perm_hook_android): 为了保证息屏状态下仍能维持服务后台运行。

# 语音解码器支持

语音转换器已经模块化，如果不加入指定的模块，则无法发送mp3/flac/wav/ogg等格式的语音。

为了完整支持，您需要下载[AudioLibrary](https://raw.githubusercontent.com/fuqiuluo/Shamrock/master/AudioLibrary.zip)并将里面的`so文件`全部解压到`QQ数据目录/Tencent/Shamrock/lib`文件夹。

**QQ数据目录**一般在`/storage/emulated/0/Android/data/com.tencent.mobileqq`

如果没有`lib`文件夹，则创建一个，`lib`文件夹内只能有`*.so`文件不能有目录存在，否则无法正常加载。

# 开发进程

☘ 努力实现中：[已实现以及不与实现的接口都在这里](https://github.com/fuqiuluo/Shamrock/wiki)
