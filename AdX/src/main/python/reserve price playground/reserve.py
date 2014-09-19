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
        for reserve in (function.boundary.low, function.boundary.high, function.boundary.high * (1 + MICRO)):
            score = f(reserve, functions)
            print reserve, score
            if score < best_score:
                best_score = score
                best_reserve = reserve
    return best_reserve


LOW_P = 0
HIGH_P = 1
MICRO_P = 2


class EndPoint(object):
    def __init__(self, val, point_type, function):
        self.val = val
        self.point_type = point_type
        self.function = function

    def __repr__(self):
        return "[{val}, {type}]".format(val=self.val, type=self.point_type)


def get_sorted_boundary_points(functions):
    points = []
    for function in functions:
        points.append(EndPoint(function.boundary.high, HIGH_P, function))
        points.append(EndPoint(function.boundary.low, LOW_P, function))
        points.append(EndPoint(function.boundary.high * (1 + MICRO), MICRO_P, function))
    points = sorted(points, key=lambda o: o.val)
    return points


class C(object):
    def __init__(self, c1, c2, c3, c4, point):
        self.c1 = c1
        self.c2 = c2
        self.c3 = c3
        self.c4 = c4
        self.point = point
        self.s = self.c1 + self.c2 * self.point + self.c3 * self.point + self.c4

    def __repr__(self):
        return "[p={p}, s={s} : {c1}, {c2}, {c3}, {c4}]".format(c1=self.c1, c2=self.c2, c3=self.c3, c4=self.c4,
                                                                p=self.point, s=self.s)

    def update(self):
        self.s = self.c1 + self.c2 * self.point + self.c3 * self.point + self.c4


def minimize_f_fast(functions):
    points = get_sorted_boundary_points(functions)
    c = {}
    for j in xrange(len(points)):
        if j is 0:
            c[j] = C(-sum(function.points.a1 for function in functions), 0, 0, 0, points[j].val)
        else:
            c[j] = C(c[j - 1].c1, c[j - 1].c2, c[j - 1].c3, c[j - 1].c4, points[j].val)
            c[j].point = points[j].val
            last_point_type = points[j - 1].point_type
            last_function_points = points[j - 1].function.points
            if last_point_type is LOW_P:
                c[j].c1 = c[j].c1 + last_function_points.a1
                c[j].c2 = c[j].c2 - last_function_points.a2
            elif last_point_type is HIGH_P:
                c[j].c2 = c[j].c1 + last_function_points.a2
                c[j].c3 = c[j].c3 + last_function_points.a3
                c[j].c4 = c[j].c1 - last_function_points.a4
            elif last_point_type is MICRO_P:
                c[j].c3 = c[j].c3 - last_function_points.a3
                c[j].c4 = c[j].c1 + last_function_points.a4
            else:
                raise Exception("Should not get here")
            c[j].update()

    print "---------"
    best_score = 100000
    best_reserve = 0
    for j in xrange(len(points)):
        print c[j]
        if c[j].s < best_score:
            best_score = c[j].s
            best_reserve = points[j].val
    return best_reserve