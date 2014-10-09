#include <vector>
#include <stdint.h>
#include "DataStructs.h"

namespace tau {
namespace tac {
class ReservePriceMinimizer {

public:
	ReservePriceMinimizer() {};
	~ReservePriceMinimizer() {};
	double minimize_f(VFunction* functions, uint64_t length);
	double minimize_f_fast(VFunction* functions, uint64_t length);
private:
	PointData get_boundary_points(VFunction* functions, uint64_t length);
	void sort_end_points(std::vector<EndPoint> &points);
	double calculate_stuff(PointData pointData, uint64_t length);
	double calc(VFunction& function, double reserve);
	double f(double reserve, VFunction* functions, uint64_t length);
};
} //namespace tac
} //namespace tau