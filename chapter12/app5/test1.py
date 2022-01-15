from airtest.core.android import Android
from airtest.core.api import *
import logging

logging.getLogger("airtest").setLevel(logging.WARNING)
device: Android = init_device('Android')
is_locked = device.is_locked()
print(f'is_locked:{is_locked}')
if is_locked:
    device.unlock()
device.wake()
app_list = device.list_app()
print(f'app list {app_list}')

uuid = device.uuid
print(f'uuid {uuid}')

display_info = device.get_display_info()
print(f'display info {display_info}')

resolution = device.get_render_resolution()
print(f'resolution {resolution}')

ip_address = device.get_ip_address()
print(f'ip address {ip_address}')

top_activity = device.get_top_activity()
print(f'top activity {top_activity}')

is_keyboard = device.is_keyboard_shown()
print(f'is keyboard show {is_keyboard}')

print(G.DEVICE_LIST)

uri = 'Android://127.0.0.1:5037/l7wgwg9puo8xeyzt'
device: Android = connect_device(uri)
print(G.DEVICE_LIST)


connect_device(uri)

result = shell('cat /proc/meminfo')
print(result)

packagename = 'com.tencent.mm'

start_app(packagename)
sleep(10)
stop_app(packagename)
snapshot('weixin.png', quality=30)

home()
pinch(in_or_out='out', center=(300, 300), percent=0.4)
