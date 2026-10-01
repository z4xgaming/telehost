import subprocess
import sys

LIBS = [
    "python-telegram-bot==20.7",
    "pyrogram==2.0.106",
    "telethon==1.34.0",
    "aiogram==3.4.1",
    "requests==2.31.0",
    "httpx==0.26.0",
    "urllib3==2.1.0",
    "PyJWT==2.8.0",
    "python-jose==3.3.0",
    "passlib==1.7.4",
    "python-dotenv==1.0.0",
    "pytz==2023.3",
    "colorama==0.4.6",
    "loguru==0.7.2",
    "redis==5.0.1",
    "sqlalchemy==2.0.25",
    "websockets==12.0",
]

def install_all():
    success = 0
    fail = 0
    for lib in LIBS:
        try:
            subprocess.check_call([sys.executable, "-m", "pip", "install", lib],
                                  stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            success += 1
        except:
            fail += 1
    return f"✅ Installed: {success}, ❌ Failed: {fail}"
