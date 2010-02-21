package cs567.particles;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class PaperParticle extends Particle{

	
	PaperParticle(Point3d x0) {
		super(x0);
		radius = .015;
		m = .1;
	}
	
	@Override
	public void interactionForce(Particle other) {
		// ok, now for the fun stuff...
		Vector3d posDif = new Vector3d();
		posDif.sub(x, other.x);
		
		Vector3d velDif = new Vector3d();
		velDif.sub(v, other.v);
		
		double d = posDif.length();
		double dSquared =d*d;
		Vector3d f = new Vector3d();
		
		if (other instanceof PaperParticle) {
	/*		int m = 6;
			int n = 5;
			double r0= this.radius + other.radius;
			double cr = r0;
			double cd = r0;
			double b1 = 1;
			double b2 = b1; //*Math.pow(r0, n-m);
			double sumR = r0;
			double sr = 50;
			double sd = 10;
			
			
			f.set(posDif);
			f.normalize();
			
			double sf = -sr*(b1/Math.pow(d/r0, m) - b2/Math.pow(d/r0, n)) + sd*(velDif.dot(f)/(d/r0));
			f.scale(sf); */
		} else {
			int m = 8;
			int n = 5;
			double r0= this.radius + other.radius;
			double cr = r0;
			double cd = r0;
			double b1 = 1;
			double b2 = b1; //*Math.pow(r0, n-m);
			double sumR = r0;
			double sr = 20;
			double sd = 10;
			
			/*sr = dSquared/(cr*cr*(sumR)*(sumR));
			sd = dSquared/(cd*cd*(sumR)*(sumR));
			
			sr = Math.max(0, 1 - sr);
			sd = Math.max(0, 1 - sd);*/
			
			f.set(posDif);
			f.normalize();
			
			double sf = -sr*(b1/Math.pow(d/r0, m) - b2/Math.pow(d/r0, n)) + sd*(velDif.dot(f)/(d/r0));
			f.scale(sf);
		}
		
		other.f.add(f);
		
	}
	

}
