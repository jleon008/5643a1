/**
 * 
 */
package cs567.particles;

import java.util.*;

/**
 * @author Kerran
 * 
 */
public interface DynamicalSystem {

	void addFilter(Filter f);
	
	void removeFilter(Filter f);
	
	void applyFilters();
	
	void addForce(Force f);

	void removeForce(Force f);

	void updateForces();

	void derivEval();

	Collection<Force> getForces();

	Collection<Particle> getParticles();

	double getTime();

	void setTime(double time);
}
