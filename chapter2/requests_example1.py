import requests
import re
'''GET请求'''

r = requests.get('https://www.httpbin.org/get')
print(r.text)
'''附加额外信息'''

data = {
    'name':'germey',
    'age':25
}
r = requests.get('https://www.httpbin.org/get',params=data)
print(r.text)
'''解析返回结果，得到json格式数据'''

r = requests.get('https://www.httpbin.org/get')
print(type(r.text))
print(r.json())
print(type(r.json()))#将json格式字符串转化为字典

'''抓取网页'''

r = requests.get('https://ssr1.scrape.center/')
pattern = re.compile('<h2.*?>(.*?)</h2>',re.S)
title = re.findall(pattern,r.text)
print(title)
'''抓取二进制文件'''

r = requests.get('https://scrape.center/favicon.ico')
print(r.text)#str类型，图片直接转化为字符串
print(r.content)#bytes类型
'''保存二进制文件'''
r = requests.get('https://scrape.center/favicon.ico')
with open('favicon.ico','wb') as f:
    f.write(r.content)

'''添加请求头'''

headers = {
    'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36'
}
r = requests.get('https://ssr1.scrape.center/',headers=headers)
print(r.text)
'''POST请求'''
data = {'name':'germey','age':'25'}
r = requests.post('https://www.httpbin.org/post',data=data)
print(r.text)

'''对响应具体解析'''

r = requests.get('https://ssr1.scrape.center/')
print(type(r.status_code),r.status_code)
print(type(r.headers),r.headers)
print(type(r.cookies),r.cookies)
print(type(r.url),r.url)
print(type(r.history),r.history)