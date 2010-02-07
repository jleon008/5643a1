package cs567.particles;

public interface Integrator {

	public void advanceTime(double timestep, DynamicalSystem sys);
	
}
