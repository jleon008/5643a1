package cs567.particles;

import java.util.*;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

public class FilterPlane extends Filter {

	private Tuple3d reference;
	private Vector3d normal, height, width;
	private Collection<Particle> targets;
	private boolean bounded = false;
	
	public FilterPlane(Tuple3d r, Vector3d n,Collection<Particle> t) {
		reference = r;
		normal = n;
		targets = t;
		normal.normalize();
	}
	
	public FilterPlane(Tuple3d r, Vector3d n,Collection<Particle> t, Vector3d h, Vector3d w) {
		reference = r;
		normal = n;
		targets = t;
		normal.normalize();
		bounded = true;
		height = h;
		width = w;
	}
	
	@Override
	public void applyFilter() {
		Vector3d tester = new Vector3d();
		for (Particle p : targets) {
			tester.sub(p.x, reference);
			double over = tester.dot(normal);
			if (over <= Particle.PARTICLE_RADIUS) {
				p.x.scaleAdd(-(over - Particle.PARTICLE_RADIUS), normal, p.x);
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
