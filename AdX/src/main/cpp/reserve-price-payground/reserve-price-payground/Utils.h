#include "Defs.h"
#include "DataStructs.h"

namespace tau {
namespace tac {
class Utils {
public:
	static VFunction* generate_random_functions(uint64_t size);
private:
	Utils(){};
	~Utils(){};
};

} //namespace tac
} //namespace tau