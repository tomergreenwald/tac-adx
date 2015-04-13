#include "ReservePriceMinimizer.h"
namespace tau {
namespace tac {

class FastReservePriceMinimizer : public ReservePriceMinimizer {

public:
	FastReservePriceMinimizer() {};
	virtual ~FastReservePriceMinimizer() {};
	virtual double minimize_f(VFunction* functions, uint64_t length);

private:
	void sort_end_points(std::vector<EndPoint> &points);
	double calculate_stuff(PointData pointData, uint64_t length);
};

} //namespace tac
} //namespace tau