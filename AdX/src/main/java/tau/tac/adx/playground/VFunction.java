package tau.tac.adx.playground;

public class VFunction {
	Boundary boundary;
	Points points;
	
	public static double MICRO = 0.01;

	public VFunction(Boundary boundary, Points points) {
		this.boundary = boundary;
		this.points = points;
	}
	
	public double calc(double reserve) {
        if (reserve <= this.boundary.low)
            return - this.points.a1;
        if (this.boundary.low < reserve && reserve <= this.boundary.high)
            return - this.points.a2 * reserve;
        if (this.boundary.high < reserve && reserve < (1 + MICRO) * this.boundary.high)
            return this.points.a3 * reserve - this.points.a4;
        return 0;
	}

}
