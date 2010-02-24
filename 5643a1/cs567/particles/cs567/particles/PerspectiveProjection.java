package cs567.particles;

import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL;
import javax.media.opengl.glu.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class PerspectiveProjection extends OrthoMap {
	private double eps = 0.03;// epsilon boundary gap
	private int width, height;
	private double r;
	private double L = 0;
	private double R;// = r*1;
	private double B = 0;
	private double T = 1;
	private double F = 0;
	private double Ba = 1;
	double d = 2.25; //distance of camera from box
	private double eyeX = 0.5;
	private double eyeY = 0.5;
	private double eyeZ = d;
	
	//for threadsafe picking
	double[] model = new double[16];
	double[] proj = new double[16];
	int[] view = new int[4];
	
	public PerspectiveProjection(int viewportWidth, int viewportHeight)
		 {
		super(viewportWidth, viewportHeight);
		width = viewportWidth;
		height = viewportHeight;
		r = (double) width / (double) height;
		// L = B = 0;
		R = r * 1;
		// T = 1;
		System.out.println("r=" + r);
		
	}
	
	public void apply_gluPerspective(GL gl) {
		
		GLU glu = new GLU();
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		
		glu.gluPerspective(45.0f, (double)width/(double)height, 1, 5);
		
		// / GET READY TO DRAW:
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		//for getting artifact perspectives right hardcode these to test
		//location <0.25,0.75,0.99>\nlook_at <0.75,0.25,0.01
		//glu.gluLookAt(0.75,0.75,0.99, 0.25,0.25,0.01, 0, 1, 0);
		
		glu.gluLookAt(eyeX, eyeY, eyeZ, 0.5, 0.5, 0.5, 0, 1, 0);
		//System.out.printf("%f, %f, %f\n", eyeX, eyeY, eyeZ);

		gl.glGetIntegerv(GL.GL_VIEWPORT, view,0);
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, model,0);
		gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, proj,0);
	}
	
	@Override
	public Point3d getPoint3d(MouseEvent e) {
		// TODO Auto-generated method stub
		Dimension size = e.getComponent().getSize();

//		double x = (double) e.getX() / (double) size.width; 
//		x *= (r + 2 * eps);
//		x -= eps;
//
//		double y = 1. - (double) e.getY() / (double) size.height;
//		y *= (1 + 2 * eps);
//		y -= eps;
		
		GLU glu = new GLU();
		
		double realy = view[3] - e.getY() - 1;
		//realy /= size.height;
		double realx = e.getX();
		//realx /= size.width;
		
		double[] objPos2 = new double[4];
		
		
		//todo play with 0.25*d to get the z (distance from camera) value correct
		boolean result2 = glu.gluUnProject(realx, realy, 0.25*d, model, 0, proj, 0, view, 0, objPos2, 0);
		
		Point3d p = new Point3d(objPos2[0],objPos2[1],objPos2[2]);
		System.out.println(p);
		// p.clampMax(1);
		// p.clampMin(0);
		return p;
	}

	public void rotate(double xrotation, double yrotation) {
		xrotation /= 180/Math.PI;
		yrotation /= 180/Math.PI;
		
		//shift eye to origin based
		eyeX -= 0.5;
		eyeY -= 0.5;
		eyeZ -= 0.5;
		
		 Matrix3d currentEye = new Matrix3d(
				 eyeX, eyeX, eyeX,
				 eyeY, eyeY, eyeY,
				 eyeZ, eyeZ, eyeZ);
		 
		 double sinx =  Math.sin(xrotation);
		 double cosx = Math.cos(xrotation);
		 Matrix3d Rx = new Matrix3d(
				 1, 0, 0,
				 0, cosx, -sinx, 
				 0, sinx, cosx);
		 
		 double siny =  Math.sin(yrotation);
		 double cosy = Math.cos(yrotation);
		 Matrix3d Ry = new Matrix3d(
				 cosy, 0, siny,
				 0, 1, 0, 
				 -siny, 0, cosy);
		 
		 Ry.mul(currentEye);
		 Rx.mul(Ry);
		 
		 eyeX = Rx.m00;
		 eyeY = Rx.m10;
		 eyeZ = Rx.m20;
		 
			//shift eye back to center of cube
			eyeX += 0.5;
			eyeY += 0.5;
			eyeZ += 0.5;
			
			System.out.printf("%f, %f, %f\n", eyeX, eyeY, eyeZ);
	}

	public void resetCamera() {
		eyeX = 0.5;
		eyeY = 0.5;
		eyeZ = d;
	}

}
