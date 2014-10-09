#include <stdio.h>
#include <cstdlib>
#include "FastReservePriceMinimizer.h"
#include "Utils.h"

using tau::tac::VFunction;
using tau::tac::MICRO_S;
using tau::tac::FastReservePriceMinimizer;
using tau::tac::Utils;


void run_random() {
	std::clock_t	start;
	double			best_reserve;
	VFunction*		functions;
	uint64_t size = 1000000;
	FastReservePriceMinimizer rpm;

	std::cout << "Expected memory footprint - " << (static_cast<long long>(size)* (48 + 64 * 3) / (1024 * 1024)) << " MB" << std::endl;
	MEASURE_TIME("Generated random functions", functions = Utils::generate_random_functions(size));

	best_reserve = rpm.minimize_f(functions, size);
	std::cout << "best reserve = " << best_reserve << std::endl;

	MEASURE_TIME("\tDeleted functions", delete functions);
}

int main() {
	std::clock_t    start;
	MEASURE_TIME_S("Running randomly", run_random(), "\nTotal run time is ");
}

