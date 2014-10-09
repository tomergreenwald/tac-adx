#include "reserve-price-payground.h"
#include <stdio.h>
#include "assert.h"
#include <cstdlib>
#include <ctime>
#include <iostream>
#include <stdint.h>
#include <algorithm>

#include "Defs.h"

bool end_point_sorter(EndPoint const& lhs, EndPoint const& rhs) {
	return lhs.val < rhs.val;
}

PointData get_boundary_points(VFunction* functions, uint64_t length) {
	PointData	res;

	res.points.resize(length * 3);

	for (int j = 0; j < length; j++) {
		res.sum += functions[j].points.a1;

		res.points[j * 3].val = functions[j].boundary.high;
		res.points[j * 3].point_type = FunctionType::HIGH;
		res.points[j * 3].function = functions[j];

		res.points[j * 3 + 1].val = functions[j].boundary.low;
		res.points[j * 3 + 1].point_type = FunctionType::LOW;
		res.points[j * 3 + 1].function = functions[j];

		res.points[j * 3 + 2].val = functions[j].boundary.high * (1 + MICRO_S);
		res.points[j * 3 + 2].point_type = FunctionType::MICRO;
		res.points[j * 3 + 2].function = functions[j];
	}
	return res;
}

void sort_end_points(std::vector<EndPoint> &points) {
	std::sort(points.begin(), points.end(), &end_point_sorter);
}

double calculate_stuff(PointData pointData, uint64_t length) {
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
		}
	}
	return best_reserve;
}

double minimize_f_fast(VFunction* functions, uint64_t length) {
	double		best_reserve;
	PointData	pointData;
	clock_t		start;

	MEASURE_TIME("Generating boundary points", pointData = get_boundary_points(functions, length));
	MEASURE_TIME("Sorting boundary points", sort_end_points(pointData.points));
	MEASURE_TIME("\tDeleted functions", delete functions);
	MEASURE_TIME("Calculated stuff", best_reserve = calculate_stuff(pointData, length));

	return best_reserve;
}

VFunction* generate_random_functions(uint64_t size) {
	VFunction* functions = new VFunction[size];
	for (int i = 0; i < size; i++) {
		functions[i].boundary.low = std::rand() % 80 + 1;
		functions[i].boundary.high = std::rand() % 100 + functions[i].boundary.low + 1;
		functions[i].points.a3 = std::rand() % 100 + 1;
		functions[i].points.a2 = MICRO_S * functions[i].points.a3;
		functions[i].points.a1 = MICRO_S * functions[i].points.a3 * functions[i].boundary.low;
		functions[i].points.a4 = functions[i].points.a3 * (1 + MICRO_S) * functions[i].boundary.high;
	}
	return functions;
}

void run_random() {
	std::clock_t	start;
	double			best_reserve;
	VFunction*		functions;
	uint64_t size = 1000000;

	std::srand(static_cast<int>(std::time(0)));
	std::cout << "Expected memory footprint - " << (static_cast<long long>(size)* (48 + 64 * 3) / (1024 * 1024)) << " MB" << std::endl;
	MEASURE_TIME("Generated random functions", functions = generate_random_functions(size));
	best_reserve = minimize_f_fast(functions, size);
	std::cout << "best reserve = " << best_reserve << std::endl;
}

int main() {
	std::clock_t    start;
	MEASURE_TIME_S("Running randomly", run_random(), "\nTotal run time is ");
}

