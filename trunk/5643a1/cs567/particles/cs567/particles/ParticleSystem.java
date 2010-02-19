package cs567.particles;

import java.util.*;
import javax.vecmath.*;
import javax.media.opengl.*;

/**
 * Maintains dynamic lists of Particle and Force objects, and provides access to
 * their state for numerical integration of dynamics.
 * 
 * @author Doug James, January 2007
 */
public class ParticleSystem implements DynamicalSystem // implements
														// Serializable
{

	private static double INTERACTION_RADIUS = 1;
	
	HashSet[][][] particleGrid = new HashSet[(int)Math.ceil(1.0/INTERACTION_RADIUS)][(int)Math.ceil(1.0/INTERACTION_RADIUS)][(int)Math.ceil(1.0/INTERACTION_RADIUS)];
	
	/** Current simulation time. */
	double time = 0;

	
	
	private Set<Filter> filters = new HashSet<Filter>();
	
	/** List of Particle objects. */
	ArrayList<Particle> P = new ArrayList<Particle>();

	
	/** List of Force objects. */
	ArrayList<Force> F = new ArrayList<Force>();

	// ArrayList<Constraint> C = new ArrayList<Constraint>();

	/** Basic constructor. */
	public ParticleSystem() {
		
		int tot = (int)Math.ceil(1.0/INTERACTION_RADIUS);
		for (int i = 0; i < tot; i++) {
			for (int j = 0; j < tot; j++) {
				for (int k = 0; k < tot; k++) {
					particleGrid[i][j][k] = new HashSet();
				}
			}
		}
		
	}

	/** Adds a force object (until removed) */
	public synchronized void addForce(Force f) {
		F.add(f);
	}

	/**
	 * Useful for removing temporary forces, such as user-interaction spring
	 * forces.
	 */
	public synchronized void removeForce(Force f) {
		F.remove(f);
	}

	/**
	 * Creates particle and adds it to the particle system.
	 * 
	 * @param p0
	 *            Undeformed/material position.
	 * @return Reference to new Particle.
	 */
	public synchronized Particle createParticle(Point3d p0) {
		Particle newP = new Particle(p0);
		P.add(newP);
		return newP;
	}

	/**
	 * Helper-function that computes nearest particle to the specified
	 * (deformed) position.
	 * 
	 * @return Nearest particle, or null if no particles.
	 */
	public synchronized Particle getNearestParticle(Point3d x) {
		Particle minP = null;
		double minDistSq = Double.MAX_VALUE;
		for (Particle particle : P) {
			double distSq = x.distanceSquared(particle.x);
			if (distSq < minDistSq) {
				minDistSq = distSq;
				minP = particle;
			}
		}
		return minP;
	}

	/**
	 * Moves all particles to undeformed/materials positions, and sets all
	 * velocities to zero. Synchronized to avoid problems with simultaneous
	 * calls to advanceTime().
	 */
	public synchronized void reset() {
		for (Particle p : P) {
			p.x.set(p.x0);
			p.v.set(0, 0, 0);
			p.f.set(0, 0, 0);
			p.setHighlight(false);
		}
		time = 0;
	}

	/**
	 * Incomplete/Debugging implementation of Forward-Euler step. WARNING:
	 * Contains buggy debugging forces.
	 */
	public synchronized void advanceTime(double dt, Integrator i) {
		updateForces();

		// / TIME-STEP: (Forward Euler for now):
		i.advanceTime(dt, this);

		time += dt;
	}

	/**
	 * Displays Particle and Force objects.
	 */
	public synchronized void display(GL gl) {
		for (Force force : F) {
			force.display(gl);
		}

		for (Particle particle : P) {
			particle.display(gl);
		}
	}

//	@Override
	public void derivEval() {
		// TODO Auto-generated method stub

	}

//	@Override
	public Collection<Force> getForces() {

		return F;
	}

//	@Override
	public Collection<Particle> getParticles() {

		return P;
	}

//	@Override
	public double getTime() {

		return time;
	}

	//@Override
	public void setTime(double time) {
		this.time = time;
		;
	}

	//@Override
	public void updateForces() {
		// TODO Auto-generated method stub
		// / Clear force accumulators:
		
		int tot = (int)Math.ceil(1.0/INTERACTION_RADIUS);
		for (int i = 0; i < tot; i++) {
			for (int j = 0; j < tot; j++) {
				for (int k = 0; k < tot; k++) {
					particleGrid[i][j][k].clear();
				}
			}
		}
		
		for (Particle p : P) {
			p.f.set(0, 0, 0);
			int gx = Math.min(Math.max((int)(p.x.x/INTERACTION_RADIUS), 0), tot-1);
			int gy = Math.min(Math.max((int)(p.x.y/INTERACTION_RADIUS), 0), tot-1);
			int gz = Math.min(Math.max((int)(p.x.z/INTERACTION_RADIUS), 0), tot-1);
			particleGrid[gx][gy][gz].add(p);
		}
		
		for (Particle p : P) {
			int gx = Math.min(Math.max((int)(p.x.x/INTERACTION_RADIUS), 0), tot-1);
			int gy = Math.min(Math.max((int)(p.x.y/INTERACTION_RADIUS), 0), tot-1);
			int gz = Math.min(Math.max((int)(p.x.z/INTERACTION_RADIUS), 0), tot-1);
			for (int i = Math.max(0, gx-1); i < Math.min(gx + 2, tot); i++) {
				for (int j = Math.max(0, gy-1); j < Math.min(gy + 2, tot); j++) {
					for (int k = Math.max(0, gz-1); k < Math.min(gz + 2, tot); k++) {
						for (Object other : particleGrid[i][j][k]) {
							if (!other.equals(p)) {
								p.interactionForce((Particle) other);
							}
						}
					}
				}
			}
		}

		{// / Gather forces: (TODO)
			for (Force force : F) {
				force.applyForce();
			}
		}
	}

	public void addFilter(Filter f) {
		filters.add(f);
		
	}

	public void removeFilter(Filter f) {
		filters.remove(f);
	}

	
	public void applyFilters() {
		for (Filter f : filters) {
			f.applyFilter();	
		}
		
	}

}
