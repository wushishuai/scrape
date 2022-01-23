import re

from lxml.html import HtmlElement, fromstring,etree

html = open('detail.html', encoding='utf-8').read()

element = fromstring(html=html)
METAS = [
    '//meta[starts-with(@property,"og:title")]/@content',
    '//meta[starts-with(@name,"og:title")]/@content',
    '//meta[starts-with(@property,"title")]/@content',
    '//meta[starts-with(@name,"title")]/@content',
    '//meta[starts-with(@property,"page:title")]/@content'
]
CONTENT_USELESS_TAGS = ['neta','style','script','link','video','audio','iframe','source','svg','path','symbol','img','footer','header']

CONTENT_STRIP_TAGS = ['span','blockquote']
CONTENT_NOISE_XPATHS = [
    '//div[contains(@class,"comment")]',
    '//div[contains(@class,"advertisement")]',
    '//div[contains(@class,"advert")]',
    '//div[contains(@style,"display: none")]',
]

def preprocess4content(element:HtmlElement):
    etree.strip_elements(element,*CONTENT_USELESS_TAGS)
    etree.strip_elements(element,*CONTENT_STRIP_TAGS)
    remove_children(element,CONTENT_NOISE_XPATHS)

    for child in children(element):

        if child.tag.lower() == 'p':
            etree.strip_tags(child,'span')
            etree.strip_tags(child,'string')

            if not (child.text and child.text.strip()):
                remove_element(child)

        if child.tag.lower() == 'div' and not child.getchildren():
            child.tag = 'p'

def remove_element(element:HtmlElement):
    parent = element.getparent()
    if parent is not None:
        parent.remove(element)
def remove_children(element:HtmlElement,xpaths=None):
    if not xpaths:
        return
    for xpath in xpaths:
        nodes = element.xpath(xpath)
        for node in nodes:
            remove_element(node)
    return element

def children(element:HtmlElement):
    yield element
    for child_element in element:
        if isinstance(child_element,HtmlElement):
            yield from children(child_element)
def extract_by_meta(element: HtmlElement) -> str:
    for xpath in METAS:
        title = element.xpath(xpath)
        if title:
            return ''.join(title)


class Element(HtmlElement):
    id: int = None
    tag_name: str = None
    number_of_char: int = None
    number_of_a_char: int = None
    number_of_descendants: int = None
    number_of_a_descendants: int = None
    number_of_p_descendants: int = None
    number_of_punctuation: int = None
    density_of_punctuation: int = None
    density_of_text: float = None
    density_score: float = None

def number_of_a_char(element:Element):
    if element is None:
        return 0
    text = ''.join(element.xpath('.//a//text('))
    text = re.sub(r'\s*','',text,flags=re.S)
    return len(text)

def number_of_p_descendants(element:Element):
    if element is None:
        return 0
    return len(element.xpath('.//p'))


PUNCTUATION = set('''! ，。？、；：“” ‘’《》% （）<> {} [] 【】 *~`,.?:;'"!%()''')
def number_of_punctuation(element:Element):
    if element is None:
        return 0
    text = ''.join(element.xpath('.//text()'))
    text = re.sub(r'\s*','',text,flags=re.S)
    punctuations = [c for c in text if c in PUNCTUATION]
    return len(punctuations)

def density_of_text(element:Element):
    if element.number_of_p_descendants - element.number_of_a_descendants == 0:
        return 0
    return (element.number_of_char - element.number_of_a_char) / (element.number_of_descendants - element.number_of_a_descendants)

def density_of_punctuation(element:Element):
    result = (element.number_of_char - element.number_of_a_char) / (element.number_of_punctuation + 1)
    return result or 1

def extract_by_title(element: HtmlElement):
    return ''.join(element.xpath('//title//text()')).strip()


def extract_by_h(element: HtmlElement):
    hs = element.xpath('//h1//text()|//h2//text()|//h3//text()')
    return hs or []

def similarity(s1,s2):
    if not s1 or s2:
        return 0
    s1_set = set(list(s1))
    s2_set = set(list(s2))
    intersection = s1_set.intersection(s2_set)
    union = s2_set.intersection(s2_set)
    return len(intersection) / len(union)


def extract_title(element:HtmlElement):
    title_extracted_by_meta = extract_by_meta(element)
    title_extracted_by_h = extract_by_h(element)
    title_extracted_by_title = extract_by_title(element)
    if title_extracted_by_meta:
        return title_extracted_by_meta

    title_extracted_by_h = sorted(title_extracted_by_h,key=lambda x:similarity(x,title_extracted_by_title),reverse=True)
    if title_extracted_by_h:
        return title_extracted_by_h[0]

    return title_extracted_by_title

