package cs567.particles;

public class Integrator_SymplecticEuler implements Integrator {

	public void advanceTime(double timestep, DynamicalSystem sys) {
		// TODO Auto-generated method stub

		Iterable<Particle> P = sys.getParticles();

		for (Particle p : P) {
			// velocity = accel * dt
			p.v.scaleAdd(timestep, p.f, p.v); // p.v += timestep * p.f;

			// / APPLY PIN CONSTRAINTS (zero out v):

			p.x.scaleAdd(timestep, p.v, p.x); // p.x += timestep * p.v;

			// / APPLY PIN CONSTRAINTS (set p=p0):

		}
		
		sys.applyFilters();
		
	}

}
