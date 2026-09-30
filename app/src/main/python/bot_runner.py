import sys
import os
import io
import traceback
from datetime import datetime

def log(logfile, msg):
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    line = f"[{ts}] {msg}\n"
    print(line, end="")
    try:
        with open(logfile, "a", encoding="utf-8") as f:
            f.write(line)
    except:
        pass

def run_bot(bot_path, logfile):
    log(logfile, "=" * 50)
    log(logfile, f"🚀 Starting bot: {bot_path}")
    log(logfile, "=" * 50)

    try:
        # Read bot file
        with open(bot_path, "r", encoding="utf-8") as f:
            code = f.read()

        # Redirect stdout to log
        old_stdout = sys.stdout
        sys.stdout = Tee(old_stdout, logfile)

        # Set working dir
        os.chdir(os.path.dirname(bot_path))

        # Auto-install requirements if exists
        req_file = os.path.join(os.path.dirname(bot_path), "requirements.txt")
        if os.path.exists(req_file):
            log(logfile, "📦 requirements.txt mila, install kar raha hoon...")
            os.system(f"pip install -r {req_file}")

        # Execute bot
        namespace = {"__name__": "__main__", "__file__": bot_path}
        exec(compile(code, bot_path, "exec"), namespace)

    except KeyboardInterrupt:
        log(logfile, "🛑 Bot stopped by user")
    except Exception as e:
        log(logfile, f"❌ Error: {e}")
        log(logfile, traceback.format_exc())
    finally:
        sys.stdout = old_stdout
        log(logfile, "🔴 Bot stopped")

class Tee:
    def __init__(self, *streams):
        self.streams = streams
    def write(self, data):
        for s in self.streams:
            try: s.write(data)
            except: pass
        # Write to log file
        try:
            with open(self.streams[1] if len(self.streams) > 1 else "log.txt", "a") as f:
                f.write(data)
        except: pass
    def flush(self):
        for s in self.streams:
            try: s.flush()
            except: pass
