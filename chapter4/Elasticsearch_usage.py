from elasticsearch import Elasticsearch
import warnings

warnings.filterwarnings('ignore')

'''创建索引'''

# es = Elasticsearch()
# result = es.indices.create(index='news',ignore=400)
# print(result)
'''删除索引'''

# es = Elasticsearch()
# result = es.indices.delete(index='news', ignore=[400, 404])
# print(result)

'''插入数据'''

# es = Elasticsearch()
# es.indices.create(index='news', ignore=400)
#
# data = {
#     'title': '乘风破浪不负韶华，奋斗青春圆梦高考',
#     'url': 'http://view.inews.qq.com/a/EDU2021041600732200',
# }
# result = es.create(index='news', id=11, body=data)
# print(result)
#
# result2 = es.index(index='news',body = data)
# print(result2)

'''更新数据'''

# es = Elasticsearch()
#
# data = {
#     'title': '乘风破浪不负韶华，奋斗青春圆梦高考',
#     'url': 'http://view.inews.qq.com/a/EDU2021041600732200',
#     'date': '2021-07-05'
# }
# result = es.update(index='news', doc_type='_doc',body = {"doc":data},id = 1)#!!!与书不同
# # result = es.index(index='news',doc_type='_doc',body = data,id = 2)
# print(result)

'''删除数据'''

# es = Elasticsearch()
# result = es.delete(index='news', id=1)
# print(result)


'''查询数据'''
'''重新创建索引并指定需要分词的字段'''
# es = Elasticsearch()
# mapping = {
#     'properties': {
#         'title': {
#             'type': 'text',
#             'analyzer': 'ik_max_word',
#             'search_analyzer': 'ik_max_word'
#         }
#     }
# }
# es.indices.delete(index='news', ignore=[400, 404])
# es.indices.create(index='news', ignore=400)
# print('------------------------')
# result = es.indices.put_mapping(index='news', body=mapping)
# print(result)
# es = Elasticsearch()
#插入数据
# datas = [
#     {
#         'title': '高考结局大不同',
#         'url': 'https://k.sina.com.cn/article_7571064628_1c3454734001011lz9.html',
#     },
#     {
#         'title': '进入职业大洗牌时代，“吃香”职业还吃香吗？',
#         'url': 'https://new.qq.com/omn/20210828/20210828A025LK00.html',
#     },
#     {
#         'title': '乘风破浪不负韶华，奋斗青春圆梦高考',
#         'url': 'http://view.inews.qq.com/a/EDU2021041600732200',
#     },
#     {
#         'title': '他，活出了我们理想的样子',
#         'url': 'https://new.qq.com/omn/20210821/20210821A020ID00.html',
#     }
# ]
#
# for data in datas:
#     es.index(index='news', body=data)
#查询
# result = es.search(index='news')
# print(result)
dsl = {
    'query': {
        'match': {
            'title': '高考 圆梦'
        }
    }
}

es = Elasticsearch()
result = es.search(index='news', body=dsl)
# print(json.dumps(result, indent=2, ensure_ascii=False))
print(result)