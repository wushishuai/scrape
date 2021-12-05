
'''urlparse实现url的识别和分段'''
from urllib.parse import urlparse

result = urlparse('https://www.baidu.com/index.html;user?id=5#comment')
print(type(result))
print(result)
'''urlunparse构造URL,接受可迭代对象，长度必须为6'''

from urllib.parse import  urlunparse

data = ['https','www.baidu.com','index.html','user','a=6','comment']
print(urlunparse(data))
'''urlsplit与urlparse类似，但单独解析params部分'''
from urllib.parse import urlsplit

result = urlsplit('https://www.baidu.com/index.html;user?id=5#comment')
print(result)

''''urlunsplit与urlunparse类似，接受可迭代对象，长度必须为5'''
from urllib.parse import  urlunsplit

data = ['https','www.baidu.com','index.html','a=6','comment']
print(urlunsplit(data))
'''urljoin 提供一个 base_url（基础链接）作为第一个参数，将新的链接作为第二个参数，该方法会分析 base_url 的 scheme、netloc 和 path 这 3 个内容并对新链接缺失的部分进行补充，最后返回结果'''
from urllib.parse import urljoin

print(urljoin('http://www.baidu.com', 'FAQ.html'))
print(urljoin('http://www.baidu.com', 'https://cuiqingcai.com/FAQ.html'))
print(urljoin('http://www.baidu.com/about.html', 'https://cuiqingcai.com/FAQ.html'))
print(urljoin('http://www.baidu.com/about.html', 'https://cuiqingcai.com/FAQ.html?question=2'))
print(urljoin('http://www.baidu.com?wd=abc', 'https://cuiqingcai.com/index.php'))
print(urljoin('http://www.baidu.com', '?category=2#comment'))
print(urljoin('www.baidu.com', '?category=2#comment'))
print(urljoin('www.baidu.com#comment', '?category=2'))#base_url 提供了三项内容 scheme、netloc 和 path。如果这 3 项在新的链接里不存在，就予以补充；如果新的链接存在，就使用新的链接的部分。而 base_url 中的 params、query 和 fragment 是不起作用的
'''urlencode 将字典化为 GET 请求参数'''
from urllib.parse import urlencode

params = {
    'name': 'germey',
    'age': 22
}
base_url = 'http://www.baidu.com?'
url = base_url + urlencode(params)
print(url)

'''parse_qs与urlencode功能相反，GET 请求参数--》字典'''

from urllib.parse import parse_qs

query = 'name=germey&amp;age=22'
print(parse_qs(query))

'''parse_qsl 与urlencode功能相反，GET 请求参数--》元组组成的列表'''

from urllib.parse import parse_qsl

query = 'name=germey&amp;age=22'
print(parse_qsl(query))

'''quote 将内容转化为 URL 编码的格式。URL 中带有中文参数时，有时可能会导致乱码的问题，此时用这个方法可以将中文字符转化为 URL 编码'''

from urllib.parse import quote

keyword = '壁纸'
url = 'https://www.baidu.com/s?wd=' + quote(keyword)
print(url)

'''unquote与quote功能相反，进行 URL 解码'''
from urllib.parse import unquote

url = 'https://www.baidu.com/s?wd=%E5%A3%81%E7%BA%B8'
print(unquote(url))