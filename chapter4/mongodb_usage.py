import pymongo
from bson.objectid import ObjectId
'''连接mongodb'''

client = pymongo.MongoClient(host='localhost', port=27017)
# client = MongoClient('mongodb://localhost:27017/')
db = client.test
# db = client['test']
collection = db.students
# collection = db['students']

'''插入数据'''
# student = {
#     'id': '20170101',
#     'name': 'Jordan',
#     'age': 20,
#     'gender': 'male'
# }
#
# result = collection.insert_one(student)
# print(result)

'''插入多条数据'''

# student1 = {
#     'id': '20170101',
#     'name': 'Jordan',
#     'age': 20,
#     'gender': 'male'
# }
#
# student2 = {
#     'id': '20170202',
#     'name': 'Mike',
#     'age': 21,
#     'gender': 'male'
# }
#
# result = collection.insert_many([student1, student2])
# print(result.inserted_ids)

'''查询数据'''

# result = collection.find_one({'name': 'Mike'})
# print(type(result))
# print(result)
#
# result = collection.find_one({'_id': ObjectId('61b456a980a6c6716dad9fdf')})
# print(result)
#
# # results = collection.find({'age': 20})#查询多条数据
# # results = collection.find({'age': {'$gt': 20}})#查询age大于20
# results = collection.find({'name': {'$regex': '^M.*'}})#正则匹配查询
# print(results)
# for result in results:
#     print(result)

'''统计计数'''

# count = collection.count_documents({})
# print(count)
#
# count = collection.count_documents({'age': 20})#指定条件计数
# print(count)

'''排序'''
# results = collection.find().sort('name', pymongo.ASCENDING)
# print([result['name'] for result in results])

'''偏移'''

# results = collection.find().sort('name', pymongo.ASCENDING).skip(2)#忽略前两个元素
# print([result['name'] for result in results])
# results = collection.find().sort('name', pymongo.ASCENDING).skip(2).limit(2)#限定结果个数
# print([result['name'] for result in results])
# results = collection.find({'_id': {'$gt': ObjectId('61b456a980a6c6716dad9fdf')}})#数据量大时（千万级或亿集）最好不要使用大偏移查询数据，可用这种操作
# print(results)

'''更新'''

# condition = {'name': 'Mike'}
# student = collection.find_one(condition)
# print(student)
# student['age'] = 26
# result = collection.update_one(condition, {'$set':student})
# print(result)
# print(result.matched_count, result.modified_count)

# condition = {'age': {'$gt': 20}}
# result = collection.update_one(condition, {'$inc': {'age': 1}})#符合条件有许多条，但仅匹配和改变一条
# print(result)
# print(result.matched_count, result.modified_count)

# condition = {'age': {'$gt': 19}}
# result = collection.update_many(condition, {'$inc': {'age': 1}})#更新所有符合条件数据
# print(result)
# print(result.matched_count, result.modified_count)

'''删除'''
# result = collection.remove({'name': 'Mike'})
# print(result)4.0弃用

# result = collection.delete_one({'name': 'Mike'})#删除一条
# print(result)
# print(result.deleted_count)

result = collection.delete_many({'age': {'$lt': 25}})#删除多条符合数据
print(result.deleted_count)