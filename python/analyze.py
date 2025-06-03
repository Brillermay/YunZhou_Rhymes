#coding:GBK
import os
from bs4 import BeautifulSoup
import re


class Poem:
    def __init__(self):
        self.title = ""
        self.poet = ""
        self.text = ""
        self.category = ""
        self.translation = ""
        self.appreciation = ""
        self.background = ""

    def __str__(self):
        return (f"��{self.title} ��\n"
                f"���ߣ�{self.poet}\n"
                f"���ࣺ{self.category}\n\n"
                f"ԭ�ģ�\n{self.text}\n\n"
                f"���ģ�\n{self.translation}\n\n"
                f"���ͣ�\n{self.appreciation}\n\n"
                f"������\n{self.background}")

    def to_dict(self):
        return {
            "title": self.title,
            "poet": self.poet,
            "text": self.text,
            "category": self.category,
            "translation": self.translation,
            "appreciation": self.appreciation,
            "background": self.background
        }


def extract_poem_from_html(html_content):
    """��HTMLԴ������ȡ��ʫ��Ϣ"""
    soup = BeautifulSoup(html_content, 'html.parser')
    poem = Poem()

    # 1. ��ȡ����
    title_tag = soup.find('h1', class_='maintitle')
    if title_tag:
        title_text = title_tag.get_text().strip()
        # ��ȡ�����е�����
        match = re.search(r'��(.+?)��', title_text)
        poem.title = match.group(1) if match else title_text

    # 2. ��ȡʫ����Ϣ
    poet_div = soup.find('div', class_='chaodai-zuozhe')
    if poet_div:
        poets = [a.get_text().strip() for a in poet_div.find_all('a')]
        poem.poet = " ".join(poets)

    # 3. ��ȡʫ������
    text_div = soup.find('div', class_='shici-text')
    if text_div:
        # �������нṹ
        text_content = []
        for child in text_div.children:
            if child.name == 'br':
                text_content.append('\n')
            elif child.string:
                text_content.append(child.string.strip())
        poem.text = ''.join(text_content).strip()

    # 4. ��ȡ����
    category_div = soup.find('div', class_='shici-fenlei')
    if category_div:
        categories = [a.get_text().strip() for a in category_div.find_all('a')]
        poem.category = " ".join(categories)

    # 5-7. ��ȡ���롢���ͺͱ���
    ziliao_div = soup.find('div', class_='shici-ziliao')
    if ziliao_div:
        for item in ziliao_div.find_all('div', class_='ziliao'):
            h2 = item.find('h2')
            if not h2:
                continue

                # �Ż�1������ȷ���ı���ȡ
            block_content = []

            # �����������
            for p_tag in item.find_all('p'):
                # �Ż�2��ȥ��<strong>��ǩ��������
                for strong_tag in p_tag.find_all('strong'):
                    strong_tag.decompose()

                    # �Ż�3���������нṹ
                p_text = []
                for child in p_tag.children:
                    if child.name == 'br':
                        p_text.append('\n')
                    elif child.string and child.string.strip():
                        p_text.append(child.string.strip())

                        # �Ż�4��ƴ�Ӷ�������
                if p_text:
                    block_content.append(''.join(p_text))

                    # �Ż�5�����ݱ������ͷ���
            if block_content:
                clean_content = '\n'.join(block_content)

                if "����" in h2.get_text():
                    poem.translation = clean_content
                elif "����" in h2.get_text():
                    poem.appreciation = clean_content
                elif "����" in h2.get_text():
                    poem.background = clean_content
    return poem


def process_html_directory(directory_path):
    """����Ŀ¼�е�����HTML�ļ�"""
    poems = []
    for filename in os.listdir(directory_path):
        if filename.endswith('.html'):
            filepath = os.path.join(directory_path, filename)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    html_content = f.read()
                    poem = extract_poem_from_html(html_content)
                    poems.append(poem)
                    print(f"�Ѵ���: {filename} �� ��{poem.title} ��")
            except Exception as e:
                print(f"�����ļ� {filename} ʱ����: {str(e)}")
    return poems


# ʹ��ʾ��
if __name__ == "__main__":
    # ����HTML�ļ��洢��'poetry_html'Ŀ¼��
    poems = process_html_directory('poetry_html')

    # �����һ����ȡ���
    if poems:
        print("\n��ȡ���ʾ��:")
        print(poems[0])

        # ����ΪJSON�ļ�
        import json

        with open('poems.json', 'w', encoding='utf-8') as f:
            json.dump([p.to_dict() for p in poems], f, ensure_ascii=False, indent=2)
        print(f"\n�ѱ��� {len(poems)} �׹�ʫ�� poems.json")