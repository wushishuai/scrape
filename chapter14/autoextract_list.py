from gerapy_auto_extractor import extract_list
from gerapy_auto_extractor.helpers import content,jsonify

html = content('list.html')
print(jsonify(extract_list(html)))