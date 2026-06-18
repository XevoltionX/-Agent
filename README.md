# 高翠网 — AI翡翠匹配平台

移动端H5翡翠货源智能匹配平台。AI理解用户需求，生成用户需求资料匹配ES全文+tag搜索匹配商品，SSE流式对话。

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | Vue 3 + Vite + Vant 4 + Pinia |
| 后端 | Spring Boot 3 + MyBatis-Plus |
| 数据库 | MySQL 8 |
| 缓存 | Redis |
| 搜索 | Elasticsearch 8 + IK分词器 |
| AI | DeepSeek V4 Pro/Flash + 千问VL |
| 支付 | 支付宝沙箱 |
| 认证 | JWT + 邮箱验证码 |

## 性能功能调优

- 🏷️VIP充值支付宝支付 — 升级权益，完成支付宝沙箱支付测试
- 🧠多语言多轮对话多模型Agent智能匹配 — 限定模型输出repy字段多语言,根据上一段对话动态发送不同长度对话历史,设计工厂模式实现多种模型接入,模型返回需求解析数据,结合es+中文分词器,搜寻top3结果
- 🖼️ 图片识别 — 上传翡翠图到阿里云→千问VL→生成标题/描述/标签等商品信息
- 📋 前端检验 — 前端检验用户输入防止xss注入攻击,查询列表实现懒加载防止数据过大影响性能
- ⁉ 高峰兜底策略 — 根据多模型架构,当初始deepseekpro模型高峰响应超时,调用flash模型异步请求,同时先返回redis内热度前3商品兜底,保证用户使用体验

## 快速启动

### 环境要求
- JDK 21
- Node.js 20+
- Redis
- Mysql
- Elasticsearch
- Docker & Docker Compose

### 2. 配置API Key
```bash
export DEEPSEEK_API_KEY=sk-xxx
export QWEN_API_KEY=sk-xxx
```

### 3. Docker Compose 一键启动
```bash
docker-compose up -d
```

首次启动会自动建表、同步ES索引。

访问 `http://localhost` 即可使用。

### 4. 手动启动（开发）

**前端**
```bash
npm install
npm run dev
```

访问 `http://localhost:5173`

## 项目结构

```
stone/
├── src/                    # Vue 3 前端
│   ├── views/              # 14个页面
│   ├── components/         # 共用组件
│   ├── stores/             # Pinia状态管理
│   ├── api/                # 接口层
│   ├── composables/        # 组合函数
│   └── assets/             # 静态资源
├── stone-server/           # Spring Boot 后端
│   └── src/main/java/com/gaocui/
│       ├── controller/     # 控制器
│       ├── service/        # 业务逻辑
│       ├── mapper/         # MyBatis Mapper
│       ├── entity/         # 实体
│       ├── llm/            # AI模型客户端
│       └── scheduler/      # 定时任务
├── docs/                   # 文档
│   ├── 高翠网PRD.md        # 产品需求文档
│   ├── 预览图/             # 原型图
│   ├──superpowers/        # 设计文档
│   ├──api接入示例/         # 各个模型api接入示例
│   ├──沙箱接入示例和相关数据/# 沙箱接入示例
│   ├──最终提示词/           # 沙箱接入示例
│   └── 报错/               # 设计过程中报错的日志
├── docker-compose.yml
└── README.md
```

## 功能模块

### 游客端
- 🤖 AI智能匹配 — 自然语言描述需求，Agent解析后ES搜索匹配
- 📱 SSE流式对话 — DeepSeek Pro优先，超时降级Flash
- 🛒 商品详情 — 图片轮播、标签、简介、详情
- ✉️ 联系卖家 — 提交邮箱，自动生成客资

### 商家端
- 📧 邮箱验证码登录/注册（Redis 120s过期）
- 📊 商家后台看板 — 商品数/客资数/VIP权益
- 📦 商品发布 — 上传图片→千问VL AI识别→编辑→发布
- 🏷️ 商品管理 — 已上架/草稿/已下架，编辑/删除/上下架
- 📋 客资管理 — 待联系/已联系，邮箱查看（VIP完整/免费打码）
- 🔔 消息通知 — 新客资提醒 + VIP到期提前通知
- 👤 个人中心 — 账户信息/修改邮箱/通知设置
- 💎 VIP会员 — 支付宝沙箱支付，升级权益

### AI功能
- 🧠 Agent智能匹配 — 用户输入→LLM解析→ES搜索→商品卡片
- 🖼️ 图片识别 — 上传翡翠图→千问VL→生成标题/描述/标签
- 📝 多轮对话 — 上下文管理，新搜索/跟进判断
- 🌐 多语言 — reply自动适配用户语言

## API接口

| 模块 | 端点 |
|------|------|
| Auth | POST /api/send-code, POST /api/login, GET /api/me |
| Product | POST /api/products/publish, PUT/DELETE /api/products/:id, GET /api/products |
| Search | GET /api/search?keyword=&tags= |
| Chat | POST /api/chat, GET /api/chat/stream (SSE) |
| Lead | GET /api/leads, GET /api/leads/:id, PUT /api/leads/:id/contact |
| Upload | POST /api/upload, POST /api/upload/base64 |
| AI | POST /api/ai/recognize (千问VL图片识别) |
| Notify | GET /api/notifications, GET /api/notifications/unread-count |
| Alipay | POST /api/alipay/create, GET /api/alipay/pay, POST /api/alipay/notify |

## 配置说明

### application.yml 关键配置
```yaml
spring:
  datasource:     # MySQL连接
  mail:           # SMTP邮箱（验证码发送）
  data.redis:     # Redis连接

gaocui:
  elasticsearch:  # ES连接
  llm:
    deepseek-api-key:    # DeepSeek API Key
    qwen-api-key:        # 千问 API Key

alipay:
  app-id:               # 支付宝沙箱AppID
  notify-url:           # 支付回调地址（需natapp内网穿透）
```

## 遗憾不足和思考反思

1. **Agent沙箱** - 为了保证模型输入的**安全性**,最有效的方法还是用**弱模型在沙箱环境**里检验用户输入后,再给真实业务模型使用,原本打算做**CubeSandbox**拉起**云沙箱MicroVM**,但是我的云服务器~~在跑我的世界服务器~~性能和内存存储都是最低挡位,剩余的性能不足以部署云沙箱和ES等组件,只能作罢.
2. **分享功能** - 分享在prd文件里没有指明分享的内容,页面设计倒不是主要问题,主要是不清楚是分享商品亦或是系统本身,不太清晰,不过prd也没有指明要做,还好吧
3. **二次筛选** - 原本想让模型返回时额外输出关键条件**价格和品类**,在es搜索到结果内进行**二次筛选**来加强匹配效果,实际上发现es搜索和模型匹配做好<u>(尤其是顺带输出目前有的tag让模型模仿着写tag)</u>之后,匹配效果已经十分好用,且像""预算10万左右",这种范围不好划定容易画蛇添足还是交给模型和es为好
4. **视图生文效果保守** - 让千问根据上传的图片生成商品详情跟prd里的感觉不对,tag打的相当保守,基本平均都是5-8个,prd预览图里能列三行的tag,价钱也定的相当之便宜,不过考虑到会由用户编辑后正式发布所以价格和tag反倒是用户自己会改.反而由于每次生成数据都会完整的查询tag表,形成了类似**自学习的机制**,阴差阳错做的不错,假设后续改进的话应该会在**提示词上优化tag数量和价钱定价**吧,还有当图片实在识别不出时返回不是翡翠,不过这牵扯的可就大了...
5. **存储** - 直接存在本地数据库,也不是不行,就是用久了肯定会卡吧,真实项目的话果然还是七云牛大法好.如果还有的改进空间的话,我想想,或许可以商品下架时删除商品图片,然后重新上架的时候找商家再次上传图片,其实这个方法还真不赖,不对,那商家不如直接重新发布好了.....好吧,看来还是七云牛是版本答案
