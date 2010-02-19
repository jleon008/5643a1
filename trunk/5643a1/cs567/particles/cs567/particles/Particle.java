package cs567.particles;

import javax.vecmath.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

/**
 * Simple particle implementation, with miscellaneous adornments.
 * 
 * @author Doug James, January 2007
 */
public class Particle {
	/** Radius of particle's circle graphic. */
	public static final double PARTICLE_RADIUS = 0.015;

	/** Display list index. */
	private static int PARTICLE_DISPLAY_LIST = -1;

	/** Highlighted appearance if true, otherwise white. */
	private boolean highlight = false;

	/** If true, then particle is pinned in space. */
	private boolean pin = false;

	/** Default mass. */
	double m = Constants.PARTICLE_MASS;

	/** Deformed Position. */
	Point3d x = new Point3d();

	Point3d xOld = new Point3d();
	
	/** Undeformed/material Position. */
	Point3d x0 = new Point3d();

	/** Velocity. */
	Vector3d v = new Vector3d();

	/** Force accumulator. */
	Vector3d f = new Vector3d();

	/**
	 * Constructs particle with the specified material/undeformed coordinate,
	 * p0.
	 */
	Particle(Point3d x0) {
		this.x0.set(x0);
		xOld.set(x0);
		x.set(x0);
	}

	/** Draws circular particle using a display list. */
	public void display(GL gl) {
		if (PARTICLE_DISPLAY_LIST < 0) {// MAKE DISPLAY LIST:
			int displayListIndex = gl.glGenLists(1);
			gl.glNewList(displayListIndex, GL.GL_COMPILE);
			drawParticle(gl, new Point3d());// /particle at origin
			gl.glEndList();
			System.out.println("MADE LIST " + displayListIndex + " : "
					+ gl.glIsList(displayListIndex));
			PARTICLE_DISPLAY_LIST = displayListIndex;
		}

		// / COLOR: DEFAULT WHITE; GREEN IF HIGHLIGHTED; ADD RED IF PINNED
		Color3f c = new Color3f(1, 1, 1);// default: white
		if (pin) {
			c.x = 1f;// add red
			c.y *= 0.2f;
			c.z = 0;
		}
		if (highlight) {
			c.y = 1;
			c.z = 0;
		}

		gl.glColor3f(c.x, c.y, c.z);

		// / DRAW ORIGIN-CIRCLE TRANSLATED TO "p":
		gl.glPushMatrix();
		gl.glTranslated(x.x, x.y, x.z);
		gl.glCallList(PARTICLE_DISPLAY_LIST);
		gl.glPopMatrix();
	}

	/** Specifies whether particle should be drawn highlighted. */
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	/** True if particle should be drawn highlighted. */
	public boolean getHighlight() {
		return highlight;
	}

	/**
	 * Specifies whether or not this particle is fixed in space via a pin
	 * constraint. (Should probably be elsewhere in a generic constraint list).
	 */
	public void setPin(boolean fix) {
		pin = fix;
	}

	/** Returns true if currently pinned. */
	public boolean isPinned() {
		return pin;
	}

	/**
	 * Draws a canonical circular particle.
	 */
	private static void drawParticle(GL gl, Point3d p) {
		double radius = PARTICLE_RADIUS;

		double vectorY1 = p.y;
		double vectorX1 = p.x;
		double vectorZ1 = p.z;
		
		GLU glu = new GLU();
		GLUquadric quadratic = glu.gluNewQuadric();
		glu.gluQuadricNormals(quadratic, GLU.GLU_SMOOTH);
		glu.gluQuadricTexture(quadratic, true);
		
		gl.glPushMatrix();
		gl.glTranslated(p.x, p.y, p.z);
		glu.gluSphere(quadratic, radius, 12, 12);
		gl.glPopMatrix();

//		gl.glBegin(GL.GL_TRIANGLES);
//		int N = 45;
//		for (int i = 0; i <= N; i++) {
//			double angle = ((double) i) * 2. * Math.PI / (double) N;
//			double vectorX = p.x + radius * Math.sin(angle);
//			double vectorY = p.y + radius * Math.cos(angle);
//			double vectorZ = p.z + 0; // TODO: 3d-ify
//			gl.glVertex3d(p.x, p.y, p.z);
//			gl.glVertex3d(vectorX1, vectorY1, vectorZ1);
//			gl.glVertex3d(vectorX, vectorY, vectorZ);
//			vectorY1 = vectorY;
//			vectorX1 = vectorX;
//			vectorZ1 = vectorZ;
//		}
		gl.glEnd();
	}

	public void interactionForce(Particle other) {
		// ok, now for the fun stuff...
		Vector3d posDif = new Vector3d();
		posDif.sub(x, other.x);
		
		Vector3d velDif = new Vector3d();
		velDif.sub(v, other.v);
		
		double d = posDif.length();
		double dSquared =d*d;
		
		int m = 6;
		int n = 5;
		double r0= 2*PARTICLE_RADIUS;
		double cr = r0;
		double cd = r0;
		double b1 = 1;
		double b2 = b1; //*Math.pow(r0, n-m);
		double sumR = 2*PARTICLE_RADIUS;
		double sr = 250;
		double sd = 70;
		
		/*sr = dSquared/(cr*cr*(sumR)*(sumR));
		sd = dSquared/(cd*cd*(sumR)*(sumR));
		
		sr = Math.max(0, 1 - sr);
		sd = Math.max(0, 1 - sd);*/
		
		Vector3d f = new Vector3d();
		f.set(posDif);
		f.normalize();
		
		double sf = -sr*(b1/Math.pow(d/r0, m) - b2/Math.pow(d/r0, n)) + sd*(velDif.dot(f)/(d/r0));
		f.scale(sf);
		other.f.add(f);
		
	}

}
