from playwright.sync_api import sync_playwright

'''http代理'''
# with sync_playwright() as p:
#     browser = p.chromium.launch(headless=False, proxy={
#         'server': 'http://127.0.0.1:4780'
#     })
#     page = browser.new_page()
#     page.goto('https://httpbin.org/get')
#     print(page.content())
#     browser.close()


'''socks代理'''

# with sync_playwright() as p:
#     browser = p.chromium.launch(headless=False, proxy={
#         'server': 'socks5://127.0.0.1:4781'
#     })
#     page = browser.new_page()
#     page.goto('https://httpbin.org/get')
#     print(page.content())
#     browser.close()


'''需认证的代理'''


with sync_playwright() as p:
    browser = p.chromium.launch(headless=False, proxy={
        'server': 'http://127.0.0.1:4780',
        'username': 'foo',
        'password': 'bar'
    })
    page = browser.new_page()
    page.goto('https://httpbin.org/get')
    print(page.content())
    browser.close()
