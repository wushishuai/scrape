# from urllib.request import HTTPPasswordMgrWithDefaultRealm,HTTPBasicAuthHandler,build_opener
# from urllib.error import URLError
# '''针对具有登录要求网页进行验证'''
# import ssl
# ssl._create_default_https_context = ssl._create_unverified_context# 全局取消证书验证，避免访问https网页报错
# username = 'admin'
# password = 'admin'
# url = 'https://ssr3.scrape.center/'
# p = HTTPPasswordMgrWithDefaultRealm()
# p.add_password(None,url,username,password)
# auth_handler = HTTPBasicAuthHandler(p)#实例化HTTPBasicAuthHandler对象
# opener = build_opener(auth_handler)#构建opener
# try:
#     result = opener.open(url)
#     html = result.read().decode('utf-8')
#     print(html)
# except URLError as e:
#     print(e.reason)
# from urllib.error import URLError
# from urllib.request import ProxyHandler,build_opener
#
# '''代理爬虫'''
# proxy_handler = ProxyHandler(
#     {
#         'http':'http://127.0.0.1:4973'
#         # 'https':'https://127.0.0.1:4973'[WinError 10061] 由于目标计算机积极拒绝，无法连接错误解决：只用设置一种代理IP地址即可，https简单点说就是http的安全版，因此只需设置一种IP。
#     }
# )
# opener = build_opener(proxy_handler)
# try:
#     response = opener.open('https://www.baidu.com')
#     print(response.read().decode('utf-8'))
# except URLError as e:
#     print(e.reason)
# import http.cookiejar,urllib.request
#
# '''获取网站的cookie'''
# cookie = http.cookiejar.CookieJar()
# handler = urllib.request.HTTPCookieProcessor(cookie)
# opener = urllib.request.build_opener(handler)
# response = opener.open('https://www.baidu.com')
# for item in cookie:
#     print(item.name+"="+item.value)
# import urllib.request,http.cookiejar
#
# '''保存cookie'''
# filename = 'cookie.txt'
# cookie = http.cookiejar.MozillaCookieJar(filename)#保存为Mozilla浏览器格式cookie
# handler = urllib.request.HTTPCookieProcessor(cookie)
# opener = urllib.request.build_opener(handler)
# response = opener.open('https://www.baidu.com')
# cookie.save(ignore_discard=True,ignore_expires=True)
# import urllib.request,http.cookiejar
#
# '''保存cookie'''
# filename = 'cookieLWP.txt'
# cookie = http.cookiejar.LWPCookieJar(filename)#保存为LWP格式cookie
# handler = urllib.request.HTTPCookieProcessor(cookie)
# opener = urllib.request.build_opener(handler)
# response = opener.open('https://www.baidu.com')
# cookie.save(ignore_discard=True,ignore_expires=True)
import urllib.request,http.cookiejar
cookie = http.cookiejar.LWPCookieJar()
cookie.load('cookieLWP.txt',ignore_expires=True,ignore_discard=True)
handler = urllib.request.HTTPCookieProcessor(cookie)
opener = urllib.request.build_opener(handler)
response = opener.open('https://www.baidu.com')
print(response.read().decode('utf-8'))
