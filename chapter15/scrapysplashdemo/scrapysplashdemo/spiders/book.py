from scrapy import Spider
from scrapy_splash import SplashRequest
import re

from scrapysplashdemo.items import BookItem

script = """
function main(splash, args)
  assert(splash:go(args.url))
  assert(splash:wait(5))
  return splash:html()
end
"""
import os


class BookSpider(Spider):
    http_user = os.getenv('SPLASH_USER')
    http_pass = os.getenv('SPLASH_PASSWORD')
    name = 'book'
    allowed_domains = ['dynamic5.scrape.center']
    base_url = 'https://dynamic5.scrape.center'

    def start_requests(self):
        start_url = f'{self.base_url}/page/1'
        yield SplashRequest(start_url, callback=self.parse_index,
                            args={'lua_source': script}, endpoint='execute')

    def parse_index(self, response):
        """
        extract books and get next page
        :param response:
        :return:
        """
        items = response.css('.item')
        for item in items:
            href = item.css('.top a::attr(href)').extract_first()
            detail_url = response.urljoin(href)
            yield SplashRequest(detail_url, callback=self.parse_detail, priority=2,
                                args={'lua_source': script}, endpoint='execute')

        # next page
        match = re.search(r'page/(\d+)', response.url)
        if not match: return
        page = int(match.group(1)) + 1
        next_url = f'{self.base_url}/page/{page}'
        yield SplashRequest(next_url, callback=self.parse_index,
                            args={'lua_source': script}, endpoint='execute')

    def parse_detail(self, response):
        """
        process detail info of book
        :param response:
        :return:
        """
        name = response.css('.name::text').extract_first()
        tags = response.css('.tags button span::text').extract()
        score = response.css('.score::text').extract_first()
        price = response.css('.price span::text').extract_first()
        cover = response.css('.cover::attr(src)').extract_first()
        tags = [tag.strip() for tag in tags] if tags else []
        score = score.strip() if score else None
        item = BookItem(name=name, tags=tags, score=score, price=price, cover=cover)
        yield item
