import httpx
import asyncio
from httpx_socks import SyncProxyTransport
from httpx_socks import AsyncProxyTransport

# proxy = '127.0.0.1:4780'
# proxies = {
#     'http://': 'http://' + proxy,
#     'https://': 'http://' + proxy
# }
#
# with httpx.Client(proxies=proxies) as client:
#     response = client.get('https://www.httpbin.org/get')
#     print(response.text)

'''同步模式'''

# transport = SyncProxyTransport.from_url('socks5://127.0.0.1:4781')
#
# with httpx.Client(transport=transport,timeout=10) as client:
#     response = client.get('https://www.httpbin.org/get')
#     print(response.text)


'''异步模式'''

transport = AsyncProxyTransport.from_url('socks5://127.0.0.1:4781')


async def main():
    async with httpx.AsyncClient(transport=transport) as client:
        response = await client.get('https://www.httpbin.org/get')
        print(response.text)

if __name__ == '__main__':
    asyncio.get_event_loop().run_until_complete(main())
