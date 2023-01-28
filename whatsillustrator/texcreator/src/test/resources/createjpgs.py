import numpy
from PIL import Image, ImageDraw, ImageFont
import os

def get_number_image(nr, width, height):
    pil_image = Image.new('RGB', (width, height), color='yellow')
    d = ImageDraw.Draw(pil_image)
    myFont = ImageFont.truetype('FreeMono.ttf', 65)
    d.text((60, 30), '%02d' % nr, font=myFont, fill=(255, 0, 0))
    return pil_image

os.makedirs('output')
file_path = 'output/image%03d.jpg'
width = 200
height = 100

for i in range(100):
    im =  get_number_image(i, width, height)
    im.save(file_path % i)
