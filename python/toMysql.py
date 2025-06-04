import json
import pymysql
from mysql.connector import Error


def import_poems_to_mysql():
    try:
        # 1. 读取JSON文件
        with open('poems.json', 'r', encoding='utf-8') as file:
            poems = json.load(file)

            # 2. 数据库连接配置（请替换为实际值）
        db_config = {
            'host': 'your_host',
            'user': 'your_username',
            'password': 'your_password',
            'database': 'your_database'
        }

        # 3. 建立数据库连接
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()

        # 4. 准备SQL插入语句
        insert_query = """
        INSERT INTO poem (title, poet, text, category, translation, appreciation, background)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        """

        # 5. 遍历并插入数据
        for poem in poems:
            # 处理空值（将空字符串转为None）
            values = (
                poem['title'],
                poem['poet'],
                poem['text'],
                poem['category'],
                poem['translation'] or None,
                poem['appreciation'] or None,
                poem['background'] or None
            )
            cursor.execute(insert_query, values)

        # 6. 提交事务并关闭连接
        conn.commit()
        print(f"成功导入 {len(poems)} 条诗词数据")

    except Error as e:
        print(f"数据库错误: {e}")
        if conn:
            conn.rollback()
    except Exception as e:
        print(f"程序错误: {e}")
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()
        # 执行导入


import_poems_to_mysql()