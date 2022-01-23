from gerapy_auto_extractor import is_list, is_detail, probability_of_detail, probability_of_list
from gerapy_auto_extractor.helpers import content, jsonify

html = content('detail.html')
print(probability_of_detail(html), probability_of_list(html))
print(is_detail(html), is_list(html))

html = content('list.html')
print(probability_of_detail(html), probability_of_list(html))
print(is_detail(html), is_list(html))
