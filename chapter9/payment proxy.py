import requests
'''隧道代理'''
url = 'http://www.httpbin.org/ip'

# 代理信息
proxy_host = 'tps281.kdlapi.com'
proxy_port = '15818'
proxy_username = 't14155613097858'
proxy_password = ''

proxy = f'http://{proxy_username}:{proxy_password}@{proxy_host}:{proxy_port}'
proxies = {
    'http': proxy,
    'https': proxy,
}
response = requests.get(url, proxies=proxies)
print(response.text)


'''接口代理'''
