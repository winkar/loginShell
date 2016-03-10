import re
import scrapy
from scrapy.linkextractors import LinkExtractor
from apkCrawler.items import ApkItem

from scrapy.spiders import Rule, CrawlSpider

class ApkSpider(CrawlSpider):
    name = "apk"
    allowed_domains = ["android.myapp.com"]
    start_urls = [
        "http://android.myapp.com/myapp/detail.htm?apkName=com.thestore.main"
    ]
    rules = (
        Rule(
            LinkExtractor(allow=(r"http://android.myapp.com/myapp/detail.htm")),
            callback='parse_apk',
            follow=True,
        ),
    )


    def parse_apk(self, response):
        item = ApkItem()

        item['url'] = response.url
        item['file_urls'] = response.xpath(r"//a[@class='det-down-btn']/@data-apkurl").extract()
        item['package'] = response.xpath(r"//a[@class='det-down-btn']/@apk").extract()

        yield item
