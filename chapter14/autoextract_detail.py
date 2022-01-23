from gerapy_auto_extractor import extract_detail
from gerapy_auto_extractor.helpers import content,jsonify

html = content('detail.html')
print(jsonify(extract_detail(html)))