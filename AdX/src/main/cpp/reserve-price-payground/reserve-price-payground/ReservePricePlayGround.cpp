#include <stdio.h>
#include <cstdlib>
#include "FastReservePriceMinimizer.h"
#include "Utils.h"
#include "auctions.pb.h"
#include <iostream>
#include <fstream>
#include <string>
#include "dirent.h"
#include <vector>


using tau::tac::VFunction;
using tau::tac::MICRO_S;
using tau::tac::FastReservePriceMinimizer;
using tau::tac::Utils;

using std::fstream;
using std::ios;
using std::cout;
using std::cerr;
using std::endl;
using std::vector;
using std::string;


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

vector<VFunction> generate_functions(DataBundle* data) {
	uint64_t size = data->reports_size();
	vector<VFunction> functions(data->reports_size());
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

bool endsWith(const string& s, const string& suffix)
{
	return s.rfind(suffix) == (s.size() - suffix.size());
}

DataBundle* generate_data_bundle(string path) {
	DataBundle* data = new DataBundle;
	fstream input(path, ios::in | ios::binary);
	if (!input) {
		cout << path << ": File not found.  Creating a new file." << endl;
	}
	else if (!data->ParseFromIstream(&input)) {
		cerr << "Failed to parse address book." << endl;
	}
	std::cout << "report count " << data->reports_size() << endl;
	std::cout << "report size " << data->ByteSize() << endl;
	return data;
}

vector<vector<VFunction>> generate_functions(char* path) {
	DIR *dir;
	struct dirent *ent;
	vector<vector<VFunction>> functionVector;
	if ((dir = opendir(path)) != NULL) {
		/* print all the files and directories within directory */
		while ((ent = readdir(dir)) != NULL) {
			if (endsWith(ent->d_name, ".protobuf")) {
				printf("%s\n", ent->d_name);
				DataBundle* data = generate_data_bundle(string(path) + string(ent->d_name));
				functionVector.push_back(generate_functions(data));
				delete data;
			}
		}
		closedir(dir);
	}
	else {
		/* could not open directory */
		cout << "Could not identify protobuf files in the given folder - " << path << endl;
	}
	cout << functionVector.size() << endl;
	return functionVector;
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
	vector<vector<VFunction>> functionVector = generate_functions("C:\\Users\\Tomer\\git\\tac-adx\\AdX\\resources\\");
	int size = 0;
	for each (auto vec in functionVector) {
		size += vec.size();
	}
	VFunction* functions = new VFunction[size];
	int pointer = 0;
	for each (auto vec in functionVector) {
		for each (auto func in vec) {
			functions[pointer++] = func;
		}
	}
	cout << pointer << endl;
	cout << size << endl;
 	std::clock_t    start;
	double best_reserve;

	//uint64_t size = data.reports_size();
	FastReservePriceMinimizer rpm;
	//include protobuf size
	std::cout << "Expected memory footprint - " << ((static_cast<long long>(size)* (48 + 64 * 3)) / (1024 * 1024)) << " MB" << std::endl;
	//MEASURE_TIME("Total revenue", std::cout << get_revenue(&data) << std::endl);
	//MEASURE_TIME("Generated functions", functions = generate_functions(&data));

	best_reserve = rpm.minimize_f(functions, size);
	std::cout << "best reserve = " << best_reserve << std::endl;

	MEASURE_TIME("\tDeleted functions", delete functions);
}

