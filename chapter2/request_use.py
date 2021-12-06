import requests

'''request 使用实例'''

r = requests.get('https://www.baidu.com/')
print(type(r))
print(r.status_code)
print(type(r.text))
print(r.text[:100])
print(r.cookies)

'''requests 其他请求类型使用'''

r = requests.get('https://www.httpbin.org/get')
r = requests.post('https://www.httpbin.org/post')
r = requests.put('https://www.httpbin.org/put')
r = requests.delete('https://www.httpbin.org/delete')
r = requests.patch('https://www.httpbin.org/patch')