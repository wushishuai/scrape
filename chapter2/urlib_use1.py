import urllib.request
'''对urlopen基本使用'''
response = urllib.request.urlopen('https://660e.com/?url=https://v.qq.com/x/cover/mzc00200lxzhhqz/d0040q5zhb7.html')
print(response.read().decode('utf-8'))#查看网页源代码
# print(type(response))#查看响应类型
print(response.status)#查看响应码
print(response.getheaders())#查看响应头
print(response.getheader('Server'))#查看响应头指定内容
