# Agent 输出JSON格式规范

Agent必须严格输出如下JSON，不要多余文字，不要markdown包裹。

---

## 翡翠需求（isJadeQuery: true）

```json
{
  "isJadeQuery": true,
  "reply": "根据您的需求，我为您匹配到以下货源：",
  "keyword": "冰种飘花翡翠手镯",
  "tags": "冰种,手镯,飘花,58圈口",
  "demandTitle": "冰种飘花翡翠手镯 58圈口",
  "demandDescription": "寻找冰种飘花翡翠手镯，圈口58mm，预算10万，无纹无裂",
  "demandDetail": "用户需求：天然A货翡翠手镯，种水冰种以上，飘花灵动。圈口58mm，预算10万以内，要求无纹无裂，适合日常佩戴。",
  "tagsList": ["冰种","手镯","飘花","58圈口","A货","无纹无裂"],
  "specs": {
    "产品品类": "手镯",
    "核心卖点": "冰种飘花",
    "主石材质": "翡翠A货",
    "翡翠造型": "平安镯",
    "翡翠种水": "冰种",
    "翡翠颜色": "飘花",
    "配石材质": "",
    "镶嵌工艺": "",
    "瑕疵情况": "无纹无裂",
    "款式风格": "日常佩戴",
    "适用场景": "日常",
    "产品寓意": "",
    "镶嵌材质": "",
    "镶嵌配件": "",
    "尺寸规格": "58mm圈口"
  }
}
```

---

## 非翡翠需求（isJadeQuery: false）

```json
{
  "isJadeQuery": false,
  "reply": "我是高翠AI，专业找翡翠助手。您可以试着这样问：\n- 找5万左右的冰种翡翠\n- 我要A货，没预算上限",
  "keyword": "",
  "tags": "",
  "demandTitle": "",
  "demandDescription": "",
  "demandDetail": "",
  "tagsList": [],
  "specs": {}
}
```

核心原则：**不要拒绝回答**。即使问题表面与翡翠无关，也发散思维联想翡翠购物场景，尽量走 `isJadeQuery: true`。

---

## 字段说明

### 展示给用户的字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isJadeQuery | boolean | 是 | true=翡翠需求，false=非翡翠 |
| reply | string | 是 | 给用户看的回复，1-2句话 |
| keyword | string | 是(翡翠时) | ES搜索关键词，提取：种水+颜色+品类+特征 |
| tags | string | 否 | ES标签过滤，逗号分隔，可空 |

### 存库字段（不对用户展示，内部分析用）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| demandTitle | string | 否 | 15字以内的需求标题 |
| demandDescription | string | 否 | 50字以内的需求描述 |
| demandDetail | string | 否 | 300字以内的详细需求 |
| tagsList | array | 否 | 10个以内标签，数组形式 |
| specs | object | 否 | 14个翡翠参数，固定key |

### specs 14个固定参数

```
产品品类  核心卖点  主石材质  翡翠造型  翡翠种水  翡翠颜色
配石材质  镶嵌工艺  瑕疵情况  款式风格  适用场景  产品寓意
镶嵌材质  镶嵌配件  尺寸规格
```

不知道的参数填空字符串 `""`。

---

## keyword 提取示例

| 用户输入 | keyword | tags |
|----------|---------|------|
| 10万预算帝王绿手镯 | 帝王绿手镯 | 帝王绿,手镯 |
| 冰种平安扣预算2万无纹无裂 | 冰种平安扣无纹无裂 | 冰种,平安扣 |
| 冰种翡翠吊坠送礼自用均可 | 冰种翡翠吊坠送礼 | 冰种,吊坠,送礼 |
| 飘花 | 飘花翡翠 | 飘花 |

---

## 后端接口

ES搜索：`GET /api/search?keyword={keyword}&tags={tags}&size=3`

## 后端处理流程

```
用户输入 → Agent → JSON → 后端解析keyword/tags → ES搜索 → 3张商品卡片 → 前端展示
                         → 后端存 demandTitle/tagsList/specs 到 jade_demands 表
```
