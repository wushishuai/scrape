import pika
'''队列持久化'''
MAX_PRIORITY = 100
QUEUE_NAME = 'scra'
connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()
channel.queue_declare(queue=QUEUE_NAME, arguments={'x-max-priority': MAX_PRIORITY},durable=True)
while True:
    data, priority = input().split()
    channel.basic_publish(exchange='', routing_key=QUEUE_NAME, properties=pika.BasicProperties(priority=int(priority),
                                                                                               delivery_mode=2),
                          body=data)
    print(f'put {data}')
