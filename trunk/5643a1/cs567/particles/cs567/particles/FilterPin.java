package cs567.particles;

import javax.vecmath.Vector3d;

public class FilterPin extends Filter {

	private Particle particle;
	
	public FilterPin(Particle p) {
		particle = p;		
	}
	
	public void applyFilter() {
 		particle.x.set(particle.x0);
		particle.v = new Vector3d(0,0,0);
	}

}
