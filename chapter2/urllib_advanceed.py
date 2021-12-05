from urllib.request import HTTPPasswordMgrWithDefaultRealm,HTTPBasicAuthHandler,build_opener
from urllib.error import URLError
'''针对具有登录要求网页进行验证'''
import ssl
ssl._create_default_https_context = ssl._create_unverified_context# 全局取消证书验证，避免访问https网页报错
username = 'admin'
password = 'admin'
url = 'https://ssr3.scrape.center/'
p = HTTPPasswordMgrWithDefaultRealm()
p.add_password(None,url,username,password)
auth_handler = HTTPBasicAuthHandler(p)#实例化HTTPBasicAuthHandler对象
opener = build_opener(auth_handler)#构建opener
try:
    result = opener.open(url)
    html = result.read().decode('utf-8')
    print(html)
except URLError as e:
    print(e.reason)