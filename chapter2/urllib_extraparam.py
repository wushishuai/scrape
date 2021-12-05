# import urllib.request
# import urllib.parse
# '''data 参数 使用bytes方法将参数——>字节流编码格式，请求方法有GET->POST'''
#
# data = bytes(urllib.parse.urlencode({'name':'germey'}),encoding = 'utf-8')#urlencode:将字典参数转化为字符串，encodeing指定编码格式
# response = urllib.request.urlopen('http://www.httpbin.org/post',data=data)
# print(response.read().decode('utf-8'))
# import urllib.request
# '''timeout参数，设置超时时间'''
# response = urllib.request.urlopen('http://www.httpbin.org/get',timeout = 0.1)
# print(response.read())

import socket
import urllib.request
import urllib.error

'''预置错误处理'''

try:
    response = urllib.request.urlopen('http://www.httpbin.org/get',timeout = 0.1)
except urllib.error.URLError as e:
    if isinstance(e.reason,socket.timeout):
        print('TIME OUT')