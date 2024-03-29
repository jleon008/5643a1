package cs567.particles;

import javax.vecmath.*;
import javax.media.opengl.*;

/**
 * Spring force between one particle and a proxy point.
 * 
 * @author Doug James, January 2007
 */
public class SpringForce1Particle implements Force {
	Particle p1;
	Point3d x2;
	ParticleSystem PS;

	static final double REST_DISTANCE = 0;

	SpringForce1Particle(Particle p1, Point3d x2, ParticleSystem PS) {
		if (p1 == null || x2 == null)
			throw new NullPointerException("p1=" + p1 + ", x2=" + x2);

		this.p1 = p1;
		this.x2 = x2;
		this.PS = PS;
	}

	public void updatePoint(Point3d x) {
		x2.set(x);
	}

	public void applyForce() {
		// / TODO: Accumulate spring/damper forces into p1.f ...
		double length = x2.distance(p1.x);
		Vector3d lengthV = new Vector3d();
		lengthV.sub(p1.x, x2);
		double f = -(Constants.STIFFNESS_STRETCH * (length - REST_DISTANCE) + Constants.SPRING_DAMPING
				* p1.v.dot(lengthV));
		lengthV.scale(f / length);
		p1.f.add(lengthV);
	}

	public void display(GL gl) {
		// / DRAW A LINE:
		gl.glColor3f(0, 1, 0);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(p1.x.x, p1.x.y, p1.x.z);
		gl.glVertex3d(x2.x, x2.y, x2.z);
		gl.glEnd();
	}

	public ParticleSystem getParticleSystem() {
		return PS;
	}
}
