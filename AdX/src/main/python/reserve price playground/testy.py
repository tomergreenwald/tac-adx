__author__ = 'Tomer'

from reserve import *
from random import randint
from datetime import datetime

func1 = VFunction(Boundary(1, 2), Points(1, 2, 3, 4))
func2 = VFunction(Boundary(2, 3), Points(11, 12, 13, 14))
func3 = VFunction(Boundary(44, 69), Points(33, 0.75, 75, 5226.75))
func4 = VFunction(Boundary(7, 45), Points(0.7, 0.1, 10, 454.5))
func5 = VFunction(Boundary(17, 18), Points(2.38, 0.14, 14, 254.52))
functions = []
print datetime.now()
for i in xrange(1):
    # if i % 100000 == 0:
    # print i
    low = randint(1, 80)
    high = randint(low + 1, 100)
    a3 = randint(1, 100)
    a2 = MICRO * a3
    a1 = MICRO * a3 * low
    a4 = a3 * (1 + MICRO) * high
    functions.append(VFunction(Boundary(low, high), Points(a1, a2, a3, a4)))
print functions
functions = [func3, func4]
print functions
# print datetime.now()
print "--------------------------"
best_reserve = minimize_f(functions)
print "[best reserve = {reserve}, score = {score}]".format(reserve=best_reserve, score=f(best_reserve, functions))
# print datetime.now()
print "--------------------------"
best_reserve = minimize_f_fast(functions)
print "[best reserve = {reserve}, score = {score}]".format(reserve=best_reserve, score=f(best_reserve, functions))
# print datetime.now()