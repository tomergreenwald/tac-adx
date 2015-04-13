package tau.tac.adx.playground;

//static double MICRO = 0.01

public class C {
	double c1, c2, c3, c4, s, point;

	public C(double c1, double c2, double c3, double c4, double point) {
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		this.c4 = c4;
		this.point = point;
		update();
	}

	public void update() {
		this.s = this.c1 + this.c2 * this.point + this.c3 * this.point
				+ this.c4;
	}
}
//
// def calc(self, reserve):
// if reserve <= this.boundary.low:
// return - this.points.a1
// if this.boundary.low < reserve <= this.boundary.high:
// return - this.points.a2 * reserve
// if this.boundary.high < reserve < (1 + MICRO) * this.boundary.high:
// return this.points.a3 * reserve - this.points.a4
// return 0
//

