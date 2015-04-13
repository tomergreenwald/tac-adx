#include "ReservePriceMinimizer.h"
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

}
}