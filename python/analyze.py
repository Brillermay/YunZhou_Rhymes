#coding:GBK
import os
from bs4 import BeautifulSoup, NavigableString, Comment
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
        if match and match.group() == "�����书���ƹ���":
            pass  # ���Ե�
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
    if not ziliao_div:
        return poem  # ���û�����ϲ��֣�ֱ�ӷ���

    # ��һ�ָ�ʽ�����ziliao div
    ziliao_items = ziliao_div.find_all('div', class_='ziliao')
    if ziliao_items:
        for item in ziliao_items:
            h2 = item.find('h2')
            if not h2:
                continue

                # �������нڵ�����
            content_blocks = []
            current_node = h2.next_sibling

            while current_node:
                # �����ı��ڵ�
                if isinstance(current_node, NavigableString):
                    stripped_text = current_node.strip()
                    if stripped_text:
                        content_blocks.append(stripped_text)
                        # ����<p>��ǩ
                elif current_node.name == 'p':
                    # �Ƴ�<strong>��ǩ
                    for strong_tag in current_node.find_all('strong'):
                        strong_tag.decompose()

                        # �����к��ı�
                    paragraph = []
                    for child in current_node.children:
                        if child.name == 'br':
                            paragraph.append('\n')
                        elif child.string and child.string.strip():
                            paragraph.append(child.string.strip())

                    if paragraph:
                        content_blocks.append(''.join(paragraph))
                        # ���������鼶Ԫ�أ���div��
                elif current_node.name in ['div', 'section']:
                    # �ݹ���ȡ�����ı�
                    block_text = current_node.get_text(strip=False)
                    if block_text.strip():
                        content_blocks.append(block_text.strip())

                current_node = current_node.next_sibling

                # �����������ݿ�
            clean_content = '\n\n'.join(content_blocks)

            # ���ݱ������ͷ���
            h2_text = h2.get_text(strip=True)
            if "��" in h2_text:
                poem.translation = clean_content
            elif "��" in h2_text or "��" in h2_text:
                poem.appreciation = clean_content
            elif "����" in h2_text:
                poem.background = clean_content
    else:
        # �ڶ��ָ�ʽ��ֱ����shici-ziliao�µ�p��ǩ��
        # �Ƴ�ע�ͽڵ�
        for comment in ziliao_div.find_all(string=lambda text: isinstance(text, Comment)):
            comment.extract()

            # �Ƴ�����Ҫ�Ľڵ�
        for note in ziliao_div.find_all('span', class_='shici-zhushi'):
            note.decompose()

            # ��ȡ�����ı�����
        full_text = ziliao_div.get_text('\n', strip=True)

        # ����ؼ���ӳ��
        keyword_mapping = {
            "��������": "translation",
            "����": "translation",
            "ʫ��": "appreciation",
            "����": "appreciation",
            "����": "appreciation",
            "����": "background"
        }

        # �ָ��ı�����
        current_keyword = None
        sections = {}

        for line in full_text.split('\n'):
            line = line.strip()
            if not line:
                continue

                # ����Ƿ��ǹؼ�����
            found_keyword = False
            for kw in keyword_mapping:
                if line.startswith(kw):
                    current_keyword = keyword_mapping[kw]
                    # �Ƴ��ؼ��ʼ������ð��
                    line = re.sub(rf'^{kw}[: ��]?', '', line).strip()
                    found_keyword = True
                    break

            if found_keyword:
                # �¹ؼ��ʣ���ʼ������
                sections[current_keyword] = [line] if line else []
            elif current_keyword:
                # ��ӵ���ǰ����
                sections.setdefault(current_keyword, []).append(line)

        # �ϲ�����������
        for section, lines in sections.items():
            content = '\n'.join(lines).strip()
            if section == "translation":
                poem.translation = content
            elif section == "appreciation":
                poem.appreciation = content
            elif section == "background":
                poem.background = content

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
                    print(f"�Ѵ���: {filename} ����{poem.title} ��")
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

        with open('std/poems.json', 'w', encoding='utf-8') as f:
            json.dump([p.to_dict() for p in poems], f, ensure_ascii=False, indent=2)
        print(f"\n�ѱ��� {len(poems)} �׹�ʫ�� poems.json")










'''

'''
# import os
# from bs4 import BeautifulSoup, NavigableString
# import re
#
#
# class Poem:
#     def __init__(self):
#         self.title = ""
#         self.poet = ""
#         self.text = ""
#         self.category = ""
#         self.translation = ""
#         self.appreciation = ""
#         self.background = ""
#
#     def __str__(self):
#         return (f"��{self.title}��\n"
#                 f"���ߣ�{self.poet}\n"
#                 f"���ࣺ{self.category}\n\n"
#                 f"ԭ�ģ�\n{self.text}\n\n"
#                 f"���ģ�\n{self.translation}\n\n"
#                 f"���ͣ�\n{self.appreciation}\n\n"
#                 f"������\n{self.background}")
#
#     def to_dict(self):
#         return {
#             "title": self.title,
#             "poet": self.poet,
#             "text": self.text,
#             "category": self.category,
#             "translation": self.translation,
#             "appreciation": self.appreciation,
#             "background": self.background
#         }
#
#
# def extract_poem_from_html(html_content):
#     """��HTMLԴ������ȡ��ʫ��Ϣ"""
#     soup = BeautifulSoup(html_content, 'html.parser')
#     poem = Poem()
#
#     # 1. ��ȡ����
#     title_tag = soup.find('h1', class_='maintitle')
#     if title_tag:
#         title_text = title_tag.get_text().strip()
#         # ��ȡ�����е�����
#         match = re.search(r'��(.+?)��', title_text)
#         if match.group() == "�����书���ƹ���":
#             x=0
#         poem.title = match.group(1) if match else title_text
#
#     # 2. ��ȡʫ����Ϣ
#     poet_div = soup.find('div', class_='chaodai-zuozhe')
#     if poet_div:
#         poets = [a.get_text().strip() for a in poet_div.find_all('a')]
#         poem.poet = " ".join(poets)
#
#     # 3. ��ȡʫ������
#     text_div = soup.find('div', class_='shici-text')
#     if text_div:
#         # �������нṹ
#         text_content = []
#         for child in text_div.children:
#             if child.name == 'br':
#                 text_content.append('\n')
#             elif child.string:
#                 text_content.append(child.string.strip())
#         poem.text = ''.join(text_content).strip()
#
#     # 4. ��ȡ����
#     category_div = soup.find('div', class_='shici-fenlei')
#     if category_div:
#         categories = [a.get_text().strip() for a in category_div.find_all('a')]
#         poem.category = " ".join(categories)
#
#     # 5-7. ��ȡ���롢���ͺͱ��� - ʹ���Ż���Ľڵ��������
#     ziliao_div = soup.find('div', class_='shici-ziliao')
#     if ziliao_div:
#         for item in ziliao_div.find_all('div', class_='ziliao'):
#             h2 = item.find('h2')
#             if not h2:
#                 continue
#
#                 # �µ�������ȡ�߼� - �������нڵ�����
#             content_blocks = []
#             current_node = h2.next_sibling
#
#             while current_node:
#                 # �����ı��ڵ�
#                 if isinstance(current_node, NavigableString):
#                     stripped_text = current_node.strip()
#                     if stripped_text:
#                         content_blocks.append(stripped_text)
#
#                         # ����<p>��ǩ
#                 elif current_node.name == 'p':
#                     # �Ƴ�<strong>��ǩ
#                     for strong_tag in current_node.find_all('strong'):
#                         strong_tag.decompose()
#
#                         # �����к��ı�
#                     paragraph = []
#                     for child in current_node.children:
#                         if child.name == 'br':
#                             paragraph.append('\n')
#                         elif child.string and child.string.strip():
#                             paragraph.append(child.string.strip())
#
#                     if paragraph:
#                         content_blocks.append(''.join(paragraph))
#
#                         # ���������鼶Ԫ�أ���div��
#                 elif current_node.name in ['div', 'section']:
#                     # �ݹ���ȡ�����ı�
#                     block_text = current_node.get_text(strip=False)
#                     if block_text.strip():
#                         content_blocks.append(block_text.strip())
#
#                 current_node = current_node.next_sibling
#
#                 # �����������ݿ�
#             clean_content = '\n\n'.join(content_blocks)
#
#             # ���ݱ������ͷ���
#             h2_text = h2.get_text(strip=True)
#             if "��" in h2_text:
#                 poem.translation = clean_content
#             elif "��" in h2_text or "��" in h2_text:
#                 poem.appreciation = clean_content
#             elif "����" in h2_text:
#                 poem.background = clean_content
#
#     return poem
#
#
# def process_html_directory(directory_path):
#     """����Ŀ¼�е�����HTML�ļ�"""
#     poems = []
#     for filename in os.listdir(directory_path):
#         if filename.endswith('.html'):
#             filepath = os.path.join(directory_path, filename)
#             try:
#                 with open(filepath, 'r', encoding='utf-8') as f:
#                     html_content = f.read()
#                     poem = extract_poem_from_html(html_content)
#                     poems.append(poem)
#                     print(f"�Ѵ���: {filename} ����{poem.title}��")
#             except Exception as e:
#                 print(f"�����ļ� {filename} ʱ����: {str(e)}")
#     return poems
#
#
# # ʹ��ʾ��
# if __name__ == "__main__":
#     # ����HTML�ļ��洢��'poetry_html'Ŀ¼��
#     poems = process_html_directory('poetry_html')
#
#     # �����һ����ȡ���
#     if poems:
#         print("\n��ȡ���ʾ��:")
#         print(poems[0])
#
#         # ����ΪJSON�ļ�
#         import json
#
#         with open('poems.json', 'w', encoding='utf-8') as f:
#             json.dump([p.to_dict() for p in poems], f, ensure_ascii=False, indent=2)
#         print(f"\n�ѱ��� {len(poems)} �׹�ʫ�� poems.json")






