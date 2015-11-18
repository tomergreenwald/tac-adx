package tau.tac.adx.playground;

//import ilog.concert.IloException;
//import ilog.concert.IloLPMatrix;
//import ilog.concert.IloMPModeler;
//import ilog.concert.IloNumExpr;
//import ilog.concert.IloNumVar;
//import ilog.cplex.IloCplex;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import tau.tac.adx.parser.Auctions.AdType;
import tau.tac.adx.parser.Auctions.AdxQuery;
import tau.tac.adx.parser.Auctions.Device;
import tau.tac.adx.parser.Auctions.MarketSegment;
import static tau.tac.adx.playground.VFunction.MICRO;

public class Utils {

//	public static EndPoint[] get_sorted_boundary_points(VFunction[] functions) {
//		EndPoint[] points = new EndPoint[functions.length * 3];
//		for (int j = 0; j < functions.length * 3; j += 3) {
//			points[j] = (new EndPoint(functions[j / 3].boundary.high,
//					FunctionType.HIGH, functions[j / 3]));
//			points[j + 1] = (new EndPoint(functions[j / 3].boundary.low,
//					FunctionType.LOW, functions[j / 3]));
//			points[j + 2] = (new EndPoint(functions[j / 3].boundary.high
//					* (1 + MICRO), FunctionType.MICRO, functions[j / 3]));
//		}
//		Arrays.sort(points, new Comparator<EndPoint>() {
//
//			@Override
//			public int compare(EndPoint o1, EndPoint o2) {
//				if (o1.val > o2.val) {
//					return 1;
//				}
//				if (o1.val == o2.val) {
//					return 0;
//				}
//				return -1;
//			}
//
//		});
//		return points;
//	}
//
//	public static double f(double reserve, VFunction[] functions) {
//		double function_sum = 0;
//		for (VFunction function : functions) {
//			function_sum += function.calc(reserve);
//		}
//		return function_sum;
//	}
//
//	public static double minimize_f(VFunction[] functions) {
//		double best_reserve = Double.NaN;
//		double best_score = Double.MAX_VALUE;
//		for (VFunction function : functions) {
//			double[] reserves = new double[3];
//			reserves[0] = function.boundary.low;
//			reserves[1] = function.boundary.high;
//			reserves[2] = function.boundary.high * MICRO;
//			for (double reserve : reserves) {
//				double score = f(reserve, functions);
//				if (score < best_score) {
//					best_score = score;
//					best_reserve = reserve;
//				}
//			}
//		}
//		return best_reserve;
//	}
//
//	public static double minimize_f_fast(VFunction[] functions)
//			throws Exception {
//		EndPoint[] points = get_sorted_boundary_points(functions);
//		C[] c = new C[points.length];
//		FunctionType last_point_type = null;
//		Points last_function_points = null;
//		for (int j = 0; j < points.length; j++) {
//			if (j == 0) {
//				double sum = 0;
//				for (int i = 0; i < functions.length; i++) {
//					sum += functions[i].points.a1;
//				}
//				c[j] = new C(-sum, 0, 0, 0, points[j].val);
//			} else {
//				c[j] = new C(c[j - 1].c1, c[j - 1].c2, c[j - 1].c3,
//						c[j - 1].c4, points[j].val);
//				c[j].point = points[j].val;
//				last_point_type = points[j - 1].point_type;
//				last_function_points = points[j - 1].function.points;
//
//				if (last_point_type == FunctionType.LOW) {
//					c[j].c1 = c[j].c1 + last_function_points.a1;
//					c[j].c2 = c[j].c2 - last_function_points.a2;
//				} else if (last_point_type == FunctionType.HIGH) {
//					c[j].c2 = c[j].c2 + last_function_points.a2;
//					c[j].c3 = c[j].c3 + last_function_points.a3;
//					c[j].c4 = c[j].c4 - last_function_points.a4;
//				} else if (last_point_type == FunctionType.MICRO) {
//					c[j].c3 = c[j].c3 - last_function_points.a3;
//					c[j].c4 = c[j].c4 + last_function_points.a4;
//				} else {
//					throw new Exception("Should not get here");
//				}
//				c[j].update();
//			}
//		}
//		double best_score = 100000;
//		double best_reserve = 0;
//		for (int j = 0; j < points.length; j++) {
//			if (c[j].s < best_score) {
//				best_score = c[j].s;
//				best_reserve = points[j].val;
//			}
//		}
//		return best_reserve;
//	}
//
//	public static double minimize_f_fast_with_rejection(VFunction[] functions,
//			double reserve_upper_bound) throws Exception {
//		EndPoint[] points = get_sorted_boundary_points(functions);
//		C[] c = new C[points.length];
//		FunctionType last_point_type = null;
//		Points last_function_points = null;
//		for (int j = 0; j < points.length; j++) {
//			if (j == 0) {
//				double sum = 0;
//				for (int i = 0; i < functions.length; i++) {
//					sum += functions[i].points.a1;
//				}
//				c[j] = new C(-sum, 0, 0, 0, points[j].val);
//			} else {
//				c[j] = new C(c[j - 1].c1, c[j - 1].c2, c[j - 1].c3,
//						c[j - 1].c4, points[j].val);
//				c[j].point = points[j].val;
//				last_point_type = points[j - 1].point_type;
//				last_function_points = points[j - 1].function.points;
//
//				if (last_point_type == FunctionType.LOW) {
//					c[j].c1 = c[j].c1 + last_function_points.a1;
//					c[j].c2 = c[j].c2 - last_function_points.a2;
//				} else if (last_point_type == FunctionType.HIGH) {
//					c[j].c2 = c[j].c2 + last_function_points.a2;
//					c[j].c3 = c[j].c3 + last_function_points.a3;
//					c[j].c4 = c[j].c4 - last_function_points.a4;
//				} else if (last_point_type == FunctionType.MICRO) {
//					c[j].c3 = c[j].c3 - last_function_points.a3;
//					c[j].c4 = c[j].c4 + last_function_points.a4;
//				} else {
//					throw new Exception("Should not get here");
//				}
//				c[j].update();
//			}
//		}
//		double best_score = Double.MAX_VALUE;
//		double best_reserve = 0;
//		for (int j = 0; j < points.length; j++) {
//			if ((c[j].s < best_score) && (points[j].val <= reserve_upper_bound)) {
//				best_score = c[j].s;
//				best_reserve = points[j].val;
//			}
//		}
//		return best_reserve;
//	}
//
	public static double[] getFeatures(AdxQuery adxQuery,
			Map<String, Integer> publisherNameToId) {
		double features[] = new double[5 + 3];
		features[0] = adxQuery.getMarketSegmentsList().contains(
				MarketSegment.MALE) ? 1 : -1;
		features[1] = adxQuery.getMarketSegmentsList().contains(
				MarketSegment.YOUNG) ? 1 : -1;
		features[2] = adxQuery.getMarketSegmentsList().contains(
				MarketSegment.LOW_INCOME) ? 1 : -1;
		features[3] = adxQuery.getDevice() == Device.MOBILE ? 1 : -1;
		features[4] = adxQuery.getAdtype() == AdType.TEXT ? 1 : -1;
		int publisherGroup = publisherNameToId.get(adxQuery.getPublisher())
				/ (publisherNameToId.size() / 3);
		for (int i = 0; i < 3; i++) {
			if (i == publisherGroup)
				features[5 + i] = 1;
			else
				features[5 + i] = -1;
		}
		return features;
	}
//
//	public static double[] DC(int m, double[][] featureVector,
//			double[] initial_w, double[] b1, double[] b2, double gamma,
//			double lambda, double Lambda) throws Exception {
//
//		System.out.println("Running DC Algorithm");
//
//		int numFeatures = initial_w.length;
//		double[] w = new double[numFeatures];
//
//		// alpha values to try
//		double[] va = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9 };
//		// The value of the quadratic programming solution for each alpha
//		double[] qpval = new double[va.length];
//		// min(qpval)
//		double min_val = Double.MAX_VALUE;
//		// argmin(qpval)
//		int curr_min_idx = 0;
//		// w associated with min(qpval)
//		double[] best_w = new double[numFeatures];
//
//		boolean found_valid_sol = false;
//
//		// Try the given Lambda and a range of alphas. If we cannot find a valid
//		// solution, double Lambda and repeat.
//		while (!found_valid_sol) {
//			min_val = Double.MAX_VALUE;
//			curr_min_idx = 0;
//
//			// For each alpha, try to solve the QP.
//			// Sometimes an alpha may not lead to a valid solution. Continue if
//			// that happens.
//			// Keep track of the best QP value
//			for (int aa = 0; aa < va.length; ++aa) {
//				double alpha = va[aa];
//				System.out.println("alpha = " + alpha);
//				initial_w = estimate_w(m, numFeatures, featureVector,
//						initial_w, b1, b2, alpha, lambda, Lambda);
//
//				w = initial_w;
//				boolean valid_alpha = true;
//				double threshold = 0.000001;
//				double del_dist = Double.MAX_VALUE;
//				int dc_iter = 0;
//
//				while (del_dist > threshold && dc_iter < 10) {
//					dc_iter++;
//					// w_t <- DCA(w) -- DCA algorithm
//					double euclidean_norm = calculate_euclidean_norm(w);
//					System.out.println("Iter " + dc_iter
//							+ ", euclidean_norm (loop start) = "
//							+ euclidean_norm);
//
//					w = dca(m, numFeatures, featureVector, w, b1, b2, gamma,
//							lambda, Lambda, qpval, aa);
//					System.out.println("qpval = " + qpval[aa]
//							+ ", w_dca_norm = " + calculate_euclidean_norm(w));
//					// w <- OPTIMIZE(objective, fixed direction w_t / |w_t|)
//					double[] u = normalize_vector(w);
//
//					double[] u_dot_xi = new double[m];
//					int num_pos_u_dot_xi = 0;
//					for (int i = 0; i < m; ++i) {
//						u_dot_xi[i] = 0.0;
//						for (int j = 0; j < numFeatures; ++j) {
//							u_dot_xi[i] += u[j] * featureVector[i][j];
//						}
//						if (u_dot_xi[i] > 0.0) {
//							num_pos_u_dot_xi++;
//						}
//					}
//
//					// if block is there because we may have u dot xi = 0 for
//					// all i
//					// then w = 0, and we might get stuck with a 0 solution
//
//					if (num_pos_u_dot_xi > 0) {
//						VFunction[] functions = new VFunction[num_pos_u_dot_xi];
//						int c = 0;
//						for (int i = 0; i < m; i++) {
//							if (u_dot_xi[i] > 0.0) {
//								double low = b2[i] / u_dot_xi[i];
//								double high = b1[i] / u_dot_xi[i];
//								double a3 = u_dot_xi[i] / gamma;
//								double a2 = u_dot_xi[i];
//								double a1 = b2[i];
//								double a4 = b1[i] * (1.0 + gamma) / gamma;
//								functions[c] = new VFunction(new Boundary(low,
//										high), new Points(a1, a2, a3, a4));
//								c++;
//							}
//						}
//
//						double eta = minimize_f_fast_with_rejection(functions,
//								Lambda);
//						del_dist = Math.abs(eta - euclidean_norm);
//						for (int j = 0; j < numFeatures; ++j) {
//							w[j] = u[j] * eta;
//						}
//						System.out.println("w_opt_norm = " + eta);
//					} else {
//						valid_alpha = false;
//						System.out
//								.println("Got a u that does not give positive bids!"
//										+ "  alpha = " + alpha);
//						break;
//					}
//					if (valid_alpha)
//						found_valid_sol = true;
//				}
//
//				// System.out.println("DC ran for " + dc_iter + " iterations.");
//				if (!valid_alpha) {
//					continue;
//				} else {
//					if (qpval[aa] < min_val) {
//						min_val = qpval[aa];
//						curr_min_idx = aa;
//						for (int j = 0; j < numFeatures; ++j) {
//							best_w[j] = w[j];
//						}
//					}
//				}
//			}
//			// Nothing found with this combination of parameters. Try increasing
//			// Lambda by 2.
//			if (!found_valid_sol)
//				Lambda *= 2.0;
//		}
//
//		// Found something. Assign and return.
//		for (int j = 0; j < numFeatures; ++j) {
//			w[j] = best_w[j];
//		}
//
//		int T = 5;
//		double[] first_T_w = new double[T];
//		for (int j = 0; j < T; ++j) {
//			first_T_w[j] = w[j];
//		}
//
//		System.out.println("Summary of DC algorithm results");
//		System.out.println("Lambda = " + Lambda);
//		System.out.println("Best alpha = " + va[curr_min_idx] + " with value "
//				+ min_val);
//		System.out.println("w_norm = " + calculate_euclidean_norm(w));
//		System.out.println("First " + T + " elements of w: "
//				+ Arrays.toString(first_T_w));
//
//		return w;
//	}
//
//	public static double[] normalize_vector(double[] w) {
//		double euclidean_norm = calculate_euclidean_norm(w);
//
//		double[] u = new double[w.length];
//		for (int j = 0; j < w.length; ++j) {
//			u[j] = w[j] / euclidean_norm;
//		}
//
//		return u;
//	}
//
//	public static double calculate_euclidean_norm(double[] w) {
//		double euclidean_norm = 0.0;
//
//		for (int j = 0; j < w.length; ++j) {
//			euclidean_norm += w[j] * w[j];
//		}
//		euclidean_norm = Math.sqrt(euclidean_norm);
//
//		return euclidean_norm;
//	}
//
//	public static double[] dca(int m, int numFeatures, double[][] x,
//			double[] initial_w, double[] b1, double[] b2, double gamma,
//			double lambda, double Lambda, double[] qpval, int aa) {
//
//		final double EPSILON = Math.pow(2.0, -20.0);
//		double maxDifference = Double.MAX_VALUE;
//		double[] w = new double[numFeatures];
//		for (int j = 0; j < numFeatures; ++j) {
//			w[j] = initial_w[j];
//		}
//
//		int iter = 0;
//		while (maxDifference > EPSILON && iter < 10) {
//			maxDifference = -Double.MAX_VALUE;
//			iter++;
//
//			double[] sgv = calculate_subgradient(m, numFeatures, w, x, b2, b1,
//					gamma);
//			double[] lp_results = solve_lp(iter, m, numFeatures, x, b1, b2,
//					gamma, lambda, Lambda, sgv, qpval, aa);
//
//			for (int j = 0; j < numFeatures; ++j) {
//				double currDiff = Math.abs(w[numFeatures - (j + 1)]
//						- lp_results[lp_results.length - (j + 1)]);
//				if (currDiff > maxDifference) {
//					maxDifference = currDiff;
//				}
//				w[numFeatures - (j + 1)] = lp_results[lp_results.length
//						- (j + 1)];
//			}
//		}
//
//		return w;
//	}
//
//	// Subgradient functions
//
//	private static double[] calculate_subgradient(int m, int numFeatures,
//			double[] w, double[][] x, double[] b2, double[] b1, double gamma) {
//		double[] sgv = subgradient_v(w, x[0], b2[0], b1[0], gamma);
//		for (int i = 1; i < m; ++i) {
//			double[] curr = subgradient_v(w, x[i], b2[i], b1[i], gamma);
//			for (int j = 0; j < numFeatures; ++j) {
//				sgv[j] += curr[j];
//			}
//		}
//		return sgv;
//	}
//
//	private static double calc_r(double[] w, double[] x) {
//		assert (w.length == x.length);
//
//		double r = 0.0;
//		for (int j = 0; j < w.length; ++j) {
//			r += w[j] * x[j];
//		}
//
//		return r;
//	}
//
//	public static double[] subgradient_v(double[] w, double[] features,
//			double b2, double b1, double gamma) {
//		assert (w.length == features.length);
//
//		double r = calc_r(w, features);
//
//		double[] res = new double[w.length];
//
//		if (r < b2) {
//			for (int i = 0; i < w.length; ++i) {
//				res[i] = -features[i];
//			}
//		} else if (r > (b1 * (1.0 + gamma))) {
//			for (int i = 0; i < w.length; ++i) {
//				res[i] = features[i] / gamma;
//			}
//		} else {
//			for (int i = 0; i < w.length; ++i) {
//				res[i] = 0.0;
//			}
//		}
//
//		return res;
//	}
//
//	// cplex functions
//
//	private static double[] solve_lp(int iter, int m, int numFeatures,
//			double[][] x, double[] b1, double[] b2, double gamma,
//			double lambda, double Lambda, double[] sgv, double[] qpval, int aa) {
//		double[] cplex_result = new double[numFeatures + m];
//		try {
//			IloCplex cplex = new IloCplex();
//			// cplex.setParam(IloCplex.IntParam.Threads, 1);
//			cplex.setOut(null); // Disable console output
//			IloLPMatrix lp = PopulateQP(cplex, m, numFeatures, x, b1, b2,
//					gamma, lambda, Lambda, sgv);
//			if (cplex.solve()) {
//				cplex_result = cplex.getValues(lp);
//				qpval[aa] = cplex.getObjValue();
//				// System.out.println(cplex.getObjValue());
//			}
//			cplex.endModel();
//			cplex.end();
//		} catch (IloException e) {
//			System.err.println("Concert Exception [" + e
//					+ "] caught at iteration " + iter);
//		}
//
//		return cplex_result;
//	}
//
//	private static IloLPMatrix PopulateQP(IloMPModeler model, int m,
//			int numFeatures, double[][] x, double[] b1, double[] b2,
//			double gamma, double lambda, double Lambda, double[] sgv)
//			throws IloException {
//		IloLPMatrix lpMatrix = model.addLPMatrix();
//
//		// s
//
//		double[] ub = new double[m];
//		double[] lb = new double[m];
//
//		for (int i = 0; i < m; ++i) {
//			ub[i] = Double.MAX_VALUE;
//			lb[i] = -Double.MAX_VALUE;
//			// lb[i] = -b1[i];
//		}
//
//		IloNumVar[] s = model.numVarArray(model.columnArray(lpMatrix, m), lb,
//				ub);
//
//		// w
//
//		double[] ub_w = new double[numFeatures];
//		double[] lb_w = new double[numFeatures];
//
//		// |w| <= Lambda, so no |w_i| can be larger than Lambda
//		for (int i = 0; i < numFeatures; ++i) {
//			ub_w[i] = Lambda;
//			lb_w[i] = -Lambda;
//			// ub_w[i] = 1.0;
//			// lb_w[i] = -1.0;
//		}
//
//		IloNumVar[] w = model.numVarArray(
//				model.columnArray(lpMatrix, numFeatures), lb_w, ub_w);
//
//		// Add objective
//
//		IloNumExpr[] all = new IloNumExpr[m + 2 * numFeatures];
//		for (int i = 0; i < numFeatures; ++i) {
//			all[m + i] = model.prod(lambda, model.square(w[i]));
//			all[m + i + numFeatures] = model.prod(sgv[i],
//					model.prod(-1.0, w[i]));
//		}
//
//		for (int i = 0; i < m; ++i) {
//			all[i] = model.prod(1.0, s[i]);
//		}
//
//		// Add constraints
//
//		IloNumExpr[] inter = new IloNumExpr[numFeatures + 1];
//
//		for (int i = 0; i < m; ++i) {
//			// s_i + w dot x_i >= 0
//			for (int j = 0; j < numFeatures; ++j) {
//				inter[j] = model.prod(x[i][j], w[j]);
//			}
//			inter[numFeatures] = s[i];
//			model.addGe(model.sum(inter), 0.0);
//
//			// gamma * s_i - w .* x_i + (1 + gamma) b_i^1 >= 0
//			for (int j = 0; j < numFeatures; ++j) {
//				inter[j] = model.prod(-1.0, model.prod(x[i][j], w[j]));
//			}
//			inter[numFeatures] = model.sum(model.prod(s[i], gamma),
//					(1.0 + gamma) * b1[i]);
//			model.addGe(model.sum(inter), 0.0);
//		}
//
//		IloNumExpr Q = model.sum(all);
//		model.add(model.minimize(Q));
//
//		return lpMatrix;
//	}
//
//	/*
//	 * Functions to initialize w by solving a quadratic program
//	 */
//	public static double[] estimate_w(int m, int numFeatures, double[][] x,
//			double[] initial_w, double[] b1, double[] b2, double alpha,
//			double lambda, double Lambda) {
//
//		final double EPSILON = Math.pow(2.0, -20.0);
//		double maxDifference = Double.MAX_VALUE;
//		double[] w = new double[numFeatures];
//		for (int j = 0; j < numFeatures; ++j) {
//			w[j] = initial_w[j];
//		}
//
//		int iter = 0;
//		while (maxDifference > EPSILON && iter < 10) {
//			maxDifference = -Double.MAX_VALUE;
//			iter++;
//
//			double[] interim_w = solve_surrogate(iter, m, numFeatures, x, b1,
//					b2, alpha, lambda, Lambda);
//
//			for (int j = 0; j < numFeatures; ++j) {
//				double currDiff = Math.abs(w[numFeatures - (j + 1)]
//						- interim_w[interim_w.length - (j + 1)]);
//				if (currDiff > maxDifference) {
//					maxDifference = currDiff;
//				}
//				w[numFeatures - (j + 1)] = interim_w[interim_w.length - (j + 1)];
//			}
//		}
//
//		return w;
//	}
//
//	private static double[] solve_surrogate(int iter, int m, int numFeatures,
//			double[][] x, double[] b1, double[] b2, double alpha,
//			double lambda, double Lambda) {
//		double[] cplex_result = new double[numFeatures + m];
//		try {
//			IloCplex cplex = new IloCplex();
//			// cplex.setParam(IloCplex.IntParam.Threads, 1);
//			cplex.setOut(null); // Disable console output
//			IloLPMatrix lp = PopulateSurrogateQP(cplex, m, numFeatures, x, b1,
//					b2, alpha, lambda, Lambda);
//			if (cplex.solve()) {
//				cplex_result = cplex.getValues(lp);
//			}
//			cplex.endModel();
//			cplex.end();
//		} catch (IloException e) {
//			System.err.println("Concert Exception [" + e
//					+ "] caught at iteration " + iter);
//		}
//
//		return cplex_result;
//	}
//
//	private static IloLPMatrix PopulateSurrogateQP(IloMPModeler model, int m,
//			int numFeatures, double[][] x, double[] b1, double[] b2,
//			double alpha, double lambda, double Lambda) throws IloException {
//		IloLPMatrix lpMatrix = model.addLPMatrix();
//
//		// s
//
//		double[] ub = new double[m];
//		double[] lb = new double[m];
//
//		for (int i = 0; i < m; ++i) {
//			ub[i] = Double.MAX_VALUE;
//			lb[i] = -Double.MAX_VALUE;
//			// lb[i] = -(b1[i] + alpha*(b2[i] - b1[i]));
//		}
//
//		IloNumVar[] s = model.numVarArray(model.columnArray(lpMatrix, m), lb,
//				ub);
//
//		// w
//
//		double[] ub_w = new double[numFeatures];
//		double[] lb_w = new double[numFeatures];
//
//		// |w| <= Lambda, so no |w_i| can be larger than Lambda
//		for (int i = 0; i < numFeatures; ++i) {
//			ub_w[i] = Lambda;
//			lb_w[i] = -Lambda;
//		}
//
//		IloNumVar[] w = model.numVarArray(
//				model.columnArray(lpMatrix, numFeatures), lb_w, ub_w);
//
//		// Add objective
//
//		IloNumExpr[] all = new IloNumExpr[m + numFeatures];
//		for (int i = 0; i < numFeatures; ++i) {
//			all[m + i] = model.prod(lambda, model.square(w[i]));
//		}
//
//		for (int i = 0; i < m; ++i) {
//			all[i] = model.prod(1.0, s[i]);
//		}
//
//		// Add constraints
//
//		IloNumExpr[] inter = new IloNumExpr[numFeatures + 1];
//
//		for (int i = 0; i < m; ++i) {
//			// s_i + w dot x_i >= 0
//			for (int j = 0; j < numFeatures; ++j) {
//				inter[j] = model.prod(x[i][j], w[j]);
//			}
//			inter[numFeatures] = s[i];
//			model.addGe(model.sum(inter), 0.0);
//
//			// s_i / Q_i(alpha, b_i) - w .* x_i + b_i^1 >= 0
//			for (int j = 0; j < numFeatures; ++j) {
//				inter[j] = model.prod(-1.0, model.prod(x[i][j], w[j]));
//			}
//			double Q = ((1.0 - alpha) * b1[i] + alpha * b2[i])
//					/ (alpha * (b1[i] - b2[i]));
//			inter[numFeatures] = model.sum(model.prod(s[i], 1.0 / Q), b1[i]);
//			model.addGe(model.sum(inter), 0.0);
//		}
//
//		IloNumExpr Q = model.sum(all);
//		model.add(model.minimize(Q));
//
//		return lpMatrix;
//	}

}
