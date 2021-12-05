from urllib.robotparser import RobotFileParser
rp = RobotFileParser()
rp.set_url('http://www.baidu.com/robots.txt')#set_url ：用来设置 robots.txt 文件的链接。如果在创建 RobotFileParser 对象时传入了链接，那么就不需要再使用这个方法设置了。
rp.read()#read：读取 robots.txt 文件并进行分析。注意，这个方法执行一个读取和分析操作，如果不调用这个方法，接下来的判断都会为 False，所以一定记得调用这个方法。这个方法不会返回任何内容，但是执行了读取操作。
print(rp.can_fetch('Baiduspider', 'http://www.baidu.com'))#该方法传入两个参数，第一个是 User-agent，第二个是要抓取的 URL。返回的内容是该搜索引擎是否可以抓取这个 URL，返回结果是 True 或 False。
print(rp.can_fetch('Baiduspider', 'http://www.baidu.com/homepage/'))

print(rp.can_fetch('Googlebot', "http://www.baidu.com/homepage/"))

from urllib.robotparser import RobotFileParser
from urllib.request import urlopen
rp = RobotFileParser()
rp.parse(urlopen('http://www.baidu.com/robots.txt').read().decode('utf-8').split('\n'))#用来解析 robots.txt 文件，传入的参数是 robots.txt 某些行的内容，它会按照 robots.txt 的语法规则来分析这些内容。


print(rp.can_fetch('Baiduspider', 'http://www.baidu.com'))
print(rp.can_fetch('Baiduspider', 'http://www.baidu.com/homepage/'))

print(rp.can_fetch('Googlebot', "http://www.baidu.com/homepage/"))
'''push test ----------'''