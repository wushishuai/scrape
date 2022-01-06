from chaojiying import Chaojiying
#
# USERNAME = 'wss1016'
# PASSWORD = 'CEyRLy98VXHFn.6'
# SOFT_ID = '927405'
# CAPTCHA_KIND = '9004'
# FILE_NAME = 'captcha2.png'
# client = Chaojiying(USERNAME, PASSWORD, SOFT_ID)
# result = client.post_pic(open(FILE_NAME, 'rb').read(), CAPTCHA_KIND)
# print(result)
# result = client.report_error('1164715017726700005')
# print(result)
# import cv2
#1164715097726700007
# image = cv2.imread('captcha2.png')
# image = cv2.circle(image, (108, 133), radius=10, color=(0, 0, 255), thickness=-1)
# image = cv2.circle(image, (227, 143), radius=10, color=(0, 0, 255), thickness=-1)
# cv2.imwrite('captcha2_label.png', image)
import cv2

image = cv2.imread('captcha2.png')
image = cv2.circle(image, (249, 170), radius=10, color=(0, 0, 255), thickness=-1)
image = cv2.circle(image, (118, 154), radius=10, color=(0, 0, 255), thickness=-1)
cv2.imwrite('captcha2_label.png', image)
