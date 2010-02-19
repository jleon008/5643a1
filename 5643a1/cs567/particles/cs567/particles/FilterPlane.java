package cs567.particles;

import java.util.*;

import javax.media.opengl.GL;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

public class FilterPlane extends Filter {

	private Tuple3d reference;
	private Vector3d normal, height, width;
	private Collection<Particle> targets;
	private boolean bounded = false;

	public FilterPlane(Tuple3d r, Vector3d n,Collection<Particle> t) {
		reference = r;
		normal = n;
		targets = t;
		normal.normalize();
	}

	public FilterPlane(Tuple3d r, Vector3d n,Collection<Particle> t, Vector3d h, Vector3d w) {
		reference = r;
		normal = n;
		targets = t;
		normal.normalize();
		bounded = true;
		h.scaleAdd(-h.dot(normal), normal, h);
		height = h;
		w.scaleAdd(-w.dot(normal), normal, w);
		w.scaleAdd(-w.dot(height)/h.length(), h, w);
		width = w;
	}

	@Override
	public void applyFilter() {
		Vector3d tester = new Vector3d();
		for (Particle p : targets) {
			tester.sub(p.x, reference);
			double over = tester.dot(normal);
			if (over <= Particle.PARTICLE_RADIUS) {
				if (bounded) {
					Vector3d bounder = new Vector3d();
					bounder.set(tester);
					if (Math.abs(bounder.dot(height)/height.length()) > height.length()) return;
					if (Math.abs(bounder.dot(width)/width.length()) > width.length()) return;
				}
				p.x.scaleAdd(-(over - Particle.PARTICLE_RADIUS), normal, p.x);
				tester.sub(p.x, reference);
				tester.scaleAdd(1, p.v, tester);
				if (tester.dot(normal) < over) {
					tester.set(normal);
					tester.scale(tester.dot(p.v));
					p.v.scaleAdd(-1 - Constants.RESTITUTION_COEFF, tester, p.v);
				}
			}
		}
	}

	@Override
	public void display(GL gl) {
		if (bounded) {
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glColor3f(1, 1, 1);
			Vector3d corner = new Vector3d();

			corner.scaleAdd(1, height, reference);
			corner.scaleAdd(1, width, corner);
			gl.glVertex3d(corner.x, corner.y, corner.z);

			corner.scaleAdd(-1, height, reference);
			corner.scaleAdd(1, width, corner);
			gl.glVertex3d(corner.x, corner.y, corner.z);

			corner.scaleAdd(-1, height, reference);
			corner.scaleAdd(-1, width, corner);
			gl.glVertex3d(corner.x, corner.y, corner.z);

			corner.scaleAdd(1, height, reference);
			corner.scaleAdd(-1, width, corner);
			gl.glVertex3d(corner.x, corner.y, corner.z);

			corner.scaleAdd(1, height, reference);
			corner.scaleAdd(1, width, corner);
			gl.glVertex3d(corner.x, corner.y, corner.z);

			gl.glEnd();
		}
	}

}
