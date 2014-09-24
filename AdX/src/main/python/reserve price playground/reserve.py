from classes import C, EndPoint, MICRO

__author__ = 'Tomer'

from bisect import bisect_left

LOW_P = 0
HIGH_P = 1
MICRO_P = 2


def binary_search(a, x, lo=0, hi=None):  # can't use a to specify default for hi
    hi = hi if hi is not None else len(a)  # hi defaults to len(a)
    pos = bisect_left(a, x, lo, hi)  # find insertion position
    return pos if pos != hi and a[pos] == x else -1  # don't walk off the end


def f(reserve, functions):
    function_sum = 0
    for function in functions:
        function_sum += function.calc(reserve)
    return function_sum


def minimize_f(functions):
    best_reserve = None
    best_score = 1000000
    for i in xrange(len(functions)):
        if i % 100 == 0:
            print i, len(functions)
        function = functions[i]
        for reserve in (function.boundary.low, function.boundary.high, function.boundary.high * (1 + MICRO)):
            score = f(reserve, functions)
            if score < best_score:
                best_score = score
                best_reserve = reserve
    return best_reserve


def get_sorted_boundary_points(functions):
    points = []
    for function in functions:
        points.append(EndPoint(function.boundary.high, HIGH_P, function))
        points.append(EndPoint(function.boundary.low, LOW_P, function))
        points.append(EndPoint(function.boundary.high * (1 + MICRO), MICRO_P, function))
    points = sorted(points, key=lambda o: o.val)
    return points


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
                c[j].c2 = c[j].c2 + last_function_points.a2
                c[j].c3 = c[j].c3 + last_function_points.a3
                c[j].c4 = c[j].c4 - last_function_points.a4
            elif last_point_type is MICRO_P:
                c[j].c3 = c[j].c3 - last_function_points.a3
                c[j].c4 = c[j].c4 + last_function_points.a4
            else:
                raise Exception("Should not get here")
            c[j].update()

    best_score = 100000
    best_reserve = 0
    for j in xrange(len(points)):
        if c[j].s < best_score:
            best_score = c[j].s
            best_reserve = points[j].val
    return best_reserve