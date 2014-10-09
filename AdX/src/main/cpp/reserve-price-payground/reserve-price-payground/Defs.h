#include <ctime>
#include <iostream>

#define MEASURE_TIME(description, function)	\
	std::cout << description;	\
	start = std::clock();	\
	function;	\
	std::cout << " in " << 1000.0 * (std::clock() - start) / CLOCKS_PER_SEC << " ms" << std::endl;

#define MEASURE_TIME_S(description, function, summary)	\
	std::cout << description << std::endl;	\
	start = std::clock();	\
	function;	\
	std::cout << summary << 1000.0 * (std::clock() - start) / CLOCKS_PER_SEC << " ms" << std::endl;