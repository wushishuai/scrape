from chaojiying import Chaojiying

USERNAME = 'wss1016'
PASSWORD = 'CEyRLy98VXHFn.6'
SOFT_ID = '927405'
CAPTCHA_KIND = '1006'
FILE_NAME = 'captcha1.png'
client = Chaojiying(USERNAME, PASSWORD, SOFT_ID)
result = client.post_pic(open(FILE_NAME, 'rb').read(), CAPTCHA_KIND)
print(result)