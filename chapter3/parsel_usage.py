from parsel import Selector

html = '''
<div class="wrap">
    <div id="container">
        <ul class="list">
             <li class="item-0">first item</li>
             <li class="item-1"><a href="link2.html">second item</a></li>
             <li class="item-0 active"><a href="link3.html"><span class="bold">third item</span></a></li>
             <li class="item-1 active"><a href="link4.html">fourth item</a></li>
             <li class="item-0"><a href="link5.html">fifth item</a></li>
         </ul>
     </div>
 </div>
'''

selector = Selector(text=html)
items = selector.css('.item-0')
print(len(items),type(items),items)
items2 = selector.xpath('//li[contains(@class,"item-0")]')
print(len(items),type(items2),items2)
'''提取文本'''
for item in items:
    text = item.xpath('.//text()').get()
    print(text)
result = selector.xpath('//li[contains(@class,"item-0")]//text()').get()
print(result)
result = selector.xpath('//li[contains(@class,"item-0")]//text()').getall()
print(result)
result = selector.css('.item-0 *::text').getall()
print(result)
'''提取属性'''

result = selector.css('.item-0.active a::attr(href)').get()
print(result)
result = selector.xpath('//li[contains(@class,"item-0") and contains(@class,"active")]/a/@href').get()
print(result)

'''正则提取'''

result = selector.css('.item-0').re('link.*')
print(result)

result = selector.css('.item-0 *::text').re('.*item')
print(result)

result = selector.css('.item-0').re_first('<span class="bold">(.*?)</span>')
print(result)