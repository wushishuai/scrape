import scrapy
from scrapy.linkextractors import LinkExtractor
from scrapy.spiders import CrawlSpider, Rule
from scrapyuniversaldemo.items import MovieItem

from ..loaders import MovieItemLoader

class MovieSpider(CrawlSpider):
    name = 'movie'
    allowed_domains = ['ssr1.scrape.center']
    start_urls = ['http://ssr1.scrape.center/']

    rules = (
        Rule(LinkExtractor(restrict_css='.item .name'), follow=True, callback='parse_detail'),
        Rule(LinkExtractor(restrict_css='.next'),follow=True)
    )

    def parse_detail(self, response):
        loader = MovieItemLoader(item=MovieItem(), response=response)
        loader.add_css('name', '.item h2::text')
        loader.add_css('categories', '.categories button span::text')
        loader.add_css('cover', '.cover::attr(src)')
        loader.add_css('published_at', '.info span::text', re='(\d{4}-\d{2}-\d{2})\s?上映')
        loader.add_xpath('score', '//p[contains(@class, "score")]/text()')
        loader.add_xpath('drama', '//div[contains(@class, "drama")]/p/text()')
        yield loader.load_item()
        # item = MovieItem()
        # item['name'] = response.css('.item h2::text').extract_first()
        # item['categories'] = response.css('.categories button span::text').extract()
        # item['cover'] = response.css('.cover::attr(src)').extract_first()
        # item['published_at'] = response.css('.info span::text').re_first('(\d{4}-\d{2}-\d{2})\s?上映')
        # item['score'] = response.xpath('//p[contains(@class, "score")]/text()').extract_first().strip()
        # item['drama'] = response.xpath('//div[contains(@class, "drama")]/p/text()').extract_first().strip()
        # yield item
        # print(response.url)


    # def parse_item(self, response):
    #     item = {}
    #     #item['domain_id'] = response.xpath('//input[@id="sid"]/@value').get()
    #     #item['name'] = response.xpath('//div[@id="name"]').get()
    #     #item['description'] = response.xpath('//div[@id="description"]').get()
    #     return item
