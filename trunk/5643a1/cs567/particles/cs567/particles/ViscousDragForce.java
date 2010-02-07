package cs567.particles;

import javax.media.opengl.GL;
import javax.vecmath.Vector3d;

public class ViscousDragForce implements Force {

	private ParticleSystem PS;

	public ViscousDragForce(ParticleSystem pS) {
		PS = pS;
	}

	public void applyForce() {
		Vector3d vdf = new Vector3d();
		for (Particle p : PS.P) {
			vdf.set(p.v);
			vdf.scale(-Constants.DAMPING_MASS);
			p.f.add(vdf);
		}

	}

	public void display(GL gl) {
		// TODO Auto-generated method stub

	}

	public ParticleSystem getParticleSystem() {
		return PS;
	}

}
