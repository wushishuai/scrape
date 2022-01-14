import requests

BASE_URL = 'https://app1.scrape.center/api/movie?offset={offset}&limit=10'

for i in range(0,10):
    offset = i*10
    url = BASE_URL.format(offset=offset)
    data = requests.get(url).json()
    print('data',data)