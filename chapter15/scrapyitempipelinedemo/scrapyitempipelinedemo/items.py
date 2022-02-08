# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

from scrapy import Item,Field

class MovieItem(Item):
    name = Field()
    categories = Field()
    score = Field()
    drama = Field()
    directors = Field()
    actors = Field()