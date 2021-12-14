import pika
'''随取随用'''
QUEUE_NAME = 'scrape'
connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()
channel.queue_declare(queue=QUEUE_NAME)
while True:
    data = input()
    channel.basic_publish(exchange='',routing_key=QUEUE_NAME,body=data)
    print(f'put {data}')