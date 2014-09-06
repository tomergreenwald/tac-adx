__author__ = 'Tomer'

from bisect import bisect_left
from copy import copy

MICRO = 0.01


def binary_search(a, x, lo=0, hi=None):  # can't use a to specify default for hi
    hi = hi if hi is not None else len(a)  # hi defaults to len(a)
    pos = bisect_left(a, x, lo, hi)  # find insertion position
    return pos if pos != hi and a[pos] == x else -1  # don't walk off the end


class Boundary(object):
    def __init__(self, low, high):
        self.low = low
        self.high = high

    def __repr__(self):
        return "[low {low}, high {high}]".format(low=self.low, high=self.high)


class Points(object):
    def __init__(self, a1, a2, a3, a4):
        self.a1 = a1
        self.a2 = a2
        self.a3 = a3
        self.a4 = a4

    def __repr__(self):
        return "[{a1}, {a2}, {a3}, {a4},]".format(a1=self.a1, a2=self.a2, a3=self.a3, a4=self.a4, )


class VFunction(object):
    def __init__(self, boundary, points):
        self.boundary = boundary
        self.points = points

    def calc(self, reserve):
        if reserve <= self.boundary.low:
            return - self.points.a1
        if self.boundary.low < reserve <= self.boundary.high:
            return - self.points.a2 * reserve
        if self.boundary.high < reserve < (1 + MICRO) * self.boundary.high:
            return self.points.a3 * reserve - self.points.a4
        return 0

    def __repr__(self):
        return "[{boundary}, {points}]".format(boundary=self.boundary, points=self.points)


def f(reserve, functions):
    function_sum = 0
    for function in functions:
        function_sum += function.calc(reserve)
    return function_sum


def minimize_f(functions):
    best_reserve = None
    best_score = 1000000
    for function in functions:
        reserve = function.boundary.high
        score = f(reserve, functions)
        print reserve, score
        if score < best_score:
            best_score = score
            best_reserve = reserve
    return best_reserve


def get_sorted_boundary_points(functions):
    points = []
    for function in functions:
        points.append(function.boundary.high)
        points.append(function.boundary.low)
        points.append(function.boundary.high * (1 + MICRO))
    points.sort()
    return points


class C(object):
    def __init__(self, c1, c2, c3, c4):
        self.c1 = c1
        self.c2 = c2
        self.c3 = c3
        self.c4 = c4

    def __repr__(self):
        return "[{c1}, {c2}, {c3}, {c4}]".format(c1=self.c1, c2=self.c2, c3=self.c3, c4=self.c4)


def minimize_f_fast(functions):
    points = get_sorted_boundary_points(functions)
    low_functions = sorted(functions, key=lambda x: x.boundary.low)
    high_functions = sorted(functions, key=lambda x: x.boundary.high)

    low_index = 0
    high_index = 0
    micro_index = 0

    c = {}
    for j in xrange(len(points)):
        if j is 0:
            c[j] = C(-sum(function.points.a1 for function in functions), 0, 0, 0)
        else:
            c[j] = copy(c[j - 1])
            while low_functions[low_index].boundary.low < points[j - 1]:
                if low_index + 1 < len(low_functions):
                    low_index += 1
                else:
                    break
            if low_functions[low_index].boundary.low == points[j - 1]:
                c[j].c1 = c[j].c1 + low_functions[low_index].points.a1
                c[j].c2 = c[j].c2 - low_functions[low_index].points.a2
                print points[j - 1], "low"
                continue

            while high_functions[high_index].boundary.high < points[j - 1]:
                if high_index + 1 < len(high_functions):
                    high_index += 1
                else:
                    break
            if high_functions[high_index].boundary.high == points[j - 1]:
                c[j].c2 = c[j].c1 + high_functions[high_index].points.a2
                c[j].c3 = c[j].c3 + high_functions[high_index].points.a3
                c[j].c4 = c[j].c1 - high_functions[high_index].points.a4
                print points[j - 1], "high"
                continue

            while high_functions[micro_index].boundary.high * (1 + MICRO) < points[j - 1]:
                if micro_index + 1 < len(high_functions):
                    micro_index += 1
                else:
                    break
            if high_functions[micro_index].boundary.high * (1 + MICRO) == points[j - 1]:
                c[j].c3 = c[j].c3 - high_functions[micro_index].points.a3
                c[j].c4 = c[j].c1 + high_functions[micro_index].points.a4
                print points[j - 1], "micro"
                continue
            raise Exception("Should not get here")

    print "---------"
    best_score = 100000
    best_reserve = 0
    for j in xrange(len(points)):
        s = c[j].c1 + c[j].c2 * points[j] + c[j].c3 * points[j] + c[j].c4
        print points[j], s
        if s < best_score:
            best_score = s
            best_reserve = points[j]
    return best_reserve