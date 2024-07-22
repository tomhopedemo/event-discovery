#!/bin/bash

chromium --virtual-time-budget=15000 --headless --user-agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36" --no-sandbox --disable-gpu --run-all-compositor-stages-before-draw --crash-dumps-dir=/tmp --dump-dom $1 > $2
