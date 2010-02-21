package cs567.particles;

/**
 * Default constants.
 * 
 * @author Doug James, January 2007
 */
public interface Constants {
	/** Restitution coefficient on [0,1]. */
	public static final double RESTITUTION_COEFF = 0.3;

	/** Mass-proportional damping. */
	public static final double DAMPING_MASS = 2.0;

	public static final double SPRING_DAMPING = 5.0;

	/** Mass of a particle. */
	public static final double PARTICLE_MASS = 1.0;

	/** Spring stretching stiffness. */
	public static final double STIFFNESS_STRETCH = 600000.0;

	/** Spring bending stiffness. */
	public static final double STIFFNESS_BEND = 500.0;

	public static final double GRAVITY = 10.0;
}
