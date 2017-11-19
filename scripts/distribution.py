#
# Produce term length distribution
#

import csv
import sys
import collections

c = collections.Counter()
reader = csv.reader(sys.stdin)
for row in reader:
    assert(len(row) == 3) # ensure the row was written correctly
    c[len(row[1])] += 1

for i in c.items():
    print(*i, sep=': ')
