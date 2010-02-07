/**
 * 
 */
package cs567.particles;

/**
 * @author Kerran
 *
 */
public interface DynamicalSystem {
	
	void addForce(Force f);
	void removeForce(Force f);
	void updateForces();
	
	void derivEval();
	
	Iterable<Force> getForces();
	Iterable<Particle> getParticles();
	
	double getTime();
	void setTime(double time);
}
