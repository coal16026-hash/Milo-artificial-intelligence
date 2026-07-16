import requests

url = "https://apihub.agnes-ai.com/v1/images/generations"
headers = {
    "Authorization": "Bearer skyATs9uzPnSZAPgSGHLkRNjQy1sCHxi96rSGi7NvizZ52Iuf1",
    "Content-Type": "application/json"
}
data = {
    "model": "agnes-image-2.1-flash",
    "prompt": "cinematic portrait of a dog",
    "size": "1024x768",
    "extra_body": {
        "response_format": "url"
    }
}

try:
    resp = requests.post(url, headers=headers, json=data)
    print("Status:", resp.status_code)
    print("Body:", resp.text)
except Exception as e:
    print("Error:", str(e))
