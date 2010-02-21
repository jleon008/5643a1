package cs567.particles;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.*;
import javax.vecmath.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.*;

/**
 * CS567: Assignment #1 "Particle Systems"
 * 
 * main() entry point class that initializes ParticleSystem, OpenGL rendering, and GUI that manages GUI/mouse events.
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

	private int frameNumber = 0;

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
		// Random r = new Random();
		// r.setSeed(System.currentTimeMillis());
		// for (int i = 0; i < 5; i++) {
		// for (int z = 0; z < 1 ; z ++)
		// for (int e = 0; e < 10; e++)
		// PS.createGooParticle(new Point3d(.27 + i*.02 + (r.nextFloat() - 1)*.00005, .5 + e*.02 + (r.nextFloat() -
		// 1)*.00005, .5 + z*.02)); }
		//
		// for (int u = 0; u < 10; u++)
		// PS.createPaperParticle(new Point3d(.2 + u*.02, .45, .5));

		PS.addFilter(new FilterPlane(new Vector3d(0, .5, .5), new Vector3d(1, 0, 0), PS.getParticles(), new Vector3d(0,
				.5, 0), new Vector3d(0, 0, .5)));

		I = new Integrator_Midpoint();
		PS.addFilter(new FilterPlane(new Vector3d(0, 0, 0), new Vector3d(0, 1, 0), PS.getParticles()));
		PS.addFilter(new FilterPlane(new Vector3d(1, 1, 0), new Vector3d(0, -1, 0), PS.getParticles()));
		PS.addFilter(new FilterPlane(new Vector3d(1, 1, 0), new Vector3d(-1, 0, 0), PS.getParticles()));
		PS.addFilter(new FilterPlane(new Vector3d(1, 1, 1), new Vector3d(0, 0, -1), PS.getParticles()));
		PS.addFilter(new FilterPlane(new Vector3d(0, 0, 0), new Vector3d(0, 0, 1), PS.getParticles()));
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

	private boolean povExport;

	private int numFrames;

	private String outFile;

	/** Maps mouse event into computational cell using OrthoMap. */
	public Point3d getPoint3d(MouseEvent e) {
		return persProj.getPoint3d(e);
	}

	/** GLEventListener implementation: Initializes JOGL renderer. */
	public void init(GLAutoDrawable drawable) {
		// DEBUG PIPELINE (can use to provide GL error feedback... disable for speed)
		//drawable.setGL(new DebugGL(drawable.getGL()));

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
		if (Constants.WALLS_ON) {
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

	private void writePov() {
		File f = new File(outFile + frameNumber + ".pov");
		try {
			PrintWriter p = new PrintWriter(f);
			p.println("#include \"colors.inc\"");
			p.println("#include \"textures.inc\"");
			p.println("#include \"finish.inc\"");
			p.println("background{Black}");
			p.println("#declare Water = pigment\n{\ncolor Blue transmit 0.7\n}");
			p.println("#declare Paper = pigment\n{\r\n" + "   gradient z\r\n" + "   color_map {\r\n"
					+ "      [0.00, rgb <0.98, 0.98, 0.87>]\r\n" + "      [1.00, rgb <0.98, 0.98, 0.87>]\r\n"
					+ "   }\r\n" + "\r\n" + "}");
			p.println("camera {\nlocation <0.25,0.75,0.99>\nlook_at <0.75,0.25,0.01>\n}");
			p.println("light_source { <0.5, .75, 0.99> color White }");

			p.println("blob\n{\nthreshold .5\n");

			for (Particle s : PS.Goo) {
				p.println("sphere { <" + s.x.x + "," + s.x.y + "," + s.x.z + ">, .03, 1 pigment {Water} }");
			}

			p
					.println("finish {\nambient 0.0\ndiffuse 0.0\nspecular 0.4\nroughness 0.003\nreflection { 0.003, 1.0 fresnel on }\n}\ninterior { ior 1.33 }\n}");

			p.println("blob\n{\nthreshold .5\n");

			for (Particle s : PS.Paper) {
				p.println("sphere { <" + s.x.x + "," + s.x.y + "," + s.x.z + ">, .03, 1 pigment {Paper} }");
			}

			p.println("finish {ambient 0.0\r\n" + "diffuse 0.1\r\n" + "specular 0.1\r\n" + "roughness 0.1\r\n"
					+ "reflection { 0.001, 1.0 fresnel on }\r\n" + "}\r\n" + "interior { ior 1.33 }\n" + "}");

			p.println("box {\n<0,0,0>, <1,1,1>\ntexture { White_Marble scale 0.5 }\n}");
			p.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			JToggleButton[] buttons = { new JToggleButton("Reset", false), new JRadioButton("Create Particle", true),
					new JRadioButton("Move Particle", false), new JRadioButton("Create Spring", false),
					new JRadioButton("Create Hair", false), new JRadioButton("Pin Constraint", false) };
			// new JRadioButton ("Rigid Constraint", false)};

			JToggleButton[] integrators = { new JRadioButton("Forward Euler", true),
					new JRadioButton("Midpoint Method", false), new JRadioButton("Symplectic Euler", false),
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
				frameNumber++;

				if (povExport) {
					if (frameNumber > numFrames)
						System.exit(0);
					writePov();
				}
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
			 * Resets ParticleSystem to undeformed/material state, disables the simulation, and removes the active Task.
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
		 * Handles keyboard events, e.g., spacebar toggles simulation/pausing, and escape resets the current Task.
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
				N_STEPS_PER_FRAME = Math.max((int) (1.05 * N_STEPS_PER_FRAME), N_STEPS_PER_FRAME + 1);
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
			// hair performance tests
			else if (key == 'h') {
				for (int i = 0; i < 2; i++) {
					CreateHairTask task = new CreateHairTask();
					double x = 0.25 + .5 * i;
					double y = 0.9;
					double z = 0.5;
					task.addParticle(new Point3d(x, y, z));
					new PinConstraintTask().Pin(new Point3d(x, y, z));
					for (int j = 0; j < 4; j++) {
						// x = (x + 0.1*(i%2) - 0.05);
						y = (y - 0.1 * (i % 2) - 0.05);
						// z = (z + 0.1*(i%2) - 0.05);
						task.addParticle(new Point3d(x, y, z));
					}

				}
			} else if (key == 'j') {
				for (int i = 0; i < 2; i++) {
					pulls[i] = new DragParticleTask();
					pulls[i].StartForce(new Point3d(0.3 + .4 * i, 0.7, 0.5));
				}
			} else if (key == 'k') {
				for (int i = 0; i < 2; i++) {
					pulls[i].EndForce();
				}
			}
			// paper maker
			else if (key == 'p') {
				int size = 10;
				Particle[][] paper = new Particle[size][size];
				double spacing = 0.02;
				Random r = new Random(System.currentTimeMillis());
				for (int kerran = 0; kerran < 3; kerran++) {
					// create particles
					for (int i = 0; i < size; i++) {
						for (int j = 0; j < size; j++) {
							paper[i][j] = PS.createPaperParticle(new Point3d(
									.3 + kerran*.1 + (i + 0.5) * spacing
									+ (r.nextFloat() - 1) * .00005, 
									0.5 - kerran*.1 + (r.nextFloat() - 1) * .00005, 
									.2 + (j + 0.5)
									* spacing + (r.nextFloat() - 1) * .00005));
						}
					}
					// connect them
					for (int i = 0; i < size; i++) {
						for (int j = 0; j < size; j++) {
							// springs
							if (j > 0) {
								SpringForce2Particle sfUp = new SpringForce2Particle(paper[i][j - 1], paper[i][j], PS);
								PS.addForce(sfUp);
							}
							if (i > 0) {
								SpringForce2Particle sfLeft = new SpringForce2Particle(paper[i - 1][j], paper[i][j], PS);
								PS.addForce(sfLeft);
							}
							if (i > 0 & j > 0) {
								SpringForce2Particle sfLeft = new SpringForce2Particle(paper[i - 1][j - 1],
										paper[i][j], PS);
								PS.addForce(sfLeft);
							}
							// bending
							// if (j > 0 && j < size - 1) {
							// SpringForceBending sfUp = new SpringForceBending(paper[i][j - 1], paper[i][j],
							// paper[i][j + 1], PS);
							// PS.addForce(sfUp);
							// }
							// if (i > 0 && i < size - 1) {
							// SpringForceBending sfLeft = new SpringForceBending(paper[i - 1][j], paper[i][j],
							// paper[i + 1][j], PS);
							// PS.addForce(sfLeft);
							// }
							// if (i > 0 && i < size - 1 && j > 0 && j < size - 1) {
							// SpringForceBending sfLeft = new SpringForceBending(paper[i - 1][j - 1], paper[i][j],
							// paper[i + 1][j + 1], PS);
							// PS.addForce(sfLeft);
							// }
							// pinning
							if (i == 0 && j == 0 || i == 0 && j == size - 1 || i == size - 1 && j == 0 || i == size - 1
									&& j == size - 1) {
								FilterPin newFilter = new FilterPin(paper[i][j]);
								PS.addFilter(newFilter);
								pinFilters.put(paper[i][j], newFilter);
								paper[i][j].setPin(true);
							}
						}
					}
				}
			} else if (key == 'g') {
				Random r = new Random();
				r.setSeed(System.currentTimeMillis());
				for (int x = 0; x < 8; x++) {
					for (int y = 0; y < 8; y++)
						for (int z = 0; z < 8; z++)
							PS
									.createGooParticle(new Point3d(
											.4 + x * .025 + (r.nextFloat() - 1) * .00005, 
											.6 + y
											* .025 + (r.nextFloat() - 1) * .00005, 
											.2 + z * .025 + (r.nextFloat() - 1)
											* .00005));
				}

			}
		}

		DragParticleTask[] pulls = new DragParticleTask[2];

		/**
		 * "Task" command base-class extended to support building/interaction via mouse interface. All objects extending
		 * Task are implemented here as inner classes for simplicity.
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
				if (0 < x0.x && 0 < x0.y && 0 < x0.z) { // only do in box
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
			 * Find nearest particle, and create a SpringForce2Particle when mouse released, unless nearest particle,
			 * p2, is same as p1.
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
				StartForce(cursorP);
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
				EndForce();

			}

			public void StartForce(Point3d x) {
				dragParticle = PS.getNearestParticle(x);

				// / START APPLYING FORCE:
				springForce = new SpringForce1Particle(dragParticle, x, PS);
				PS.addForce(springForce);// to be removed later
			}

			public void EndForce() {
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
				Pin(cursorP);
			}

			public void Pin(Point3d x) {
				Particle p1 = PS.getNearestParticle(x); // / = constant
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

			if (args.length != 0) {
				psb.povExport = true;
				psb.numFrames = Integer.parseInt(args[0]);
				N_STEPS_PER_FRAME = Integer.parseInt(args[1]);
				psb.outFile = args[2] +"_" + (int)((System.currentTimeMillis() / 1000) % Math.pow(Math.PI, 10))  + "_";
			}

			psb.start();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("OOPS: " + e);
		}
	}
}
