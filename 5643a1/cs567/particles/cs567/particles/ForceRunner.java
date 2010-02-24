package cs567.particles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

public class ForceRunner implements Callable<Object> {

	List<Particle> P;
	
	HashSet[] particleGrid;

	public List<Force> F;
	
	@Override
	public Object call() {
		// TODO Auto-generated method stub
		InterParticleForces();
		//System.out.println(Runtime.getRuntime().availableProcessors());;
		return 0;

	}
	
	private void InterParticleForces() {
		if (Constants.PARTICLE_PARTICLE_ON) {
			
			int tot = (int) Math.ceil(1.0 / ParticleSystem.INTERACTION_RADIUS);


			//apply interaation forces
			for (int pi = 0; pi< P.size(); pi++) {
				Particle p = P.get(pi);
				int gx = Math.min(Math.max((int) (p.x.x / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
				int gy = Math.min(Math.max((int) (p.x.y / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
				int gz = Math.min(Math.max((int) (p.x.z / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
				for (int i = Math.max(0, gx - 1); i < Math.min(gx + 2, tot); i++) {
					for (int j = Math.max(0, gy - 1); j < Math.min(gy + 2, tot); j++) {
						for (int k = Math.max(0, gz - 1); k < Math.min(gz + 2, tot); k++) {
							for (Object other : particleGrid[i + ParticleSystem.size * j + ParticleSystem.size * ParticleSystem.size * k]) {
								if (!other.equals(p)) {
									p.interactionForce((Particle) other);
								}
							}
						}
					}
				}
			}
		}
		
		// apply all forces
		for (int i = 0; i < F.size(); i++) {
			Force force = F.get(i);
			force.applyForce();
		}
		
	}

}
