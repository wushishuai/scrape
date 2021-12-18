import requests
from urllib.parse import quote

# 返回html
# url = 'http://localhost:8050/render.html?url=https://www.baidu.com'
# response = requests.get(url)
# print(response.text)
# 延时
# url = 'http://localhost:8050/render.html?url=https://www.baidu.com&wait=10'
# response = requests.get(url)
# print(response.text)

# 获取页面截图

# url = 'http://localhost:8050/render.png?url=https://www.jd.com&wait=5&width=1000&height=700'
# response = requests.get(url)
# with open('taobao.png', 'wb') as f:
#     f.write(response.content)

lua = '''
function main(splash,args)
    local treat = require("treat")
    local response = splash:http_get("https://www.baidu.com")
    return {html=treat.as_string(response.body),
    url=response.url,
    status=response.status
    }
end
'''

url = 'http://localhost:8050/execute?lua_source=' + quote(lua)
response = requests.get(url)
print(response.text)
