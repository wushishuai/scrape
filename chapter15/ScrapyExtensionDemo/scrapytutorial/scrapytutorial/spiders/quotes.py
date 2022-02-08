import scrapy
from scrapytutorial.items import QuoteItem

count = 11

class QuotesSpider(scrapy.Spider):
    name = 'quotes'
    allowed_domains = ['quotes.toscrape.com/']
    start_urls = ['https://quotes.toscrape.com/']

    def parse(self, response):
        quotes = response.css('.quote')
        for quote in quotes:
            item = QuoteItem()
            item['text'] = quote.css('.text::text').extract_first()
            item['author'] = quote.css('.author::text').extract_first()
            item['tags'] = quote.css('.tags .tag::text').extract()
            yield item
        # l = response.css('.pager .next a::attr(href)').extract_first()
        # url = response.urljoin(l)
        # yield scrapy.Request(url=url, callback=self.parse)
        global count
        count = count - 1
        if count >0:
            next = response.css('.pager .next a::attr(href)').extract_first()
            url = response.urljoin(next)
            yield scrapy.Request(url=url, callback=self.parse, dont_filter=True)
