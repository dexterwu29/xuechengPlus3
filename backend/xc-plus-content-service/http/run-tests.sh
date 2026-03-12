#!/usr/bin/env bash
# 课程管理接口一键测试脚本
# 用法：./run-tests.sh [结果目录，默认 http/results/$(date +%Y%m%d-%H%M%S)]
# 前置：服务运行于 http://localhost:11001

set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_URL="${BASE_URL:-http://localhost:11001/content}"
COMPANY_ID="${COMPANY_ID:-1}"
OUT_DIR="${1:-$SCRIPT_DIR/results/$(date +%Y%m%d-%H%M%S)}"
mkdir -p "$OUT_DIR"

run() {
  local name="$1"
  local method="$2"
  local path="$3"
  local body="$4"
  local out="$OUT_DIR/${name}.json"
  if [ -n "$body" ]; then
    curl -s -X "$method" "$BASE_URL$path" \
      -H "Content-Type: application/json" \
      -H "X-Company-Id: $COMPANY_ID" \
      -d "$body" > "$out"
  else
    curl -s -X "$method" "$BASE_URL$path" \
      -H "X-Company-Id: $COMPANY_ID" > "$out"
  fi
  echo "[$name] -> $out"
}

run_no_company() {
  local name="$1"
  local path="$2"
  local out="$OUT_DIR/${name}.json"
  curl -s -X GET "$BASE_URL$path" > "$out"
  echo "[$name] -> $out"
}

echo "=== 课程管理接口测试 ==="
echo "BASE_URL=$BASE_URL  OUT_DIR=$OUT_DIR"
echo ""

# 一、分类
run_no_company "01-分类树" "/course-categories/tree"

# 二、分页
run "02-分页-无条件" POST "/courses/page" '{"pageNo":1,"pageSize":10}'
run "03-分页-按名称" POST "/courses/page" '{"pageNo":1,"pageSize":10,"courseName":"Java"}'
run "04-分页-按审核状态" POST "/courses/page" '{"pageNo":1,"pageSize":10,"auditStatus":"202004"}'
run "05-分页-按发布状态-pageSize2" POST "/courses/page" '{"pageNo":1,"pageSize":2,"publishStatus":"203002"}'
run "06-分页-组合" POST "/courses/page" '{"pageNo":1,"pageSize":5,"courseName":"AI","auditStatus":"202004","publishStatus":"203002"}'

# 三、CRUD
run "07-新增课程" POST "/courses" '{"name":"脚本测试新增","users":"测试","tags":"测试","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001","description":"脚本测试"}'
run "08-新增-名称为空" POST "/courses" '{"name":"","users":"测试","tags":"测试","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001","description":"应失败"}'
run "09-详情" GET "/courses/1" ""
run "10-详情-不存在" GET "/courses/99999" ""
run "11-更新" PUT "/courses/1" '{"name":"Java核心技术（脚本更新）","users":"零基础","tags":"Java","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001","description":"更新"}'

# 四、教学计划
run "12-教学计划树" GET "/courses/1/teachplans" ""
run "13-新增章" POST "/courses/1/teachplans" '{"pname":"脚本测试章","parentId":0,"grade":1,"orderBy":4,"isPreviewEnabled":"0"}'

# 五、营销
run "14-获取营销" GET "/courses/1/market" ""
run "15-保存营销" PUT "/courses/1/market" '{"charge":"201001","price":199,"originalPrice":299,"validDays":365}'

# 六、师资
run "16-教师列表" GET "/courses/1/teachers" ""
run "17-新增教师" POST "/courses/1/teachers" '{"teacherName":"脚本测试讲师","position":"初级","description":"脚本测试"}'

echo ""
echo "=== 完成，结果保存在 $OUT_DIR ==="
echo "查看：cat $OUT_DIR/05-分页-按发布状态-pageSize2.json | head -50"
