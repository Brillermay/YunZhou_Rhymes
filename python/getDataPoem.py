#coding:GBK
import os
import time
import random
import requests
from concurrent.futures import ThreadPoolExecutor


def fetch_page(page_num):
    """爬取单个页面并保存"""
    url = f"https://www.chagushici.com/shici/{page_num}"
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36',
        'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
        'Accept-Encoding': 'gzip, deflate, br'
    }

    try:
        # 随机延时避免封禁
        time.sleep(random.uniform(1.0, 3.0))

        response = requests.get(url, headers=headers, timeout=20)

        # 检查响应状态
        if response.status_code == 200:
            save_path = os.path.join('poetry_html', f'{page_num}.html')
            with open(save_path, 'w', encoding='utf-8') as f:
                f.write(response.text)
            print(f"页面 {page_num} 保存成功")
            return f"页面 {page_num} 保存成功"
        else:
            return f"页面 {page_num} 请求失败: HTTP {response.status_code}"

    except Exception as e:
        return f"页面 {page_num} 出错: {str(e)}"


def crawl_poetry_site():
    """主爬虫函数"""
    # 创建保存目录
    os.makedirs('poetry_html', exist_ok=True)

    # 设置爬取范围
    start_page = 1
    end_page = 5000

    print(f"开始爬取诗词网站 (共 {end_page} 页)")
    start_time = time.time()

    # 使用线程池提高效率
    with ThreadPoolExecutor(max_workers=10) as executor:
        results = list(executor.map(fetch_page, range(start_page, end_page + 1)))

    # 统计结果
    success_count = sum(1 for r in results if '成功' in r)
    failure_count = end_page - success_count

    print(f"\n爬取完成! 用时: {time.time() - start_time:.2f} 秒")
    print(f"成功: {success_count} 页 | 失败: {failure_count} 页")
    print(f"HTML文件保存在: {os.path.abspath('poetry_html')}")

    # 保存日志
    with open('crawl_log.txt', 'w', encoding='utf-8') as log:
        log.write("\n".join(results))


if __name__ == "__main__":
    crawl_poetry_site()