from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver import ActionChains
from selenium.common.exceptions import NoSuchElementException, TimeoutException
from selenium.webdriver import ChromeOptions
import time

'''基本用法'''
# browser = webdriver.Chrome()
# try:
#     browser.get('https://www.baidu.com')
#     # input = browser.find_element_by_id('kw')
#     input = browser.find_element('id', 'kw')
#     input.send_keys('Python')
#     input.send_keys(Keys.ENTER)
#     wait = WebDriverWait(browser, 10)
#     wait.until(EC.presence_of_element_located((By.ID, 'content_left')))
#     print(browser.current_url)
#     print(browser.get_cookies())
#     print(browser.page_source)
# finally:
#     browser.close()

'''初始化浏览器对象'''

# brower = webdriver.Chrome()
# brower = webdriver.Firefox()
# brower = webdriver.Edge()
# brower = webdriver.Safari()

'''访问页面'''
# brower = webdriver.Chrome()

# brower.get('https://www.taobao.com')
# print(brower.page_source)
# brower.close()
'''查找单个节点'''

# brower = webdriver.Chrome()
# brower.get('https://www.taobao.com')
# input_first = brower.find_element(By.ID,'q')
# input_second = brower.find_element(By.CSS_SELECTOR,'#q')
# input_third = brower.find_element(By.XPATH,'//*[@id="q"]')
# print(input_first,input_second,input_third)
# brower.close()


'''查找多个节点'''

# brower = webdriver.Chrome()
# brower.get('https://www.taobao.com')
# lis = brower.find_elements(By.CSS_SELECTOR,'.service-bd li')
# print(lis)
# brower.close()

'''节点交互'''
# brower = webdriver.Chrome()
# brower.get('https://www.taobao.com')
# input = brower.find_element(By.ID,'q')
# input.send_keys('iphone')
# time.sleep(1)
# input.clear()
# input.send_keys('ipad')
# button = brower.find_element(By.CLASS_NAME,'btn-search')
# button.click()

'''动作链'''

# brower = webdriver.Chrome()
# url = ('https://www.runoob.com/try/try.php?filename=jqueryui-api-droppable')
# brower.get(url)
# brower.switch_to.frame('iframeResult')
# source = brower.find_element(By.CSS_SELECTOR,'#draggable')
# target = brower.find_element(By.CSS_SELECTOR,'#droppable')
# actions = ActionChains(brower)
# actions.drag_and_drop(source,target)
# actions.perform()


'''运行javascript'''

# brower = webdriver.Chrome()
# brower.get('https://www.zhihu.com/explore')
# brower.execute_script('window.scrollTo(0,document.body.scrollHeight)')
# brower.execute_script('alert("TO Botton")')


'''获取节点信息'''

# 获取属性

# brower = webdriver.Chrome()
# url = 'https://spa2.scrape.center/'
# brower.get(url)
# # log = brower.find_element(By.CLASS_NAME,'logo-image')
# # print(log)
# # print(log.get_attribute('src'))
# #获取文本
#
# input = brower.find_element(By.CLASS_NAME,'logo-title')
# print(input.text)
# #获取ID,位置，标签名和大小
#
# print(input.id)
# print(input.location)
# print(input.tag_name)
# print(input.size)


'''切换Frame'''

# brower = webdriver.Chrome()
# url = 'http://www.runoob.com/try/try.php?filename=jqueryui-api-droppable'
# brower.get(url)
# brower.switch_to.frame('iframeResult')
# try:
#     logo = brower.find_element(By.CLASS_NAME, 'logo')
# except NoSuchElementException:
#     print('NO LOGO')
# brower.switch_to.parent_frame()
# logo = brower.find_element(By.CLASS_NAME, 'logo')
# print(logo)
# print(logo.text)

'''延时等待'''

# 隐式等待

# browser = webdriver.Chrome()
# browser.implicitly_wait(10)
# browser.get('https://spa2.scrape.center/')
# input = browser.find_element(By.CLASS_NAME,'logo-image')
# print(input)

# 显示等待


# browser = webdriver.Chrome()
# browser.get('https://www.taobao.com/')
# wait = WebDriverWait(browser, 10)
# input = wait.until(EC.presence_of_element_located((By.ID, 'q')))
# button = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, '.btn-search')))
# print(input, button)

'''前进和后退'''

# browser = webdriver.Chrome()
# browser.get('https://www.baidu.com/')
# browser.get('https://www.taobao.com/')
# browser.get('https://www.python.org/')
# browser.back()
# time.sleep(1)
# browser.forward()
# browser.close()

'''cookie'''

# browser = webdriver.Chrome()
# browser.get('https://www.zhihu.com/explore')
# print(browser.get_cookies())
# browser.add_cookie({'name': 'name', 'domain': 'www.zhihu.com', 'value': 'germey'})
# print(browser.get_cookies())
# browser.delete_all_cookies()
# print(browser.get_cookies())

'''选项卡管理'''

# browser = webdriver.Chrome()
# browser.get('https://www.baidu.com')
# browser.execute_script('window.open()')
# print(browser.window_handles)
# browser.switch_to.window(browser.window_handles[1])
# browser.get('https://www.taobao.com')
# time.sleep(1)
# browser.switch_to.window(browser.window_handles[0])
# browser.get('https://python.org')

'''异常处理'''

# browser = webdriver.Chrome()
# browser.get('https://www.baidu.com')
# browser.find_element(By.ID, 'hello')

# brower = webdriver.Chrome()
# try:
#     brower.get('https://www.baidu.com/')
# except TimeoutException:
#     print('time out')
# try:
#     brower.find_element(By.ID, 'hello')
# except NoSuchElementException:
#     print('no element')
# finally:
#     brower.close()

'''反屏蔽'''

# browser = webdriver.Chrome()
# browser.get('https://antispider1.scrape.center/')
# time.sleep(1)
# browser.close()

# option = ChromeOptions()
# option.add_experimental_option('excludeSwitches', ['enable-automation'])
# option.add_experimental_option('useAutomationExtension', False)
# browser = webdriver.Chrome(options=option)
# browser.execute_cdp_cmd('Page.addScriptToEvaluateOnNewDocument',
#                         {'source': 'Object.defineProperty(navigator, "webdriver", {get: () => undefined})'})
# browser.get('https://antispider1.scrape.center/')


'''无头模式'''

option = ChromeOptions()
option.add_argument('--headless')
browser = webdriver.Chrome(options=option)
browser.set_window_size(1366, 768)
browser.get('https://www.baidu.com')
browser.get_screenshot_as_file('preview.png')
