#include "ReservePriceMinimizer.h"

namespace tau {
namespace tac {

class StandardReservePriceMinimizer : public ReservePriceMinimizer {

public:
	StandardReservePriceMinimizer() {};
	virtual ~StandardReservePriceMinimizer() {};
	virtual double minimize_f(VFunction* functions, uint64_t length);
private:
	double f(double reserve, VFunction* functions, uint64_t length);
	double calc(VFunction& function, double reserve);
};

} //namespace tac
} //namespace tau