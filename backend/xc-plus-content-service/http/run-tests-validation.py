#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
课程管理接口 - 校验版测试脚本
用法: python run-tests-validation.py [结果目录]
前置: mvn spring-boot:run 启动服务，端口 11001
包含：成功校验用例 + 失败校验用例（期望返回 code!=0）
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
    OUT_DIR = script_dir / "results" / ("validation-" + datetime.now().strftime("%Y%m%d-%H%M%S"))
OUT_DIR = Path(OUT_DIR)
OUT_DIR.mkdir(parents=True, exist_ok=True)

COMPANY1 = "1"


def req(method, path, body=None, company_id=None, save_as=None):
    url = f"{BASE_URL.rstrip('/')}{path}"
    headers = {"Content-Type": "application/json"} if body else {}
    if company_id:
        headers["X-Company-Id"] = company_id
    data = body.encode("utf-8") if body else None
    r = Request(url, data=data, headers=headers, method=method)
    try:
        with urlopen(r, timeout=30) as resp:
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
    print("=== 课程管理接口 - 校验版测试 ===")
    print(f"BASE_URL={BASE_URL}  OUT_DIR={OUT_DIR}\n")

    # ========== 一、成功校验用例 ==========
    print("--- 成功校验用例 ---")
    req("POST", "/courses/page", '{"pageNo":1,"pageSize":10}', COMPANY1, "01-成功-分页")
    req("POST", "/courses/page", '{"pageNo":1,"pageSize":5,"courseName":"Java"}', COMPANY1, "02-成功-分页带条件")
    cr = req("POST", "/courses", '{"name":"校验测试课程","users":"测试","tags":"测试","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001","description":"测试"}', COMPANY1)
    new_cid = cr.get("data") if cr.get("code") == 0 else None
    if new_cid:
        req("PUT", f"/courses/{new_cid}", '{"name":"校验测试课程-已更新","users":"测试","tags":"测试","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001"}', COMPANY1, "04-成功-课程更新")
        req("PUT", f"/courses/{new_cid}/market", '{"charge":"201001","price":99,"originalPrice":199,"validDays":365}', COMPANY1, "05-成功-营销保存")
        req("POST", f"/courses/{new_cid}/teachers", '{"teacherName":"校验测试讲师","position":"高级","description":"测试"}', COMPANY1, "06-成功-师资新增")
        pr = req("POST", f"/courses/{new_cid}/teachplans", '{"pname":"校验测试章","parentId":0,"grade":1,"orderBy":1,"isPreviewEnabled":"0"}', COMPANY1)
        new_pid = pr.get("data") if pr.get("code") == 0 else None
        if new_pid:
            req("PUT", f"/teachplans/{new_pid}", '{"pname":"校验测试章-已修改","parentId":0,"grade":1,"orderBy":1,"isPreviewEnabled":"0"}', COMPANY1, "08-成功-计划修改")
        req("DELETE", f"/courses/{new_cid}", company_id=COMPANY1)

    # ========== 二、失败校验用例 ==========
    print("--- 失败校验用例 ---")

    # 课程创建
    req("POST", "/courses", '{"name":"","users":"x","tags":"x","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001"}', COMPANY1, "10-失败-课程名称为空")
    req("POST", "/courses", '{"name":"x","users":"x","tags":"x","mt":"1-001-001","st":"1-001-001","grade":"99999","teachMode":"200001"}', COMPANY1, "11-失败-课程等级非法")
    req("POST", "/courses", '{"name":"x","users":"x","tags":"x","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"99999"}', COMPANY1, "12-失败-教育模式非法")
    req("POST", "/courses", '{"name":"' + "x"*101 + '","users":"x","tags":"x","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001"}', COMPANY1, "12b-失败-课程名称超100")
    req("POST", "/courses", '{"name":"x","users":"' + "x"*501 + '","tags":"x","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001"}', COMPANY1, "12c-失败-适用人群超500")
    req("POST", "/courses", '{"name":"x","users":"x","tags":"' + "x"*51 + '","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001"}', COMPANY1, "12d-失败-标签超50")
    req("POST", "/courses", '{"name":"x","users":"x","tags":"x","mt":"1-001-001","st":"1-001-001","grade":"204001","teachMode":"200001","description":"' + "x"*5001 + '"}', COMPANY1, "12e-失败-课程介绍超5000")

    # 分页
    req("POST", "/courses/page", '{"pageNo":0,"pageSize":10}', COMPANY1, "13-失败-分页pageNo小于1")
    req("POST", "/courses/page", '{"pageNo":1,"pageSize":0}', COMPANY1, "14-失败-分页pageSize小于1")
    req("POST", "/courses/page", '{"pageNo":1,"pageSize":200}', COMPANY1, "15-失败-分页pageSize超100")
    req("POST", "/courses/page", '{"pageNo":1,"pageSize":10,"auditStatus":"99999"}', COMPANY1, "15b-失败-审核状态非法")
    req("POST", "/courses/page", '{"pageNo":1,"pageSize":10,"publishStatus":"99999"}', COMPANY1, "15c-失败-发布状态非法")

    # 营销
    req("PUT", "/courses/1/market", '{"charge":"201001","price":-1,"originalPrice":1,"validDays":30}', COMPANY1, "16-失败-营销价格负数")
    req("PUT", "/courses/1/market", '{"charge":"201001","price":99,"originalPrice":-1,"validDays":30}', COMPANY1, "17-失败-营销原价负数")
    req("PUT", "/courses/1/market", '{"charge":"201001","price":99,"originalPrice":199,"validDays":0}', COMPANY1, "18-失败-营销收费有效期0")
    req("PUT", "/courses/1/market", '{"charge":"201001"}', COMPANY1, "19-失败-营销收费未填价格")
    req("PUT", "/courses/1/market", '{"charge":"99999","price":99,"originalPrice":199,"validDays":30}', COMPANY1, "19b-失败-收费规则非法")

    # 师资
    req("POST", "/courses/1/teachers", '{"teacherName":"","position":"x","description":"x"}', COMPANY1, "20-失败-师资姓名为空")
    req("POST", "/courses/1/teachers", '{"teacherName":"' + "x"*61 + '","position":"x","description":"x"}', COMPANY1, "20b-失败-师资姓名超60")
    req("POST", "/courses/1/teachers", '{"teacherName":"x","position":"' + "x"*256 + '","description":"x"}', COMPANY1, "20c-失败-师资职位超255")
    req("POST", "/courses/1/teachers", '{"teacherName":"x","position":"x","description":"' + "x"*1025 + '"}', COMPANY1, "20d-失败-师资简介超1024")

    # 教学计划
    req("POST", "/courses/1/teachplans", '{"pname":"","parentId":0,"grade":1,"orderBy":0,"isPreviewEnabled":"0"}', COMPANY1, "21-失败-计划名称为空")
    req("POST", "/courses/1/teachplans", '{"pname":"x","parentId":0,"grade":3,"orderBy":0,"isPreviewEnabled":"0"}', COMPANY1, "22-失败-计划层级非法")
    req("POST", "/courses/1/teachplans", '{"pname":"x","parentId":0,"grade":1,"orderBy":-1,"isPreviewEnabled":"0"}', COMPANY1, "23-失败-计划排序负数")
    req("POST", "/courses/1/teachplans", '{"pname":"x","parentId":0,"grade":1,"orderBy":0,"isPreviewEnabled":"2"}', COMPANY1, "24-失败-计划试看非法")
    req("POST", "/courses/1/teachplans", '{"pname":"' + "x"*65 + '","parentId":0,"grade":1,"orderBy":0,"isPreviewEnabled":"0"}', COMPANY1, "24b-失败-计划名称超64")
    req("POST", "/courses/1/teachplans", '{"pname":"x","parentId":0,"grade":1,"orderBy":0,"isPreviewEnabled":"0","mediaType":"' + "x"*11 + '"}', COMPANY1, "24c-失败-课程类型超10")

    # 媒资
    req("POST", "/teachplans/1/media", '{"mediaId":"","mediaFileName":"x"}', COMPANY1, "25-失败-媒资ID为空")
    req("POST", "/teachplans/1/media", '{"mediaId":"' + "x"*33 + '","mediaFileName":"x"}', COMPANY1, "25b-失败-媒资ID超32")
    req("POST", "/teachplans/1/media", '{"mediaId":"x","mediaFileName":"' + "x"*151 + '"}', COMPANY1, "25c-失败-媒资文件名超150")

    # 营销-字段长度
    req("PUT", "/courses/1/market", '{"charge":"201000","qq":"x","wechat":"' + "x"*65 + '","phone":"x"}', COMPANY1, "26-失败-营销微信超64")

    # ========== 三、结果分析 ==========
    analyze_results(OUT_DIR)


def analyze_results(out_dir):
    """分析校验测试结果"""
    out_dir = Path(out_dir)
    if not out_dir.exists():
        return
    files = sorted(out_dir.glob("*.json"))
    passed, failed = [], []
    for f in files:
        try:
            raw = f.read_text(encoding="utf-8", errors="replace")
            j = json.loads(raw)
        except Exception:
            continue
        code = j.get("code")
        name = f.stem
        if "成功" in name:
            if code == 0:
                passed.append(name)
            else:
                failed.append(f"{name} (期望code=0，实际{code})")
        else:
            if code is not None and code != 0:
                passed.append(name)
            else:
                failed.append(f"{name} (期望code!=0，实际{code}，校验未生效)")

    print("\n" + "=" * 50)
    print("【校验版测试结果】")
    print("=" * 50)
    print(f"通过: {len(passed)} 个")
    print(f"失败: {len(failed)} 个")
    if failed:
        for x in failed:
            print(f"  - {x}")
    print("=" * 50)


if __name__ == "__main__":
    main()
