#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
课程管理接口一键测试 - 完整 CRUD 流程 + 越权
用法: python run-tests.py [结果目录]
前置: mvn spring-boot:run 启动服务，端口 11001
"""
import json
import os
import sys
from datetime import datetime
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

BASE_URL = os.environ.get("BASE_URL", "http://localhost:11001/content")
OUT_DIR = sys.argv[1] if len(sys.argv) > 1 else None
if not OUT_DIR:
    script_dir = Path(__file__).resolve().parent
    OUT_DIR = script_dir / "results" / datetime.now().strftime("%Y%m%d-%H%M%S")
OUT_DIR = Path(OUT_DIR)
OUT_DIR.mkdir(parents=True, exist_ok=True)

# 机构归属: 课程1-15属机构1, 课程16-22属机构2
COURSE_ORG1, COURSE_ORG2 = 1, 16
COMPANY1, COMPANY2 = "1", "2"


def req(method, path, body=None, company_id=None, save_as=None):
    url = f"{BASE_URL.rstrip('/')}{path}"
    headers = {"Content-Type": "application/json"} if body else {}
    if company_id:
        headers["X-Company-Id"] = company_id
    data = body.encode("utf-8") if body else None
    r = Request(url, data=data, headers=headers, method=method)
    try:
        with urlopen(r, timeout=15) as resp:
            data = resp.read().decode()
    except HTTPError as e:
        data = e.read().decode()
    except URLError as e:
        data = json.dumps({"code": -1, "msg": str(e), "data": None})
    j = json.loads(data)
    if save_as:
        (OUT_DIR / f"{save_as}.json").write_text(json.dumps(j, ensure_ascii=False, indent=2))
        print(f"[{save_as}] -> {OUT_DIR / (save_as + '.json')}")
    return j


def main():
    print("=== 课程管理接口测试（完整流程 + 越权）===")
    print(f"BASE_URL={BASE_URL}  OUT_DIR={OUT_DIR}\n")

    # ========== 一、分类 ==========
    req("GET", "/course-categories/tree", save_as="01-分类树")

    # ========== 二、课程 - 完整流程 ==========
    req("POST", "/courses/page", '{"pageNo":1,"pageSize":5}', COMPANY1, "02-课程-列表")
    req("GET", f"/courses/{COURSE_ORG1}", company_id=COMPANY1, save_as="03-课程-查id1")
    cr = req("POST", "/courses", '{"name":"流程测试课程","users":"测试","tags":"测试","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001","description":"流程测试"}', COMPANY1)
    new_cid = cr.get("data") if cr.get("code") == 0 else None
    if new_cid:
        req("PUT", f"/courses/{new_cid}", '{"name":"流程测试课程-已更新","users":"测试","tags":"测试","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001","description":"已更新"}', COMPANY1, "05-课程-更新")
        req("GET", f"/courses/{new_cid}", company_id=COMPANY1, save_as="06-课程-再查id")
        req("DELETE", f"/courses/{new_cid}", company_id=COMPANY1, save_as="07-课程-删除")
    req("POST", "/courses/page", '{"pageNo":1,"pageSize":5}', COMPANY1, "08-课程-再列表")
    # 课程越权
    req("GET", f"/courses/{COURSE_ORG2}", company_id=COMPANY1, save_as="09-越权-课程-查16用机构1")
    req("PUT", f"/courses/{COURSE_ORG1}", '{"name":"越权测试","users":"x","tags":"x","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001"}', COMPANY2, "10-越权-课程-改1用机构2")
    req("DELETE", f"/courses/{COURSE_ORG1}", company_id=COMPANY2, save_as="11-越权-课程-删1用机构2")

    # ========== 三、师资 - 完整流程 ==========
    req("GET", f"/courses/{COURSE_ORG1}/teachers", company_id=COMPANY1, save_as="12-师资-列表")
    req("GET", f"/courses/{COURSE_ORG1}/teachers/1", company_id=COMPANY1, save_as="13-师资-查id1")
    tr = req("POST", f"/courses/{COURSE_ORG1}/teachers", '{"teacherName":"流程测试讲师","position":"初级","description":"流程测试"}', COMPANY1)
    new_tid = tr.get("data") if tr.get("code") == 0 else None
    if new_tid:
        req("PUT", f"/courses/{COURSE_ORG1}/teachers/{new_tid}", '{"teacherName":"流程测试讲师-已更新","position":"中级","description":"已更新"}', COMPANY1, "15-师资-更新")
        req("GET", f"/courses/{COURSE_ORG1}/teachers/{new_tid}", company_id=COMPANY1, save_as="16-师资-再查id")
        req("DELETE", f"/courses/{COURSE_ORG1}/teachers/{new_tid}", company_id=COMPANY1, save_as="17-师资-删除")
    req("GET", f"/courses/{COURSE_ORG1}/teachers", company_id=COMPANY1, save_as="18-师资-再列表")
    # 师资越权
    req("GET", f"/courses/{COURSE_ORG2}/teachers", company_id=COMPANY1, save_as="19-越权-师资-列表16用机构1")
    req("POST", f"/courses/{COURSE_ORG2}/teachers", '{"teacherName":"越权","position":"x","description":"x"}', COMPANY1, "20-越权-师资-新增16用机构1")
    req("PUT", f"/courses/{COURSE_ORG2}/teachers/17", '{"teacherName":"越权","position":"x","description":"x"}', COMPANY1, "21-越权-师资-改17用机构1")
    req("DELETE", f"/courses/{COURSE_ORG2}/teachers/17", company_id=COMPANY1, save_as="22-越权-师资-删17用机构1")

    # ========== 四、营销 - 完整流程 ==========
    req("GET", f"/courses/{COURSE_ORG1}/market", company_id=COMPANY1, save_as="23-营销-查")
    req("PUT", f"/courses/{COURSE_ORG1}/market", '{"charge":"201001","price":188,"originalPrice":288,"validDays":365}', COMPANY1, "24-营销-保存")
    req("GET", f"/courses/{COURSE_ORG1}/market", company_id=COMPANY1, save_as="25-营销-再查")
    # 营销越权
    req("GET", f"/courses/{COURSE_ORG2}/market", company_id=COMPANY1, save_as="26-越权-营销-查16用机构1")
    req("PUT", f"/courses/{COURSE_ORG2}/market", '{"charge":"201001","price":1,"originalPrice":1,"validDays":30}', COMPANY1, "27-越权-营销-保存16用机构1")

    # ========== 五、教学计划 - 完整流程 ==========
    req("GET", f"/courses/{COURSE_ORG1}/teachplans", company_id=COMPANY1, save_as="28-计划-树")
    pr = req("POST", f"/courses/{COURSE_ORG1}/teachplans", '{"pname":"流程测试章","parentId":0,"grade":1,"orderBy":99,"isPreviewEnabled":"0"}', COMPANY1)
    new_pid = pr.get("data") if pr.get("code") == 0 else None
    if new_pid:
        req("PUT", f"/teachplans/{new_pid}", '{"pname":"流程测试章-已修改","parentId":0,"grade":1,"orderBy":99,"isPreviewEnabled":"0"}', COMPANY1, "30-计划-修改")
        req("GET", f"/courses/{COURSE_ORG1}/teachplans", company_id=COMPANY1, save_as="31-计划-再树")
        req("DELETE", f"/teachplans/{new_pid}", company_id=COMPANY1, save_as="32-计划-删除")
    req("GET", f"/courses/{COURSE_ORG1}/teachplans", company_id=COMPANY1, save_as="33-计划-再树")
    # 计划越权（teachplan 12 属课程3/机构1，用机构2访问）
    req("GET", f"/courses/{COURSE_ORG2}/teachplans", company_id=COMPANY1, save_as="34-越权-计划-树16用机构1")
    req("POST", f"/courses/{COURSE_ORG2}/teachplans", '{"pname":"越权章","parentId":0,"grade":1,"orderBy":1,"isPreviewEnabled":"0"}', COMPANY1, "35-越权-计划-新增16用机构1")

    # ========== 六、分页校验 ==========
    req("POST", "/courses/page", '{"pageNo":1,"pageSize":2,"publishStatus":"203002"}', COMPANY1, "36-分页-pageSize2")
    req("POST", "/courses/page", '{"pageNo":1,"pageSize":10,"auditStatus":"202004"}', COMPANY1, "37-分页-审核状态")

    # ========== 七、失败用例 ==========
    req("POST", "/courses", '{"name":"","users":"x","tags":"x","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001"}', COMPANY1, "38-失败-新增名称为空")
    req("GET", "/courses/99999", company_id=COMPANY1, save_as="39-失败-详情不存在")

    # ========== 八、审计字段测试（保留记录，便于在 DB 中验证 create_time/update_time/create_by/update_by）==========
    # 更新课程1：name 含「测试修改时间」，验证 update_time、update_by=1
    req("PUT", f"/courses/{COURSE_ORG1}", '{"name":"Java核心技术从入门到精通-测试修改时间","users":"零基础学员、转行开发者","tags":"Java,后端,Spring","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001","description":"系统讲解Java SE核心语法"}', COMPANY1, "40-审计-更新课程1-测试修改时间")
    # 新增课程：name 含「测试修改时间」，不删除，验证 create_time、create_by=1
    cr2 = req("POST", "/courses", '{"name":"测试修改时间-新增","users":"测试","tags":"测试","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001","description":"用于验证审计字段"}', COMPANY1)
    if cr2.get("code") == 0 and cr2.get("data"):
        req("PUT", f"/courses/{cr2['data']}", '{"name":"测试修改时间-新增-已更新","users":"测试","tags":"测试","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001","description":"已更新，验证update_by"}', COMPANY1, "41-审计-更新新课程-测试修改时间")
        # 不删除，保留在 DB 中供人工核对 create_time/update_time/create_by/update_by

    # ========== 九、结果分析 ==========
    analyze_results(OUT_DIR)


def analyze_results(out_dir):
    """分析测试结果，输出越权与流程通过情况"""
    out_dir = Path(out_dir)
    if not out_dir.exists():
        return
    files = sorted(out_dir.glob("*.json"))
    passed, failed, yuequan_ok, yuequan_fail = [], [], [], []
    for f in files:
        try:
            raw = f.read_text(encoding="utf-8", errors="replace")
            j = json.loads(raw)
        except Exception:
            continue
        code = j.get("code")
        name = f.stem
        if name.startswith("越权-") or "越权" in name:
            # 越权用例：期望 code != 0（403/404 等）
            if code is not None and code != 0:
                yuequan_ok.append(name)
            else:
                yuequan_fail.append(name)
        elif "失败" in name:
            # 失败用例：期望 code != 0
            if "新增名称为空" in name:
                if code != 0:
                    passed.append(name)
                else:
                    failed.append(f"{name} (API未校验name为空，返回了code=0)")
            elif "详情不存在" in name:
                if code == 404 or code != 0:
                    passed.append(name)
                else:
                    failed.append(name)
            else:
                if code != 0:
                    passed.append(name)
                else:
                    failed.append(name)
        else:
            # 正常流程：期望 code == 0
            if code == 0:
                passed.append(name)
            else:
                failed.append(f"{name} (code={code})")

    print("\n" + "=" * 50)
    print("【测试结果分析】")
    print("=" * 50)
    print(f"正常流程通过: {len(passed)} 个")
    print(f"正常流程失败: {len(failed)} 个")
    if failed:
        for x in failed:
            print(f"  - {x}")
    print(f"越权防护通过: {len(yuequan_ok)} 个")
    if yuequan_fail:
        print(f"越权防护失败（需修复）: {len(yuequan_fail)} 个")
        for x in yuequan_fail:
            print(f"  - {x}")
    print("=" * 50)


if __name__ == "__main__":
    main()
