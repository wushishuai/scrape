import requests
import socks
import socket

# proxy = '127.0.0.1:4780'
# proxies = {
#     'http': 'http://' + proxy,
#     'https': 'http://' + proxy
# }
# try:
#     response = requests.get('http://www.httpbin.org/get', proxies=proxies)
#     print(response.text)
# except requests.exceptions.ConnectionError as e:
#     print('Error', e.args)

# proxy = '127.0.0.1:4781'
# proxies = {
#     'http':'socks5://'+proxy,
#     'https':'socks5://'+proxy
# }
#
# try:
#     response = requests.get('http://www.httpbin.org/get', proxies=proxies)
#     print(response.text)
# except requests.exceptions.ConnectionError as e:
#     print('Error', e.args)

socks.set_default_proxy(socks.SOCKS5, '127.0.0.1', 4781)
socket.socket = socks.socksocket

try:
    response = requests.get('http://www.httpbin.org/get')
    print(response.text)
except requests.exceptions.ConnectionError as e:
    print('Error', e.args)
