package tau.tac.adx.playground;

import java.util.Arrays;
import java.util.Random;

public class ProblemInput {
	private static Random r = new Random(1);	// Used for generating x, w
	
	// Inputs
	public int num_bidders = 0;
	public int num_features = 0;
	public double gamma = 0.0;
	public double lambda = 0.0;
	public double Lambda = 0.0;
	
	// Computed
	public double lambda_sq = 0.0;	// lambda * lambda
	public double [][] features;	// Feature vector

	public double [] w;		// w
	public double w_dot_w_sq;		// w .* w
	public double sqrt_w_dot_w_sq;	// sqrt(w .* w)
	public double max_w;			// largest element in w
	public double min_w;			// smallest element in w

	public double [] b2;	// lower bid, w .* x / 2
	public double [] b1;	// higher bid, w .* x
	public double [][] b;	// bidder, (b2, b1)
	public double max_b1;
	public double min_b1;
	
	public double [] discontinuous_pts;	// set of b2, b1, b1 * (1 + gamma)
	public double [] L_at_discontinuous_pts;
	public double argmin_L;
	public double min_L;
	
	public ProblemInput(int num_bidders, int num_features, double gamma, double lambda, double Lambda) {
		this.num_bidders = num_bidders;
		this.num_features = num_features;
		this.gamma = gamma;
		this.lambda = lambda;
		this.lambda_sq = lambda * lambda;
		this.Lambda = Lambda;
		
		w = new double[num_features];
		features = new double[num_bidders][num_features];
		b2 = new double[num_bidders];
		b1 = new double[num_bidders];
		
		compute_w();
		compute_features();
		compute_bids();
		compute_discontinuous_pts();
		compute_L_at_discontinuous_pts();
	}
	
	public void compute_w() {
		max_w = -Double.MAX_VALUE;
		min_w = Double.MAX_VALUE;
		w_dot_w_sq = 0.0;
		for (int j = 0; j < w.length; ++j) {
			w[j] = r.nextDouble();
			//w[j] = 10.0*r.nextDouble();
			//w[j] = r.nextInt(2);
			//w[j] = 1.0;
			max_w = Math.max(max_w, w[j]);
			min_w = Math.min(min_w, w[j]);
			w_dot_w_sq += w[j] * w[j];
		}
		sqrt_w_dot_w_sq = Math.sqrt(w_dot_w_sq);
	}
	
	public void compute_features() {
		for (int i = 0; i < num_bidders; ++i) {
			for (int j = 0; j < num_features; ++j) {
				//features[i][j] = r.nextInt(2); // features are {0,1}
				features[i][j] = r.nextDouble(); // features are {0,1}
				//features[i][j] = 1.0;
			}
		}
	}
	
	public double [] b2_given_w(double [] given_w) {
		double [] b2 = new double [this.num_bidders];
		for (int i = 0; i < this.num_bidders; ++i) {
			double sum_wx = 0.0;
			for (int j = 0; j < this.num_features; ++j) {
				sum_wx += this.features[i][j] * given_w[j];
			}
			b2[i] = 0.5 * sum_wx;
		}
		return b2;
	}
	
	public double [] b1_given_w(double [] given_w) {
		double [] b1 = new double [this.num_bidders];
		for (int i = 0; i < this.num_bidders; ++i) {
			double sum_wx = 0.0;
			for (int j = 0; j < this.num_features; ++j) {
				sum_wx += this.features[i][j] * given_w[j];
			}
			b1[i] = sum_wx;
		}
		return b1;
	}
	
	public void compute_bids() {
		this.b2 = b2_given_w(this.w);
		this.b1 = b1_given_w(this.w);
		
		this.max_b1 = -Double.MAX_VALUE;
		this.min_b1 = Double.MAX_VALUE;
		for (int i = 0; i < this.num_bidders; ++i) {
			this.max_b1 = Math.max(max_b1, b1[i]);
			this.min_b1 = Math.min(min_b1, b1[i]);
		}
		
		this.b = new double [this.num_bidders][2];
		for (int i = 0; i < this.num_bidders; ++i) {
			this.b[i][0] = this.b2[i];
			this.b[i][1] = this.b1[i];
		}
	}	
	
	public void compute_discontinuous_pts() {
		this.discontinuous_pts = new double [this.num_bidders * 3 + 1];
		
		for (int i = 0; i < this.num_bidders; ++i) {
			this.discontinuous_pts[i                     ] = this.b2[i];
			this.discontinuous_pts[i +   this.num_bidders] = this.b1[i];
			this.discontinuous_pts[i + 2*this.num_bidders] = this.b1[i] * (1.0 + gamma);
		}
		this.discontinuous_pts[this.num_bidders * 3] = 0.0;
		Arrays.sort(this.discontinuous_pts);
	}
	
	public void compute_L_at_discontinuous_pts() {
		this.L_at_discontinuous_pts = Functions.L(this.discontinuous_pts, this.b2, this.b1, this.gamma);
		
		this.argmin_L = 0.0;
		this.min_L = Double.MAX_VALUE;
		for (int k = 0; k < this.L_at_discontinuous_pts.length; k++) {
			if (this.L_at_discontinuous_pts[k] < this.min_L) {
				this.min_L = this.L_at_discontinuous_pts[k];
				this.argmin_L = this.discontinuous_pts[k];
			}
		}
	}
	
	public void print_summary() {
		System.out.println("ProblemInput Summary");
		System.out.println("--------------------");
		
		System.out.println("num_bidders = " + this.num_bidders);
		System.out.println("num_features = " + this.num_features);
		System.out.println("gamma = " + this.gamma);
		System.out.println("lambda = " + this.lambda);
		
		System.out.println("max_b1 = " + this.max_b1);
		System.out.println("min_b1 = " + this.min_b1);
		
		System.out.println("max_w = " + this.max_w);
		System.out.println("min_w = " + this.min_w);
		System.out.println("w_dot_w_sq = " + this.w_dot_w_sq);
		System.out.println("sqrt_w_dot_w_sq = " + this.sqrt_w_dot_w_sq);
		
		boolean print_x = false;
		if (print_x) {
			System.out.println("x:");
			for (int j = 0; j < this.features.length; ++j)
				System.out.println(j + ": " + Arrays.toString(this.features[j]));
		}
		
		System.out.println("w: " + Arrays.toString(this.w));
		
		System.out.println("b2: " + Arrays.toString(this.b2));
		
		System.out.println("b1: " + Arrays.toString(this.b1));
		
		System.out.println("discontinuous_pts: " + Arrays.toString(this.discontinuous_pts));
		
		System.out.println("L_at_discontinuous_pts: " + Arrays.toString(this.L_at_discontinuous_pts));
		
		System.out.println("argmin_L: " + this.argmin_L);
		System.out.println("min_L: " + this.min_L);
		
		System.out.println("--------------------");
	}
	
	public void compare_w(double [] w2) {
		boolean print_each_array_elem = false;
		System.out.println("Compare euclidean norm of true w with recovered w (w2)");
		System.out.println("w euclidean norm = " + calculate_euclidean_norm(this.w));
		System.out.println("found w euclidean norm = " + calculate_euclidean_norm(w2));
		
		System.out.println("Compare true w with recovered w (w2)");
		double sum_delw = 0.0;
		for (int j = 0; j < this.num_features; ++j) {
			double delw = this.w[j] - w2[j];
			sum_delw += Math.abs(delw);
			if (print_each_array_elem) {
				System.out.println(
					"w[" + j + "] = " + this.w[j]
					+ "    w2[" + j + "] = " + w2[j]
					+ "    del = " + delw);
			}
		}
		System.out.println("Sum of absolute differences = " + sum_delw);
		
		System.out.println("Compare true b1 with recovered b1 (b1_2)");
		double [] b2_2 = new double [this.num_bidders];
		double [] b1_2 = new double [this.num_bidders];
		double sum_delb1 = 0.0;
		
		for (int i = 0; i < this.num_bidders; ++i) {
			b2_2[i] = 0.0;
			b1_2[i] = 0.0;
			for (int j = 0; j < this.num_features; ++j) {
				b1_2[i] += w2[j] * this.features[i][j];
			}
			b2_2[i] = b1_2[i] / 2.0;
			double delb = this.b1[i] - b1_2[i];
			sum_delb1 += Math.abs(delb);
			if (print_each_array_elem) {
				System.out.println(
					"b1[" + i + "] = " + this.b1[i]
					+ "    b1_2[" + i + "] = " + b1_2[i]
					+ "    del = " + delb);
			}
		}
		System.out.println("Sum of absolute differences = " + sum_delb1);
	}
	
	public double calculate_euclidean_norm(double [] w) {
		double euclidean_norm = 0.0;
		
		for (int j = 0; j < w.length; ++j) {
			euclidean_norm += w[j] * w[j];
		}
		euclidean_norm = Math.sqrt(euclidean_norm);
		
		return euclidean_norm;
	}
}
