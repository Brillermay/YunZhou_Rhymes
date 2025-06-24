#coding:gbk
import json
import pymysql  # 改用pymysql库
from pymysql import Error  # 引入Error类


def import_poems_to_mysql():
    conn = None  # 初始化连接对象
    try:
        # 1. 读取JSON文件
        with open('std/poems.json', 'r', encoding='utf-8') as file:
            poems = json.load(file)

            # 2. 数据库连接配置（请替换为实际值）
        db_config = {
            'host': 'localhost',
            'user': 'root',
            'password': 'npnpnpnow',
            'database': 'es'
        }

        # 3. 建立数据库连接（使用pymysql）
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        truncate="truncate table poem"
        cursor.execute(truncate)
        conn.commit()

        # 4. 准备SQL插入语句
        insert_query = """
        INSERT INTO poem (title, poet, text, category, translation, appreciation, background)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        """

        # 5. 遍历并插入数据
        count = 0  # 成功计数
        for poem in poems:
            # 处理空值（将空字符串转为None）
            values = (
                poem.get('title', ''),
                poem.get('poet', ''),
                poem.get('text', ''),
                poem.get('category', ''),
                poem.get('translation') or None,
                poem.get('appreciation') or None,
                poem.get('background') or None
            )
            cursor.execute(insert_query, values)
            count += 1

        # 6. 提交事务并关闭连接
        conn.commit()
        print(f"成功导入 {count}/{len(poems)} 条诗词数据")

    except Error as e:
        print(f"数据库错误: {e}")
        if conn:
            conn.rollback()
    except Exception as e:
        print(f"程序错误: {e}")
    finally:
        if conn:  # 更安全的连接关闭检查
            cursor.close()
            conn.close()
            print("数据库连接已关闭")


# 执行导入
if __name__ == "__main__":
    import_poems_to_mysql()