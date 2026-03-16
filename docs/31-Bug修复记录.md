# 学成在线 xuechengPlus3 - Bug修复记录（第31批）

> 开始日期：2026-03-16
> 状态：已完成

---

## 问题列表

| 问题ID | 描述 | 状态 | 修复日期 |
|--------|------|------|----------|
| #31-01 | 课程列表封面图片显示为 `media:xxx` 而非图片URL | ✅ 已修复 | 2026-03-16 |
| #31-02 | 预览页面收费规则显示错误；缺少授课教师信息 | ✅ 已修复 | 2026-03-16 |
| #31-03 | 课程大纲编辑页缺少试看开关；章节标题未完整显示 | ✅ 已修复 | 2026-03-16 |
| #31-04 | 师资管理页面按钮语义不明；四步页面按钮布局不统一 | ✅ 已修复 | 2026-03-16 |

---

## #31-01: 课程列表封面图片显示错误 ✅

### 复现步骤
1. 以 teacher1_org1 身份登录
2. 筛选草稿状态课程（courseStatus: "000"）
3. 编辑课程「STM32嵌入式系统开发」
4. 上传课程图片1张，显示上传成功且缩略图正常
5. 返回课程列表

### 问题分析
- 后端返回的 `pic` 字段格式为 `media:fileId`
- 课程列表直接使用 `c.pic` 作为img的src，没有进行URL转换

### 修复方案
在 `CoursesView.vue` 中添加：
1. `getFileInfo` API 调用导入
2. `extractFileId()` 函数解析 `media:fileId`
3. `getCoursePicUrl()` 函数获取实际URL
4. `loadCoursePicUrls()` 批量加载所有课程封面
5. 模板中使用 `coursePicUrls.get(c.id!)` 替代 `c.pic`

### 修改文件
- `frontend/src/views/CoursesView.vue`

---

## #31-02: 预览页面收费规则显示错误 + 缺少授课教师信息 ✅

### 复现步骤
1. 编辑课程营销信息，设置收费规则为「收费」
2. 保存并进入预览页面

### 问题分析
1. 收费规则判断逻辑错误：
   - 原代码：`charge === '201001'` 显示免费
   - 正确逻辑：`201000`免费，`201001`收费
2. 预览页面没有授课教师信息展示区域

### 修复方案
1. 修正收费规则判断逻辑：
   ```vue
   <a-tag v-if="course.charge === '201000'" color="green">免费</a-tag>
   <a-tag v-else-if="course.charge === '201001' && course.price" color="red">¥{{ course.price }}</a-tag>
   ```
2. 添加授课教师信息展示区域：
   - 导入 `listTeachers` API
   - 在 `load()` 函数中加载教师数据
   - 添加教师信息展示HTML和样式

### 修改文件
- `frontend/src/views/CoursePreviewView.vue`

---

## #31-03: 课程大纲编辑页问题 ✅

### 问题分析
1. 节（grade=2）缺少试看开关（is_preview_enabled字段）
2. `.node-label` 设置了 `white-space: nowrap`，导致长标题被截断
3. 右侧媒资面板存在，但用户可能没有注意到

### 修复方案
1. 在节节点添加试看开关：
   ```vue
   <label class="preview-toggle" title="试看开关" @click.stop>
     <input type="checkbox" :checked="child.isPreviewEnabled === '1'" @change="togglePreview(child)" />
     <span class="preview-label">试看</span>
   </label>
   ```
2. 添加 `togglePreview()` 函数处理试看状态切换
3. 修改节点标题样式，允许完整显示：
   ```css
   .node-label {
     white-space: normal;
     word-break: break-word;
     line-height: 1.4;
   }
   ```

### 修改文件
- `frontend/src/views/CourseEditView.vue`

---

## #31-04: 师资管理页面按钮语义不明 + 四步页面按钮不统一 ✅

### 预期结果
- 前三步（基本信息、课程营销、课程大纲）：【取消】【保存草稿】【下一步】
- 第四步（师资管理）：【取消】【保存草稿】【提交审核】
- 每一步都保留【预览】【提交审核】按钮（大而醒目）

### 修复方案
1. 统一四步页面的按钮布局
2. 添加新函数：
   - `saveAndNext(currentStep)` - 保存并进入下一步
   - `saveTeachersAndDraft()` - 保存草稿
   - `saveTeachersAndSubmit()` - 提交审核
3. 移除header原有的预览/提交按钮，分散到每一步中
4. 添加新按钮样式：
   - `.form-actions` - 主要按钮组
   - `.btn-secondary` - 次要按钮（取消）
   - `.btn-next` - 下一步按钮
   - `.preview-actions` - 预览/提交按钮区域

### 修改文件
- `frontend/src/views/CourseEditView.vue`

---

## 测试验证

### 31.1 次测试（小 c 初版修复后）

| 问题ID | 验证项 | 结果 | 备注 |
|--------|--------|------|------|
| #31-01 | 上传课程图片后，课程列表正常显示封面图片 | ✅ 通过 | — |
| #31-01 | 封面图片能够正常加载（不再显示 `media:xxx`） | ✅ 通过 | — |
| #31-02 | 收费课程预览页面显示「¥xxx」标签 | ✅ 通过 | — |
| #31-02 | 免费课程预览页面显示「免费」标签 | ✅ 通过 | — |
| #31-02 | 预览页面显示授课教师信息 | ✅ 通过 | UI 可再优化：教师区域做成单独 card，与周围有颜色和 margin 区分 |
| #31-03 | 试看开关以章为单位 | ❌ 未通过 | 应以章为试看单元，子节不单独设置 |
| #31-03 | 试看开关可正常切换 | ✅ 通过 | 消息提示良好，但试看范围不对 |
| #31-03 | 章节标题完整显示 | ❌ 未通过 | 显示范围不够，标题挤在一起 |
| #31-04 | 每一步有【取消】【保存草稿】【下一步】按钮 | ✅ 通过 | — |
| #31-04 | 第四步有【提交审核】替代【下一步】 | ✅ 通过 | — |
| #31-04 | 每一步有【预览】【提交审核】按钮 | ✅ 通过 | 按钮过于拥挤，需限制展示区域、优化样式 |

---

### 31.2 次测试（针对 31.1 反馈的修复）

**修复内容：**

1. **按钮布局优化**
   - 将【取消】【预览】【保存草稿】【提交审核】【下一步】合并为单行水平居中
   - 各按钮样式区分：取消（描边灰）、预览（描边金）、保存草稿（填充金）、提交审核/下一步（填充深色）

2. **试看规则调整**
   - 试看开关移至章级别，移除节级别试看
   - 规则：章设为试看时，其所有子节自动视为试看
   - 切换章试看时，递归更新该章及所有子节的 `isPreviewEnabled`

3. **章节标题显示**
   - 章节标题单行显示，不换行
   - 充分利用横向空间，超长时省略号截断

4. **媒资展示简化**
   - 课程大纲编辑页右侧媒资区：不再展示视频文件列表
   - 统一为播放图标 + 总时长；多视频时显示时长总和
   - 提供「管理」链接，可展开查看文件列表并进行解绑等操作
   - 课程预览页教学大纲：小节展示为播放图标 + 时长，不再展示文件名列表

5. **媒资区改为抽屉**
   - 点击小节后，媒资区以抽屉形式从右侧拉出，不占用主页面宽度
   - 流程：选择小节 → 上传媒资 → 可预览
   - 四步页面宽度统一，章节树全宽展示

6. **媒资列表默认展开**
   - 媒资文件列表默认展开，用户可点击「收起」折叠
   - 折叠后可点击「展开」恢复显示

7. **章节标题 30 字限制**
   - 新增/编辑章节时，标题最多 30 字
   - 输入框 `maxlength="30"`，弹窗输入时校验并提示

**修改文件：**
- `frontend/src/views/CourseEditView.vue`
- `frontend/src/views/CoursePreviewView.vue`

**31.2 测试结果：**

| 验证项 | 结果 | 备注 |
|--------|------|------|
| 按钮单行水平居中，样式区分明显 | ✅ 通过 | — |
| 试看仅章级别，子节继承 | ✅ 通过 | 待访客视角再检查一遍 |
| 章节标题单行显示 | ✅ 通过 | — |
| 媒资区显示播放图标 + 时长，无文件列表 | ❌ 未通过 | 预览区缺少视频按钮和子节总时长 |
| 点击小节后抽屉从右侧拉出，展示媒资上传/管理 | ⚠️ 部分通过 | 展示区域需拉宽；需支持 md/pdf 在组件/窗口内预览 |
| 媒资列表默认展开，可折叠/展开 | ✅ 通过 | — |
| 章节标题最多 30 字 | ✅ 通过 | — |

---

### 31.3 次测试（针对 31.2 反馈的修复）

**修复内容：**

1. **预览区播放与时长**
   - 课程预览页教学大纲：每个小节显示播放图标 + 该小节下所有视频的总时长
   - 提供视频播放按钮，点击可播放/预览

2. **媒资抽屉优化**
   - 抽屉展示区域拉宽（如 width 由 380 调整为 420 或更大）
   - 支持 md、pdf 文件在抽屉内组件/窗口预览，无需跳转新窗口
   - 预留适当大小的预览窗口区域
   - 支持页数显示（如 1/n），便于多页文档浏览

**修改文件：**
- `frontend/src/views/CourseEditView.vue`
- `frontend/src/views/CoursePreviewView.vue`

**实现说明：**
- 抽屉内仅保留「预览」按钮，点击后弹出**同页面 Modal**（800px 宽），视频/PDF/MD 在弹窗中预览，不缩在抽屉内
- 媒资抽屉 width 由 380 调整为 440
- PDF 使用 pdfjs-dist 获取总页数，显示「1/n」及上一页/下一页
- 预览页小节：播放图标可点击，点击后弹出同页面 Modal 预览第一个媒资

**31.3 测试结果：**

| 验证项 | 结果 | 备注 |
|--------|------|------|
| 预览页小节显示播放图标 + 子节下所有视频总时长 | ❌ 未通过 | 已上传视频都没有显示出时长，需要筛选视频文件并计算时长总和 |
| 预览页小节有视频播放/预览按钮 | ⚠️ 部分通过 | 有按钮但不要求实际播放 |
| 媒资抽屉展示区域更宽（440px） | ✅ 通过 | — |
| 点击预览后弹出同页面 Modal，视频/PDF/MD 在弹窗中预览 | ⚠️ 部分通过 | PDF通过，MD要求下载需MD控件 |
| 多页 PDF 显示页数 1/n，支持上一页/下一页 | ❌ 未通过 | 翻页功能不起作用，不需要保留 |

---

### 31.4 次测试（针对 31.3 反馈的修复）

**修复内容：**

1. **视频时长存储与显示**
   - 添加 `duration` 字段到 `media_file` 和 `teachplan_media` 表
   - 后端实体类和VO添加 `duration` 字段
   - 新增 `updateMediaDuration` API 更新视频时长
   - 前端上传视频后通过 video 元素获取时长并更新到后端
   - 预览页和编辑页显示视频总时长

2. **移除PDF翻页功能**
   - 移除 pdfjs-dist 依赖
   - 移除页数显示（1/n）和翻页按钮
   - PDF预览简化为直接iframe展示

3. **MD文件预览**
   - MD文件暂时保持iframe预览（"在新窗口打开"）
   - 用户可选择在新窗口打开查看

**修改文件：**

**后端：**
- `backend/c-plus-content-service/src/main/resources/initSql/v3_1_add_media_duration.sql`（新增）
- `backend/.../model/entity/MediaFile.java`
- `backend/.../model/vo/MediaFileVO.java`
- `backend/.../model/entity/TeachplanMedia.java`
- `backend/.../model/vo/TeachplanMediaVO.java`
- `backend/.../controller/TeachplanMediaController.java`
- `backend/.../service/TeachplanMediaService.java`
- `backend/.../service/impl/TeachplanMediaServiceImpl.java`
- `backend/.../util/VideoDurationUtil.java`（新增）

**前端：**
- `frontend/src/api/content/teachplanMedia.ts`
- `frontend/src/views/CourseEditView.vue`

**实现说明：**

1. **数据库字段添加**
   ```sql
   ALTER TABLE `media_file` ADD COLUMN `duration` int DEFAULT NULL COMMENT '视频时长(秒)' AFTER `file_type`;
   ALTER TABLE `teachplan_media` ADD COLUMN `duration` int DEFAULT NULL COMMENT '视频时长(秒)' AFTER `media_type`;
   ```

2. **前端获取视频时长**
   - 上传视频成功后，创建隐藏的 video 元素
   - 监听 `loadedmetadata` 事件获取 `duration`
   - 调用 `updateMediaDuration` API 更新到后端

3. **移除PDF翻页**
   - 删除 `pdfNumPages`、`pdfCurrentPage` 变量
   - 删除 `loadPdfNumPages` 函数
   - 删除 `.page-nav-btn` 样式
   - PDF预览简化为直接iframe展示

**31.4.1 新增：MD5重复文件处理**
- **问题**：MySQL已有MD5记录但MinIO文件被删除后，重新上传相同文件时报错
- **修复**：
  - 修改 `MediaUploadServiceImpl.uploadSimple()` 和 `completeChunkUpload()`
  - 检测到MySQL已有记录但MinIO文件不存在时，UPDATE原记录而非INSERT
  - 更新文件路径、文件名、大小、时间和操作人

**31.4.2 新增：AVI视频同步转码支持**
- **问题**：AVI格式视频无法在浏览器中直接播放，需要转码为MP4
- **方案**：
  - 上传AVI文件到MinIO（支持断点续传）
  - 上传完成后自动触发同步转码
  - 转码后的MP4文件保存到MinIO
  - 自动更新teachplan_media关联关系
  - 自动删除原AVI文件（MySQL记录 + MinIO文件）节省存储
- **后端实现**：
  - 新增 `TranscodeProperties` 配置类，支持自定义FFmpeg路径
  - 新增 `VideoTranscodeService` 使用FFmpeg转码为MP4
  - 扩展 `VideoDurationUtil` 添加 `transcodeToMp4()` 方法
  - 新增 `/media/{fileId}/transcode/check` 和 `/media/{fileId}/transcode` API
  - 转码成功后自动更新 `teachplan_media.file_id` 为新MP4的fileId
  - 转码成功后删除原AVI的 `media_file` 记录和MinIO文件
- **前端实现**：
  - 上传AVI等不支持格式后，自动触发转码（无需确认）
  - 显示转码进度提示
  - 转码完成后提示"转码完成，原AVI文件已自动删除"
  - 预览页检测不支持格式时，提示用户转码
- **配置要求**：
  - 需在 `application-local.yml` 中配置 FFmpeg 路径
  - 示例：`transcode.ffmpeg-path: D:\myDataSJTU\ffmpeg\bin\ffmpeg.exe`

**修改文件（31.4新增）：**

**后端：**
- `backend/.../config/TranscodeProperties.java` - 转码配置类（新增）
- `backend/.../service/impl/MediaUploadServiceImpl.java` - MD5重复处理
- `backend/.../util/VideoDurationUtil.java` - 转码功能，支持自定义FFmpeg路径
- `backend/.../service/VideoTranscodeService.java` - 转码服务接口
- `backend/.../service/impl/VideoTranscodeServiceImpl.java` - 转码服务实现，使用配置
- `backend/.../controller/MediaController.java` - 转码API
- `backend/.../resources/application-local.yml` - 添加转码配置
- `backend/.../resources/application-example.yml` - 添加转码配置示例

**前端：**
- `frontend/src/api/content/media.ts` - 转码API、格式检测
- `frontend/src/views/CourseEditView.vue` - AVI上传后自动转码
- `frontend/src/views/CoursePreviewView.vue` - AVI预览限制

**配置说明：**
在 `application-local.yml` 中添加：
```yaml
transcode:
  ffmpeg-path: D:/myDataSJTU/ffmpeg/bin/ffmpeg.exe  # Windows 建议用正斜杠
  # ffmpeg-path: /usr/bin/ffmpeg  # Linux路径
  auto-transcode: true
  timeout-seconds: 600
```

**31.4.3 新增：MD 弹窗预览 + FFmpeg Nacos 配置**

1. **MD 预览**：iframe 加载 .md 会触发浏览器下载。改为后端接口 `/media/{fileId}/content` 返回文本，前端用 `marked` 解析为 HTML 在弹窗内渲染，实现直接预览。
2. **FFmpeg Nacos**：`application-local.yml` 的 transcode 可能被 Nacos 覆盖。在 Nacos 中配置 `xc-plus-content-service.yaml` 或 `xc-plus-content-service-local.yaml`，添加 transcode 块，详见 `docs/nacos-transcode-config.md`。

---

## 31.4 待验证批复（文书整理）

> 批复日期：2026-03-16  
> 状态：已归档，遗留问题已迁移至 **32-视频转码相关-待处理问题.md**

### 一、已通过项

| 序号 | 验证项 | 批复结论 |
|------|--------|----------|
| 1 | 执行 SQL：`v3_1_add_media_duration.sql` | 通过 |
| 2 | 上传新视频后，能正确获取并显示时长 | 通过 |
| 3 | PDF 预览不再显示翻页按钮 | 通过 |
| 4 | MinIO 删除文件后重新上传相同 MD5 文件成功 | 通过 |

### 二、待进一步验证项

| 序号 | 验证项 | 批复说明 |
|------|--------|----------|
| 1 | 预览页小节显示视频总时长（MM:SS 格式） | 时长已显示。当前预览页可预览 pdf 和 md 文档，是否与子节绑定媒资顺序相关，待验证 |
| 2 | MD 文件可点击「在新窗口打开」 | 当前新窗口打开行为不符合预期，md 不需要在新窗口打开 |

### 三、遗留待处理项（视频转码相关）

| 序号 | 验证项 | 批复说明 |
|------|--------|----------|
| 1 | 上传 AVI 格式视频时自动转码（配置好 FFmpeg 路径后） | 路径配置正确，但有超时时间限制（10s），需转为后台任务，标记为待处理，需评估方案 |
| 2 | 转码后的 MP4 文件正常播放 | 同上 |
| 3 | 大文件转码时显示进度提示 | 同上 |
| 4 | 转码完成后原 AVI 文件从 MinIO 和 MySQL 中删除 | 同上 |

> **说明**：上述遗留项已整理至 `32-视频转码相关-待处理问题.md`，待后续批次处理。

---

*31 批次已完成归档*
