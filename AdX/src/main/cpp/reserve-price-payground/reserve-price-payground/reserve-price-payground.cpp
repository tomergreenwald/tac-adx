#include <stdio.h>
#include "assert.h"
#include <cstdlib>
#include <ctime>
#include <iostream>
#include <stdint.h>

struct Boundary {
	double low, high;
};

struct C {
	double c1, c2, c3, c4, s, point;
};
void update(C* c) {
	c->s = c->c1 + c->c2 * c->point + c->c3 * c->point
		+ c->c4;
}

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
	EndPoint* points;
	double sum;
};

PointData get_sorted_boundary_points(VFunction* functions, uint64_t length) {
	PointData	res;
	EndPoint*	points = new EndPoint[length * 3];
	double		sum = 0;

	for (int j = 0; j < length; j++) {
		sum += functions[j].points.a1;

		points[j * 3].val = functions[j].boundary.high;
		points[j * 3].point_type = FunctionType::HIGH;
		points[j * 3].function = functions[j];

		points[j * 3 + 1].val = functions[j].boundary.low;
		points[j * 3 + 1].point_type = FunctionType::LOW;
		points[j * 3 + 1].function = functions[j];

		points[j * 3 + 2].val = functions[j].boundary.high * (1 + MICRO);
		points[j * 3 + 2].point_type = FunctionType::MICRO;
		points[j * 3 + 2].function = functions[j];
	}
	res.points = points;
	res.sum = sum;
	return res;
}

#define MEASURE_TIME(description, function)	\
	std::cout << description;	\
	start = std::clock();	\
	function;	\
	std::cout << " in " << 1000.0 * (std::clock() - start) / CLOCKS_PER_SEC << " ms" << std::endl;

#define MEASURE_TIME_S(description, function, summary)	\
	std::cout << description << std::endl;	\
	start = std::clock();	\
	function;	\
	std::cout << summary << 1000.0 * (std::clock() - start) / CLOCKS_PER_SEC << " ms" << std::endl;

double calculate_stuff(PointData pointData, uint64_t length) {
	C				currentC;
	C				previousC;
	FunctionType	last_point_type;
	Points			last_function_points;
	double			best_score = 100000;
	double			best_reserve = 0;

	for (int j = 0; j < length; j++) {
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

			last_point_type = pointData.points[j - 1].point_type;
			last_function_points = pointData.points[j - 1].function.points;

			switch (last_point_type) {
			case FunctionType::LOW:
				currentC.c1 = currentC.c1 + last_function_points.a1;
				currentC.c2 = currentC.c2 - last_function_points.a2;
				break;
			case FunctionType::HIGH:
				currentC.c2 = currentC.c2 + last_function_points.a2;
				currentC.c3 = currentC.c3 + last_function_points.a3;
				currentC.c4 = currentC.c4 - last_function_points.a4;
				break;
			case FunctionType::MICRO:
				currentC.c3 = currentC.c3 - last_function_points.a3;
				currentC.c4 = currentC.c4 + last_function_points.a4;
				break;
			default:
				assert(false); //Should not get here
			}
			update(&currentC);
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

	MEASURE_TIME("Sorted boundary points", pointData = get_sorted_boundary_points(functions, length));
	MEASURE_TIME("\tDeleted functions", delete functions);
	MEASURE_TIME("Calculated stuff", best_reserve = calculate_stuff(pointData, length));
	MEASURE_TIME("\tDeleted points", delete pointData.points);

	return best_reserve;
}

VFunction* generate_random_functions(uint64_t size) {
	VFunction* functions = new VFunction[size];
	for (int i = 0; i < size; i++) {
		functions[i].boundary.low = std::rand() % 80 + 1;
		functions[i].boundary.high = std::rand() % 100 + functions[i].boundary.low + 1;
		functions[i].points.a3 = std::rand() % 100 + 1;
		functions[i].points.a2 = MICRO * functions[i].points.a3;
		functions[i].points.a1 = MICRO * functions[i].points.a3 * functions[i].boundary.low;
		functions[i].points.a4 = functions[i].points.a3 * (1 + MICRO) * functions[i].boundary.high;
	}
	return functions;
}

void run_random() {
	std::clock_t	start;
	double			best_reserve;
	VFunction*		functions;
	uint64_t size = 10000000;

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

