package cs567.particles;

import javax.vecmath.*;
import javax.media.opengl.*;

/** 
 * Spring force between one particle and a proxy point. 
 * 
 * @author Doug James, January 2007
 */
public class SpringForce1Particle implements Force
{
    Particle p1;
    Point2d  x2;
    ParticleSystem PS;

    SpringForce1Particle(Particle p1, Point2d x2, ParticleSystem PS)
    {
	if(p1==null || x2==null) throw new NullPointerException("p1="+p1+", x2="+x2);

	this.p1 = p1;
	this.x2 = x2;
	this.PS = PS;
    }

    public void updatePoint(Point2d x) {
	x2.set(x);
    }

    public void applyForce()
    {
	/// TODO: Accumulate spring/damper forces into p1.f ...

    }

    public void display(GL gl)
    {
	/// DRAW A LINE:
	gl.glColor3f(0,1,0);
	gl.glBegin(GL.GL_LINES);
	gl.glVertex2d(p1.x.x, p1.x.y);
	gl.glVertex2d(x2.x,   x2.y);
	gl.glEnd();	
    }

    public ParticleSystem getParticleSystem() { return PS; }
}
