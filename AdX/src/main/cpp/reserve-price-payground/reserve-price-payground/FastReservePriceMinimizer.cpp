#include "FastReservePriceMinimizer.h"
#include <algorithm>
#include "assert.h"

namespace tau {
namespace tac {

	double FastReservePriceMinimizer::minimize_f(VFunction* functions, uint64_t length) {
		double		best_reserve;
		PointData	pointData;
		clock_t		start;

		MEASURE_TIME("Generating boundary points", pointData = get_boundary_points(functions, length));
		MEASURE_TIME("Sorting boundary points", sort_end_points(pointData.points));
		MEASURE_TIME("Calculated stuff", best_reserve = calculate_stuff(pointData, length));

		return best_reserve;
	}

bool end_point_sorter(EndPoint const& lhs, EndPoint const& rhs) {
	return lhs.val < rhs.val;
}

void FastReservePriceMinimizer::sort_end_points(std::vector<EndPoint> &points) {
	std::sort(points.begin(), points.end(), end_point_sorter);
}

double FastReservePriceMinimizer::calculate_stuff(PointData pointData, uint64_t length) {
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

}
}