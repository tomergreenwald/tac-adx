#include <vector>
#include "Defs.h"
#include "DataStructs.h"

namespace tau {
namespace tac {
class ReservePriceMinimizer {

public:
	ReservePriceMinimizer() {};
	virtual ~ReservePriceMinimizer() {};
	virtual double minimize_f(VFunction* functions, uint64_t length) = 0;
protected:
	PointData get_boundary_points(VFunction* functions, uint64_t length);
private:
	double calc(VFunction& function, double reserve);
	double f(double reserve, VFunction* functions, uint64_t length);
};
} //namespace tac
} //namespace tau