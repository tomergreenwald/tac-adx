package tau.tac.adx.playground;

import java.util.Arrays;
import java.util.Comparator;

public class Utils {

	public static double MICRO = 0.01;

	public static EndPoint[] get_sorted_boundary_points(VFunction[] functions) {
		EndPoint[] points = new EndPoint[functions.length*3];
		for (int j = 0; j < functions.length*3; j+=3) {
			points[j] = (new EndPoint(functions[j%3].boundary.high,
					FunctionType.HIGH, functions[j%3]));
			points[j+1] = (new EndPoint(functions[j%3].boundary.low,
					FunctionType.LOW, functions[j%3]));
			points[j+2] = (new EndPoint(functions[j%3].boundary.high * (1 + MICRO),
					FunctionType.MICRO, functions[j%3]));
		}
		Arrays.sort(points, new Comparator<EndPoint>() {

			@Override
			public int compare(EndPoint o1, EndPoint o2) {
				return (int) (100 * (o1.val - o2.val));
			}

		});
		return points;
	}

	public static double minimize_f_fast(VFunction[] functions)
			throws Exception {
		EndPoint[] points = get_sorted_boundary_points(functions);
		C[] c = new C[points.length];
		FunctionType last_point_type = null;
		Points last_function_points = null;
		for (int j = 0; j < points.length; j++) {
			if (j == 0) {
				double sum = 0;
				for (int i = 0; i < functions.length; i++) {
					sum += functions[i].points.a1;
				}
				c[j] = new C(-sum, 0, 0, 0, points[j].val);
			} else {
				c[j] = new C(c[j - 1].c1, c[j - 1].c2, c[j - 1].c3,
						c[j - 1].c4, points[j].val);
				c[j].point = points[j].val;
				last_point_type = points[j - 1].point_type;
				last_function_points = points[j - 1].function.points;

				if (last_point_type == FunctionType.LOW) {
					c[j].c1 = c[j].c1 + last_function_points.a1;
					c[j].c2 = c[j].c2 - last_function_points.a2;
				} else if (last_point_type == FunctionType.HIGH) {
					c[j].c2 = c[j].c2 + last_function_points.a2;
					c[j].c3 = c[j].c3 + last_function_points.a3;
					c[j].c4 = c[j].c4 - last_function_points.a4;
				} else if (last_point_type == FunctionType.MICRO) {
					c[j].c3 = c[j].c3 - last_function_points.a3;
					c[j].c4 = c[j].c4 + last_function_points.a4;
				} else {
					throw new Exception("Should not get here");
				}
				c[j].update();
			}
		}
		double best_score = 100000;
		double best_reserve = 0;
		for (int j = 0; j < points.length; j++) {
			if (c[j].s < best_score) {
				best_score = c[j].s;
				best_reserve = points[j].val;
			}
		}
		return best_reserve;
	}
}
