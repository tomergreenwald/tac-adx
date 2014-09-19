__author__ = 'Tomer'

MICRO = 0.01


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


class EndPoint(object):
    def __init__(self, val, point_type, function):
        self.val = val
        self.point_type = point_type
        self.function = function

    def __repr__(self):
        return "[{val}, {type}]".format(val=self.val, type=self.point_type)


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


class Points(object):
    def __init__(self, a1, a2, a3, a4):
        self.a1 = a1
        self.a2 = a2
        self.a3 = a3
        self.a4 = a4

    def __repr__(self):
        return "[{a1}, {a2}, {a3}, {a4},]".format(a1=self.a1, a2=self.a2, a3=self.a3, a4=self.a4, )


class Boundary(object):
    def __init__(self, low, high):
        self.low = low
        self.high = high

    def __repr__(self):
        return "[low {low}, high {high}]".format(low=self.low, high=self.high)