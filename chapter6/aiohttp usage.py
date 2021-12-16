import aiohttp
import asyncio

'''基本实例'''

# async def fetch(session, url):
#     async with session.get(url) as response:
#         return await response.text(), response.status
#
#
# async def main():
#     async with aiohttp.ClientSession() as session:
#         html, status = await fetch(session, 'https://www.baidu.com')
#         print(f'html:{html[:100]}...')
#         print(f'status:{status}')
#
#
# if __name__ == '__main__':
#     loop = asyncio.get_event_loop()
#     loop.run_until_complete(main())


'''url参数设置'''

#
# async def main():
#     params = {'name': 'germey', 'age': 25}
#     async with aiohttp.ClientSession() as session:
#         async with session.get('https://www.httpbin.org/get', params=params) as response:
#             print(await response.text())
#
#
# if __name__ == '__main__':
#     asyncio.get_event_loop().run_until_complete(main())


'''post 请求'''

# async def main():
#     data = {'name': 'germey', 'age': 25}
#     async with aiohttp.ClientSession() as session:
#         async with session.post('https://www.httpbin.org/post', data=data) as response:
#             print(await response.text())
#
#
# if __name__ == '__main__':
#     asyncio.get_event_loop().run_until_complete(main())

'''响应'''

# async def main():
#     data = {'name': 'germey', 'age': 25}
#     async with aiohttp.ClientSession() as session:
#         async with session.post('https://www.httpbin.org/post', data=data) as response:
#             print('status:', response.status)
#             print('headers:', response.headers)
#             print('body:', await response.text())
#             print('bytes:', await response.read())
#             print('json:', await response.json())
#
#
# if __name__ == '__main__':
#     asyncio.get_event_loop().run_until_complete(main())


'''超时设置'''

# async def main():
#     timeout = aiohttp.ClientTimeout(total=1)
#     async with aiohttp.ClientSession(timeout=timeout) as session:
#         async with session.get('https://www.httpbin.org/get') as response:
#             print('status:', response.status)
#
#
# if __name__ == '__main__':
#     asyncio.get_event_loop().run_until_complete(main())


'''并发限制
借助asyncio的semaphore控制并发量'''

CONCURRENCY = 5
URL = 'https://www.baidu.com'

semaphore = asyncio.Semaphore(CONCURRENCY)
session = None


async def scrape_api():
    async with semaphore:
        print('scraping', URL)
        async with session.get(URL) as response:
            await asyncio.sleep(1)
            return await response.text()


async def main():
    global session
    session = aiohttp.ClientSession()
    scrape_index_tasks = [asyncio.ensure_future(scrape_api()) for _ in range(10000)]
    await asyncio.gather(*scrape_index_tasks)


if __name__ == '__main__':
    asyncio.get_event_loop().run_until_complete(main())
