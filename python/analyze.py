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
    if ziliao_div:
        for item in ziliao_div.find_all('div', class_='ziliao'):
            h2 = item.find('h2')
            if not h2:
                continue

                # 优化1：更精确的文本提取
            block_content = []

            # 处理段落内容
            for p_tag in item.find_all('p'):
                # 优化2：去除<strong>标签及其内容
                for strong_tag in p_tag.find_all('strong'):
                    strong_tag.decompose()

                    # 优化3：保留换行结构
                p_text = []
                for child in p_tag.children:
                    if child.name == 'br':
                        p_text.append('\n')
                    elif child.string and child.string.strip():
                        p_text.append(child.string.strip())

                        # 优化4：拼接段落内容
                if p_text:
                    block_content.append(''.join(p_text))

                    # 优化5：根据标题类型分类
            if block_content:
                clean_content = '\n'.join(block_content)

                if "翻译" in h2.get_text():
                    poem.translation = clean_content
                elif "鉴赏" in h2.get_text():
                    poem.appreciation = clean_content
                elif "背景" in h2.get_text():
                    poem.background = clean_content
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
                    print(f"已处理: {filename} → 《{poem.title} 》")
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

        with open('poems.json', 'w', encoding='utf-8') as f:
            json.dump([p.to_dict() for p in poems], f, ensure_ascii=False, indent=2)
        print(f"\n已保存 {len(poems)} 首古诗到 poems.json")