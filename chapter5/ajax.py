import requests
import logging
import random
import pymongo
import time

# url = 'https://spa1.scrape.center/'
# html = requests.get(url).text
# print(html)

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s: %(message)s')
INDEX_URL = 'https://spa1.scrape.center/api/movie/?limit={limit}&offset={offset}'
DETAIL_URL = 'https://spa1.scrape.center/api/movie/{id}'
LIMIT = 10
TOTAL_PAGE = 10
MONGO_CONNECTTON_STRING = 'mongodb://localhost:27017'
client = pymongo.MongoClient(MONGO_CONNECTTON_STRING)
db = client['movies']
collection = db['movies']

user_agent_list = [
    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36",
    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36",
    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36",
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.62 Safari/537.36",
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)",
    "Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10.5; en-US; rv:1.9.2.15) Gecko/20110303 Firefox/3.6.15"
]
#
# headers = {
#     'User-Agent': 'Mozilla/5.0',
#     'Content-Type': 'application/json',
#     'method': 'GET',
#     'Accept': 'application/vnd.github.cloak-preview'
# }
# proxies = {
#     "http": '209.141.35.151:80',
#     "https": '209.141.35.151:80',
# }


def scrape_api(url):
    logging.info('scraping %s...', url)
    try:
        # headers['User-Agent'] = random.choice(user_agent_list)
        headers = {'User-Agent': random.choice(user_agent_list)}
        time.sleep(6)
        response = requests.get(url,headers=headers)
        if response.status_code == 200:
            return response.json()
        logging.error('get invalid status code %s while sraping %s', response.status_code, url)
    except requests.RequestException:
        logging.error('error occurred while scraping %s', url, exc_info=True)


def scrape_index(page):
    url = INDEX_URL.format(limit=LIMIT, offset=LIMIT * (page - 1))
    return scrape_api(url)


def scrape_detail(id):
    url = DETAIL_URL.format(id=id)
    return scrape_api(url)


def save_data(data):
    collection.update_one({
        'name': data.get('name')}, {
        '$set': data}, upsert=True)


def main():
    for page in range(1, TOTAL_PAGE + 1):
        index_data = scrape_index(page)
        for item in index_data.get('results'):
            id = item.get('id')
            detail_data = scrape_detail(id)
            logging.info('detail data %s', detail_data)
            save_data(detail_data)
            logging.info('data saved successfully')


if __name__ == '__main__':
    main()
