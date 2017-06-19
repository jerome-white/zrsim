import csv
import sys
import collections

c = collections.Counter()
reader = csv.reader(sys.stdin)
for row in reader:
    c[len(row[1])] += 1

for i in c.items():
    print(*i, sep=': ')
