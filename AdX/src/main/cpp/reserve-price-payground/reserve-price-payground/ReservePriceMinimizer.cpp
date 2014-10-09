#include "ReservePriceMinimizer.h"
#include "Defs.h"
#include <algorithm>
#include "assert.h"

namespace tau {
namespace tac {

PointData ReservePriceMinimizer::get_boundary_points(VFunction* functions, uint64_t length) {
	PointData	res;

	res.sum = 0;
	res.points.resize(length * 3);

	for (int j = 0; j < length; j++) {
		res.sum += functions[j].points.a1;

		res.points[j * 3].val = functions[j].boundary.low;
		res.points[j * 3].point_type = FunctionType::LOW;
		res.points[j * 3].function = functions[j];

		res.points[j * 3 + 1].val = functions[j].boundary.high;
		res.points[j * 3 + 1].point_type = FunctionType::HIGH;
		res.points[j * 3 + 1].function = functions[j];

		res.points[j * 3 + 2].val = functions[j].boundary.high * (1 + MICRO_S);
		res.points[j * 3 + 2].point_type = FunctionType::MICRO;
		res.points[j * 3 + 2].function = functions[j];
	}
	return res;
}

bool end_point_sorter(EndPoint const& lhs, EndPoint const& rhs) {
	return lhs.val < rhs.val;
}

void ReservePriceMinimizer::sort_end_points(std::vector<EndPoint> &points) {
	std::sort(points.begin(), points.end(), end_point_sorter);
}

double ReservePriceMinimizer::calculate_stuff(PointData pointData, uint64_t length) {
	C				currentC;
	C				previousC;
	FunctionType	prev_point_type;
	Points			prev_functions_points;
	double			best_score = 100000;
	double			best_reserve = 0;

	for (int j = 0; j < length * 3; j++) {
		if (j == 0) {
			previousC.c1 = -pointData.sum;
			previousC.c2 = 0;
			previousC.c3 = 0;
			previousC.c4 = 0;
			previousC.point = pointData.points[j].val;
		}
		else {
			memcpy(&currentC, &previousC, sizeof(currentC));
			currentC.point = pointData.points[j].val;

			prev_point_type = pointData.points[j - 1].point_type;
			prev_functions_points = pointData.points[j - 1].function.points;

			switch (prev_point_type) {
			case FunctionType::LOW:
				currentC.c1 += prev_functions_points.a1;
				currentC.c2 -= prev_functions_points.a2;
				break;
			case FunctionType::HIGH:
				currentC.c2 += prev_functions_points.a2;
				currentC.c3 += prev_functions_points.a3;
				currentC.c4 -= prev_functions_points.a4;
				break;
			case FunctionType::MICRO:
				currentC.c3 -= prev_functions_points.a3;
				currentC.c4 += prev_functions_points.a4;
				break;
			default:
				assert(false); //Should not get here
			}
			currentC.s = currentC.c1 + currentC.c2 * currentC.point + currentC.c3 * currentC.point + currentC.c4;

			if (currentC.s < best_score) {
				best_score = currentC.s;
				best_reserve = pointData.points[j].val;
			}
			previousC = currentC;
		}
	}
	return best_reserve;
}

double ReservePriceMinimizer::calc(VFunction& function, double reserve) {
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

double ReservePriceMinimizer::minimize_f_fast(VFunction* functions, uint64_t length) {
	double		best_reserve;
	PointData	pointData;
	clock_t		start;

	MEASURE_TIME("Generating boundary points", pointData = get_boundary_points(functions, length));
	MEASURE_TIME("Sorting boundary points", sort_end_points(pointData.points));
	MEASURE_TIME("Calculated stuff", best_reserve = calculate_stuff(pointData, length));

	return best_reserve;
}

double ReservePriceMinimizer::f(double reserve, VFunction* functions, uint64_t length) {
	double sum = 0;
	for (size_t i = 0; i < length; i++)	{
		sum += calc(functions[i], reserve);
	}
	return sum;
}

double ReservePriceMinimizer::minimize_f(VFunction* functions, uint64_t length) {
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
}
}