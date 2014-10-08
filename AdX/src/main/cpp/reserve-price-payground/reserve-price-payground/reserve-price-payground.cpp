#include <stdio.h>
#include "assert.h"
#include <cstdlib>
#include <ctime>
#include <iostream>

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

PointData get_sorted_boundary_points(VFunction* functions, int length) {
	EndPoint* points = new EndPoint[length * 3];
	double sum = 0;
	for (int j = 0; j < length * 3; j += 3) {
		//sum += functions[j / 3].points.a1;

		points[j].val = functions[j / 3].boundary.high;
		points[j].point_type = FunctionType::HIGH;
		points[j].function = functions[j / 3];

		points[j + 1].val = functions[j / 3].boundary.low;
		points[j + 1].point_type = FunctionType::LOW;
		points[j + 1].function = functions[j / 3];

		points[j + 2].val = functions[j / 3].boundary.high * (1 + MICRO);
		points[j + 2].point_type = FunctionType::MICRO;
		points[j + 2].function = functions[j / 3];
	}
	PointData res;
	res.points = points;
	res.sum = sum;
	return res;
	delete functions;
}

#define MEASURE_TIME(description, function)	\
	std::cout << description << std::endl;	\
	start = std::clock();	\
	function;	\
	std::cout << "CPU time used: " << 1000.0 * (std::clock() - start) / CLOCKS_PER_SEC << " ms\n";	\
	std::cout << "--------------------------" << std::endl;

double calculate_stuff(PointData pointData, int length) {
	C* c = new C[length];
	FunctionType last_point_type;
	Points last_function_points;
	double best_score = 100000;
	double best_reserve = 0;

	for (int j = 0; j < length; j++) {
		if (j == 0) {

			c[j].c1 = -pointData.sum;
			c[j].c2 = 0;
			c[j].c3 = 0;
			c[j].c4 = 0;
			c[j].point = pointData.points[j].val;
		}
		else {
			c[j].c1 = c[j - 1].c1;
			c[j].c2 = c[j - 1].c2;
			c[j].c3 = c[j - 1].c3;
			c[j].c4 = c[j - 1].c4;
			c[j].point = pointData.points[j].val;

			last_point_type = pointData.points[j - 1].point_type;
			last_function_points = pointData.points[j - 1].function.points;

			if (last_point_type == FunctionType::LOW) {
				c[j].c1 = c[j].c1 + last_function_points.a1;
				c[j].c2 = c[j].c2 - last_function_points.a2;
			}
			else if (last_point_type == FunctionType::HIGH) {
				c[j].c2 = c[j].c2 + last_function_points.a2;
				c[j].c3 = c[j].c3 + last_function_points.a3;
				c[j].c4 = c[j].c4 - last_function_points.a4;
			}
			else if (last_point_type == FunctionType::MICRO) {
				c[j].c3 = c[j].c3 - last_function_points.a3;
				c[j].c4 = c[j].c4 + last_function_points.a4;
			}
			else {
				assert(false); //Should not get here
			}
			update(&c[j]);
			if (c[j].s < best_score) {
				best_score = c[j].s;
				best_reserve = pointData.points[j].val;
			}
		}
	}
	delete pointData.points;
	delete c;
	return best_reserve;
}

double minimize_f_fast(VFunction* functions, int length) {
	double best_reserve;
	PointData pointData;
	clock_t start;

	MEASURE_TIME("Sorting boundary points", pointData = get_sorted_boundary_points(functions, length));
	MEASURE_TIME("Calculating stuff", best_reserve = calculate_stuff(pointData, length));
	return best_reserve;
}

int main() {
	std::srand(static_cast<int>(std::time(0)));
	int size = 10000000;
	std::cout << "generating " << size << " functions" << std::endl;
	std::clock_t    start;
	VFunction* functions = new VFunction[size];

	start = std::clock();
	for (int i = 0; i < size; i++) {
		functions[i].boundary.low = std::rand() % 80 + 1;
		functions[i].boundary.high = std::rand() % 100 + functions[i].boundary.low + 1;
		functions[i].points.a3 = std::rand() % 100 + 1;
		functions[i].points.a2 = MICRO * functions[i].points.a3;
		functions[i].points.a1 = MICRO * functions[i].points.a3 * functions[i].boundary.low;
		functions[i].points.a4 = functions[i].points.a3 * (1 + MICRO) * functions[i].boundary.high;
	}
	std::cout << "CPU time used: " << 1000.0 * (std::clock() - start) / CLOCKS_PER_SEC << " ms\n";
	std::cout << "--------------------------" << std::endl;

	//start = std::clock();
	double best_reserve = minimize_f_fast(functions, size);
	//end = std::clock();
	//std::cout << "CPU time used: " << 1000.0 * (end - start) / CLOCKS_PER_SEC << " ms" << std::endl;
	std::cout << "best reserve = " << best_reserve << std::endl;
}

