package cs567.particles;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.vecmath.*;
import javax.media.opengl.*;

/**
 * Maintains dynamic lists of Particle and Force objects, and provides access to their state for numerical integration
 * of dynamics.
 * 
 * @author Doug James, January 2007
 */
public class ParticleSystem implements DynamicalSystem // implements Serializable
{

	static double INTERACTION_RADIUS = .03;
	static int size = (int) Math.ceil(1.0 / INTERACTION_RADIUS);
	// HashSet[][][] particleGrid = new
	// HashSet[(int)Math.ceil(1.0/INTERACTION_RADIUS)][(int)Math.ceil(1.0/INTERACTION_RADIUS)][(int)Math.ceil(1.0/INTERACTION_RADIUS)];
	HashSet[] particleGrid = new HashSet[size * size * size];

	/** Current simulation time. */
	double time = 0;

	private Set<Filter> filters = new HashSet<Filter>();

	/** List of Particle objects. */
	ArrayList<Particle> P = new ArrayList<Particle>();

	public ArrayList<Particle> Goo = new ArrayList<Particle>();
	public ArrayList<Particle> Paper = new ArrayList<Particle>();

	/** List of Force objects. */
	ArrayList<Force> F = new ArrayList<Force>();

	// ArrayList<Constraint> C = new ArrayList<Constraint>();

	ArrayList<ForceRunner> forceRunners = new ArrayList<ForceRunner>();
	// ArrayList<Thread> forceThreads = new ArrayList<Thread>();
	final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
	ExecutorService executor = Executors.newCachedThreadPool();

	/** Basic constructor. */
	public ParticleSystem() {

		for (int i = 0; i < THREAD_COUNT; i++) {
			ForceRunner fr = new ForceRunner();
			forceRunners.add(fr);
			// Thread t = new Thread(fr);
			// forceThreads.add(t);
		}

		int tot = (int) Math.ceil(1.0 / INTERACTION_RADIUS);
		for (int i = 0; i < tot; i++) {
			for (int j = 0; j < tot; j++) {
				for (int k = 0; k < tot; k++) {
					particleGrid[i + j * size + k * size * size] = new HashSet();
				}
			}
		}

	}

	/** Adds a force object (until removed) */
	public synchronized void addForce(Force f) {
		F.add(f);
	}

	/**
	 * Useful for removing temporary forces, such as user-interaction spring forces.
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

	public synchronized Particle createGooParticle(Point3d p0) {
		Particle newP = new GooParticle(p0);
		P.add(newP);
		Goo.add(newP);
		return newP;
	}

	public synchronized Particle createPaperParticle(Point3d p0) {
		Particle newP = new PaperParticle(p0);
		P.add(newP);
		Paper.add(newP);
		return newP;
	}

	/**
	 * Helper-function that computes nearest particle to the specified (deformed) position.
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
	 * Moves all particles to undeformed/materials positions, and sets all velocities to zero. Synchronized to avoid
	 * problems with simultaneous calls to advanceTime().
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
	 * Incomplete/Debugging implementation of Forward-Euler step. WARNING: Contains buggy debugging forces.
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

		for (Filter f : filters) {
			f.display(gl);
		}

		for (Particle particle : P) {
			particle.display(gl);
		}
	}

	// @Override
	public void derivEval() {
		// TODO Auto-generated method stub

	}

	// @Override
	public Collection<Force> getForces() {

		return F;
	}

	// @Override
	public Collection<Particle> getParticles() {

		return P;
	}

	// @Override
	public double getTime() {

		return time;
	}

	// @Override
	public void setTime(double time) {
		this.time = time;
		;
	}

	// @Override
	public void updateForces() {
		// TODO Auto-generated method stub
		// / Clear force accumulators:

		for (Particle p : P) {
			p.f.set(0, 0, 0);
		}

		if (Constants.PARTICLE_PARTICLE_ON) {
			// for (int i = 0; i < tot; i++) {
			// for (int j = 0; j < tot; j++) {
			// for (int k = 0; k < 1; k++) {
			// particleGrid[i][j][k].clear();
			// }
			// }
			// }

			int tot = (int) Math.ceil(1.0 / ParticleSystem.INTERACTION_RADIUS);
			// assign particles to particle grid
			for (Particle p : P) {
				int gx = Math.min(Math.max((int) (p.x.x / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
				int gy = Math.min(Math.max((int) (p.x.y / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
				int gz = Math.min(Math.max((int) (p.x.z / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
				int ogx = Math.min(Math.max((int) (p.xOld.x / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
				int ogy = Math.min(Math.max((int) (p.xOld.y / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
				int ogz = Math.min(Math.max((int) (p.xOld.z / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
				if (gz != ogz || gy != ogy || gx != ogx) {
					particleGrid[ogx + ParticleSystem.size * ogy + ParticleSystem.size * ParticleSystem.size * ogz]
							.remove(p);
					particleGrid[gx + ParticleSystem.size * gy + ParticleSystem.size * ParticleSystem.size * gz].add(p);
				}
			}
		}

		boolean THREADS_ON = true;
		if (THREADS_ON) {
			for (int i = 0; i < THREAD_COUNT; i++) {
				ForceRunner fr = forceRunners.get(i);

				// assign particles
				{
					int chunkSize = P.size() / THREAD_COUNT;
					int fromIndex = i * chunkSize;
					int toIndex = fromIndex + chunkSize;

					// catch rounding error and off by one error on last chunk
					if (P.size() - toIndex < chunkSize) {
						toIndex = P.size();
					}
					fr.P = this.P.subList(fromIndex, toIndex);
					fr.particleGrid = this.particleGrid;
				}

				// assign forces
				{
					int chunkSize = F.size() / THREAD_COUNT;
					int fromIndex = i * chunkSize;
					int toIndex = fromIndex + chunkSize;

					// catch rounding error and off by one error on last chunk
					if (F.size() - toIndex < chunkSize) {
						toIndex = F.size() - 1;
					}
					fr.F = this.F.subList(fromIndex, toIndex);
				}

				// run it
				// forceThreads.get(i).start();
				// executor.execute(fr);
				// executor.submit(fr);
			}

			List<Future<Object>> status = null;

			try {
				status = executor.invokeAll(forceRunners);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			for (Future<Object> f : status) {

				try {
					Object o = f.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		} else {

			if (Constants.PARTICLE_PARTICLE_ON) {

				// keep this consistent with particle system TODO refactor it

				int tot = (int) Math.ceil(1.0 / ParticleSystem.INTERACTION_RADIUS);

				// apply interaation forces
				for (int pi = 0; pi < P.size(); pi++) {
					Particle p = P.get(pi);
					int gx = Math.min(Math.max((int) (p.x.x / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
					int gy = Math.min(Math.max((int) (p.x.y / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
					int gz = Math.min(Math.max((int) (p.x.z / ParticleSystem.INTERACTION_RADIUS), 0), tot - 1);
					for (int i = Math.max(0, gx - 1); i < Math.min(gx + 2, tot); i++) {
						for (int j = Math.max(0, gy - 1); j < Math.min(gy + 2, tot); j++) {
							for (int k = Math.max(0, gz - 1); k < Math.min(gz + 2, tot); k++) {
								for (Object other : particleGrid[i + ParticleSystem.size * j + ParticleSystem.size
										* ParticleSystem.size * k]) {
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
