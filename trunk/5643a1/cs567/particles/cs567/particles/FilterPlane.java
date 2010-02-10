package cs567.particles;

import java.util.*;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

public class FilterPlane extends Filter {

	private Tuple3d reference;
	private Vector3d normal;
	private Collection<Particle> targets;
	
	public FilterPlane(Tuple3d r, Vector3d n,Collection<Particle> t) {
		reference = r;
		normal = n;
		targets = t;
		normal.normalize();
	}
	
	@Override
	public void applyFilter() {
		Vector3d tester = new Vector3d();
		for (Particle p : targets) {
			tester.sub(p.x, reference);
			double over = tester.dot(normal);
			if (over <= 0) {
				p.x.scaleAdd(-over, normal, p.x);
				tester.sub(p.x, reference);
				tester.scaleAdd(1, p.v, tester);
				if (tester.dot(normal) < over) {
					tester.set(normal);
					tester.scale(tester.dot(p.v));
					p.v.scaleAdd(-1 - Constants.RESTITUTION_COEFF, tester, p.v);
				}
			}
		}
	}

}
