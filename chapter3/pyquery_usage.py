from pyquery import PyQuery as pq
import requests

html = '''
<div>
    <ul>
         <li class="item-0">first item</li>
         <li class="item-1"><a href="link2.html">second item</a></li>
         <li class="item-0 active"><a href="link3.html"><span class="bold">third item</span></a></li>
         <li class="item-1 active"><a href="link4.html">fourth item</a></li>
         <li class="item-0"><a href="link5.html">fifth item</a></li>
     </ul>
 </div>
'''
'''字符串初始化'''
doc = pq(html)
# print(doc('li'))
'''url初始化'''

# doc = pq(url='https://cuiqingcai.com')
# print(doc('title'))

# doc = pq(requests.get('https://cuiqingcai.com').text)
# print(doc('title'))
'''文本初始化'''

doc = pq(filename='test.html')
# print(doc('li'))

'''基本css选择器'''

html = '''
<div id="container">
    <ul class="list">
         <li class="item-0">first item</li>
         <li class="item-1"><a href="link2.html">second item</a></li>
         <li class="item-0 active"><a href="link3.html"><span class="bold">third item</span></a></li>
         <li class="item-1 active"><a href="link4.html">fourth item</a></li>
         <li class="item-0"><a href="link5.html">fifth item</a></li>
     </ul>
 </div>
'''

doc = pq(html)
# print(doc('#container .list li'))
# print(type(doc('#container .list li')))


items = doc('.list')
# print(type(items))
# print(items)
# lis = items.find('li')#find() 方法会将符合条件的所有节点选择出来
# print(type(lis))
# print(lis)

''' find 的查找范围是节点的所有子孙节点，而如果只想查找子节点，可以用children 方法'''
lis = items.children()
# print(type(lis))
# print(lis)
lis = items.children('.active')
# print(lis)

'''父节点'''

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

doc = pq(html)
items = doc('.list')
container = items.parent()#直接父节点
# print(type(container))
# print(container)

doc = pq(html)
items = doc('.list')
# parents = items.parents()
# print(type(parents))
# print(parents)
parent = items.parents('.wrap')
# print(parent)

'''兄弟节点'''

doc = pq(html)
li = doc('.list .item-0.active')
# print(li.siblings())
# print(li.siblings('.active'))


'''遍历'''

li = doc('.item-0.active')
# print(li)
# print(str(li))

lis = doc('li').items()
# print(type(lis))
# for li in lis:
#     print(li, type(li))

'''获取信息'''

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
from pyquery import PyQuery as pq
doc = pq(html)
a = doc('.item-0.active a')
# print(a, type(a))
# print(a.attr('href'))
# print(a.attr.href)

a = doc('a')
# print(a, type(a))
# print(a.attr('href'))#返回结果包含多个节点时，调用 attr 方法，只会得到第一个节点的属性
# print(a.attr.href)


# for item in a.items():#想获取所有的 a 节点的属性.利用遍历
#     print(item.attr('href'))
a = doc('.item-0.active a')
# print(a)
# print(a.text())#获取文本
# li = doc('.item-0.active')
# print(li)
# print(li.html())#获取节点内部的 HTML 文本
html = '''
<div class="wrap">
    <div id="container">
        <ul class="list">
             <li class="item-1"><a href="link2.html">second item</a></li>
             <li class="item-0 active"><a href="link3.html"><span class="bold">third item</span></a></li>
             <li class="item-1 active"><a href="link4.html">fourth item</a></li>
             <li class="item-0"><a href="link5.html">fifth item</a></li>
         </ul>
     </div>
 </div>
'''
doc = pq(html)
li = doc('li')
# print(li.html())
# print(li.text())
# print(type(li.text()))
'''**如果得到的结果是多个节点，并且想要获取每个节点的内部 HTML 文本，则需要遍历每个节点。而 text() 方法不需要遍历就可以获取，它将所有节点取文本之后合并成一个字符串'''

'''节点操作'''

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

doc = pq(html)
li = doc('.item-0.active')
# print(li)
# li.removeClass('active')
# print(li)
# li.addClass('active')
# print(li)

html = '''
<ul class="list">
     <li class="item-0 active"><a href="link3.html"><span class="bold">third item</span></a></li>
</ul>
'''
doc = pq(html)
li = doc('.item-0.active')
# print(li)
# li.attr('name', 'link')
# print(li)
# li.text('changed item')
# print(li)
# li.html('<span>changed item</span>')
# print(li)
'''如果 attr 方法只传入第一个参数的属性名，则是获取这个属性值；如果传入第二个参数，可以用来修改属性值。text 和 html 方法如果不传参数，则是获取节点内纯文本和 HTML 文本；如果传入参数，则进行赋值。'''


html = '''
<div class="wrap">
    Hello, World
    <p>This is a paragraph.</p>
 </div>
'''

doc = pq(html)
wrap = doc('.wrap')
# print(wrap.text())
#
# wrap.find('p').remove()
# print(wrap.text())

'''伪类选择器'''

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

doc = pq(html)
li = doc('li:first-child')
print(li)
li = doc('li:last-child')
print(li)
li = doc('li:nth-child(2)')
print(li)
li = doc('li:gt(2)')
print(li)
li = doc('li:nth-child(2n)')
print(li)
li = doc('li:contains(second)')
print(li)