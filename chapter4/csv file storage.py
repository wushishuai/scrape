import csv
import pandas as pd
with open('data.csv','w') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['id','name','age'])
    writer.writerow(['10001','Mike',20])
    writer.writerow(['10002','Bob',22])
    writer.writerow(['10003','Jordan',21])
with open('data.csv','w') as csvfile:
    writer = csv.writer(csvfile,delimiter=' ')
    writer.writerow(['id','name','age'])
    writer.writerow(['10001','Mike',20])
    writer.writerow(['10002','Bob',22])
    writer.writerow(['10003','Jordan',21])
with open('data.csv','w') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['id', 'name', 'age'])
    writer.writerows([['10001','Mike',20],['10002','Bob',22],['10003','Jordan',21]])
with open('data.csv','w') as csvfile:
    filenames = ['id','name','age']
    writer = csv.DictWriter(csvfile,fieldnames=filenames)
    writer.writeheader()
    writer.writerow({'id':'10001','name':'Mike','age':20})
    writer.writerow({'id':'10002','name':'BOb','age':22})
    writer.writerow({'id':'10003','name':'Jordan','age':21})
with open('data.csv','a') as csvfile:
    filenames = ['id', 'name', 'age']
    writer = csv.DictWriter(csvfile, fieldnames=filenames)
    writer.writerow({'id': '10004', 'name': 'Durant', 'age': 22})

with open('data.csv','a',encoding='utf-8') as csvfile:
    filenames = ['id', 'name', 'age']
    writer = csv.DictWriter(csvfile, fieldnames=filenames)
    writer.writerow({'id': '10004', 'name': '王伟', 'age': 22})
data = [
    {'id':'10001','name':'Mike','age':20},
    {'id':'10002','name':'Bob','age':22},
    {'id':'10003','name':'Jordan','age':21},
]
df = pd.DataFrame(data)
df.to_csv('data2.csv',index=False)

with open('data.csv','r',encoding='utf-8') as csvfile:
    reader = csv.reader(csvfile)
    for row in reader:
        print(row)
df = pd.read_csv('data2.csv')
print(df)
data = df.values.tolist()
print(data)
for index,row in df.iterrows():
    print(row.tolist())