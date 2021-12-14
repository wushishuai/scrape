import pika
'''优先级队列'''
MAX_PRIORITY = 100
QUEUE_NAME = 'scrap'
connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()
channel.queue_declare(queue=QUEUE_NAME, arguments={'x-max-priority': MAX_PRIORITY})
while True:
    data, priority = input().split()
    channel.basic_publish(exchange='', routing_key=QUEUE_NAME, properties=pika.BasicProperties(priority=int(priority)),
                          body=data)
    print(f'put {data}')
