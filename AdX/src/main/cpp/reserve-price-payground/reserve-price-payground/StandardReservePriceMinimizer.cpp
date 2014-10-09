#include "StandardReservePriceMinimizer.h"
#include <algorithm>
#include "assert.h"

namespace tau {
namespace tac {

double StandardReservePriceMinimizer::minimize_f(VFunction* functions, uint64_t length) {
	double			best_reserve;
	double			best_score = 1000000;
	double			current_score;
	double			current_reserve;
	PointData		pointData;
	std::clock_t	start;

	MEASURE_TIME("Generating boundary points", pointData = get_boundary_points(functions, length));
	for (size_t i = 0; i < pointData.points.size(); i++) {
		current_reserve = pointData.points[i].val;
		current_score = f(current_reserve, functions, length);
		if (current_score < best_score) {
			best_score = current_score;
			best_reserve = current_reserve;
		}
	}

	return best_reserve;
}

double StandardReservePriceMinimizer::f(double reserve, VFunction* functions, uint64_t length) {
	double sum = 0;
	for (size_t i = 0; i < length; i++)	{
		sum += calc(functions[i], reserve);
	}
	return sum;
}

double StandardReservePriceMinimizer::calc(VFunction& function, double reserve) {
	if (reserve <= function.boundary.low) {
		return -function.points.a1;
	}
	if (function.boundary.low < reserve && reserve <= function.boundary.high) {
		return -function.points.a2 * reserve;
	}
	if (function.boundary.high < reserve && reserve < (1 + MICRO_S) * function.boundary.high) {
		return function.points.a3 * reserve - function.points.a4;
	}
	return 0;
}

}
}