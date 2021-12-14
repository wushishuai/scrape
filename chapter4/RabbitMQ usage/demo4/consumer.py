import pika
QUEUE_NAME = 'scra'
connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()

while True:
    input()
    method_frame,header,body = channel.basic_get(queue=QUEUE_NAME,auto_ack=True)
    if body:
        print(f'Get {body}')