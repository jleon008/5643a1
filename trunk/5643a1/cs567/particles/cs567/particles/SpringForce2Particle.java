package cs567.particles;

import javax.vecmath.*;
import javax.media.opengl.*;

/** 
 * Spring force between two particles. 
 * 
 * @author Doug James, January 2007
 */
public class SpringForce2Particle implements Force
{
    Particle p1;
    Particle p2;
    ParticleSystem PS;

    SpringForce2Particle(Particle p1, Particle p2, ParticleSystem PS)
    {
	if(p1==null || p2==null) throw new NullPointerException("p1="+p1+", p2="+p2);

	this.p1 = p1;
	this.p2 = p2;
	this.PS = PS;
    }

    public void applyForce()
    {
	/// TODO: Accumulate spring/damper forces into p1.f and p2.f ...

    }

    public void display(GL gl)
    {
	/// DRAW A LINE:
	float g = 0.5f;
	gl.glColor3f(g,g,g);//gray
	gl.glBegin(GL.GL_LINES);
	gl.glVertex3d(p1.x.x, p1.x.y, p1.x.z);
	gl.glVertex3d(p2.x.x, p2.x.y, p2.x.z);
	gl.glEnd();	
    }

    public ParticleSystem getParticleSystem() { return PS; }
}
