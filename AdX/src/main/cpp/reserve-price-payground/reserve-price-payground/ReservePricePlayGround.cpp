#include <stdio.h>
#include <cstdlib>
#include <stdint.h>
#include "Defs.h"
#include "ReservePriceMinimizer.h"

using tau::tac::VFunction;
using tau::tac::MICRO_S;
using tau::tac::ReservePriceMinimizer;

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
	ReservePriceMinimizer rpm;

	std::srand(static_cast<int>(std::time(0)));
	std::cout << "Expected memory footprint - " << (static_cast<long long>(size)* (48 + 64 * 3) / (1024 * 1024)) << " MB" << std::endl;
	MEASURE_TIME("Generated random functions", functions = generate_random_functions(size));

	best_reserve = rpm.minimize_f_fast(functions, size);
	std::cout << "best reserve = " << best_reserve << std::endl;

	MEASURE_TIME("\tDeleted functions", delete functions);
}

int main() {
	std::clock_t    start;
	MEASURE_TIME_S("Running randomly", run_random(), "\nTotal run time is ");
}

