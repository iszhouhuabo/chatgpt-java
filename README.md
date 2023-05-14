## 简介

本项目是一个使用 `SpringBoot` 实现的 `ChatGPT` 后端系统，配合前端使用可以实现聊天机器人的功能。前端项目使用 `Vue` 搭建。

## 前端项目

前端项目地址：xxxx

## 支持功能

目前主要支持以下功能：

- [x] `SSE`交互模式
- [x] 会话`Token`统计

## 使用方法

### 本地开发

1. 克隆本项目到本地。
2. 使用 IDE 打开项目，等待 `Gradle` 下载所需依赖。
3. 配置内置的 API KEY, 对应参数：`API_KEY`<br>
   如果你想使用前端输入的 Key，那么将无需做配置；如果你配置了内置`API_KEY`，又不想使用，可以将 `USE_INTERNAL_KEY`
   设置成 `false`，那么将不会使用到内置的 `API_KEY`
4. `ChatApplication` 启动项目。

### `Docker` 部署

```aidl
// 构建项目
./gradlew clean build
```

```shell
# 发布镜像到本地的 Docker
docker build -t zhouhuabo/chatgpt-java:v1 .  # 把 zhouhuabo 换成自己的 docker hub username
```