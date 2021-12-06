import requests
from requests.packages import urllib3
from requests.auth import HTTPBasicAuth

'''使用requests文件上传'''

# files = {'file':open('favicon.ico','rb')}
# r = requests.post('https://www.httpbin.org/post',files=files)
# print(r.text)
#
# '''cookie设置'''
#
# r = requests.get('https://www.baidu.com')
# print(r.cookies)
# for key,value in r.cookies.items():
#     print(key + '=' + value)
# proxies = {
#     'http':'5.58.33.187:55507',
#     'https':'5.58.33.187:55507'
#
# }
# headers = {
#     'Cookie':'_octo=GH1.1.1586078633.1625488879; _device_id=c25df80b0184dfacf300852ae99b956f; user_session=CH3ERsfpB1g6v9KsQDSYeBUgiAS1p9Ai9xKw2V7jntng1Ycu; __Host-user_session_same_site=CH3ERsfpB1g6v9KsQDSYeBUgiAS1p9Ai9xKw2V7jntng1Ycu; logged_in=yes; dotcom_user=wushishuai; color_mode=%7B%22color_mode%22%3A%22auto%22%2C%22light_theme%22%3A%7B%22name%22%3A%22light%22%2C%22color_mode%22%3A%22light%22%7D%2C%22dark_theme%22%3A%7B%22name%22%3A%22dark%22%2C%22color_mode%22%3A%22dark%22%7D%7D; tz=Asia%2FShanghai; has_recent_activity=1; _gh_sess=Fj96r1Zcg1Lvb8Ge22gYiz4Qz%2BJSQjQ6iZUPY7HpPI9Mj%2F3MDOF7oJwz3xmV6340bSBw6Ls%2BOcLkW8AhTwU81qt7mOfnMU7014rCErr6nM0y%2BXqqLUxhW9yXVl641Qm6eIdLIQDzwnwmx3CF9d6vKa7f124lOwW%2BapAxHQwAWxoSmDa9NQ%2FmrG7LTi%2BXp41wjXD8VOB0Mnpcz4Epog3Cz6D744gRGR7mbFLLmO60rksgrbTxr0%2FYva7zalmREM4X5QzmJI0WqLs1b7mclKWJmifabhXiR8Ay4EdfE6JkxTjuOTP%2Fqrg4uT%2FJpY1rXTARaE2FvfGpRI2HcEFYmBzcxB4oevYvoVGAl8YXGgwCgvjYfGvC0JSZ6kCEERpVH%2BAxAehHNlL6Egu66HPMwXZZOD%2FHydpjP7PU30SqP0GUD2U1VfiGvtOR8EadBwm1EJs8eNjFgcPed308JJe3n0M7xwg3THlcfzfD0iuZcwnOfBNB9MgHmsKjpVYXgK%2F10LLK4myWgVWwNPXPgz7CzW8PPe5PdYitmUuI--yI3aVVH19qdhMlnG--H0vVAPzKipWgzql86pTzSw%3D%3D',
#     'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36'
# }
# r = requests.get('https://github.com/',headers=headers,proxies=proxies)
# print(r.text)

'''seddion维持'''

requests.get('https://www.httpbin.org/cookies/set/number/123456789')
r = requests.get('https://www.httpbin.org/cookies')
print(r.text)
s = requests.session()
s.get('https://www.httpbin.org/cookies/set/number/123456789')
r = s.get('https://www.httpbin.org/cookies')
print(r.text)

'''ssl证书验证'''
urllib3.disable_warnings()#屏蔽警告
response = requests.get('https://ssr2.scrape.center/',verify=False)#忽略证书验证
print(response.status_code)

'''超时设置'''

r = requests.get('https://www.httpbin.org/get',timeout=1)
print(r.status_code)

'''身份认证'''
r = requests.get('https://ssr3.scrape.center/',auth = HTTPBasicAuth('admin','admin'))
print(r.status_code)
# r = requests.get('https://api.github.com/user', auth=('user', 'pass'))#这种写法貌似有问题
# print(r.status_code)
'''代理设置'''
proxies = {
    'http':'5.58.33.187:55507',
    'https':'5.58.33.187:55507'

}#可能失效
headers = {
    'Cookie':'_octo=GH1.1.1586078633.1625488879; _device_id=c25df80b0184dfacf300852ae99b956f; user_session=CH3ERsfpB1g6v9KsQDSYeBUgiAS1p9Ai9xKw2V7jntng1Ycu; __Host-user_session_same_site=CH3ERsfpB1g6v9KsQDSYeBUgiAS1p9Ai9xKw2V7jntng1Ycu; logged_in=yes; dotcom_user=wushishuai; color_mode=%7B%22color_mode%22%3A%22auto%22%2C%22light_theme%22%3A%7B%22name%22%3A%22light%22%2C%22color_mode%22%3A%22light%22%7D%2C%22dark_theme%22%3A%7B%22name%22%3A%22dark%22%2C%22color_mode%22%3A%22dark%22%7D%7D; tz=Asia%2FShanghai; has_recent_activity=1; _gh_sess=Fj96r1Zcg1Lvb8Ge22gYiz4Qz%2BJSQjQ6iZUPY7HpPI9Mj%2F3MDOF7oJwz3xmV6340bSBw6Ls%2BOcLkW8AhTwU81qt7mOfnMU7014rCErr6nM0y%2BXqqLUxhW9yXVl641Qm6eIdLIQDzwnwmx3CF9d6vKa7f124lOwW%2BapAxHQwAWxoSmDa9NQ%2FmrG7LTi%2BXp41wjXD8VOB0Mnpcz4Epog3Cz6D744gRGR7mbFLLmO60rksgrbTxr0%2FYva7zalmREM4X5QzmJI0WqLs1b7mclKWJmifabhXiR8Ay4EdfE6JkxTjuOTP%2Fqrg4uT%2FJpY1rXTARaE2FvfGpRI2HcEFYmBzcxB4oevYvoVGAl8YXGgwCgvjYfGvC0JSZ6kCEERpVH%2BAxAehHNlL6Egu66HPMwXZZOD%2FHydpjP7PU30SqP0GUD2U1VfiGvtOR8EadBwm1EJs8eNjFgcPed308JJe3n0M7xwg3THlcfzfD0iuZcwnOfBNB9MgHmsKjpVYXgK%2F10LLK4myWgVWwNPXPgz7CzW8PPe5PdYitmUuI--yI3aVVH19qdhMlnG--H0vVAPzKipWgzql86pTzSw%3D%3D',
    'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36'
}
r = requests.get('https://github.com/',headers=headers,proxies=proxies)
print(r.text)