# -*- encoding=utf8 -*-
__author__ = "wss"

from airtest.core.api import *

auto_setup(__file__)


from poco.drivers.android.uiautomation import AndroidUiautomationPoco
poco = AndroidUiautomationPoco(use_airtest_input=True, screenshot_each_action=False)
poco("App5").click()
poco("android.support.v7.widget.RecyclerView").wait_for_appearance(10)
poco(zOrders="{'global': 0, 'local': 9}")