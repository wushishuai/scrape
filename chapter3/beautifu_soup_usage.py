from bs4 import BeautifulSoup
import re

soup = BeautifulSoup('<p>Hello</p>', 'lxml')
# print(soup.p.string)

html = """
<html><head><title>The Dormouse's story</title></head>
<body>
<p class="title" name="dromouse"><b>The Dormouse's story</b></p>
<p class="story">Once upon a time there were three little sisters; and their names were
<a href="http://example.com/elsie" class="sister" id="link1"><!-- Elsie --></a>,
<a href="http://example.com/lacie" class="sister" id="link2">Lacie</a> and
<a href="http://example.com/tillie" class="sister" id="link3">Tillie</a>;
and they lived at the bottom of a well.</p>
<p class="story">...</p>
"""

soup = BeautifulSoup(html, 'lxml')
# print(soup.prettify())#可以把要解析的字符串以标准的缩进格式输出,可以自动更正格式;这一步不是由 prettify() 方法做的，而是在初始化 BeautifulSoup 时就完成了。
# print(soup.title.string)

'''节点选择器
直接调用节点的名称就可以选择节点元素，再调用 string 属性就可以得到节点内的文本了，这种选择方式速度非常快。如果单个节点结构层次非常清晰，可以选用这种方式来解析'''

html = """
<html><head><title>The Dormouse's story</title></head>
<body>
<p class="title" name="dromouse"><b>The Dormouse's story</b></p>
<p class="story">Once upon a time there were three little sisters; and their names were
<a href="http://example.com/elsie" class="sister" id="link1"><!-- Elsie --></a>,
<a href="http://example.com/lacie" class="sister" id="link2">Lacie</a> and
<a href="http://example.com/tillie" class="sister" id="link3">Tillie</a>;
and they lived at the bottom of a well.</p>
<p class="story">...</p>
"""
soup = BeautifulSoup(html, 'lxml')
# print(soup.title)
# print(type(soup.title.string))
# print(soup.head)
# print(soup.p)#当有多个节点时，这种选择方式只会选择到第一个匹配的节点，其他的后面节点都会忽略。


'''提取信息'''

# print(soup.title.name)#获取名称
# print(soup.p.attrs)#获取属性
# print(soup.p.attrs['name'])
# print(soup.p['name'])
# print(soup.p['class'])
# print(soup.p.string)#获取内容
'''嵌套选择'''
html = """
<html><head><title>The Dormouse's story</title></head>
<body>
"""

soup = BeautifulSoup(html, 'lxml')
# print(soup.head.title)
# print(type(soup.head.titl))
# print(soup.head.title.string)
# print(soup.head.string)


'''关联选择'''

html = """
<html>
    <head>
        <title>The Dormouse's story</title>
    </head>
    <body>
        <p class="story">
            Once upon a time there were three little sisters; and their names were
            <a href="http://example.com/elsie" class="sister" id="link1">
                <span>Elsie</span>
            </a>
            <a href="http://example.com/lacie" class="sister" id="link2">Lacie</a> 
            and
            <a href="http://example.com/tillie" class="sister" id="link3">Tillie</a>
            and they lived at the bottom of a well.
        </p>
        <p class="story">...</p>
"""

soup = BeautifulSoup(html, 'lxml')
# print(soup.p.contents)#p的直接子节点

# print(soup.p.children)#生成器类型,直接子节点
# for i, child in enumerate(soup.p.children):
#     print(i,child)

# print(soup.p.descendants)#所有子孙节点
# for i,child in enumerate(soup.p.descendants):
#     print(i,child)


'''父节点和祖先节点'''


html = """
<html>
    <head>
        <title>The Dormouse's story</title>
    </head>
    <body>
        <p class="story">
            Once upon a time there were three little sisters; and their names were
            <a href="http://example.com/elsie" class="sister" id="link1">
                <span>Elsie</span>
            </a>
        </p>
        <p class="story">...</p>
"""

soup = BeautifulSoup(html, 'lxml')
# print(soup.a.parent)#直接父节点

html = """
<html>
    <body>
        <p class="story">
            <a href="http://example.com/elsie" class="sister" id="link1">
                <span>Elsie</span>
            </a>
        </p>
"""

soup = BeautifulSoup(html,'lxml')
# print(type(soup.a.parents))#生成器类型
# print(list(enumerate(soup.a.parents)))
'''兄弟节点'''
html = """
<html>
    <body>
        <p class="story">
            Once upon a time there were three little sisters; and their names were
            <a href="http://example.com/elsie" class="sister" id="link1">
                <span>Elsie</span>
            </a>
            Hello
            <a href="http://example.com/lacie" class="sister" id="link2">Lacie</a> 
            and
            <a href="http://example.com/tillie" class="sister" id="link3">Tillie</a>
            and they lived at the bottom of a well.
        </p>
"""

soup = BeautifulSoup(html, 'lxml')
# print('Next Sibling',soup.a.next_sibling)
# print('Prev Sibling',soup.a.previous_sibling)
# print('Next Siblings',list(enumerate(soup.a.next_siblings)))
# print('Prev Siblings',list(enumerate(soup.a.previous_siblings)))

'''提取信息'''


html = """
<html>
    <body>
        <p class="story">
            Once upon a time there were three little sisters; and their names were
            <a href="http://example.com/elsie" class="sister" id="link1">Bob</a><a href="http://example.com/lacie" class="sister" id="link2">Lacie</a> 
        </p>
"""
soup = BeautifulSoup(html,'lxml')
# print('Next Sibling:')
# print(type(soup.a.next_sibling))
# print(soup.a.next_sibling)
# print(soup.a.next_sibling.string)
# print('Parent:')
# print(type(soup.a.parents))
# print(list(soup.a.parents)[0])
# print(list(soup.a.parents)[0].attrs['class'])

'''方法选择器
find_all'''


html='''
<div class="panel">
    <div class="panel-heading">
        <h4>Hello</h4>
    </div>
    <div class="panel-body">
        <ul class="list" id="list-1">
            <li class="element">Foo</li>
            <li class="element">Bar</li>
            <li class="element">Jay</li>
        </ul>
        <ul class="list list-small" id="list-2">
            <li class="element">Foo</li>
            <li class="element">Bar</li>
        </ul>
    </div>
</div>
'''

soup = BeautifulSoup(html, 'lxml')
# print(soup.find_all(name='ul'))
# print(type(soup.find_all(name='ul')[0]))
# for ul in soup.find_all(name='ul'):
#     print(ul.find_all(name='li'))
#     for li in ul.find_all(name='li'):
#         print(li.string)

html='''
<div class="panel">
    <div class="panel-heading">
        <h4>Hello</h4>
    </div>
    <div class="panel-body">
        <ul class="list" id="list-1" name="elements">
            <li class="element">Foo</li>
            <li class="element">Bar</li>
            <li class="element">Jay</li>
        </ul>
        <ul class="list list-small" id="list-2">
            <li class="element">Foo</li>
            <li class="element">Bar</li>
        </ul>
    </div>
</div>
'''
soup = BeautifulSoup(html, 'lxml')
# print(soup.find_all(attrs={'id':'list-1'}))
# print(soup.find_all(attrs={'name':'elements'}))
#
# print(soup.find_all(id='list-1'))
# print(soup.find_all(class_='element'))

html='''
<div class="panel">
    <div class="panel-body">
        <a>Hello, this is a link</a>
        <a>Hello, this is a link, too</a>
    </div>
</div>
'''
soup = BeautifulSoup(html, 'lxml')
# print(soup.find_all(text=re.compile('link')))#text 参数可用来匹配节点的文本，传入的形式可以是字符串，可以是正则表达式对象

'''find:返回第一个匹配的元素'''

html='''
<div class="panel">
    <div class="panel-heading">
        <h4>Hello</h4>
    </div>
    <div class="panel-body">
        <ul class="list" id="list-1">
            <li class="element">Foo</li>
            <li class="element">Bar</li>
            <li class="element">Jay</li>
        </ul>
        <ul class="list list-small" id="list-2">
            <li class="element">Foo</li>
            <li class="element">Bar</li>
        </ul>
    </div>
</div>
'''

soup = BeautifulSoup(html,'lxml')

# print(soup.find(name='ul'))
# print(type(soup.find(name='ul')))
# print(soup.find(class_='list'))
'''find_parents 和 find_parent：前者返回所有祖先节点，后者返回直接父节点。

find_next_siblings 和 find_next_sibling：前者返回后面所有的兄弟节点，后者返回后面第一个兄弟节点。

find_previous_siblings 和 find_previous_sibling：前者返回前面所有的兄弟节点，后者返回前面第一个兄弟节点。

find_all_next 和 find_next：前者返回节点后所有符合条件的节点，后者返回第一个符合条件的节点。

find_all_previous 和 find_previous：前者返回节点前所有符合条件的节点，后者返回第一个符合条件的节点。'''

'''CSS 选择器'''

html='''
<div class="panel">
    <div class="panel-heading">
        <h4>Hello</h4>
    </div>
    <div class="panel-body">
        <ul class="list" id="list-1">
            <li class="element">Foo</li>
            <li class="element">Bar</li>
            <li class="element">Jay</li>
        </ul>
        <ul class="list list-small" id="list-2">
            <li class="element">Foo</li>
            <li class="element">Bar</li>
        </ul>
    </div>
</div>
'''

soup = BeautifulSoup(html, 'lxml')
# print(soup.select('.panel .panel-heading'))
# print(soup.select('ul li'))
# print(soup.select('#list-2 .element'))
# print(type(soup.select('ul')[0]))\

for ul in soup.select('ul'):
    print(ul['id'])
    print(ul.attrs['id'])

for li in soup.select('li'):
    print('Get text', li.get_text())
    print('string', li.string)