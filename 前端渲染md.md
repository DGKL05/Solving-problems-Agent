前端开发中，我们常需要将 Markdown 文本（如接口文档、博客内容、用户评论）渲染成美观的 HTML 页面。不同于纯文本展示，Markdown 渲染需要借助专门的库解析语法规则，再结合样式实现可视化。本文将聚焦 “如何在前端页面中渲染 Markdown 内容”，从主流库选型到实战案例，带你快速掌握核心方法。

在 HTML 中展示你写的这一段数学内容时，公式没有渲染，**根本原因是没有引入数学公式渲染库**。浏览器的引擎本身不识别 LaTeX 语法，`\frac`、`\ln` 这些代码会被当成普通文本显示，必须借助 **MathJax** 或 **KaTeX** 这样的库来解析和渲染。

下面提供一套**即插即用、具体到你的代码**的解决方案。

---

## 推荐方案：使用 MathJax 3（最兼容、配置灵活）

### 1. 完整 HTML 示例（直接可用）

将你的内容放入以下模板，公式即可正常显示：

<pre><div class="efm_ant-codeHighlighter css-var-re4"><div class="efm_ant-codeHighlighter-code"><pre><code class="language-html"><span><span class="token"><!</span><span class="token doctype-tag">DOCTYPE</span><span class="token"> </span><span class="token name">html</span><span class="token">></span><span>
</span></span><span><span></span><span class="token"><</span><span class="token">html</span><span class="token"> </span><span class="token">lang</span><span class="token">=</span><span class="token">"</span><span class="token">zh-CN</span><span class="token">"</span><span class="token">></span><span>
</span></span><span><span></span><span class="token"><</span><span class="token">head</span><span class="token">></span><span>
</span></span><span><span>  </span><span class="token"><</span><span class="token">meta</span><span class="token"> </span><span class="token">charset</span><span class="token">=</span><span class="token">"</span><span class="token">UTF-8</span><span class="token">"</span><span class="token">></span><span>
</span></span><span><span>  </span><span class="token"><</span><span class="token">title</span><span class="token">></span><span>微分方程求解</span><span class="token"></</span><span class="token">title</span><span class="token">></span><span>
</span></span><span><span>  </span><span class="token"><!-- MathJax 配置：关键一步，让它在页面加载后自动渲染 --></span><span>
</span></span><span><span>  </span><span class="token"><</span><span class="token">script</span><span class="token">></span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">    </span><span class="token script language-javascript dom">window</span><span class="token script language-javascript">.</span><span class="token script language-javascript property-access maybe-class-name">MathJax</span><span class="token script language-javascript"> </span><span class="token script language-javascript">=</span><span class="token script language-javascript"> </span><span class="token script language-javascript">{</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">      </span><span class="token script language-javascript literal-property">tex</span><span class="token script language-javascript">:</span><span class="token script language-javascript"> </span><span class="token script language-javascript">{</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">        </span><span class="token script language-javascript">// 行内公式用 $...$ 包裹</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">        </span><span class="token script language-javascript literal-property">inlineMath</span><span class="token script language-javascript">:</span><span class="token script language-javascript"> </span><span class="token script language-javascript">[</span><span class="token script language-javascript">[</span><span class="token script language-javascript">'$'</span><span class="token script language-javascript">,</span><span class="token script language-javascript"> </span><span class="token script language-javascript">'$'</span><span class="token script language-javascript">]</span><span class="token script language-javascript">]</span><span class="token script language-javascript">,</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">        </span><span class="token script language-javascript">// 显示公式（独立成行）用 $$...$$ 或者你原文中的 [ ... ]</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">        </span><span class="token script language-javascript">// 注意：方括号在正则里需转义，但这里写成字符串即可</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">        </span><span class="token script language-javascript literal-property">displayMath</span><span class="token script language-javascript">:</span><span class="token script language-javascript"> </span><span class="token script language-javascript">[</span><span class="token script language-javascript">[</span><span class="token script language-javascript">'$$'</span><span class="token script language-javascript">,</span><span class="token script language-javascript"> </span><span class="token script language-javascript">'$$'</span><span class="token script language-javascript">]</span><span class="token script language-javascript">,</span><span class="token script language-javascript"> </span><span class="token script language-javascript">[</span><span class="token script language-javascript">'['</span><span class="token script language-javascript">,</span><span class="token script language-javascript"> </span><span class="token script language-javascript">']'</span><span class="token script language-javascript">]</span><span class="token script language-javascript">]</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">      </span><span class="token script language-javascript">}</span><span class="token script language-javascript">,</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">      </span><span class="token script language-javascript literal-property">svg</span><span class="token script language-javascript">:</span><span class="token script language-javascript"> </span><span class="token script language-javascript">{</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">        </span><span class="token script language-javascript literal-property">fontCache</span><span class="token script language-javascript">:</span><span class="token script language-javascript"> </span><span class="token script language-javascript">'global'</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">      </span><span class="token script language-javascript">}</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">    </span><span class="token script language-javascript">}</span><span class="token script language-javascript">;</span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">  </span><span class="token"></</span><span class="token">script</span><span class="token">></span><span>
</span></span><span><span>  </span><span class="token"><!-- 引入 MathJax 核心库 --></span><span>
</span></span><span><span>  </span><span class="token"><</span><span class="token">script</span><span class="token"> </span><span class="token">id</span><span class="token">=</span><span class="token">"</span><span class="token">MathJax-script</span><span class="token">"</span><span class="token"> </span><span class="token">defer</span><span class="token">
</span></span><span><span class="token">          </span><span class="token">src</span><span class="token">=</span><span class="token">"</span><span class="token">https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-svg.js</span><span class="token">"</span><span class="token">></span><span class="token script language-javascript">
</span></span><span><span class="token script language-javascript">  </span><span class="token"></</span><span class="token">script</span><span class="token">></span><span>
</span></span><span><span></span><span class="token"></</span><span class="token">head</span><span class="token">></span><span>
</span></span><span><span></span><span class="token"><</span><span class="token">body</span><span class="token">></span><span>
</span></span><span><span>  </span><span class="token"><</span><span class="token">h2</span><span class="token">></span><span>微分方程特解求解过程</span><span class="token"></</span><span class="token">h2</span><span class="token">></span><span>
</span></span><span>
</span><span><span>  </span><span class="token"><!-- 你的原始内容，方括号 [ ... ] 作为显示公式 --></span><span>
</span></span><span><span>  </span><span class="token"><</span><span class="token">p</span><span class="token">></span><span>步骤1: 分离变量</span><span class="token"></</span><span class="token">p</span><span class="token">></span><span>
</span></span><span><span>  </span><span class="token"><</span><span class="token">p</span><span class="token">></span><span>首先，我们将给定的微分方程 \(y' \sin x = y \ln y\) 写成更易于处理的形式。这里，\(y'\) 表示 \(y\) 对 \(x\) 的导数，即 \(\frac{dy}{dx}\)。因此，方程可以写作：</span><span class="token"></</span><span class="token">p</span><span class="token">></span><span>
</span></span><span>  [
</span><span>    \frac{dy}{dx} \sin x = y \ln y
</span><span>  ]
</span><span><span>  </span><span class="token"><</span><span class="token">p</span><span class="token">></span><span>接下来，我们将方程两边按照变量 \(y\) 和 \(x\) 分离，得到：</span><span class="token"></</span><span class="token">p</span><span class="token">></span><span>
</span></span><span>  [
</span><span>    \frac{dy}{y \ln y} = \frac{dx}{\sin x}
</span><span>  ]
</span><span><span>  </span><span class="token"><</span><span class="token">p</span><span class="token">></span><span>步骤2: 积分</span><span class="token"></</span><span class="token">p</span><span class="token">></span><span>
</span></span><span><span>  </span><span class="token"><</span><span class="token">p</span><span class="token">></span><span>现在我们对等式两边进行积分。左边是对 \(y\) 的积分，右边是对 \(x\) 的积分。</span><span class="token"></</span><span class="token">p</span><span class="token">></span><span>
</span></span><span><span>  </span><span class="token"><!-- 后面步骤同理，完整粘贴即可 --></span><span>
</span></span><span>
</span><span><span>  </span><span class="token"><</span><span class="token">p</span><span class="token">></span><span>最终得到特解：</span><span class="token"></</span><span class="token">p</span><span class="token">></span><span>
</span></span><span>  [
</span><span>    y = e^{\csc x - \cot x}
</span><span>  ]
</span><span><span></span><span class="token"></</span><span class="token">body</span><span class="token">></span><span>
</span></span><span><span></span><span class="token"></</span><span class="token">html</span><span class="token">></span></span></code></pre></div></div></pre>

**如果想保留原文里的 `[ ... ]` 定界符**，上面的配置已经支持了。
**更推荐的做法**是把所有 `[ ... ]` 替换为 `$$ ... $$`，这样可以避免方括号与普通文本冲突，也更通用。

### 2. 如果你把公式放在 `<script>` 或 JS 字符串中（如动态加载）

必须处理**转义**问题。例如在 JS 字符串中写 `\frac` 会被转义成换页符，导致公式损坏。解决办法是使用 `String.raw` 或双重反斜杠：

<pre><div class="efm_ant-codeHighlighter css-var-re4"><div class="efm_ant-codeHighlighter-code"><pre><code class="language-javascript"><span><span class="token">// 用 String.raw 避免反斜杠转义</span><span>
</span></span><span><span></span><span class="token">const</span><span> content </span><span class="token">=</span><span> </span><span class="token known-class-name">String</span><span class="token">.</span><span class="token property-access">raw</span><span class="token template-string template-punctuation">`</span><span class="token template-string">
</span></span><span class="token template-string">  当 $a \ne 0$ 时，方程 $ax^2 + bx + c = 0$ 的解为：
</span><span class="token template-string">  $$x = {-b \pm \sqrt{b^2-4ac} \over 2a}.$$
</span><span><span class="token template-string"></span><span class="token template-string template-punctuation">`</span><span class="token">;</span><span>
</span></span><span>
</span><span><span></span><span class="token">// 插入 DOM 后，手动触发渲染</span><span>
</span></span><span><span></span><span class="token dom">document</span><span class="token">.</span><span class="token method property-access">getElementById</span><span class="token">(</span><span class="token">'formula-container'</span><span class="token">)</span><span class="token">.</span><span class="token property-access">innerHTML</span><span> </span><span class="token">=</span><span> content</span><span class="token">;</span><span>
</span></span><span><span></span><span class="token maybe-class-name">MathJax</span><span class="token">.</span><span class="token method property-access">typesetPromise</span><span class="token">(</span><span class="token">[</span><span class="token dom">document</span><span class="token">.</span><span class="token method property-access">getElementById</span><span class="token">(</span><span class="token">'formula-container'</span><span class="token">)</span><span class="token">]</span><span class="token">)</span><span class="token">;</span></span></code></pre></div></div></pre>

### 3. 常见“不渲染”的坑及对策


| 现象                                    | 原因                                         | 解决方法                                                                       |
| --------------------------------------- | -------------------------------------------- | ------------------------------------------------------------------------------ |
| 公式显示为原始代码（如`\frac{dy}{dx}`） | MathJax 未加载或未执行                       | 确认`tex-svg.js`正确引入；检查网络/firewall                                    |
| 页面加载时公式闪白再显示                | MathJax 渲染晚于页面显示                     | 给包含公式的元素加`visibility:hidden`，渲染后再显示；或使用`defer`加载 MathJax |
| 公式完全空白                            | MathML 被误用（Chrome 原生 MathML 支持有限） | 确保内容为 LaTeX 语法，而不是`<math>`标签；一直用 MathJax 处理                 |
| 部分公式未渲染，特别是有`\text{}`或中文 | 中文未在`\text{}`里，或字体缺失              | 中文部分必须包裹在`\text{中文}`中，并确保主字体支持中文（默认是）              |

---

## 轻量替代：KaTeX（更快，但要求 \$\$ 定界符）

如果你的公式量很大、追求加载速度，可改用 KaTeX。但 KaTeX **默认只认 `$$...$$` 和 `$...$`**，不认方括号。因此需要将原文所有 `[` `]` 替换为 `$$`。

<pre><div class="efm_ant-codeHighlighter css-var-re4"><div class="efm_ant-codeHighlighter-code"><pre><code class="language-html"><span><span class="token"><</span><span class="token">link</span><span class="token"> </span><span class="token">rel</span><span class="token">=</span><span class="token">"</span><span class="token">stylesheet</span><span class="token">"</span><span class="token"> </span><span class="token">href</span><span class="token">=</span><span class="token">"</span><span class="token">https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css</span><span class="token">"</span><span class="token">></span><span>
</span></span><span><span></span><span class="token"><</span><span class="token">script</span><span class="token"> </span><span class="token">defer</span><span class="token"> </span><span class="token">src</span><span class="token">=</span><span class="token">"</span><span class="token">https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js</span><span class="token">"</span><span class="token">></span><span class="token"></</span><span class="token">script</span><span class="token">></span><span>
</span></span><span><span></span><span class="token"><</span><span class="token">script</span><span class="token"> </span><span class="token">defer</span><span class="token"> </span><span class="token">src</span><span class="token">=</span><span class="token">"</span><span class="token">https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js</span><span class="token">"</span><span class="token">
</span></span><span><span class="token">        </span><span class="token special-attr">onload</span><span class="token special-attr">=</span><span class="token special-attr">"</span><span class="token special-attr javascript language-javascript">renderMathInElement</span><span class="token special-attr javascript language-javascript">(</span><span class="token special-attr javascript language-javascript dom">document</span><span class="token special-attr javascript language-javascript">.</span><span class="token special-attr javascript language-javascript property-access">body</span><span class="token special-attr javascript language-javascript">)</span><span class="token special-attr javascript language-javascript">;</span><span class="token special-attr">"</span><span class="token">></span><span class="token"></</span><span class="token">script</span><span class="token">></span></span></code></pre></div></div></pre>

---

## 针对特定框架/环境的快速提示

* **微信小程序 (mp-html)**：必须安装 LaTeX 插件，且公式**只能用 `$$...$$`** 包裹，不能用 `\[\]` 或 `[ ]`。同时 JS 中使用 `String.raw` 避免转义。
* **HarmonyOS (ArkWeb)**：官方建议用 `Web` 组件加载一个写好的 HTML 页面，在页面内用 MathJax/KaTeX 渲染公式，因为系统暂未提供原生数学公式组件。

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
