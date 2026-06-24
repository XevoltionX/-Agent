# 更新日志

## v1.1.0 (2026-06-24)

### 新增
- 豆包 LLM 客户端（doubao-seed-2-0-mini-260428），VL 图片识别改用 base64
- MySQL LIKE 搜索模式，低配服务器无需安装 ES
- ES 条件加载：`search.mode=mysql` 时完全跳过 ES，不会启动报错
- 标签按使用频次排序取前 100 条（`GROUP BY + COUNT + ORDER BY DESC`）
- 示例商品数据 `docs/sample_products.sql`（15 件翡翠商品 + 3 个商家 + 73 个标签）
- 宝塔面板部署教程 `docs/宝塔部署教程.md`
- `CHANGELOG.md`

### 修复
- SSE 流式传输中文/英文空格丢失问题
- Agent 预算字段改为 JSON 结构化提取，不再从原始输入正则解析
- 防重复提交多层防护（submitting 标记 + 透明遮罩 + pointer-events）
- VL 识别非翡翠图片 price=0 拦截 + 前端 toast 提示
- XSS 过滤（`<>"'` 剥离）
- 图片上传限制（单张 8MB，总数 60MB）
- VL 超时延长至 120s

### 变更
- ES 搜索代码保留但改为条件加载，默认使用 MySQL 搜索模式
- `ProductSearchService` / `ElasticsearchConfig` / `EsInitializer` / `EsController` 加 `@ConditionalOnProperty`
- `ChatService` / `ProductService` ES 依赖改为 `ObjectProvider` 注入

---

## v1.0.0 (2026-06-20)

### 初始版本
- 高翠网 AI 翡翠匹配平台完整功能
- 首页 AI 对话搜索（SSE 流式）
- 商品详情页
- 商家登录/注册（邮箱验证码）
- 商家后台面板
- 商品发布（AI 图片识别生成）
- 商品管理（上架/下架/编辑）
- 客资管理
- 通知系统
- 账户权限管理
- 支付宝沙箱 VIP 支付
- JWT 认证
