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
	private double xrot, yrot;
	
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
		glu.gluPerspective(45.0f, (double)width/height, 0, 5);

		glu.gluLookAt(eyeX, eyeY, eyeZ, 0.5, 0.5, 0.5, 0, 1, 0);
	}
	
	@Override
	public Point3d getPoint3d(MouseEvent e) {
		// TODO Auto-generated method stub
		Dimension size = e.getComponent().getSize();

		double x = (double) e.getX() / (double) size.width; 
		x *= (r + 2 * eps);
		x -= eps;

		double y = 1. - (double) e.getY() / (double) size.height;
		y *= (1 + 2 * eps);
		y -= eps;
		
		Point3d p = new Point3d(x, y, 0);
		// p.clampMax(1);
		// p.clampMin(0);
		System.out.println(p);
		return p;
	}

	public void rotate(float xrotation, float yrotation) {
		 xrot = xrotation - xrot;
		 yrot = yrotation - yrot;
		 Matrix3d currentEye = new Matrix3d(
				 eyeX, eyeX, eyeX,
				 eyeY, eyeY, eyeY,
				 eyeZ, eyeZ, eyeZ);
		 
		 double sinx =  Math.sin(xrot*180/Math.PI);
		 double cosx = Math.cos(xrot*180/Math.PI);
		 Matrix3d Rx = new Matrix3d(
				 1, 0, 0,
				 0, cosx, -sinx, 
				 0, sinx, cosx);
		 
		 double siny =  Math.sin(yrot*180/Math.PI);
		 double cosy = Math.cos(yrot*180/Math.PI);
		 Matrix3d Ry = new Matrix3d(
				 cosy, 0, siny,
				 0, 1, 0, 
				 -siny, 0, cosy);
		 
		 Ry.mul(currentEye);
		 Rx.mul(Ry);
		 
		 eyeX = Rx.m00;
		 eyeY = Rx.m10;
		 eyeZ = Rx.m20;
		 
	}

}
