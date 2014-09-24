__author__ = 'Tomer'

from random import randint
from datetime import datetime

from reserve import *
from classes import *

functions = []

for i in xrange(10000):
    low = randint(1, 80)
    high = randint(low + 1, 100)
    a3 = randint(1, 100)
    a2 = MICRO * a3
    a1 = MICRO * a3 * low
    a4 = a3 * (1 + MICRO) * high
    functions.append(VFunction(Boundary(low, high), Points(a1, a2, a3, a4)))
print len(functions), "functions"
print "--------------------------"
pre = datetime.now()
best_reserve = minimize_f(functions)
post = datetime.now()
print "timer", (post - pre)
print "[best reserve = {reserve}, score = {score}]".format(reserve=best_reserve, score=f(best_reserve, functions))

print "--------------------------"
pre = datetime.now()
best_reserve = minimize_f_fast(functions)
post = datetime.now()
print "timer", (post - pre)
print "[best reserve = {reserve}, score = {score}]".format(reserve=best_reserve, score=f(best_reserve, functions))
