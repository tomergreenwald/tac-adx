package tau.tac.adx.playground;

import java.util.Random;

import static tau.tac.adx.playground.Utils.MICRO;

public class testy {
	public static void main(String[] args) throws Exception {
		Random random = new Random();
		int size = 100000;
		VFunction[] functions = new VFunction[size];
		long pre = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			double low = random.nextInt(80) + 1;
			double high = random.nextInt(100) + low + 1;
			double a3 = random.nextInt(100) + 1;
			double a2 = MICRO * a3;
			double a1 = MICRO * a3 * low;
			double a4 = a3 * (1 + MICRO) * high;
			functions[i] = new VFunction(new Boundary(low, high), new Points(
					a1, a2, a3, a4));
		}
		long post = System.currentTimeMillis();
		System.out.println("timer " + (post - pre) + " millis");
		System.out.println(functions.length + " functions");
		System.out.println("--------------------------");

		// # pre = datetime.now()
		// # best_reserve = minimize_f(functions)
		// # post = datetime.now()
		// # print "timer", (post - pre)
		// # print
		// "[best reserve = {reserve}, score = {score}]".format(reserve=best_reserve,
		// score=f(best_reserve, functions))

		// print "--------------------------"
		// pre = datetime.now()
		pre = System.currentTimeMillis();
		double best_reserve = Utils.minimize_f_fast(functions);
		post = System.currentTimeMillis();
		System.out.println("timer " + (post - pre) + " millis");
		// post = datetime.now()
		// print "timer", (post - pre)
		System.out.println("best reserve = " + best_reserve);

	}
}
