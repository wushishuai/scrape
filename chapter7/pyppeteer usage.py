import asyncio
from pyppeteer import launch
from pyquery import PyQuery as pq

'''使用示例'''
# async def main():
#     brower = await launch()
#     page = await brower.newPage()
#     await page.goto('https://spa2.scrape.center/')
#     await page.waitForSelector('.item .name')
#     doc = pq(await page.content())
#     names = [item.text() for item in doc('.item .name').items()]
#     print('Name:',names)
#     await brower.close()
#
# asyncio.get_event_loop().run_until_complete(main())

# width,height = 1366,768
# async def main():
#     browser = await launch()
#     page = await browser.newPage()
#     await page.setViewport({'width':width,'height':height})
#     await page.goto('https://spa2.scrape.center/')
#     await page.waitForSelector('.item .name')
#     await asyncio.sleep(2)
#     await page.screenshot(path='example.png')
#     dimensions = await page.evaluate('''() => {
#     return {
#     width: document.documentElement.clientWidth,
#     height: document.documentElement.clientHeight,
#     deviceScaleFactor:window.devicePixelRatio
#     }
#     }''')
#     print(dimensions)
#     await browser.close()
# asyncio.get_event_loop().run_until_complete(main())

'''对launch方法操作'''

'''无头模式'''

# async def main():
#     await launch(headless = False)
#     await asyncio.sleep(100)
#
# asyncio.get_event_loop().run_until_complete(main())

'''调试模式'''

#
# async def main():
#     browser = await launch(devtools=True)
#     page = await browser.newPage()
#     await page.goto('https://www.baidu.com')
#     await asyncio.sleep(100)
#
#
# asyncio.get_event_loop().run_until_complete(main())

'''禁用提示条'''

# async def main():
#     browser = await launch(devtools=True,args=['--disable-infobars'])
#     page = await browser.newPage()
#     await page.goto('https://www.baidu.com')
#     await asyncio.sleep(100)
#
# asyncio.get_event_loop().run_until_complete(main())

'''防止检测'''

# async def main():
#     browser = await launch(devtools=True,args=['--disable-infobars'])
#     page = await browser.newPage()
#     await page.evaluateOnNewDocument('Object.defineProperty(navigator,"webdriver",{get:() => undefined})')
#     await page.goto('https://antispider1.scrape.center/')
#     await asyncio.sleep(100)
#
# asyncio.get_event_loop().run_until_complete(main())

'''页面大小设置'''
# width, height = 1366, 768
#
#
# async def main():
#     browser = await launch(headless=False, args=['--disable-infobars', f'--window-size={width},{height}'])
#     page = await browser.newPage()
#     await page.setViewport({'width': width, 'height': height})
#     await page.evaluateOnNewDocument('Object.defineProperty(navigator, "webdriver", {get: () => undefined})')
#     await page.goto('https://antispider1.scrape.center/')
#     await asyncio.sleep(100)
#
#
# asyncio.get_event_loop().run_until_complete(main())


'''用户数据持久化'''

# width, height = 1366, 768
#
#
# async def main():
#     browser = await launch(headless=False, userDataDir='./userdata',
#                            args=['--disable-infobars', f'--window-size={width},{height}'])
#     page = await browser.newPage()
#     await page.setViewport({'width': width, 'height': height})
#     await page.goto('https://www.taobao.com')
#     await asyncio.sleep(100)
#
#
# asyncio.get_event_loop().run_until_complete(main())

'''对browser方法操作'''
'''开启无痕模式'''
# width, height = 1200, 768
#
# async def main():
#     browser = await launch(headless=False,
#                            args=['--disable-infobars', f'--window-size={width},{height}'])
#     context = await browser.createIncognitoBrowserContext()
#     page = await context.newPage()
#     await page.setViewport({'width': width, 'height': height})
#     await page.goto('https://www.baidu.com')
#     await asyncio.sleep(100)
#
#
# asyncio.get_event_loop().run_until_complete(main())

'''关闭'''

# async def main():
#     browser = await launch()
#     page = await browser.newPage()
#     await page.goto('https://spa2.scrape.center/')
#     await browser.close()
#
#
# asyncio.get_event_loop().run_until_complete(main())

'''页面对象操作'''

# async def main():
#     browser = await launch()
#     page = await browser.newPage()
#     await page.goto('https://spa2.scrape.center/')
#     await page.waitForSelector('.item .name')
#     j_result1 = await page.J('.item .name')
#     j_result2 = await page.querySelector('.item .name')
#     jj_result1 = await page.JJ('.item .name')
#     jj_result2 = await page.querySelectorAll('.item .name')
#     print('J Result1:', j_result1)
#     print('J Result2:', j_result2)
#     print('JJ Result1:', jj_result1)
#     print('JJ Result2:', jj_result2)
#     await browser.close()
#
#
# asyncio.get_event_loop().run_until_complete(main())

'''选项卡操作'''

# async def main():
#     browser = await launch(headless=False)
#     page = await browser.newPage()
#     await page.goto('https://www.baidu.com')
#     page = await browser.newPage()
#     await page.goto('https://www.bing.com')
#     pages = await browser.pages()
#     print('pages:', pages)
#     page1 = pages[1]
#     await page1.bringToFront()
#     await asyncio.sleep(100)
#
#
# asyncio.get_event_loop().run_until_complete(main())

'''页面操作'''

# async def main():
#     browser = await launch(headless=True)#保存pdf时需将headless设为True
#     page = await browser.newPage()
#     await page.goto('https://www.baidu.com')
#     await page.goto('https://spa2.scrape.center/')
#     # 后退
#     await page.goBack()
#     # 前进
#     await page.goForward()
#     # 刷新
#     await page.reload()
#     # 保存 PDF
#     await asyncio.sleep(4)
#     await page.pdf(path='example2.pdf')
#     # 截图
#     await page.screenshot(path='example2.png')
#     # 设置页面 HTML
#     await page.setContent('<h2>Hello World</h2>')
#     # 设置 User-Agent
#     await page.setUserAgent('Python')
#     # 设置 Headers
#     await page.setExtraHTTPHeaders(headers={})
#     # 关闭
#     await page.close()
#     await browser.close()
#
#
# asyncio.get_event_loop().run_until_complete(main())

'''模拟点击'''


# async def main():
#     browser = await launch(headless=False)
#     page = await browser.newPage()
#     await page.goto('https://spa2.scrape.center/')
#     await page.waitForSelector('.item .name')
#     await page.click('.item .name', options={
#         'button': 'right',
#         'clickCount': 1,  # 1 or 2
#         'delay': 3000,  # 毫秒
#     })
#     await asyncio.sleep(5)
#     await browser.close()
#
#
# asyncio.get_event_loop().run_until_complete(main())

'''输入文本'''

# async def main():
#     browser = await launch(headless=False)
#     page = await browser.newPage()
#     await page.goto('https://www.taobao.com')
#     # 后退
#     await page.type('#q', 'iPad')
#     # 关闭
#     await asyncio.sleep(10)
#     await browser.close()
#
#
# asyncio.get_event_loop().run_until_complete(main())


'''获取信息'''


# async def main():
#     browser = await launch(headless=False)
#     page = await browser.newPage()
#     await page.goto('https://spa2.scrape.center/')
#     await asyncio.sleep(5)
#     print('HTML:', await page.content())
#     print('Cookies:', await page.cookies())
#     await browser.close()
#
#
# asyncio.get_event_loop().run_until_complete(main())

'''执行JavaScript'''
width,height = 1366,768

async def main():
    browser = await launch()
    page = await browser.newPage()
    await page.setViewport({'width':width,'height':height})
    await page.goto('https://spa2.scrape.center/')
    await page.waitForSelector('.item .name')
    await asyncio.sleep(2)
    dimensions = await page.evaluate('''() => {
        return {
            width: document.documentElement.clientWidth,
            height: document.documentElement.clientHeight,
            deviceScaleFactor: window.devicePixelRatio,
        }
    }''')

    print(dimensions)
    # >>> {'width': 800, 'height': 600, 'deviceScaleFactor': 1}
    await browser.close()


asyncio.get_event_loop().run_until_complete(main())