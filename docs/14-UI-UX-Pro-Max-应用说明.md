# 学成在线三代 - UI-UX-Pro-Max 设计系统应用说明

> 本文档记录前端开发中 **UI-UX-Pro-Max** 设计系统的具体应用位置，便于开发效果检验。

---

## 一、设计系统来源

- **技能**：https://github.com/nextlevelbuilder/ui-ux-pro-max-skill
- **行业**：在线教育 / 知识付费
- **参考**：ai.codefather.cn/painting 的现代 AI 风格（大图、渐变遮罩、居中文案）

---

## 二、UI-UX-Pro-Max 应用位置清单

### 2.1 配色系统（`src/styles/main.css`）

| 变量 | 值 | 来源 |
|------|-----|------|
| `--color-primary` | `#1a1a2e` | UI-UX-Pro-Max 教育行业深色主色 |
| `--color-accent` | `#e9b44c` | 琥珀色/金色 CTA，吸引付费 |
| `--color-cta` | `#e9b44c` | 转化导向 CTA 色 |
| `--color-bg` | `#0f0f1a` | 深色背景 |
| `--color-bg-card` | `rgba(26, 26, 46, 0.85)` | 玻璃态卡片背景 |

**对应规则**：`color` 域 - 教育/知识付费行业调色板

---

### 2.2 字体系统（`index.html` + `main.css`）

| 用途 | 字体 | 来源 |
|------|------|------|
| 标题 | Source Serif 4 | UI-UX-Pro-Max 字体配对 - 优雅、专业 |
| 正文 | Noto Sans SC | 中文无衬线，可读性佳 |

**对应规则**：`typography` 域 - 教育/知识付费字体配对

---

### 2.3 间距与动效（`main.css`）

| 项目 | 值 | 来源 |
|------|-----|------|
| 间距基准 | 8px (0.25rem) | UI-UX-Pro-Max 8px 节奏 |
| 过渡时长 | 150ms / 200ms / 300ms | `duration-timing` 规则 |
| 圆角 | 6px / 10px / 16px / 24px | 统一圆角体系 |

**对应规则**：`ux` 域 - `duration-timing`、`spacing`

---

### 2.4 布局与组件

| 组件/页面 | 应用点 | 对应规则 |
|-----------|--------|----------|
| **Header 导航** | 浮动导航 + 玻璃态 (backdrop-filter) | `floating-navbar`、`glass-card` |
| **Banner 轮播** | 大图 + 渐变遮罩 + 居中文案 | 参考 ai.codefather.cn |
| **分类网格** | Bento Grid 风格 | `bento-grid` 风格 |
| **课程卡片** | hover 状态 + 边框高亮 | `cursor-pointer`、`hover-feedback` |
| **CTA 按钮** | 金色主色 + 阴影 | 转化导向配色 |

---

### 2.5 可访问性（`main.css`）

| 项目 | 实现 | 对应规则 |
|------|------|----------|
| `prefers-reduced-motion` | 动画/过渡降级 | `reduced-motion` |
| `:focus-visible` | 2px 金色轮廓 | `focus-states` |
| 最小对比度 | 正文 `#e8e8e8` 于深色背景 | `color-contrast` |

---

### 2.6 图标与交互

| 项目 | 实现 | 对应规则 |
|------|------|----------|
| 箭头/图标 | SVG（非 emoji） | `no-emoji-icons` |
| 可点击元素 | `cursor-pointer` | `cursor-pointer` |
| 悬停反馈 | 颜色/阴影过渡 | `hover-feedback` |

---

## 三、与 ai.codefather.cn 的对应关系

| 设计元素 | ai.codefather.cn 风格 | 学成在线实现 |
|----------|------------------------|--------------|
| 主视觉 | 大图 + 渐变遮罩 | Banner 轮播 + `banner-overlay` |
| 文案布局 | 居中、层次分明 | `banner-content` 居中 |
| 配色 | 深色 + 高亮色 | 深色主色 + 琥珀色 CTA |
| 导航 | 浮动/玻璃态 | Header `backdrop-filter` |

---

## 四、验证检查清单

开发完成后可逐项核对：

- [ ] 配色：深色主色 + 琥珀色 CTA
- [ ] 字体：Source Serif 4 + Noto Sans SC
- [ ] 动效：150–300ms 过渡
- [ ] 无 emoji 作为图标（使用 SVG）
- [ ] 可点击元素有 `cursor-pointer`
- [ ] 焦点状态可见
- [ ] `prefers-reduced-motion` 已处理
- [ ] Banner 轮播正常切换
- [ ] 响应式：375px / 768px / 1024px

---

## 五、占位图说明

Banner 使用 placehold.co 占位图，格式示例：

```
https://placehold.co/1920x600/1a1a2e/e9b44c?text=Banner+1
```

后续可替换为真实课程/活动图片。
