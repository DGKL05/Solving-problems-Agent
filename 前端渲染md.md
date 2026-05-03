前端开发中，我们常需要将 Markdown 文本（如接口文档、博客内容、用户评论）渲染成美观的 HTML 页面。不同于纯文本展示，Markdown 渲染需要借助专门的库解析语法规则，再结合样式实现可视化。本文将聚焦 “如何在前端页面中渲染 Markdown 内容”，从主流库选型到实战案例，带你快速掌握核心方法。

一、前端渲染 Markdown 的核心逻辑
Markdown 本质是 “轻量级标记语言”，无法直接被浏览器识别。前端渲染的核心流程是：

解析：通过库将 Markdown 文本（如 # 标题）转换为 HTML 字符串（如 <h1>标题</h1>）；
渲染：将解析后的 HTML 插入页面 DOM 中；
美化：通过 CSS（或现成样式库）优化排版（如标题间距、代码块高亮）；
增强（可选）：支持表格、公式、代码高亮、自定义组件等进阶功能。
目前主流的前端 Markdown 渲染库有 marked.js（轻量灵活）、showdown.js（功能全面）、React-Markdown（React 生态专用）等，下文将逐一讲解其用法。

二、3 个主流渲染库实战教程

1. marked.js：轻量首选（原生 JS / 框架通用）
   marked.js 是目前最流行的 Markdown 解析库之一，体积小（约 30KB）、解析速度快，支持自定义渲染规则，适合原生 JS 项目或各类框架（Vue/React）。

步骤 1：安装与引入
CDN 引入（快速测试，无需构建工具）：

<!-- 引入marked核心库 -->

<script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>

<!-- 可选：代码高亮需搭配highlight.js -->

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/styles/github-dark.min.css">

<script src="https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/lib/highlight.min.js"></script>

运行项目并下载源码
html
npm 安装（工程化项目）：
npm install marked highlight.js --save
运行项目并下载源码
bash
步骤 2：基础渲染示例（原生 JS）
实现 “输入 Markdown 文本，实时预览渲染结果” 的功能：

<!-- HTML结构：输入区 + 预览区 -->

<div class="container">
  <textarea id="markdownInput" placeholder="请输入Markdown内容..."># 标题
**加粗文本**
`代码片段`
- 列表项1
- 列表项2</textarea>
  <div id="previewArea" class="markdown-body"></div>
</div>

<script>
// 1. 获取DOM元素
const input = document.getElementById('markdownInput');
const preview = document.getElementById('previewArea');
 
// 2. 配置marked（启用代码高亮）
marked.setOptions({
  highlight: (code, lang) => {
    // 若指定语言且highlight支持，則高亮；否则默认处理
    return lang && hljs.getLanguage(lang) 
      ? hljs.highlight(code, { language: lang }).value 
      : hljs.highlightAuto(code).value;
  },
  breaks: true, // 支持换行符（\n）转换为<br>
  gfm: true // 支持GitHub Flavored Markdown（如表格、删除线）
});
 
// 3. 渲染函数：将Markdown转为HTML并插入预览区
function renderMarkdown() {
  const markdownText = input.value;
  const html = marked.parse(markdownText); // 核心解析方法
  preview.innerHTML = html;
}
 
// 4. 初始化渲染 + 监听输入变化
renderMarkdown();
input.addEventListener('input', renderMarkdown);
</script>

<!-- 基础样式：避免排版混乱 -->

<style>
.container { display: flex; gap: 20px; margin: 20px; }
#markdownInput { width: 40%; height: 500px; padding: 10px; }
#previewArea { width: 50%; padding: 10px; border: 1px solid #eee; }
/* 配合highlight.js的代码块样式 */
pre code { display: block; padding: 10px; background: #1e1e1e; color: #fff; border-radius: 4px; }
</style>

运行项目并下载源码
html

步骤 3：进阶配置（自定义渲染）
若需修改默认渲染规则（如自定义标题标签、链接 跳转方式），可通过marked.Renderer()实现：

// 自定义渲染器
const renderer = new marked.Renderer();

// 示例1：将<h1>标题改为带class的<h1 class="custom-h1">
renderer.heading = (text, level) => {
return `<h${level} class="custom-h${level}">${text}</h${level}>`;
};

// 示例2：所有链接默认新窗口打开（添加target="_blank"）
renderer.link = (href, title, text) => {
const titleAttr = title ? `title="${title}"` : '';
return `<a href="${href}" ${titleAttr} target="_blank" rel="noopener">${text}</a>`;
};

// 使用自定义渲染器
const html = marked.parse(markdownText, { renderer });
运行项目并下载源码
javascript
运行

2. React-Markdown：React 生态专用（安全无 XSS）
   如果是 React 项目，推荐使用react-markdown（而非直接用 marked+innerHTML，避免 XSS 风险）。它基于组件化思想，支持插件扩展，且默认过滤危险 HTML。

步骤 1：安装依赖
npm install react-markdown @types/react-markdown highlight.js --save

# 若需支持表格、公式等，需安装对应插件

npm install remark-gfm remark-math rehype-katex --save
运行项目并下载源码
bash
步骤 2：React 组件示例（支持代码高亮 + 表格 + 公式）
import React, { useState } from 'react';
import ReactMarkdown from 'react-markdown';
// 插件：支持GitHub样式（表格、删除线）、数学公式
import remarkGfm from 'remark-gfm';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
// 代码高亮样式
import 'highlight.js/styles/github-light.min.css';
// 公式样式（需引入katex.css）
import 'katex/dist/katex.min.css';

const MarkdownPreview = () => {
// 初始Markdown内容（可替换为接口请求的数据）
const [markdownText, setMarkdownText] = useState(`# React-Markdown示例

## 1. 表格（需remark-gfm插件）


| 姓名 | 技术栈 |
| ---- | ------ |
| 张三 | React  |
| 李四 | Vue    |

## 2. 代码块（自动高亮）

\`\`\`javascript
// React组件示例
function App() {
return <h1>Hello Markdown</h1>;
}
\`\`\`

## 3. 数学公式（需remark-math+rehype-katex）

欧拉公式：$e^{iπ} + 1 = 0$
`);

return (
<div style={{ display: 'flex', gap: '20px', padding: '20px' }}>
{/* 输入区 */}
<textarea
value={markdownText}
onChange={(e) => setMarkdownText(e.target.value)}
style={{ width: '40%', height: '600px', padding: '10px' }}
/>
{/* 预览区：核心组件ReactMarkdown */}
<ReactMarkdown
className="markdown-body" // 可配合github-markdown-css美化
remarkPlugins={[remarkGfm, remarkMath]} // Markdown语法扩展插件
rehypePlugins={[rehypeKatex]} // HTML处理插件（公式渲染）
style={{ width: '50%', padding: '20px', border: '1px solid #eee' }}
>
{markdownText}
</ReactMarkdown>
</div>
);
};

export default MarkdownPreview;
运行项目并下载源码
javascript
运行

关键优势：

安全：默认不渲染script、iframe等危险标签，无需手动处理 XSS；
插件化：通过remark-*（Markdown 语法扩展）和rehype-*（HTML 处理）插件支持复杂功能；
TS 友好：自带类型定义，避免类型报错。
3. showdown.js：功能全面（适合复杂场景）
showdown.js 是另一个成熟的 Markdown 解析库，支持更多自定义配置（如自动链接、缩写），适合需要高度定制的场景（如企业级文档系统）。

基础使用示例（Vue 项目）：

<template>
  <div class="markdown-container">
    <textarea v-model="markdownText" @input="render" placeholder="输入Markdown..."></textarea>
    <div v-html="htmlContent" class="preview"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import showdown from 'showdown';
import 'highlight.js/styles/atom-one-light.css';
import hljs from 'highlight.js';
 
// 初始化showdown转换器
const converter = new showdown.Converter({
  tables: true, // 支持表格
  strikethrough: true, // 支持删除线（~~文本~~）
  autolink: true, // 自动识别链接（无需[]()）
  extensions: [
    // 自定义扩展：代码高亮
    () => {
      return {
        type: 'output',
        filter: (html) => {
          // 匹配<pre><code>标签，对代码块高亮
          return html.replace(
            /<pre><code([^>]*)>/g,
            (match, attrs) => `<pre><code${attrs}>`,
          ).replace(
            /<code([^>]*)>([\s\S]*?)<\/code>/g,
            (match, attrs, code) => {
              const langMatch = attrs.match(/class="language-(\w+)"/);
              const lang = langMatch ? langMatch[1] : 'plaintext';
              const highlighted = hljs.highlight(code, { language: lang }).value;
              return `<code${attrs}>${highlighted}</code>`;
            },
          );
        },
      };
    },
  ],
});
 
const markdownText = ref('# Showdown Vue示例\n**加粗文本**\n`const a = 1`');
const htmlContent = ref('');
 
// 渲染函数
const render = () => {
  htmlContent.value = converter.makeHtml(markdownText.value);
};
 
// 初始化渲染
onMounted(() => render());
</script>

<style scoped>
.markdown-container { display: flex; gap: 20px; padding: 20px; }
textarea { width: 40%; height: 500px; padding: 10px; }
.preview { width: 50%; padding: 10px; border: 1px solid #eee; }
pre code { padding: 10px; border-radius: 4px; }
</style>

运行项目并下载源码
javascript
运行

三、渲染优化与注意事项

1. 解决 XSS 安全风险
   直接使用innerHTML（如 marked + 原生 JS）可能导致 XSS 攻击（如输入 <script>alert('恶意代码')</script>）。

解决方案：

用DOMPurify净化 HTML（适合原生 JS/showdown）：
npm install dompurify --save
运行项目并下载源码
bash
import DOMPurify from 'dompurify';
// 解析后先净化，再插入DOM
const html = marked.parse(markdownText);
const safeHtml = DOMPurify.sanitize(html); // 过滤危险标签/属性
preview.innerHTML = safeHtml;
运行项目并下载源码
javascript
运行
React 项目优先用react-markdown（默认安全），Vue 项目可配合vue-dompurify-html指令。
2. 样式美化：复用成熟 CSS 库
手动写 Markdown 样式繁琐，推荐直接引入现成样式库，实现 “GitHub  风格”“知乎风格” 等排版：

github-markdown-css（最常用）：

<!-- CDN引入 -->

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/github-markdown-css/github-markdown.min.css">
运行项目并下载源码
javascript
运行
使用时给预览区添加markdown-body类：

<div id="previewArea" class="markdown-body"></div>
运行项目并下载源码
html
其他可选：gitlab-markdown.css（GitLab 风格）、zhihu-markdown.css（知乎风格）。
3. 大文档渲染性能优化
若渲染超长 Markdown（如万字文档），可能导致页面卡顿。优化方案：

分片渲染：只渲染当前可视区域内容（可配合react-window/vue-virtual-scroller）；
懒加载图片：解析 Markdown 中的图片链接，替换为懒加载格式（如loading="lazy"）；
缓存解析结果：对相同的 Markdown 文本，缓存解析后的 HTML，避免重复解析。
四、常见场景选型建议
项目场景	推荐库	核心原因
原生 JS / 小项目	marked.js	轻量、速度快、学习成本低
React 项目	React-Markdown	组件化、安全无 XSS、插件丰富
Vue 项目	showdown.js + vue-dompurify-html	配置灵活、支持 Vue 指令
企业级文档 / 复杂语法	showdown.js	扩展能力强、支持自定义语法
静态站点（如博客）	Next.js/VuePress 内置	无需手动配置，支持 SSR/SSG
————————————————
版权声明：本文为CSDN博主「芸简新章」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/2401_85080888/article/details/154532663
