# 学成在线三代 - 前端

Vue 3 + Vite + TypeScript 前端应用，对接 `xc-plus-content-service`（端口 11001）。

## 技术栈

- Vue 3
- Vite 6
- Vue Router 4
- Pinia
- Axios
- TypeScript

## 启动

```bash
npm install
npm run dev
```

访问 http://localhost:5173

## 构建

```bash
npm run build
```

## 代理配置

开发时 `/content` 代理到 `http://localhost:11001`，请确保后端服务已启动。

## UI 设计

- **UI-UX-Pro-Max** 设计系统应用说明见 `docs/14-UI-UX-Pro-Max-应用说明.md`
- 参考 ai.codefather.cn/painting 风格：大图 + 渐变遮罩 + 居中文案
- 深色主色 + 琥珀色 CTA，转化导向
