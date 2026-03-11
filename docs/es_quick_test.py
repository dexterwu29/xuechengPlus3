#!/usr/bin/env python3
"""
Elasticsearch 8.11 快速测试脚本

依赖安装（需 8.x，与 ES 8.11 兼容）:
    pip install "elasticsearch>=8.0,<9.0"

运行示例输出:
    === 1. 集群信息 ===
    集群名: docker-cluster, 版本: 8.11.0

    === 2. 创建索引并写入文档 ===
    已写入 2 条文档

    === 3. 搜索 '课程' ===
      - 学成在线课程: 这是一门Java微服务实战课程，包含Spring Boot、...

    === 4. 索引统计 ===
    索引 xcplus_test 文档数: 2

    完成!
"""

from elasticsearch import Elasticsearch

# 连接地址（替换为你的服务器 IP）
ES_HOST = "http://111.229.46.188:9200"

# 创建客户端（8.x 无安全认证时）
# 需使用 elasticsearch 8.x：pip install "elasticsearch>=8.0,<9.0"
es = Elasticsearch(
    ES_HOST,
    request_timeout=30,
)

# 1. 查看集群信息
print("=== 1. 集群信息 ===")
info = es.info()
print(f"集群名: {info['cluster_name']}, 版本: {info['version']['number']}\n")

# 2. 创建测试索引
INDEX = "xcplus_test"

print("=== 2. 创建索引并写入文档 ===")
if es.indices.exists(index=INDEX):
    es.indices.delete(index=INDEX)

# 创建索引（可选：指定 mapping）
es.indices.create(index=INDEX, body={
    "mappings": {
        "properties": {
            "title": {"type": "text", "analyzer": "standard"},
            "content": {"type": "text", "analyzer": "standard"},
            "create_time": {"type": "date"}
        }
    }
})

# 写入文档
doc = {
    "title": "学成在线课程",
    "content": "这是一门Java微服务实战课程，包含Spring Boot、Spring Cloud等内容",
    "create_time": "2025-03-11T10:00:00"
}
es.index(index=INDEX, document=doc, id=1)
es.index(index=INDEX, document={
    "title": "Elasticsearch入门",
    "content": "Elasticsearch全文检索与搜索实战",
    "create_time": "2025-03-11T11:00:00"
}, id=2)
print("已写入 2 条文档\n")

# 3. 刷新索引使可搜索
es.indices.refresh(index=INDEX)

# 4. 搜索
print("=== 3. 搜索 '课程' ===")
resp = es.search(index=INDEX, query={"match": {"content": "课程"}})
for hit in resp["hits"]["hits"]:
    print(f"  - {hit['_source']['title']}: {hit['_source']['content'][:30]}...")

# 5. 查看索引文档数
print("\n=== 4. 索引统计 ===")
count = es.count(index=INDEX)
print(f"索引 {INDEX} 文档数: {count['count']}")

# 6. 清理测试索引（可选）
# es.indices.delete(index=INDEX)
# print("已删除测试索引")

print("\n完成!")
