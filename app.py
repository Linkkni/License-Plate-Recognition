import re
from flask import Flask, request, jsonify
from sympy import true
from ultralytics import YOLO
import easyocr
import cv2
import numpy as np

app = Flask(__name__)

# Loading easyocr reader
reader = easyocr.Reader(['en'], gpu = False)

#Import enabling GPU is Nvidia and setup correctly
#reader = easyocr.Reader(['en'], gpu = True)


def preprocess_image(img):


    if len(img.shape) == 3:
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    else:
        gray = img

    noise_detect = cv2.bilateralFilter(gray, 9, 75, 75, cv2.BORDER_DEFAULT)


    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))

    clade_apply = clahe.apply(noise_detect)

    return clade_apply

def fix_common_ocr_errors(text):

    clean_text = text.replace(" ", "").replace(".", "").replace("-", "").upper()

    return clean_text.upper()



def is_valid_taiwan_plate(text):
    # Define regex patterns for Taiwan license plates

    "Check various Taiwan license plate formats"
    patterns = [
        r"^[A-Z]{2}[0-9]{4}$",  # Old plates & Electric (XX-1234)
        r"^[A-Z]{3}[0-9]{4}$",  # Popular plates (ABC-1234)
        r"^[0-9]{4}[A-Z]{2}$",  # Light Truck, Race Car, Motorcycle (1234-AB)
        r"^[A-Z]{2}[0-9]{3}$",  # Commercial Vehicle 3-digit (XX-123)
        r"^[A-Z]{3}[0-9]{3}$",  # Commercial Vehicle 3-digit (XXX-123)
    ]

    for pat in patterns:
        if re.match(pat, text):
            print(f"FOUND VALID TAIWAN PLATE: '{text}'")
            return True
    return False
@app.route('/detect', methods=['POST'])
def detect_plate():
    try:
        # Load image from request
        file = request.files['image']

        # Convert image to opencv format
        npimg = np.frombuffer((file.read()), np.uint8)
        img = cv2.imdecode(npimg, cv2.IMREAD_COLOR)

        #img preprocessing
        processed_img = preprocess_image(img)



        # Use easyOCR to detect text
        result = reader.readtext(img, detail=0, allowlist='0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ')

        finalPlate = None
        max_len = 0

        print("Raw OCR", result)
        print(f"Raw OCR result: {result}")  # DEBUG: See what OCR found

        place_text = ""
        for text in result:
            clean_text = fix_common_ocr_errors(text)
            print(f"Checking: '{clean_text}'")  # DEBUG: See each cleaned text

            if len(clean_text) < 4 or len(clean_text) > 8:
                continue

            if clean_text.isalpha():
                print(f"Ignore Text(Only Letters): '{clean_text}'")
                continue

            if clean_text.isdigit():
                print(f"Ignore Text(Only Digits): '{clean_text}'")
                continue

            if is_valid_taiwan_plate(clean_text):
                print(f"FOUND VALID TAIWAN PLATE: '{clean_text}'")

                if len(clean_text) > max_len:
                    max_len = len(clean_text)
                    finalPlate = clean_text

            # if len(clean_text) > 3 and re.search(pattern, clean_text):
            #     if len(clean_text) > max_len:
            #         max_len = len(clean_text)
            #         finalPlate = clean_text

            # for pat in pattern:
            #     if re.match(pat, clean_text):
            #         print(f"FOUND VALID TAIWAN PLATE: '{clean_text}'")
            #         return jsonify({"status": "success", "plate": clean_text})

        if finalPlate:
            print(f"Final Plate: '{finalPlate}'")
            return jsonify({"status": "success", "plate": finalPlate})
        else:
            return jsonify({"status": "error", "message": "No Plate Detected", "debug": result})

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)})




if __name__ == '__main__':
    # Run server at 9091 port
    app.run(host='0.0.0.0', port=9091, debug=True)

# To run the app, use the command: python app.py