import pika
import requests
import pickle


TOTAL = 100
QUEUE_NAME = 'scrape_queue'
connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()
session = requests.session()

def scrape(request):
    try:
        response = session.send(request.prepare())
        print(f'success scraped {response.url}')
    except requests.RequestException:
        print(f'error occurred when scraping {request.url}')
while True:
    method_frame,header,body = channel.basic_get(queue=QUEUE_NAME,auto_ack=True)
    if body:
        request = pickle.loads(body)
        print(f'Get {request}')
        scrape(request)