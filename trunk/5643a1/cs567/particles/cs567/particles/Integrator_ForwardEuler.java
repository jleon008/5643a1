package cs567.particles;

public class Integrator_ForwardEuler implements Integrator {

	public void advanceTime(double timestep, DynamicalSystem sys) {
		Iterable<Particle> P = sys.getParticles();

		for (Particle p : P) {
			p.xOld.set(p.x);
			p.x.scaleAdd(timestep, p.v, p.x); // p.x += timestep * p.v;
			p.v.scaleAdd(timestep/p.m, p.f, p.v); // p.v += timestep * p.f;

			// / APPLY PIN CONSTRAINTS (set p=p0, and zero out v):
		}
		sys.applyFilters();

	}

}
