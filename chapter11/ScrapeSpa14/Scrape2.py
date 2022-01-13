import time
import requests
import pywasm
from wasmer import engine,Store,Module,Instance
from wasmer_compiler_cranelift import Compiler

store = Store(engine.JIT(Compiler))
module = Module(store,open('Wasm.wasm','rb').read())
instance = Instance(module)
BASE_URL = 'https://spa14.scrape.center'
TOTAL_PAGE = 10

runtime = pywasm.load('./Wasm.wasm')
for i in range(TOTAL_PAGE):
    offset = i*10
    sign = instance.exports.encrypt(offset,int(time.time()))
    url = f'{BASE_URL}/api/movie/?limit=10&offset={offset}&sign={sign}'
    response = requests.get(url)
    print(response.json())