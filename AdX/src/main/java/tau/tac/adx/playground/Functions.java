package tau.tac.adx.playground;


public class Functions {
	public static double calc_r(double [] w, double [] x) {
		assert(w.length == x.length);
		
		double r = 0.0;
		for (int j = 0; j < w.length; ++j) {
			r += w[j] * x[j];
		}
		
		return r;
	}
	
	public static double u(double r, double b1, double gamma) {
		double u = 0.0;
		
		if (r < b1) {
			u = -r;
		}
		else {
			u = (r - (1.0 + gamma)*b1) / gamma;
		}
		return u;
	}
	
	public static double u(double [] w, double [] x, double b1, double gamma) {
		return u(calc_r(w, x), b1, gamma);
	}
	
	public static double v(double r, double b2, double b1, double gamma) {
		double v = 0.0;
		
		if (r < b2) {
			v = -r + b2;
		} else if (r > b1 * (1.0 + gamma)) {
			v = r - (1.0 + gamma)*b1;
			v /= gamma;
		} else {
			v = 0.0;
		}
		
		return v;
	}
	
	public static double [] subgradient_v(double [] w, double [] features, double b2, double b1, double gamma) {
		assert (w.length == features.length);
		
		double r = calc_r(w, features);
		
		double [] res = new double[w.length];
		
		if (r < b2) {
			for (int i = 0; i < w.length; ++i) {
				res[i] = -features[i];
			}
		} else if (r > (b1 * (1.0 + gamma))) {
			for (int i = 0; i < w.length; ++i) {
				res[i] = features[i] / gamma;
			}
		} else {
			for (int i = 0; i < w.length; ++i) {
				res[i] = 0.0;
			}
		}
		
		return res;
	}
	
	public static double v(double [] w, double [] x, double b2, double b1, double gamma) {
		return v(calc_r(w, x), b2, b1, gamma);
	}
	
	public static double L(double [] w, double [][] x, double [] b2, double [] b1, double gamma) {
		assert(b2.length == b1.length);
		assert(w.length == x[0].length);
		
		double L = 0.0;
		
		for (int i = 0; i < x.length; ++i) {
			L += u(w, x[i], b1[i], gamma) - v(w, x[i], b2[i], b1[i], gamma);
		}
		
		return L;
	}
	
	public static double [] L(double [] r, double [] b2, double [] b1, double gamma) {
		assert(b2.length == b1.length);
		
		double [] L = new double[r.length];
		
		for (int rIdx = 0; rIdx < r.length; ++rIdx) {
			L[rIdx] = 0.0;
			for (int i = 0; i < b2.length; ++i) {
				L[rIdx] += u(r[rIdx], b1[i], gamma) - v(r[rIdx], b2[i], b1[i], gamma);
			}
		}
		
		return L;
	}
}
