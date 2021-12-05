# from urllib import request,error
#
# '''URLError异常处理简单示例'''
#
# import ssl
# ssl._create_default_https_context = ssl._create_unverified_context# 全局取消证书验证，避免访问https网页报错
# try:
#     response = request.urlopen('https://cuiqingcai.com/ttttttt')
# except error.URLError as e:
#     print(e.reason)
# from urllib import error,request
# '''HTTPError异常处理简单示例'''
# import ssl
# ssl._create_default_https_context = ssl._create_unverified_context# 全局取消证书验证，避免访问https网页报错
# try:
#     response = request.urlopen('https://cuiqingcai.com/ttttttt')
# except error.HTTPError as e:
#     print(e.reason,e.code,e.headers,sep='\n')#code:返回状态码,reason:返回错误原因，headers:返回请求头
# from urllib import error,request
# '''HTTPError是URLError子类，可同时使用，先判断是否为HTTPError，不是用URLError'''
# import ssl
# ssl._create_default_https_context = ssl._create_unverified_context# 全局取消证书验证，避免访问https网页报错
# try:
#     response = request.urlopen('https://cuiqingcai.com/ttttttt')
# except error.HTTPError as e:
#     print(e.reason,e.code,e.headers,sep='\n')#code:返回状态码,reason:返回错误原因，headers:返回请求头
# except error.URLError as e:
#     print(e.reason)
# else:
#     print('Request Successfully')#处理正常逻辑
import socket
import urllib.request
import urllib.error
'''reason属性返回的不一定是字符串，也可能是个对象'''
try:
    response = urllib.request.urlopen('https://www.baidu.com',timeout=0.1)
except urllib.error.URLError as e:
    print(type(e.reason))
    if isinstance(e.reason,socket.timeout):#判断两个类是否相同
        print('TIME OUT')