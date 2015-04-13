#pragma once
#include <vector>

namespace tau {
namespace tac {

static const double MICRO_S = 0.01;

struct Boundary {
	double low, high;
};

struct C {
	double c1, c2, c3, c4, s, point;
};

enum FunctionType {
	LOW, HIGH, MICRO
};

struct Points {
	double a1, a2, a3, a4;
};

struct VFunction {
	Boundary boundary;
	Points points;
};

struct EndPoint {
	double val;
	FunctionType point_type;
	VFunction function;
};

struct PointData {
	std::vector<EndPoint> points;
	double sum;
};

} //namespace tac
} //namespace tau