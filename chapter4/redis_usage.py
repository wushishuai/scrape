from redis import StrictRedis,ConnectionPool
'''连接redis'''
# redis = StrictRedis(host='localhost', port=6379, db=0, password='root')
# redis.set('name', 'Bob')
# print(redis.get('name'))
#
# pool = ConnectionPool(host='localhost', port=6379, db=0, password='foobared')
# redis = StrictRedis(connection_pool=pool)

url = 'redis://:foobared@localhost:6379/0'
pool = ConnectionPool.from_url(url)
redis = StrictRedis(connection_pool=pool)