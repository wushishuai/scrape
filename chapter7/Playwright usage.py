from playwright.sync_api import sync_playwright
import asyncio
from playwright.async_api import async_playwright
import re
import time

'''基本使用'''
'''同步模式'''
# with sync_playwright() as p:
#     for browser_type in [p.chromium,p.firefox,p.webkit]:
#         browser = browser_type.launch(headless=False)
#         page = browser.new_page()
#         page.goto('https://www.baidu.com')
#         page.screenshot(path=f'screenshot-{browser_type.name}.png')
#         print(page.title())
#         browser.close()

'''异步模式'''

# async def main():
#     async with async_playwright() as p:
#         for browser_type in [p.chromium,p.firefox,p.webkit]:
#             browser = await browser_type.launch()
#             page = await browser.new_page()
#             await page.goto('https://www.baidu.com')
#             await page.screenshot(path=f'screenshot-{browser_type.name}.png')
#             print(await page.title())
#             await browser.close()
#
# asyncio.run(main())

'''代码生成'''

'''支持移动端浏览器'''

# with sync_playwright() as p:
#     iphone_12_pro_max = p.devices['iPhone 12 Pro Max']
#     browser = p.webkit.launch(headless=False)
#     context = browser.new_context(
#         **iphone_12_pro_max,
#         locale='zh-CN'
#     )
#     page = context.new_page()
#     page.goto('https://www.whatismybrowser.com/')
#     page.wait_for_load_state(state='networkidle')
#     page.screenshot(path='browser-iphone.png')
#     browser.close()

'''选择器'''

'''常用操作方法'''

'''事件监听'''


# def on_response(response):
#     # print(f'Status {response.status}:{response.url}')
#     if '/api/movie/' in response.url and response.status == 200:
#         print(response.json())
#
#
# with sync_playwright() as p:
#     browser = p.chromium.launch(headless=False)
#     page = browser.new_page()
#     page.on('response', on_response)
#     page.goto('https://spa6.scrape.center/')
#     page.wait_for_load_state('networkidle')
#     browser.close()

'''获取页面源代码'''


# with sync_playwright() as p:
#     browser = p.chromium.launch(headless=False)
#     page = browser.new_page()
#     page.goto('https://spa6.scrape.center/')
#     page.wait_for_load_state('networkidle')
#     html = page.content()
#     print(html)
#     browser.close()

'''网络劫持'''


# with sync_playwright() as p:
#     browser = p.chromium.launch(headless=False)
#     page = browser.new_page()
#
#     def cancel_request(route, request):
#         route.abort()
#
#     page.route(re.compile(r"(\.png)|(\.jpg)"), cancel_request)
#     page.goto("https://spa6.scrape.center/")
#     page.wait_for_load_state('networkidle')
#     page.screenshot(path='no_picture.png')
#     time.sleep(10)
#     browser.close()

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False)
    page = browser.new_page()

    def modify_response(route, request):
        route.fulfill(path="./custom_response.html")

    page.route('/', modify_response)
    page.goto("https://spa6.scrape.center/")
    time.sleep(10)
    browser.close()