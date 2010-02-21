package cs567.particles;

import javax.media.opengl.GL;
import javax.vecmath.Vector3d;

public class GravitationalForce implements Force {

	private ParticleSystem PS;

	public GravitationalForce(ParticleSystem pS) {
		PS = pS;
	}

	public void applyForce() {
		Vector3d g = new Vector3d(0, -Constants.GRAVITY, 0);
		for (Particle p : PS.P) {
			p.f.scaleAdd(p.m,g, p.f);
		}

	}

	public void display(GL gl) {
		// TODO Auto-generated method stub

	}

	public ParticleSystem getParticleSystem() {
		return PS;
	}
}
