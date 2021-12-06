import httpx
response = httpx.get('https://www.httpbin.org/get')
print(response.status_code)
print(response.headers)
print(response.text)

headers = {
    'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36'
}
response = httpx.get('https://www.httpbin.org/get',headers=headers)
print(response.text)

client = httpx.Client(http2=True)
response = client.get('https://spa16.scrape.center/')
print(response.text)

with httpx.Client() as client:
    response = client.get('https://www.httpbin.org/get')
    print(response)

'''等价于以下用法'''

client = httpx.Client()
try:
    response = client.get('https://www.httpbin.org/get')
finally:
    client.close()
'''附带参数'''

url = 'https://www.httpbin.org/headers'
with httpx.Client(headers=headers) as client:
    r = client.get(url)
    print(r.json()['headers']['User-Agent'])
'''支持HTTP/2.0'''
client = httpx.Client(http2=True)
response = client.get('https://www.httpbin.org/get')
print(response.text)
print(response.http_version)

'''支持异步请求'''

import httpx
import asyncio


async def fetch(url):
    async with httpx.AsyncClient(http2=True) as client:
        response = await client.get(url)
        print(response.text)

if __name__ == '__main__':
    asyncio.get_event_loop().run_until_complete(fetch('https://httpbin.org/get'))