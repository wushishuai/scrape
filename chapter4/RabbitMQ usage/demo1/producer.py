import pika
'''基本使用'''
QUEUE_NAME = 'scrape'
connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()
channel.queue_declare(queue=QUEUE_NAME)
channel.basic_publish(exchange='', routing_key=QUEUE_NAME, body='Hello World!')
