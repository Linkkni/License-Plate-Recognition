import re
from flask import Flask, request, jsonify
from ultralytics import YOLO
import easyocr
import cv2
import numpy as np

app = Flask(__name__)

# Loading easyocr reader
reader = easyocr.Reader(['en'])


@app.route('/detect', methods=['POST'])
def detect_plate():
    try:
        # Load image from request
        file = request.files['image']

        # Convert image to opencv format
        npimg = np.frombuffer((file.read()), np.uint8)
        img = cv2.imdecode(npimg, cv2.IMREAD_COLOR)

        # Use easyOCR to detect text
        result = reader.readtext(img, detail=0, allowlist='0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ')

        pattern = r"([0-9]{2}[A-Z][0-9A-Z]{4,6})|([A-Z]{2}[0-9]{5,6})"

        print(f"Raw OCR result: {result}")  # DEBUG: See what OCR found

        place_text = ""
        for text in result:
            clean_text = text.replace(" ", "").replace(".", "").replace("-", "").upper()
            print(f"Checking: '{clean_text}'")  # DEBUG: See each cleaned text

            match = re.search(pattern, clean_text)
            print(f"Pattern match: {match}")  # DEBUG: See if pattern matches

            if match:
                place_text = clean_text
                print(f"FOUND PLATE: {place_text}")  # DEBUG
                break

        if place_text:
            return jsonify({"status": "success", "plate": place_text})
        else:
            return jsonify({"status": "error", "message": "No Plate Detected", "debug": result})

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)})


if __name__ == '__main__':
    # Run server at 9091 port
    app.run(host='0.0.0.0', port=9091, debug=True)

# To run the app, use the command: python app.py