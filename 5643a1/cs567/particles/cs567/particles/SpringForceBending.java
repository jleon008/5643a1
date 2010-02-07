package cs567.particles;

import javax.vecmath.*;
import javax.media.opengl.*;

/** 
 * Bending spring force between three particles. 
 * 
 * @author Doug James, January 2007
 */
public class SpringForceBending implements Force
{
    Particle p0;
    Particle p1;
    Particle p2;
    ParticleSystem PS;

    /** 
     * Constructs a bending force affecting the angle implied by the
     * three-particle chain, p0-p1-p2.  
     */
    SpringForceBending(Particle p0, Particle p1, Particle p2, ParticleSystem PS)
    {
	if(p0==null || p1==null || p2==null) 
	    throw new NullPointerException("p0="+p0+", p1="+p1+", p2="+p2);

	this.p0 = p0;
	this.p1 = p1;
	this.p2 = p2;
	this.PS = PS;
    }

    public void applyForce()
    {
	/// TODO: Accumulate spring and damper(optional) forces into
	/// p0.f, p1.f, p2.f (Nonzero bend angles are an optional feat):
    	
    	Vector3d a = new Vector3d();
    	a.sub(p1.x,p0.x);
    	Vector3d b = new Vector3d();
    	b.sub(p2.x,p1.x);
    	
    	double f2scale = Constants.STIFFNESS_BEND / (2 * b.length());
    	double f0scale = Constants.STIFFNESS_BEND / (2 * a.length());
    	
    	a.normalize();
    	b.normalize();
    	
    	Vector3d f0 = new Vector3d(a);
    	f0.scale(a.dot(b));
    	f0.sub(b);
    	f0.scale(f0scale);
    	
    	Vector3d f2 = new Vector3d();
    	f2.scale(a.dot(b));
    	f2.sub(a,f2);
    	f2.scale(f2scale);
    	
    	Vector3d f1 = new Vector3d(f0);
    	f1.negate();
    	f1.sub(f2);
    	
    	p0.f.add(f0);
    	p1.f.add(f1);
    	p2.f.add(f2);
    	
    }

    public void display(GL gl)
    {
	/// OPTIONAL: DRAW A SEMI-CIRCLE OR SOMETHING TO SHOW BENDING FORCES AT WORK

    }

    public ParticleSystem getParticleSystem() { return PS; }
}
