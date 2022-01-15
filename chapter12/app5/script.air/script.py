# -*- encoding=utf8 -*-
__author__ = "wss"

from airtest.core.api import *

auto_setup(__file__)
# keyevent("HOME")

touch(Template(r"tpl1642244203477.png", record_pos=(-0.191, -0.6), resolution=(1080, 2400)))
wait(Template(r"tpl1642244226073.png", record_pos=(-0.215, -0.762), resolution=(1080, 2400)))
swipe(Template(r"tpl1642244306655.png", record_pos=(0.019, 0.499), resolution=(1080, 2400)), vector=[0.4913, -0.4564])
keyevent("HOME")

