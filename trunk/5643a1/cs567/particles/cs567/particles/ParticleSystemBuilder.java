package cs567.particles;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.vecmath.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.*;

/**
 * CS567: Assignment #1 "Particle Systems"
 * 
 * main() entry point class that initializes ParticleSystem, OpenGL rendering, and GUI that manages
 * GUI/mouse events.
 * 
 * Spacebar toggles simulation advance.
 * 
 * @author Doug James, January 2007
 */
public class ParticleSystemBuilder implements GLEventListener {
	private FrameExporter frameExporter;

	private Map<Particle, FilterPin> pinFilters = new HashMap<Particle, FilterPin>();

	private static int N_STEPS_PER_FRAME = 500;

	/** Default graphics time step size. */
	public static final double DT = 0.01;

	/** Main window frame. */
	JFrame frame = null;

	private int width, height;

	/** The single ParticleSystem reference. */
	ParticleSystem PS; // TODO use DynamicalSystem
	Integrator I;

	/**
	 * Object that handles all GUI and user interactions of building Task objects, and simulation.
	 */
	BuilderGUI gui;

	/** Main constructor. Call start() to begin simulation. */
	ParticleSystemBuilder() {
		PS = new ParticleSystem();
		PS.addForce(new GravitationalForce(PS));
		PS.addForce(new ViscousDragForce(PS));
		/*
		 * Random r = new Random(); r.setSeed(System.currentTimeMillis()); for (int i = 0; i < 5;
		 * i++) { for (int e = 0; e < 10; e++) PS.createParticle(new Point3d(.25 + i*.015 +
		 * (r.nextFloat() - 1)*.0075, .02 + e*.015 + (r.nextFloat() - 1)*.0075, 0)); }
		 */
		I = new Integrator_Midpoint();
		PS.addFilter(new FilterPlane(new Vector3d(0, 0, 0), new Vector3d(0, 1, 0), PS
				.getParticles()));
		PS.addFilter(new FilterPlane(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0), PS
				.getParticles()));
		PS.addFilter(new FilterPlane(new Vector3d(1, 1, 0), new Vector3d(0, -1, 0), PS
				.getParticles()));
		PS.addFilter(new FilterPlane(new Vector3d(1, 1, 0), new Vector3d(-1, 0, 0), PS
				.getParticles()));
	}

	/**
	 * Builds and shows windows/GUI, and starts simulator.
	 */
	public void start() {
		if (frame != null)
			return;

		gui = new BuilderGUI();// / MOVED HERE SINCE CALLED BY frame/animator

		frame = new JFrame("CS567 Particle System Builder");
		GLCanvas canvas = new GLCanvas();
		canvas.addGLEventListener(this);
		frame.add(canvas);

		final Animator animator = new Animator(canvas);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// Run this on another thread than the AWT event queue to
				// make sure the call to Animator.stop() completes before
				// exiting
				new Thread(new Runnable() {
					public void run() {
						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});

		frame.pack();
		frame.setSize(600, 600);
		frame.setLocation(200, 0);
		frame.setVisible(true);
		animator.start();
	}

	private PerspectiveProjection persProj;

	/** Maps mouse event into computational cell using OrthoMap. */
	public Point3d getPoint3d(MouseEvent e) {
		return persProj.getPoint3d(e);
	}

	/** GLEventListener implementation: Initializes JOGL renderer. */
	public void init(GLAutoDrawable drawable) {
		// DEBUG PIPELINE (can use to provide GL error feedback... disable for speed)
		// drawable.setGL(new DebugGL(drawable.getGL()));

		GL gl = drawable.getGL();
		System.err.println("INIT GL IS: " + gl.getClass().getName());

		gl.setSwapInterval(1);

		gl.glLineWidth(3);

		drawable.addMouseListener(gui);
		drawable.addMouseMotionListener(gui);

		drawable.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				gui.dispatchKey(e.getKeyChar(), e);
			}
		});
	}

	/** GLEventListener implementation */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
	}

	/** GLEventListener implementation */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		System.out.println("width=" + width + ", height=" + height);
		height = Math.max(height, 1); // avoid height=0;

		this.width = width;
		this.height = height;

		GL gl = drawable.getGL();
		gl.glViewport(0, 0, width, height);

		// SETUP PERSPECTIVE PROJECTION AND MAPPING INTO UNIT CELL:
		persProj = new PerspectiveProjection(width, height);
		persProj.apply_gluPerspective(gl);

	}

	/**
	 * Main event loop: OpenGL display + simulation advance. GLEventListener implementation.
	 */
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		gl.glClearColor(0, 0, 0, 0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// / DRAW COMPUTATIONAL CELL BOUNDARY:
		{
			// front
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glColor3f(1, 1, 1);
			gl.glVertex3d(0, 0, 0);
			gl.glVertex3d(1, 0, 0);
			gl.glVertex3d(1, 1, 0);
			gl.glVertex3d(0, 1, 0);
			gl.glVertex3d(0, 0, 0);
			gl.glEnd();
			// back
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glColor3f(1, 1, 1);
			gl.glVertex3d(0, 0, 1);
			gl.glVertex3d(1, 0, 1);
			gl.glVertex3d(1, 1, 1);
			gl.glVertex3d(0, 1, 1);
			gl.glVertex3d(0, 0, 1);
			gl.glEnd();
			// 4 connecting posts
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glColor3f(1, 1, 1);
			gl.glVertex3d(0, 0, 0);
			gl.glVertex3d(0, 0, 1);
			gl.glEnd();
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glColor3f(1, 1, 1);
			gl.glVertex3d(0, 1, 0);
			gl.glVertex3d(0, 1, 1);
			gl.glEnd();
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glColor3f(1, 1, 1);
			gl.glVertex3d(1, 0, 0);
			gl.glVertex3d(1, 0, 1);
			gl.glEnd();
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glColor3f(1, 1, 1);
			gl.glVertex3d(1, 1, 0);
			gl.glVertex3d(1, 1, 1);
			gl.glEnd();
		}

		// / SIMULATE/DISPLAY HERE (Handled by BuilderGUI):
		gui.simulateAndDisplayScene(gl);

		// update camera
		persProj.apply_gluPerspective(gl);

		if (frameExporter != null) {
			frameExporter.writeFrame();
		}
	}

	/** Interaction central: Handles windowing/mouse events, and building state. */
	class BuilderGUI implements MouseListener, MouseMotionListener// ,
	// KeyListener
	{
		boolean simulate = false;

		/** Current build task (or null) */
		Task task;

		JFrame guiFrame;
		TaskSelector taskSelector = new TaskSelector();
		IntegratorSelector integratorSelector = new IntegratorSelector();

		BuilderGUI() {
			guiFrame = new JFrame("Tasks");
			guiFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			// guiFrame.setLayout(new SpringLayout());
			guiFrame.setLayout(new GridLayout(6, 1));

			ButtonGroup buttonGroup = new ButtonGroup();
			ButtonGroup integratorGroup = new ButtonGroup();
			JToggleButton[] buttons = { new JToggleButton("Reset", false),
					new JRadioButton("Create Particle", true),
					new JRadioButton("Move Particle", false),
					new JRadioButton("Create Spring", false),
					new JRadioButton("Create Hair", false),
					new JRadioButton("Pin Constraint", false) };
			// new JRadioButton ("Rigid Constraint", false)};

			JToggleButton[] integrators = { new JRadioButton("Forward Euler", true),
					new JRadioButton("Midpoint Method", false),
					new JRadioButton("Symplectic Euler", false),
					new JRadioButton("Velocity Verlet", false), };

			for (int i = 0; i < buttons.length; i++) {
				buttonGroup.add(buttons[i]);
				guiFrame.add(buttons[i]);
				buttons[i].addActionListener(taskSelector);
			}

			for (int i = 0; i < integrators.length; i++) {
				integratorGroup.add(integrators[i]);
				guiFrame.add(integrators[i]);
				integrators[i].addActionListener(integratorSelector);
			}

			guiFrame.setSize(200, 200);
			guiFrame.pack();
			guiFrame.setVisible(true);

			task = new CreateParticleTask();
		}

		/**
		 * Simulate then display particle system and any builder adornments.
		 */
		void simulateAndDisplayScene(GL gl) {
			// / TODO: OVERRIDE THIS INTEGRATOR (Doesn't use Force objects
			// properly)
			if (simulate) {
				if (false) {// ONE EULER STEP
					PS.advanceTime(DT, I);
				} else {// MULTIPLE STEPS FOR STABILITY WITH FORWARD EULER
					// (UGH!)
					int nSteps = N_STEPS_PER_FRAME;
					double dt = DT / (double) nSteps;
					for (int k = 0; k < nSteps; k++) {
						PS.advanceTime(dt, I);// /
					}
				}

				// / TODO: PROCESS COLLISIONS HERE:

			}

			// Draw particles, springs, etc.
			PS.display(gl);

			// Display Task, e.g., currently drawn spring.
			if (task != null)
				task.display(gl);

		}

		/**
		 * ActionListener implementation to manage Task selection using (radio) buttons.
		 */
		class TaskSelector implements ActionListener {
			/**
			 * Resets ParticleSystem to undeformed/material state, disables the simulation, and
			 * removes the active Task.
			 */
			void resetToRest() {
				PS.reset();// synchronized
				simulate = false;
				task = null;
			}

			/** Creates new Task objects to handle specified button action. */
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				System.out.println(cmd);

				resetToRest();
				if (cmd.equals("Reset")) {
				} else if (cmd.equals("Create Particle")) {
					task = new CreateParticleTask();
				} else if (cmd.equals("Move Particle")) {
					task = new MoveParticleTask();
				} else if (cmd.equals("Create Spring")) {
					task = new CreateSpringTask();
				} else if (cmd.equals("Create Hair")) {
					task = new CreateHairTask();
				} else if (cmd.equals("Pin Constraint")) {
					task = new PinConstraintTask();
				}
				// else if(cmd.equals("Rigid Constraint")){
				// task = new RigidConstraintTask();
				// }
				else {
					System.out.println("UNHANDLED ActionEvent: " + e);
				}
			}

		}

		class IntegratorSelector implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				System.out.println(cmd);

				if (cmd.equals("Reset")) {
				} else if (cmd.equals("Forward Euler")) {
					I = new Integrator_ForwardEuler();
				} else if (cmd.equals("Midpoint Method")) {
					I = new Integrator_Midpoint();
				} else if (cmd.equals("Symplectic Euler")) {
					I = new Integrator_SymplecticEuler();
				} else if (cmd.equals("Velocity Verlet")) {
					I = new Integrator_VelocityVerlet();
				} else {
					System.out.println("UNHANDLED ActionEvent: " + e);
				}
			}

		}

		// Methods required for the implementation of MouseListener
		public void mouseEntered(MouseEvent e) {
			if (task != null)
				task.mouseEntered(e);
		}

		public void mouseExited(MouseEvent e) {
			if (task != null)
				task.mouseExited(e);
		}

		public void mousePressed(MouseEvent e) {
			if (task != null)
				task.mousePressed(e);
		}

		public void mouseReleased(MouseEvent e) {
			if (task != null)
				task.mouseReleased(e);
		}

		public void mouseClicked(MouseEvent e) {
			if (task != null)
				task.mouseClicked(e);
		}

		// Methods required for the implementation of MouseMotionListener
		public void mouseDragged(MouseEvent e) {
			if (task != null)
				task.mouseDragged(e);
		}

		public void mouseMoved(MouseEvent e) {
			if (task != null)
				task.mouseMoved(e);
		}

		/**
		 * Handles keyboard events, e.g., spacebar toggles simulation/pausing, and escape resets the
		 * current Task.
		 */
		public void dispatchKey(char key, KeyEvent e) {
			// System.out.println("CHAR="+key+", keyCode="+e.getKeyCode()+", e="+e);
			if (key == ' ') {// SPACEBAR --> TOGGLE SIMULATE
				simulate = !simulate;
				if (simulate) {
					task = new DragParticleTask();
				} else {
					task = null;
				}
			} else if (e.toString().contains("Escape")) {// sloth
				System.out.println("ESCAPE");

				Task lastTask = task;
				taskSelector.resetToRest();// sets task=null;
				lastTask.reset();
				task = lastTask;
			} else if (key == 'e') {// toggle exporter
				frameExporter = ((frameExporter == null) ? (new FrameExporter()) : null);
				System.out.println("'e' : frameExporter = " + frameExporter);
			} else if (key == '=') {// increase nsteps
				N_STEPS_PER_FRAME = Math.max((int) (1.05 * N_STEPS_PER_FRAME),
						N_STEPS_PER_FRAME + 1);
				System.out.println("N_STEPS_PER_FRAME=" + N_STEPS_PER_FRAME + ";  dt="
						+ (DT / (double) N_STEPS_PER_FRAME));
			} else if (key == '-') {// decrease nsteps
				int n = Math.min((int) (0.95 * N_STEPS_PER_FRAME), N_STEPS_PER_FRAME - 1);
				N_STEPS_PER_FRAME = Math.max(1, n);
				System.out.println("N_STEPS_PER_FRAME=" + N_STEPS_PER_FRAME + ";  dt="
						+ (DT / (double) N_STEPS_PER_FRAME));
			}
			// rotation stuff
			else if (key == 'w') {
				persProj.rotate(-30, 0);
			} else if (key == 's') {
				persProj.rotate(30, 0);
			} else if (key == 'a') {
				persProj.rotate(0, -30);
			} else if (key == 'd') {
				persProj.rotate(0, 30);
			} else if (key == 'x') {
				persProj.resetCamera();
			}
			// hair performance tests //TODO this
			else if (key == 'h') {
				CreateHairTask task = new CreateHairTask();
				double x = 0.1, y = 0.1, z = 0.5;
				for (int i = 1; i <= 4; i++) {
					task.addParticle(new Point3d(x * 2 * i, 5*y + y * 2 * (i % 2), z));
				}
			}
		}

		/**
		 * "Task" command base-class extended to support building/interaction via mouse interface.
		 * All objects extending Task are implemented here as inner classes for simplicity.
		 */
		abstract class Task implements MouseListener, MouseMotionListener {
			/**
			 * Displays any task-specific OpengGL information, e.g., highlights, etc.
			 */
			public void display(GL gl) {
			}

			// Methods required for the implementation of MouseListener
			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}

			// Methods required for the implementation of MouseMotionListener
			public void mouseDragged(MouseEvent e) {
			}

			public void mouseMoved(MouseEvent e) {
			}

			/**
			 * Override to specify reset behavior during "escape" key events, etc.
			 */
			abstract void reset();

		}

		/** Clicking task that creates particles. */
		class CreateParticleTask extends Task {
			// private Particle lastCreatedParticle = null;

			public void mousePressed(MouseEvent e) {
				Point3d x0 = getPoint3d(e);
				if (0 < x0.x && 0 < x0.y && 0 < x0.z) { //only do in box
					Particle lastCreatedParticle = PS.createParticle(x0);
				}
			}

			void reset() {
			}
		}

		/** Task to move nearest particle. */
		class MoveParticleTask extends Task {
			private Particle moveParticle = null;

			/** Start moving nearest particle to mouse press. */
			public void mousePressed(MouseEvent e) {
				// FIND NEAREST PARTICLE:
				Point3d cursorP = getPoint3d(e);// cursor position
				moveParticle = PS.getNearestParticle(cursorP);

				// / START MOVING (+ HIGHLIGHT):
				updatePosition(cursorP);
			}

			/** Update moved particle state. */
			private void updatePosition(Point3d newX) {
				if (moveParticle == null)
					return;
				moveParticle.setHighlight(true);
				moveParticle.x.set(newX);
				moveParticle.x0.set(newX);
			}

			/** Update particle. */
			public void mouseDragged(MouseEvent e) {
				Point3d cursorP = getPoint3d(e);// cursor position
				updatePosition(cursorP);
			}

			/** Invokes reset() */
			public void mouseReleased(MouseEvent e) {
				reset();
			}

			/** Disable highlight, and nullify moveParticle. */
			void reset() {
				if (moveParticle != null)
					moveParticle.setHighlight(false);
				moveParticle = null;
			}

			public void display(GL gl) {
			}
		}

		/** Creates inter-particle springs. */
		class CreateSpringTask extends Task {
			private Particle p1 = null;
			private Particle p2 = null;
			private Point3d cursorP = null;

			CreateSpringTask() {
			}

			/** Start making a spring from the nearest particle. */
			public void mousePressed(MouseEvent e) {
				// FIND NEAREST PARTICLE:
				cursorP = getPoint3d(e);// cursor position
				p1 = PS.getNearestParticle(cursorP); // / = constant (since at
				// rest)
				p2 = null;
			}

			/** Update cursor location for display */
			public void mouseDragged(MouseEvent e) {
				cursorP = getPoint3d(e);// update cursor position
			}

			/**
			 * Find nearest particle, and create a SpringForce2Particle when mouse released, unless
			 * nearest particle, p2, is same as p1.
			 */
			public void mouseReleased(MouseEvent e) {
				cursorP = getPoint3d(e);// cursor position
				p2 = PS.getNearestParticle(cursorP); // / = constant (since at
				// rest)
				if (p1 != p2) {// make force object
					SpringForce2Particle newForce = new SpringForce2Particle(p1, p2, PS);// params
					PS.addForce(newForce);
				}
				// / RESET:
				p1 = p2 = null;
				cursorP = null;
			}

			/** Cancel any spring creation. */
			void reset() {
				p1 = p2 = null;
				cursorP = null;
			}

			/**
			 * Draw spring-in-progress. NOTE: created springs are drawn by ParticleSystem.
			 */
			public void display(GL gl) {
				if (cursorP == null || p1 == null)
					return;

				// / DRAW A LINE:
				gl.glColor3f(1, 1, 1);
				gl.glBegin(GL.GL_LINES);
				gl.glVertex3d(cursorP.x, cursorP.y, cursorP.z);
				gl.glVertex3d(p1.x.x, p1.x.y, p1.x.z);
				gl.glEnd();
			}
		}

		/**
		 * Runtime dragging of nearest particle using a spring force.
		 */
		class DragParticleTask extends Task {
			private Particle dragParticle = null;
			private Point3d cursorP = null;

			private SpringForce1Particle springForce = null;

			public void mousePressed(MouseEvent e) {
				// FIND NEAREST PARTICLE:
				cursorP = getPoint3d(e);// cursor position
				dragParticle = PS.getNearestParticle(cursorP);

				// / START APPLYING FORCE:
				springForce = new SpringForce1Particle(dragParticle, cursorP, PS);
				PS.addForce(springForce);// to be removed later
			}

			/** Cancel any particle dragging and forces. */
			void reset() {
				dragParticle = null;
				cursorP = null;
				if (springForce != null)
					PS.removeForce(springForce);
			}

			public void mouseDragged(MouseEvent e) {
				cursorP = getPoint3d(e);// cursor position

				// / UPDATE DRAG FORCE ANCHOR:
				springForce.updatePoint(cursorP);
			}

			public void mouseReleased(MouseEvent e) {
				cursorP = null;
				dragParticle = null;

				// / CANCEL/REMOVE FORCE:
				PS.removeForce(springForce);
			}

			public void display(GL gl) {
			}
		}

		/** Create hair task. */
		class CreateHairTask extends Task {
			ArrayList<Particle> hairParticles = new ArrayList<Particle>();

			/** Create new particle. */
			public void mousePressed(MouseEvent e) {
				addParticle(getPoint3d(e));
			}

			public void addParticle(Point3d p) {
				Particle p2 = PS.createParticle(p);
				if (hairParticles.size() > 0) {// / ADD STRETCH SPRING p1-p2:
					Particle p1 = hairParticles.get(hairParticles.size() - 1);
					PS.addForce(new SpringForce2Particle(p1, p2, PS));

					if (hairParticles.size() > 1) {// / ADD BENDING SPRING TO
						// p0-p1-p2
						Particle p0 = hairParticles.get(hairParticles.size() - 2);
						PS.addForce(new SpringForceBending(p0, p1, p2, PS));
					}
				}
				hairParticles.add(p2);// finally add new particle to list
			}

			public void mouseDragged(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void display(GL gl) {
			}

			void reset() {
				hairParticles.clear();
			}
		}

		/** Toggle pin constraints. */
		class PinConstraintTask extends Task {
			/** Toggle pin constraint on nearest particle. */
			public void mousePressed(MouseEvent e) {
				Point3d cursorP = getPoint3d(e);
				Particle p1 = PS.getNearestParticle(cursorP); // / = constant
				// (since at
				// rest)
				if (p1 != null) {// TOGGLE PIN:
					if (pinFilters.containsKey(p1)) {
						PS.removeFilter(pinFilters.get(p1));
						pinFilters.remove(p1);
						p1.setPin(false);
					} else {
						FilterPin newFilter = new FilterPin(p1);
						PS.addFilter(newFilter);
						pinFilters.put(p1, newFilter);
						p1.setPin(true);
					}
				}
			}

			public void mouseDragged(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void display(GL gl) {
			}

			void reset() {
			}
		}

		// /** SKIP: Rigid body constraint. */
		// class RigidConstraintTask extends Task
		// {
		// HashSet<Particle> rigidSet = new HashSet<Particle>();

		// /** Toggle rigid constraints on nearest particle. */
		// public void mousePressed(MouseEvent e)
		// {
		// Point3d cursorP = getPoint3d(e);
		// Particle p1 = PS.getNearestParticle(cursorP); /// = constant (since
		// at rest)
		// if(p1 != null) {// TOGGLE SET:
		// if(rigidSet.contains(p1)) {//REMOVE
		// rigidSet.remove(p1);
		// p1.setHighlight(false);
		// }
		// else {// ADD
		// rigidSet.add(p1);
		// p1.setHighlight(true);
		// }
		// }
		// }
		// public void mouseDragged(MouseEvent e) {}
		// public void mouseReleased(MouseEvent e) {}
		// public void display(GL gl) {}
		// void reset() { rigidSet.clear(); }
		// }
	}

	private static int exportId = -1;

	private class FrameExporter {
		private int nFrames = 0;

		FrameExporter() {
			exportId += 1;
		}

		void writeFrame() {
			long timeNS = -System.nanoTime();
			String number = Utils.getPaddedNumber(nFrames, 5, "0");
			String filename = "frames/export" + exportId + "-" + number + ".png";// / BUG: DIRECTORY
																					// MUST EXIST!

			try {
				java.io.File file = new java.io.File(filename);
				if (file.exists())
					System.out.println("WARNING: OVERWRITING PREVIOUS FILE: " + filename);

				// / WRITE IMAGE: ( :P Screenshot asks for width/height -->
				// cache in GLEventListener.reshape() impl)
				com.sun.opengl.util.Screenshot.writeToFile(file, width, height);

				System.out.println((timeNS / 1000000) + "ms:  Wrote image: " + filename);

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("OOPS: " + e);
			}

			nFrames += 1;
		}
	}

	/**
	 * ### Runs the ParticleSystemBuilder. ###
	 */
	public static void main(String[] args) {
		try {
			ParticleSystemBuilder psb = new ParticleSystemBuilder();
			psb.start();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("OOPS: " + e);
		}
	}
}
