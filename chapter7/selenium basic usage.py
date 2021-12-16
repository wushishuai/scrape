from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver import ActionChains
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

brower = webdriver.Chrome()
url = ('https://www.runoob.com/try/try.php?filename=jqueryui-api-droppable')
brower.get(url)
brower.switch_to.frame('iframeResult')
source = brower.find_element(By.CSS_SELECTOR,'#draggable')
target = brower.find_element(By.CSS_SELECTOR,'#droppable')
actions = ActionChains(brower)
actions.drag_and_drop(source,target)
actions.perform()