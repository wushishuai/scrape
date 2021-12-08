from lxml import etree
text = '''
<div>
    <ul>
         <li class="item-0"><a href="link1.html">first item</a></li>
         <li class="item-1"><a href="link2.html">second item</a></li>
         <li class="item-inactive"><a href="link3.html">third item</a></li>
         <li class="item-1"><a href="link4.html">fourth item</a></li>
         <li class="item-0"><a href="link5.html">fifth item</a>
     </ul>
 </div>
'''
html = etree.HTML(text)
result = etree.tostring(html)
# print(result.decode('utf-8'))

'''不声明，直接读取文本文件'''

html = etree.parse('./test.html',etree.HTMLParser())
result = etree.tostring(html)
# print(result.decode('utf-8'))

'''所有节点'''
html = etree.parse('./test.html',etree.HTMLParser())
result = html.xpath('//*')
# print(result)

'''匹配指定节点名称'''

html = etree.parse('./test.html',etree.HTMLParser())
result = html.xpath('//li')
# print(result)
# print(result[0])

html = etree.parse('./test.html', etree.HTMLParser())
result = html.xpath('//li/a')
# print(result)

html = etree.parse('./test.html', etree.HTMLParser())
result = html.xpath('//ul//a')
# print(result)
'''父节点'''

html = etree.parse('./test.html', etree.HTMLParser())
# result = html.xpath('//a[@href="link4.html"]/../@class')


html = etree.parse('./test.html', etree.HTMLParser())
result = html.xpath('//a[@href="link4.html"]/parent::*/@class')
# print(result)

'''属性匹配'''

html = etree.parse('./test.html', etree.HTMLParser())

result = html.xpath('//li[@class="item-0"]')
# print(result)

'''文本获取'''

html = etree.parse('./test.html', etree.HTMLParser())

result = html.xpath('//li[@class="item-0"]/text()')
# print(result)
'''如果要想获取子孙节点内部的所有文本，可以直接用 // 加 text 
方法的方式，这样可以保证获取到最全面的文本信息，但是可能会夹杂一些换行符等特殊字符。如果想获取某些特定子孙节点下的所有文本，可以先选取到特定的子孙节点，然后再调用 text 方法方法获取其内部文本，这样可以保证获取的结果是整洁的 '''


html = etree.parse('./test.html', etree.HTMLParser())
result = html.xpath('//li[@class="item-0"]/a/text()')
# print(result)

html = etree.parse('./test.html', etree.HTMLParser())
result = html.xpath('//li[@class="item-0"]//text()')
# print(result)

'''属性获取'''


html = etree.parse('./test.html', etree.HTMLParser())
result = html.xpath('//li/a/@href')
# print(result)

'''属性多值匹配'''


text = '''  
<li class="li li-first"><a href="link.html">first item</a></li> 
'''
html = etree.HTML(text)
result = html.xpath('//li[@class="li"]/a/text()')
# print(result)

result = html.xpath('//li[contains(@class,"li")]/a/text()')#contains 方法，第一个参数传入属性名称，第二个参数传入属性值，只要此属性包含所传入的属性值，就可以完成匹配了
# print(result)

'''多属性匹配'''

text = '''  
<li class="li li-first" name="item"><a href="link.html">first item</a></li>
'''
html = etree.HTML(text)
result = html.xpath('//li[contains(@class,"li") and @name ="item"]/a/text()')
# print(result)

'''按序选择'''

text = '''
<div>
    <ul>
         <li class="item-0"><a href="link1.html">first item</a></li>
         <li class="item-1"><a href="link2.html">second item</a></li>
         <li class="item-inactive"><a href="link3.html">third item</a></li>
         <li class="item-1"><a href="link4.html">fourth item</a></li>
         <li class="item-0"><a href="link5.html">fifth item</a>
     </ul>
 </div>
'''
html = etree.HTML(text)
result = html.xpath('//li[1]/a/text()')
# print(result)
result = html.xpath('//li[last()]/a/text()')
# print(result)
result = html.xpath('//li[position()<3]/a/text()')
# print(result)
result = html.xpath('//li[last()-2]/a/text()')
# print(result)


'''节点轴选择'''

text = '''
<div>
    <ul>
         <li class="item-0"><a href="link1.html"><span>first item</span></a></li>
         <li class="item-1"><a href="link2.html">second item</a></li>
         <li class="item-inactive"><a href="link3.html">third item</a></li>
         <li class="item-1"><a href="link4.html">fourth item</a></li>
         <li class="item-0"><a href="link5.html">fifth item</a>
     </ul>
 </div>
'''
html = etree.HTML(text)
result = html.xpath('//li[1]/ancestor::*')
print(result)
result = html.xpath('//li[1]/ancestor::div')
print(result)
result = html.xpath('//li[1]/attribute::*')
print(result)
result = html.xpath('//li[1]/child::a[@href="link1.html"]')
print(result)
result = html.xpath('//li[1]/descendant::span')
print(result)
result = html.xpath('//li[1]/following::*[2]')
print(result)
result = html.xpath('//li[1]/following-sibling::*')
print(result)
