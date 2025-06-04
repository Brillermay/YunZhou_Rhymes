import json
import pymysql
from mysql.connector import Error


def import_poems_to_mysql():
    try:
        # 1. ��ȡJSON�ļ�
        with open('poems.json', 'r', encoding='utf-8') as file:
            poems = json.load(file)

            # 2. ���ݿ��������ã����滻Ϊʵ��ֵ��
        db_config = {
            'host': 'your_host',
            'user': 'your_username',
            'password': 'your_password',
            'database': 'your_database'
        }

        # 3. �������ݿ�����
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()

        # 4. ׼��SQL�������
        insert_query = """
        INSERT INTO poem (title, poet, text, category, translation, appreciation, background)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        """

        # 5. ��������������
        for poem in poems:
            # �����ֵ�������ַ���תΪNone��
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

        # 6. �ύ���񲢹ر�����
        conn.commit()
        print(f"�ɹ����� {len(poems)} ��ʫ������")

    except Error as e:
        print(f"���ݿ����: {e}")
        if conn:
            conn.rollback()
    except Exception as e:
        print(f"�������: {e}")
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()
        # ִ�е���


import_poems_to_mysql()