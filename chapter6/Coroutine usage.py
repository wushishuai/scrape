import asyncio
import time

import requests
import aiohttp

'''定义协程'''
# async def execute(x):
#     print('Number:',x)
#
# coroutine = execute(1)
# print('Coroutine',coroutine)
# print('After calling execute')
#
# loop =asyncio.get_event_loop()
# loop.run_until_complete(coroutine)
# print('Afetr calling loop')
#
# async def execute(x):
#     print('Number:',x)
#     return x
#
# coroutine = execute(1)
# print('Coroutine',coroutine)
# print('After calling execute')
#
# loop =asyncio.get_event_loop()
# task = loop.create_task(coroutine)
# print('Task',task)
# loop.run_until_complete(task)
# print('Task:',task)
# print('After calling loop')

# async def execute(x):
#     print('Number:', x)
#     return x
#
#
# coroutine = execute(1)
# print('Coroutine', coroutine)
# print('After calling execute')
#
# task = asyncio.ensure_future(coroutine)
# print('Task:', task)
# loop = asyncio.get_event_loop()
# loop.run_until_complete(task)
# print('Task:', task)
# print('After calling loop')


'''绑定回调'''

# async def request():
#     url = 'https://www.baidu.com'
#     status = requests.get(url).status_code
#     return status
#
#
# def callback(task):
#     print('Status:', task.result())
#
#
# coroutine = request()
# task = asyncio.ensure_future(coroutine)
# task.add_done_callback(callback)
# print('Task:', task)
#
# loop = asyncio.get_event_loop()
# loop.run_until_complete(task)
# print('Task:', task)

# async def request():
#     url = 'https://www.baidu.com'
#     status = requests.get(url).status_code
#     return status
#
#
# coroutine = request()
# task = asyncio.ensure_future(coroutine)
# print('Task:', task)
#
# loop = asyncio.get_event_loop()
# loop.run_until_complete(task)
# print('Task:', task)
# print('task result:', task.result())


'''多任务协程'''

# async def request():
#     url = 'https://www.baidu.com'
#     status = requests.get(url).status_code
#     return status
#
#
# tasks = [asyncio.ensure_future(request()) for _ in range(5)]
# print('Tasks:', tasks)
# loop = asyncio.get_event_loop()
# loop.run_until_complete(asyncio.wait(tasks))
#
# for task in tasks:
#     print('Task Result:', task.result())


'''协程实现'''

# start = time.time()
#
#
# async def get(url):
#     session = aiohttp.ClientSession()
#     response = await session.get(url)
#     await response.text()
#     await session.close()
#     return response
#
#
# async def request():
#     url = 'https://www.httpbin.org/delay/5'
#     print('Waiting for ', url)
#     response = await get(url)
#     print('Get response from ', url, 'response', response)
#
#
# tasks = [asyncio.ensure_future(request()) for _ in range(10)]
# loop = asyncio.get_event_loop()
# loop.run_until_complete(asyncio.wait(tasks))
#
# end = time.time()
# print('Cost time', end - start)


'''高并发下'''


def test(number):
    start = time.time()

    async def get(url):
        session = aiohttp.ClientSession()

        response = await session.get(url)
        await response.text()
        await session.close()
        return response

    async def request():
        url = 'https://www.baidu.com/'
        await get(url)

    tasks = [asyncio.ensure_future(request()) for _ in range(number)]
    loop = asyncio.get_event_loop()
    loop.run_until_complete(asyncio.wait(tasks))

    end = time.time()
    print('Number:', number, 'Cost time:', end - start)


for number in [1, 3, 5, 10, 15, 30, 50, 75, 100, 200, 500]:
    test(number)