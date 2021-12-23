import json
import logging

from selenium import webdriver
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait
from urllib.parse import urljoin
from os import makedirs
from os.path import exists

RESULTS_DIR = 'results'
exists(RESULTS_DIR) or makedirs(RESULTS_DIR)

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s:%(message)s')

INDEX_URL = 'https://spa2.scrape.center/page/{page}'
DETAIL_URL = 'https://spa2.scrape.center/'
TIME_OUT = 10
TOTAL_PAGE = 10

# browser = webdriver.Chrome()
options = webdriver.ChromeOptions()
options.add_argument('--headless')
browser = webdriver.Chrome(options=options)
wait = WebDriverWait(browser, TIME_OUT)


def scrape_page(url, condition, locator):
    logging.info('scraping %s', url)
    try:
        browser.get(url)
        wait.until(condition(locator))
    except TimeoutException:
        logging.error('error occurred while scraping %s', url, exc_info=True)


def scrape_index(page):
    url = INDEX_URL.format(page=page)
    scrape_page(url, condition=EC.visibility_of_all_elements_located, locator=(By.CSS_SELECTOR, '#index .item'))


def parse_index():
    elements = browser.find_elements(By.CSS_SELECTOR, '#index .item')
    for element in elements:
        tag = element.find_element(By.TAG_NAME, 'a')
        # logging.error(element.text)
        href = tag.get_attribute('href')
        yield urljoin(DETAIL_URL, href)


def scrape_detail(url):
    scrape_page(url, condition=EC.visibility_of_all_elements_located, locator=(By.TAG_NAME, 'h2'))


def parse_detail():
    url = browser.current_url
    name = browser.find_element(By.TAG_NAME, 'h2').text
    categories = [element.text for element in browser.find_elements(By.CSS_SELECTOR, '.categories button span')]
    cover = browser.find_element(By.CSS_SELECTOR, '.cover').get_attribute('src')
    score = browser.find_element(By.CLASS_NAME, 'score').text
    drama = browser.find_element(By.CSS_SELECTOR, '.drama p').text
    return {
        'url': url,
        'name': name,
        'categories': categories,
        'cover': cover,
        'score': score,
        'drama': drama
    }


def save_data(data):
    name = data.get('name')
    data_path = f'{RESULTS_DIR}/{name}.json'
    json.dump(data, open(data_path, 'w', encoding='utf-8'), ensure_ascii=False, indent=2)


def main():
    try:
        for page in range(1, TOTAL_PAGE + 1):
            scrape_index(page)
            detail_urls = parse_index()
            # logging.info('details urls %s', list(detail_urls))
            for detail_url in list(detail_urls):
                logging.info('get detail url %s', detail_url)
                scrape_detail(detail_url)
                detail_data = parse_detail()
                logging.info('detail data %s', detail_data)
                save_data(detail_data)
    finally:
        browser.close()


if __name__ == '__main__':
    main()
