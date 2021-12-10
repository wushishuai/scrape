import pymysql

'''创建数据库'''
# db = pymysql.connect(host='localhost',user='root',password=None,port=3306)
# cursor = db.cursor()
# cursor.execute('SELECT VERSION()')
# data = cursor.fetchone()
# print('Database version :',data)
# cursor.execute("CREATE DATABASE spiders DEFAULT CHARACTER SET utf8mb4")
# db.close()
'''创建表'''

# db = pymysql.connect(host='localhost', user='root', password=None, port=3306, db='spiders')
# cursor = db.cursor()
# sql = 'CREATE TABLE IF NOT EXISTS students (id VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL, age INT NOT NULL, PRIMARY KEY (id))'
# cursor.execute(sql)
# db.close()
'''插入数据'''

# id = '20120001'
# name = 'Bob'
# age = 20
# db = pymysql.connect(host='localhost', user='root', password=None, port=3306, db='spiders')
# cursor = db.cursor()
# sql = 'INSERT INTO students(id,name,age) values(%ss,%ss,%ss)'
# try:
#     cursor.execute(sql, (id, name, age))
#     db.commit()
#     print('tr')
# except:
#     db.rollback()
# db.close()
# id = '20120001'
# user = 'Bob'
# age = 20
#
# db = pymysql.connect(host='localhost', user='root', password=None, port=3306, db='spiders')
# cursor = db.cursor()
# sql = "INSERT INTO students(id, name, age) VALUES (%s, %s, %s)"
# try:
#     cursor.execute(sql, (id, user, age))
#     db.commit()
# except:
#     db.rollback()
# db.close()

'''优化插入数据'''

# data = {
#     'id': '2012000b',
#     'name': 'Mike',
#     'age': 22
# }
# db = pymysql.connect(host='localhost', user='root', password=None, port=3306, db='spiders')
# cursor = db.cursor()
# table = 'students'
# keys = ','.join(data.keys())
# values = ','.join(['%s'] * len(data))
# sql = 'INSERT INTO {table}({keys}) VALUES ({values})'.format(table=table, keys=keys, values=values)
# try:
#     if cursor.execute(sql, tuple(data.values())):
#         print("Successful")
#         db.commit()
# except:
#     print("Failed")
#     db.rollback()
# db.close()
'''更新数据'''
# db = pymysql.connect(host='localhost', user='root', password=None, port=3306, db='spiders')
# cursor = db.cursor()
# sql = 'UPDATE students SET age = %s WHERE name = %s'
# try:
#     cursor.execute(sql,(25,'Bob'))
#     db.commit()
#     print("successful")
# except:
#     db.rollback()
# db.close()


'''优化更新数据，主键存在更新数据，主键不存在插入数据'''

# db = pymysql.connect(host='localhost', user='root', password=None, port=3306, db='spiders')
# cursor = db.cursor()
# data = {
#     'id': '20120001',
#     'name': 'Bob',
#     'age': 20
# }
# table = 'students'
# keys = ','.join(data.keys())
# values = ','.join(['%s'] * len(data))
# sql = 'INSERT INTO {table}({keys}) VALUES ({values}) ON DUPLICATE KEY UPDATE'.format(table=table, keys=keys,
#                                                                                      values=values)
# update = ','.join([" {key} = %s".format(key=key) for key in data])
# sql += update
# print(sql)
# try:
#     if cursor.execute(sql, tuple(data.values()) * 2):
#         print("successful")
#         db.commit()
# except:
#     db.rollback()
# db.close()

'''删除数据'''

# db = pymysql.connect(host='localhost', user='root', password=None, port=3306, db='spiders')
# cursor = db.cursor()
# table = 'students'
# condition = 'age > 20'
# sql = 'DELETE FROM {table} WHERE {condition}'.format(table=table,condition=condition)
# try:
#     cursor.execute(sql)
#     db.commit()
#     print("successful")
# except:
#     db.rollback()
# db.close()

'''查询数据'''

db = pymysql.connect(host='localhost', user='root', password=None, port=3306, db='spiders')
cursor = db.cursor()
sql = 'SELECT *FROM students WHERE age >=20'

try:
    cursor.execute(sql)
    print('Count:',cursor.rowcount)
    one = cursor.fetchone()
    print('one:',one)
    results = cursor.fetchall()
    print('results:',results)
    print('results type',type(results))
    for row in results:
        print(row)
except:
    print('error')
try:
    cursor.execute(sql)
    print('count:',cursor.rowcount)
    row = cursor.fetchone()
    while row:
        print('row:',row)
        row = cursor.fetchone()
except:
    print('error')