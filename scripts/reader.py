import sys
import itertools

for i in itertools.count():
    c = sys.stdin.read(1)
    if not c:
        break
    print(i, c)
