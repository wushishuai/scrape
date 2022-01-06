from flask import Flask, request, jsonify
from loguru import logger

app = Flask(__name__)


@app.route('/sms', methods=['POST'])
def receive():
    sms_content = request.form.get('content')
    logger.debug(f'received {sms_content}')

    return jsonify(status='success')


if __name__ == '__main__':
    app.run(debug=True)
