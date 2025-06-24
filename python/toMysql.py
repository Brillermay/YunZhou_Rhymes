#coding:gbk
import json
import pymysql  # ����pymysql��
from pymysql import Error  # ����Error��


def import_poems_to_mysql():
    conn = None  # ��ʼ�����Ӷ���
    try:
        # 1. ��ȡJSON�ļ�
        with open('std/poems.json', 'r', encoding='utf-8') as file:
            poems = json.load(file)

            # 2. ���ݿ��������ã����滻Ϊʵ��ֵ��
        db_config = {
            'host': 'localhost',
            'user': 'root',
            'password': 'npnpnpnow',
            'database': 'es'
        }

        # 3. �������ݿ����ӣ�ʹ��pymysql��
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        truncate="truncate table poem"
        cursor.execute(truncate)
        conn.commit()

        # 4. ׼��SQL�������
        insert_query = """
        INSERT INTO poem (title, poet, text, category, translation, appreciation, background)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        """

        # 5. ��������������
        count = 0  # �ɹ�����
        for poem in poems:
            # �����ֵ�������ַ���תΪNone��
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

        # 6. �ύ���񲢹ر�����
        conn.commit()
        print(f"�ɹ����� {count}/{len(poems)} ��ʫ������")

    except Error as e:
        print(f"���ݿ����: {e}")
        if conn:
            conn.rollback()
    except Exception as e:
        print(f"�������: {e}")
    finally:
        if conn:  # ����ȫ�����ӹرռ��
            cursor.close()
            conn.close()
            print("���ݿ������ѹر�")


# ִ�е���
if __name__ == "__main__":
    import_poems_to_mysql()