#include <stdio.h>
#include <cstdlib>
#include "FastReservePriceMinimizer.h"
#include "Utils.h"
#include "auctions.pb.h"
#include <iostream>
#include <fstream>
#include <string>


using tau::tac::VFunction;
using tau::tac::MICRO_S;
using tau::tac::FastReservePriceMinimizer;
using tau::tac::Utils;

using std::fstream;
using std::ios;
using std::cout;
using std::cerr;
using std::endl;


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

VFunction* generate_functions(DataBundle* data) {
	uint64_t size = data->reports_size();
	VFunction* functions = new VFunction[data->reports_size()];
	for (size_t i = 0; i < size; i++) {
		auto report = data->reports().Get(i);
		functions[i].boundary.high = report.firstbid();
		functions[i].boundary.low = report.secondsbid();
		functions[i].points.a3 = MICRO_S / (report.reservedprice() - (1 + MICRO_S) * functions[i].boundary.high);
		functions[i].points.a2 = MICRO_S * functions[i].points.a3;
		functions[i].points.a1 = MICRO_S * functions[i].points.a3 * functions[i].boundary.low;
		functions[i].points.a4 = functions[i].points.a3 * (1 + MICRO_S) * functions[i].boundary.high;

	}
	return functions;
}

double get_revenue(DataBundle* data) {
	double revenue = 0;
	for each (auto report in data->reports()) {
		if (report.secondsbid() > report.reservedprice()) {
			revenue += report.secondsbid();
		}
		else if (report.firstbid() > report.reservedprice()) {
			revenue += report.reservedprice();
		}
	}
	return revenue;
}

int main() {
 	std::clock_t    start;
	double best_reserve;
// 	MEASURE_TIME_S("Running randomly", run_random(), "\nTotal run time is ");
	char* path = "C:\\Users\\Tomer\\git\\tac-adx\\AdX\\resources\\log-29.protobuf";
	DataBundle data;
	fstream input(path, ios::in | ios::binary);
	if (!input) {
		cout << path << ": File not found.  Creating a new file." << endl;
	}
	else if (!data.ParseFromIstream(&input)) {
		cerr << "Failed to parse address book." << endl;
		return -1;
	}
	std::cout << "report count " <<	data.reports_size() << endl;
	std::cout << "report size " << data.ByteSize() << endl;

	VFunction*		functions;
	uint64_t size = data.reports_size();
	FastReservePriceMinimizer rpm;

	std::cout << "Expected memory footprint - " << ((static_cast<long long>(size)* (48 + 64 * 3) + data.ByteSize()) / (1024 * 1024)) << " MB" << std::endl;
	MEASURE_TIME("Total revenue", std::cout << get_revenue(&data) << std::endl);
	MEASURE_TIME("Generated functions", functions = generate_functions(&data));

	best_reserve = rpm.minimize_f(functions, size);
	std::cout << "best reserve = " << best_reserve << std::endl;

	MEASURE_TIME("\tDeleted functions", delete functions);
}

