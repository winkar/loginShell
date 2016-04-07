# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class ApkItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    package = scrapy.Field()
    url = scrapy.Field()
    file_urls = scrapy.Field()
    files = scrapy.Field()
    category = scrapy.Field()
    category_href = scrapy.Field()
