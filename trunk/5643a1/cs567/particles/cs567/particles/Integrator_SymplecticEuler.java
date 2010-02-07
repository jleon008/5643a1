package cs567.particles;

public class Integrator_SymplecticEuler implements Integrator {

	public void advanceTime(double timestep, DynamicalSystem sys) {
		// TODO Auto-generated method stub

		Iterable<Particle> P = sys.getParticles();

		for (Particle p : P) {
			// velocity = accel * dt
			p.v.scaleAdd(timestep, p.f, p.v); // p.v += timestep * p.f;

			// / APPLY PIN CONSTRAINTS (zero out v):
			if (p.isPinned()) {
				p.v.set(0, 0, 0);
			}

			p.x.scaleAdd(timestep, p.v, p.x); // p.x += timestep * p.v;

			// / APPLY PIN CONSTRAINTS (set p=p0):
			if (p.isPinned()) {
				p.x.set(p.x0);
			}
		}
	}

}
