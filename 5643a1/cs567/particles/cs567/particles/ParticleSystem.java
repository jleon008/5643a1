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
public class ParticleSystem implements DynamicalSystem // implements Serializable
{
	/** Current simulation time. */
	double time = 0;

	/** List of Particle objects. */
	ArrayList<Particle> P = new ArrayList<Particle>();

	/** List of Force objects. */
	ArrayList<Force> F = new ArrayList<Force>();

	// ArrayList<Constraint> C = new ArrayList<Constraint>();

	/** Basic constructor. */
	public ParticleSystem() {
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
	public synchronized void advanceTime(double dt) {
		// / Clear force accumulators:
		for (Particle p : P)
			p.f.set(0, 0, 0);

		{// / Gather forces: (TODO)
			for (Force force : F) {
				force.applyForce();
			}
		}

		// / TIME-STEP: (Forward Euler for now):
		for (Particle p : P) {
			p.v.scaleAdd(dt, p.f, p.v); // p.v += dt * p.f;
			p.x.scaleAdd(dt, p.v, p.x); // p.x += dt * p.v;

			// / APPLY PIN CONSTRAINTS (set p=p0, and zero out v):
			if (p.isPinned()) {
				p.v.set(0, 0, 0);
				p.x.set(p.x0);
			}
		}

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

	@Override
	public void derivEval() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterable<Force> getForces() {
		
		return F;
	}

	@Override
	public Iterable<Particle> getParticles() {
		
		return P;
	}

	@Override
	public double getTime() {
		
		return time;
	}

	@Override
	public void setTime(double time) {
		this.time = time;;
	}

	@Override
	public void updateForces() {
		// TODO Auto-generated method stub
	}

}
