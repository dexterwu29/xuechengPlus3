# Nacos 转码配置说明（31.4 FFmpeg）

## 问题

`application-local.yml` 中的 `transcode.ffmpeg-path` 可能被 Nacos 配置覆盖而无法生效。  
需在 Nacos 中显式配置 transcode，或确保 `application-local.yml` 正确加载，实现实时转码。

**排查**：启动后查看日志 `Transcode config: ffmpegPath=xxx`。若为 `ffmpeg`（默认值），说明配置未加载。

## 配置步骤

1. 登录 Nacos 控制台（如 `http://<你的Nacos地址>:8848/nacos`）
2. 进入 **配置管理** → **配置列表**
3. 选择以下任一方式：

### 方式一：主配置（所有环境生效）

- **Data ID**: `xc-plus-content-service.yaml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: YAML

### 方式二：本地 profile 专用（仅 profile=local 时生效）

- **Data ID**: `xc-plus-content-service-local.yaml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: YAML

4. 在配置内容中**添加或合并**以下内容：

```yaml
# 视频转码配置（31.4：支持 AVI 等格式转码为 MP4）
transcode:
  # Windows 路径建议使用正斜杠，避免 YAML 转义问题；请替换为你的实际路径
  ffmpeg-path: D:/path/to/ffmpeg/bin/ffmpeg.exe
  # ffprobe-path 不配置时，会从 ffmpeg 路径自动推导
  auto-transcode: true
  timeout-seconds: 600
```

5. 点击 **发布** 保存

## 配置优先级

Spring Cloud 配置加载顺序（后者覆盖前者）：

1. `application.yml`
2. `application-local.yml`（profile=local）
3. Nacos: `xc-plus-content-service.yaml`

若 Nacos 中已有 `xc-plus-content-service.yaml` 且未包含 transcode，则 `application-local.yml` 的 transcode 会生效。  
若 Nacos 中有 transcode 配置，则以 Nacos 为准。**建议在 Nacos 中显式配置**，便于多环境统一管理。

## 路径说明

- **Windows**：建议使用 `D:/path/to/ffmpeg.exe`，避免反斜杠 `\` 在 YAML 中的转义问题
- **Linux**：如 `/usr/bin/ffmpeg`

## 验证

1. 重启 `xc-plus-content-service` 服务
2. 查看启动日志中的 `Transcode config: ffmpegPath=xxx`，确认配置已加载
3. 上传 AVI 格式视频，应自动触发转码
4. 转码日志：`Starting transcoding: xxx to MP4, using ffmpeg: <你配置的路径>`

## 备选方案：添加到系统 PATH

若配置文件始终不生效，可将 FFmpeg 的 `bin` 目录加入系统环境变量 PATH：

- **Windows**：`此电脑` → `属性` → `高级系统设置` → `环境变量` → 编辑 `Path`，添加 `D:\path\to\ffmpeg\bin`
- **Linux**：`export PATH=$PATH:/usr/bin`（若 ffmpeg 在 /usr/bin）

加入 PATH 后，默认配置 `ffmpeg-path: ffmpeg` 即可工作，无需在配置文件中写绝对路径。
