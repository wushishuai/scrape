import json

'''读取json'''
str = '''
[{
    "name": "Bob",
    "gender": "male",
    "birthday": "1992-10-18"
}, {
    "name": "Selina",
    "gender": "female",
    "birthday": "1995-10-18"
}]
'''

print(type(str))
data = json.loads(str)
print(data)
print(type(data))
print(data[0]['name'])
print(data[0].get('name'))

with open('data.json',encoding='utf-8') as file:
    str = file.read()
    data = json.loads(str)
    print(data)
data = json.load(open('data.json',encoding='utf-8'))
print(data)

data = [{
    'name': '王伟',
    'gender': '男',
    'birthday': '1992-10-18'
}]
with open('test.json','w',encoding='utf-8') as file:
    file.write(json.dumps(data,indent=2,ensure_ascii=False))
json.dump(data,open('test2.json','w',encoding='utf-8'),indent=2,ensure_ascii=False)