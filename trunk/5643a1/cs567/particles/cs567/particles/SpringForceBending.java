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
	/// p0.f, p1.f, p2.f (Nonzero bend angles are an optional
	/// feat):

    }

    public void display(GL gl)
    {
	/// OPTIONAL: DRAW A SEMI-CIRCLE OR SOMETHING TO SHOW BENDING FORCES AT WORK

    }

    public ParticleSystem getParticleSystem() { return PS; }
}
