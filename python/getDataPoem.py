#coding:GBK
import os
import time
import random
import requests
from concurrent.futures import ThreadPoolExecutor


def fetch_page(page_num):
    """��ȡ����ҳ�沢����"""
    url = f"https://www.chagushici.com/shici/{page_num}"
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36',
        'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
        'Accept-Encoding': 'gzip, deflate, br'
    }

    try:
        # �����ʱ������
        time.sleep(random.uniform(1.0, 3.0))

        response = requests.get(url, headers=headers, timeout=20)

        # �����Ӧ״̬
        if response.status_code == 200:
            save_path = os.path.join('poetry_html', f'{page_num}.html')
            with open(save_path, 'w', encoding='utf-8') as f:
                f.write(response.text)
            print(f"ҳ�� {page_num} ����ɹ�")
            return f"ҳ�� {page_num} ����ɹ�"
        else:
            return f"ҳ�� {page_num} ����ʧ��: HTTP {response.status_code}"

    except Exception as e:
        return f"ҳ�� {page_num} ����: {str(e)}"


def crawl_poetry_site():
    """�����溯��"""
    # ��������Ŀ¼
    os.makedirs('poetry_html', exist_ok=True)

    # ������ȡ��Χ
    start_page = 1
    end_page = 5000

    print(f"��ʼ��ȡʫ����վ (�� {end_page} ҳ)")
    start_time = time.time()

    # ʹ���̳߳����Ч��
    with ThreadPoolExecutor(max_workers=10) as executor:
        results = list(executor.map(fetch_page, range(start_page, end_page + 1)))

    # ͳ�ƽ��
    success_count = sum(1 for r in results if '�ɹ�' in r)
    failure_count = end_page - success_count

    print(f"\n��ȡ���! ��ʱ: {time.time() - start_time:.2f} ��")
    print(f"�ɹ�: {success_count} ҳ | ʧ��: {failure_count} ҳ")
    print(f"HTML�ļ�������: {os.path.abspath('poetry_html')}")

    # ������־
    with open('crawl_log.txt', 'w', encoding='utf-8') as log:
        log.write("\n".join(results))


if __name__ == "__main__":
    crawl_poetry_site()