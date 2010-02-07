package cs567.particles;

import javax.media.opengl.*;

/**
 * Particle system force.
 * 
 * @author Doug James, January 2007
 */
public interface Force {
	/**
	 * Causes force to be applied to affected particles.
	 */
	public void applyForce();

	/** Display any instructive force information, e.g., connecting spring. */
	public void display(GL gl);

	/** Reference to the ParticleSystem this force affects. */
	public ParticleSystem getParticleSystem();
}
