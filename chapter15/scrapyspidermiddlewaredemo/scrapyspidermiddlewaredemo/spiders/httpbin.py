import scrapy
from scrapy import Spider,Request

from scrapyspidermiddlewaredemo.items import DemoItem

class HttpbinSpider(scrapy.Spider):
    name = 'httpbin'
    allowed_domains = ['www.httpbin.org']
    start_urls = 'http://www.httpbin.org/get'
    def start_requests(self):
        for i in range(5):
            url = f'{self.start_urls}?query={i}'
            yield Request(url,callback=self.parse)

    def parse(self, response):
        print('Status', response.status)
        item = DemoItem(**response.json())
        yield item

        # print(response.text)

