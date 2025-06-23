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
        return (f"《{self.title} 》\n"
                f"作者：{self.poet}\n"
                f"分类：{self.category}\n\n"
                f"原文：\n{self.text}\n\n"
                f"译文：\n{self.translation}\n\n"
                f"鉴赏：\n{self.appreciation}\n\n"
                f"背景：\n{self.background}")

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
    """从HTML源码中提取古诗信息"""
    soup = BeautifulSoup(html_content, 'html.parser')
    poem = Poem()

    # 1. 提取标题
    title_tag = soup.find('h1', class_='maintitle')
    if title_tag:
        title_text = title_tag.get_text().strip()
        # 提取《》中的内容
        match = re.search(r'《(.+?)》', title_text)
        if match and match.group() == "《幸武功庆善宫》":
            pass  # 调试点
        poem.title = match.group(1) if match else title_text

        # 2. 提取诗人信息
    poet_div = soup.find('div', class_='chaodai-zuozhe')
    if poet_div:
        poets = [a.get_text().strip() for a in poet_div.find_all('a')]
        poem.poet = " ".join(poets)

    # 3. 提取诗文内容
    text_div = soup.find('div', class_='shici-text')
    if text_div:
        # 保留换行结构
        text_content = []
        for child in text_div.children:
            if child.name == 'br':
                text_content.append('\n')
            elif child.string:
                text_content.append(child.string.strip())
        poem.text = ''.join(text_content).strip()

    # 4. 提取分类
    category_div = soup.find('div', class_='shici-fenlei')
    if category_div:
        categories = [a.get_text().strip() for a in category_div.find_all('a')]
        poem.category = " ".join(categories)

    # 5-7. 提取翻译、鉴赏和背景
    ziliao_div = soup.find('div', class_='shici-ziliao')
    if not ziliao_div:
        return poem  # 如果没有资料部分，直接返回

    # 第一种格式：多个ziliao div
    ziliao_items = ziliao_div.find_all('div', class_='ziliao')
    if ziliao_items:
        for item in ziliao_items:
            h2 = item.find('h2')
            if not h2:
                continue

                # 处理所有节点类型
            content_blocks = []
            current_node = h2.next_sibling

            while current_node:
                # 处理文本节点
                if isinstance(current_node, NavigableString):
                    stripped_text = current_node.strip()
                    if stripped_text:
                        content_blocks.append(stripped_text)
                        # 处理<p>标签
                elif current_node.name == 'p':
                    # 移除<strong>标签
                    for strong_tag in current_node.find_all('strong'):
                        strong_tag.decompose()

                        # 处理换行和文本
                    paragraph = []
                    for child in current_node.children:
                        if child.name == 'br':
                            paragraph.append('\n')
                        elif child.string and child.string.strip():
                            paragraph.append(child.string.strip())

                    if paragraph:
                        content_blocks.append(''.join(paragraph))
                        # 处理其他块级元素（如div）
                elif current_node.name in ['div', 'section']:
                    # 递归提取所有文本
                    block_text = current_node.get_text(strip=False)
                    if block_text.strip():
                        content_blocks.append(block_text.strip())

                current_node = current_node.next_sibling

                # 连接所有内容块
            clean_content = '\n\n'.join(content_blocks)

            # 根据标题类型分类
            h2_text = h2.get_text(strip=True)
            if "译" in h2_text:
                poem.translation = clean_content
            elif "赏" in h2_text or "析" in h2_text:
                poem.appreciation = clean_content
            elif "背景" in h2_text:
                poem.background = clean_content
    else:
        # 第二种格式：直接在shici-ziliao下的p标签中
        # 移除注释节点
        for comment in ziliao_div.find_all(string=lambda text: isinstance(text, Comment)):
            comment.extract()

            # 移除不需要的节点
        for note in ziliao_div.find_all('span', class_='shici-zhushi'):
            note.decompose()

            # 提取所有文本内容
        full_text = ziliao_div.get_text('\n', strip=True)

        # 定义关键词映射
        keyword_mapping = {
            "中文译文": "translation",
            "译文": "translation",
            "诗意": "appreciation",
            "赏析": "appreciation",
            "鉴赏": "appreciation",
            "背景": "background"
        }

        # 分割文本内容
        current_keyword = None
        sections = {}

        for line in full_text.split('\n'):
            line = line.strip()
            if not line:
                continue

                # 检查是否是关键词行
            found_keyword = False
            for kw in keyword_mapping:
                if line.startswith(kw):
                    current_keyword = keyword_mapping[kw]
                    # 移除关键词及后面的冒号
                    line = re.sub(rf'^{kw}[: ：]?', '', line).strip()
                    found_keyword = True
                    break

            if found_keyword:
                # 新关键词，初始化内容
                sections[current_keyword] = [line] if line else []
            elif current_keyword:
                # 添加到当前部分
                sections.setdefault(current_keyword, []).append(line)

        # 合并各部分内容
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
    """处理目录中的所有HTML文件"""
    poems = []
    for filename in os.listdir(directory_path):
        if filename.endswith('.html'):
            filepath = os.path.join(directory_path, filename)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    html_content = f.read()
                    poem = extract_poem_from_html(html_content)
                    poems.append(poem)
                    print(f"已处理: {filename} →《{poem.title} 》")
            except Exception as e:
                print(f"处理文件 {filename} 时出错: {str(e)}")
    return poems


# 使用示例
if __name__ == "__main__":
    # 假设HTML文件存储在'poetry_html'目录中
    poems = process_html_directory('poetry_html')

    # 输出第一个提取结果
    if poems:
        print("\n提取结果示例:")
        print(poems[0])

        # 保存为JSON文件
        import json

        with open('std/poems.json', 'w', encoding='utf-8') as f:
            json.dump([p.to_dict() for p in poems], f, ensure_ascii=False, indent=2)
        print(f"\n已保存 {len(poems)} 首古诗到 poems.json")










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
#         return (f"《{self.title}》\n"
#                 f"作者：{self.poet}\n"
#                 f"分类：{self.category}\n\n"
#                 f"原文：\n{self.text}\n\n"
#                 f"译文：\n{self.translation}\n\n"
#                 f"鉴赏：\n{self.appreciation}\n\n"
#                 f"背景：\n{self.background}")
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
#     """从HTML源码中提取古诗信息"""
#     soup = BeautifulSoup(html_content, 'html.parser')
#     poem = Poem()
#
#     # 1. 提取标题
#     title_tag = soup.find('h1', class_='maintitle')
#     if title_tag:
#         title_text = title_tag.get_text().strip()
#         # 提取《》中的内容
#         match = re.search(r'《(.+?)》', title_text)
#         if match.group() == "《幸武功庆善宫》":
#             x=0
#         poem.title = match.group(1) if match else title_text
#
#     # 2. 提取诗人信息
#     poet_div = soup.find('div', class_='chaodai-zuozhe')
#     if poet_div:
#         poets = [a.get_text().strip() for a in poet_div.find_all('a')]
#         poem.poet = " ".join(poets)
#
#     # 3. 提取诗文内容
#     text_div = soup.find('div', class_='shici-text')
#     if text_div:
#         # 保留换行结构
#         text_content = []
#         for child in text_div.children:
#             if child.name == 'br':
#                 text_content.append('\n')
#             elif child.string:
#                 text_content.append(child.string.strip())
#         poem.text = ''.join(text_content).strip()
#
#     # 4. 提取分类
#     category_div = soup.find('div', class_='shici-fenlei')
#     if category_div:
#         categories = [a.get_text().strip() for a in category_div.find_all('a')]
#         poem.category = " ".join(categories)
#
#     # 5-7. 提取翻译、鉴赏和背景 - 使用优化后的节点遍历方法
#     ziliao_div = soup.find('div', class_='shici-ziliao')
#     if ziliao_div:
#         for item in ziliao_div.find_all('div', class_='ziliao'):
#             h2 = item.find('h2')
#             if not h2:
#                 continue
#
#                 # 新的内容提取逻辑 - 处理所有节点类型
#             content_blocks = []
#             current_node = h2.next_sibling
#
#             while current_node:
#                 # 处理文本节点
#                 if isinstance(current_node, NavigableString):
#                     stripped_text = current_node.strip()
#                     if stripped_text:
#                         content_blocks.append(stripped_text)
#
#                         # 处理<p>标签
#                 elif current_node.name == 'p':
#                     # 移除<strong>标签
#                     for strong_tag in current_node.find_all('strong'):
#                         strong_tag.decompose()
#
#                         # 处理换行和文本
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
#                         # 处理其他块级元素（如div）
#                 elif current_node.name in ['div', 'section']:
#                     # 递归提取所有文本
#                     block_text = current_node.get_text(strip=False)
#                     if block_text.strip():
#                         content_blocks.append(block_text.strip())
#
#                 current_node = current_node.next_sibling
#
#                 # 连接所有内容块
#             clean_content = '\n\n'.join(content_blocks)
#
#             # 根据标题类型分类
#             h2_text = h2.get_text(strip=True)
#             if "译" in h2_text:
#                 poem.translation = clean_content
#             elif "赏" in h2_text or "析" in h2_text:
#                 poem.appreciation = clean_content
#             elif "背景" in h2_text:
#                 poem.background = clean_content
#
#     return poem
#
#
# def process_html_directory(directory_path):
#     """处理目录中的所有HTML文件"""
#     poems = []
#     for filename in os.listdir(directory_path):
#         if filename.endswith('.html'):
#             filepath = os.path.join(directory_path, filename)
#             try:
#                 with open(filepath, 'r', encoding='utf-8') as f:
#                     html_content = f.read()
#                     poem = extract_poem_from_html(html_content)
#                     poems.append(poem)
#                     print(f"已处理: {filename} →《{poem.title}》")
#             except Exception as e:
#                 print(f"处理文件 {filename} 时出错: {str(e)}")
#     return poems
#
#
# # 使用示例
# if __name__ == "__main__":
#     # 假设HTML文件存储在'poetry_html'目录中
#     poems = process_html_directory('poetry_html')
#
#     # 输出第一个提取结果
#     if poems:
#         print("\n提取结果示例:")
#         print(poems[0])
#
#         # 保存为JSON文件
#         import json
#
#         with open('poems.json', 'w', encoding='utf-8') as f:
#             json.dump([p.to_dict() for p in poems], f, ensure_ascii=False, indent=2)
#         print(f"\n已保存 {len(poems)} 首古诗到 poems.json")






