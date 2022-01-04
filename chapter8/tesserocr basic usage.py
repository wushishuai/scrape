import tesserocr
from PIL import Image
import numpy as np
import time
import re
from selenium import webdriver
from io import BytesIO
from retrying import retry
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException

'''简单使用'''
# image = Image.open('test.png')
# result = tesserocr.image_to_text(image)
# print(result)
# print(tesserocr.file_to_text('test.png'))

'''处理验证码'''

# image = Image.open('test.png')
# # print(np.array(image).shape)
# # print(image.mode)#查看图片信息
#
# image = image.convert('L')
# threshold = 100
# array = np.array(image)
# array = np.where(array > threshold,255,0)
# image = Image.fromarray(array.astype('uint8'))
# print(tesserocr.image_to_text(image))
# # image.show()
'''实战'''


def preprocess(image):
    image = image.convert('L')
    array = np.array(image)
    array = np.where(array > 100, 255, 0)
    image = Image.fromarray(array.astype('uint8'))
    return image


@retry(stop_max_attempt_number=50, retry_on_result=lambda x: x is False)
def login():
    browser.get('https://captcha7.scrape.center/')
    browser.find_element(By.CSS_SELECTOR, '.username input[type="text]').send_keys('admin')
    browser.find_element(By.CSS_SELECTOR, '.password input[type="password"]').send_keys('admin')
    captcha = browser.find_element(By.CSS_SELECTOR, '#captcha')
    image = Image.open(BytesIO(captcha.screenshot_as_png))
    image = preprocess(image)
    captcha = tesserocr.image_to_text(image)
    captcha = re.sub('[^A-Za-z0-9]', '', captcha)
    browser.find_element(By.CSS_SELECTOR, '.captcha input[type="text"]').send_keys(captcha)
    browser.find_element(By.CSS_SELECTOR, '.login').click()
    try:
        WebDriverWait(browser, 10).until(EC.presence_of_element_located((By.XPATH, '//h2[contains(.,"登录成功")]')))
        time.sleep(10)
        browser.close()
        return True
    except TimeoutException:
        return False


if __name__ == '__main__':
    browser = webdriver.Chrome()
    login()
