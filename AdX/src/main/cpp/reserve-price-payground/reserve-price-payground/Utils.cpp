#include "Utils.h"

namespace tau {
namespace tac {

VFunction* Utils::generate_random_functions(uint64_t size) {
	VFunction* functions = new VFunction[size];
	std::srand(static_cast<int>(std::time(0)));
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

} //namespace tac
} //namespace tau