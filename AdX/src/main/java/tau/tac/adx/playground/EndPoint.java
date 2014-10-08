package tau.tac.adx.playground;

public class EndPoint {
	double val;
	FunctionType point_type;
	VFunction function;

	public EndPoint(double val, FunctionType point_type, VFunction function) {
		this.val = val;
		this.point_type = point_type;
		this.function = function;
	}
}
