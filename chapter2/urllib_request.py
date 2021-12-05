# import urllib.request
# '''Request基本使用'''
#
# request = urllib.request.Request('https://www.python.org')
# response = urllib.request.urlopen(request)
# print(response.read().decode('utf-8'))

# from urllib import request,parse
# url = 'https://www.httpbin.org/post'
#
# header = {
#     'User-Agent':'Mozilla/4.0 (compatible;MSIE 5.5;Window NT)',#伪装浏览器
#     'Host':'www.httpbin.org'
# }#请求头
# dict = {'name':'germey'}
# data = bytes(parse.urlencode(dict),encoding = 'utf-8')
# req = request.Request(url = url,data=data,headers=header,method='POST')
# response = request.urlopen(req)
# print(response.read().decode('utf-8'))
from urllib import request,parse
url = 'https://www.httpbin.org/post'


dict = {'name':'germey'}
data = bytes(parse.urlencode(dict),encoding = 'utf-8')
req = request.Request(url = url,data=data,method='POST')
req.add_header('User-Agent','Mozilla/4.0 (compatible;MSIE 5.5;Window NT)')#利用add_header添加header
response = request.urlopen(req)
print(response.read().decode('utf-8'))