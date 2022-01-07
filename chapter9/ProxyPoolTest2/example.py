import requests

proxypool_url = 'http://127.0.0.1:5555/random'


def get_proxy():
    try:
        response = requests.get(proxypool_url)
        if response.status_code == 200:
            return response.text
    except ConnectionError:
        return None


proxy = get_proxy()
proxies = {
    'http': 'http://' + proxy,
    'https': 'https://' + proxy,
}

try:
    response = requests.get('http://www.httpbin.org/get', proxies=proxies)
    print(response.text)
except requests.exceptions.ConnectionError as e:
    print('Error', e.args)
