package cs567.particles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3d;

public class Integrator_Midpoint implements Integrator {

	public void advanceTime(double timestep, DynamicalSystem sys) {
		// TODO Auto-generated method stub
		Iterable<Particle> P = sys.getParticles();

		Map<Particle, Vector3d> x_original = new HashMap<Particle, Vector3d>();
		Map<Particle, Vector3d> v_original = new HashMap<Particle, Vector3d>();

		for (Particle p : P) {
			// save originals
			x_original.put(p, new Vector3d(p.x));
			v_original.put(p, new Vector3d(p.v));
			p.xOld.set(p.x);
			// do half time step of forward euler
			p.x.scaleAdd(timestep / 2, p.v, p.x); // p.x += timestep * p.v;
			p.v.scaleAdd(timestep / (2*p.m), p.f, p.v); // p.v += timestep * p.f;

			// / APPLY PIN CONSTRAINTS (set p=p0, and zero out v):
		}

		sys.applyFilters();
		sys.updateForces();

		P = sys.getParticles();

		for (Particle p : P) {
			p.xOld.set(p.x);
			p.x.scaleAdd(timestep, p.v, x_original.get(p));
			p.v.scaleAdd(timestep / p.m, p.f, v_original.get(p));

			// / APPLY PIN CONSTRAINTS (set p=p0, and zero out v)
		}
		sys.applyFilters();

	}

}
