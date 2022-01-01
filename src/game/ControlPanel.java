package game;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;

import tau.smlab.syntech.controller.executor.ControllerExecutor;
import tau.smlab.syntech.controller.jit.BasicJitController;

/*
 * Manages the simulation - GUI, controller input/output, board (visualization)
 */

public class ControlPanel {
	// board dimensions
	int x;
	int y;
	// board constants
	final int dim = 100;
	static final int y_offset = 30;
	Random rand = new Random();

	Boolean[] targetIIsRed =new Boolean[3];
	
	int startPosColor =0;
	int num_robots;
	int num_obstacles;
	Point[] robots;
	Point[] obstacles;
	Point[] goals;

	// holds the robots previous position (for use when animating transitions)
	Point[] robots_prev = new Point[num_robots];

	// Board and GUI elements
	JFrame frame;
	Board board;
	JButton advance_button;
	JButton autorun_button;

	// holds states for the animation
	boolean ready_for_next;
	boolean autorun;
	ArrayList<Point> trgts;
	// The controller and its inputs
	ControllerExecutor executor;
	Map<String, String> inputs = new HashMap<String, String>();
	int numberOfRedTurnsForTile;
	// The path to the controller files
	String path;
	String rotation = "UP";
	
	boolean cleaningRequest =false;
	boolean greenLight =false;
	boolean emptied =false;
	boolean fullTank = false;
    boolean cleaned;

	public ControlPanel(int x, int y, int num_robots, Point[] obstacles, Point[] goals, String path) {
		this.x = x;
		this.y = y;
		this.num_robots = num_robots;
		this.num_obstacles = obstacles.length;
		this.robots = new Point[num_robots];
		this.robots_prev = new Point[num_robots];
		this.obstacles = obstacles;
		this.goals = goals;
		this.path = path;
		System.out.print(path);
		this.trgts =this.getRandomTargetsLocations();
		Random rand =new Random();
	    numberOfRedTurnsForTile = rand.nextInt(3)+2;
		
	}

	public void init() throws Exception {
		autorun = false;

		for (int i = 0; i < num_robots; i++) {
			robots[i] = new Point();
			robots_prev[i] = new Point();
		}

		// init controller
		executor = new ControllerExecutor(new BasicJitController(), this.path);
		
		//Add environments target variables to the inputs
		
		for (Point t : this.trgts)
		{System.out.print(t.getX());
		System.out.print(t.getY());
		System.out.println();}
				
		inputs.put("targetX", Integer.toString(trgts.get(0).getX()));
		inputs.put("targetY", Integer.toString(trgts.get(0).getY()));
		
		//inputs.put("numberOfRedTurnsForTile", Integer.toString(numberOfRedTurnsForTile));

		executor.initState(inputs);

		Map<String, String> sysValues = executor.getCurrOutputs();
		
		// set initial robot locations
		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots_prev[i].setY(Integer.parseInt(sysValues.get("robotY")));
			robots[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots[i].setY(Integer.parseInt(sysValues.get("robotY")));
		}

		setUpUI();
	}

	// handle next turn
	void next() throws Exception {
		ready_for_next = false;
		advance_button.setText("...");
		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(robots[i].getX());
			robots_prev[i].setY(robots[i].getY());
			System.out.print("Robot Loc:") ;
			System.out.print(robots[i].getX());
			System.out.print(robots[i].getY());

			System.out.println();
		}

		inputs.put("greenLight",Boolean.toString(greenLight));
		inputs.put("emptied",Boolean.toString(emptied));

		inputs.put("cleaningRequest",Boolean.toString(cleaningRequest));

		inputs.put("targetX", Integer.toString(trgts.get(0).getX()));
		inputs.put("targetY", Integer.toString(trgts.get(0).getY()));
		

		System.out.println("h");

		goals[0] = trgts.get(0);
		

		executor.updateState(inputs);

		// Receive updated values from the controller
		Map<String, String> sysValues = executor.getCurrOutputs();
		/*
		System.out.print("j");

		this.targetIIsRed[0] = Boolean.parseBoolean(sysValues.get("targetAIsRed"));
		this.targetIIsRed[1] = Boolean.parseBoolean(sysValues.get("targetBIsRed"));
		this.targetIIsRed[2] = Boolean.parseBoolean(sysValues.get("targetCIsRed"));
		System.out.println(sysValues.get("targetATurnsPassed"));
		System.out.println(sysValues.get("targetBTurnsPassed"));
		System.out.println(sysValues.get("targetCTurnsPassed"));

		System.out.print("k");

		System.out.print(targetIIsRed[0]);
		System.out.print(targetIIsRed[1]);
		System.out.print(targetIIsRed[2]);
*/
		// Update robot locations
		for (int i = 0; i < num_robots; i++) {
			robots[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots[i].setY(Integer.parseInt(sysValues.get("robotY")));
		}
		//If the robot is on a target then we want to generate a new target
//		if(trgts.get(0).getX() == robots[0].getX() && trgts.get(0).getY() == robots[0].getY())
		cleaned = Boolean.parseBoolean(sysValues.get("cleaned"));
		if(cleaned)
		{
			trgts = this.getRandomTargetsLocations();
			System.out.println(trgts.get(0).getX());
			System.out.println(trgts.get(0).getY());

		}
		rotation = sysValues.get("robotRotation");
		fullTank = Boolean.parseBoolean(sysValues.get("fullTank"));
		System.out.println(rotation);
		int  p = rand.nextInt(10);

		if (cleaningRequest == false)
		{
			if(p<2)
				cleaningRequest = true;
			
		}
		if(p<5)
		{
			greenLight =true;
		}
		else {
			
			greenLight =false;
		}
		
		if(robots[0].x==0 &robots[0].y ==0 & fullTank & p<5)
		{
			
				emptied =true;
		}
		else
		{
		emptied = false;			
				}
		System.out.println(Boolean.parseBoolean(sysValues.get("cleaned")));



		// Animate transition
		board.animate();
	}

	void setUpUI() throws Exception {
		advance_button = new JButton();
		autorun_button = new JButton();
		frame = new JFrame();
		frame.add(advance_button);
		frame.add(autorun_button);
		board = new Board(this);
		board.init();
		frame.setTitle("Robots");
		frame.setSize(x * dim + 8 + 150, y * dim + y_offset + 8);
		board.setSize(x * dim, y * dim);
		frame.setLayout(null);
		frame.add(board, BorderLayout.CENTER);

		// Handle presses of the "next step" button
		advance_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (ready_for_next && !autorun)
						next();
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});
		advance_button.setBounds(x * dim + 8, 0, 130, 50);
		advance_button.setText("Start");

		// Handle presses of the "autorun/stop autorun" button
		autorun_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (autorun) {
						autorun = false;
						autorun_button.setText("Auto run");
					} else if (ready_for_next) {
						autorun = true;
						autorun_button.setText("Stop Auto run");
						next();
					}
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});
		autorun_button.setBounds(x * dim + 8, 50, 130, 50);
		autorun_button.setText("Auto run");
		frame.setVisible(true);
		board.setVisible(true);
		advance_button.setVisible(true);
		autorun_button.setVisible(true);
		ready_for_next = true;
	}

	
	
	/*****************************************************************************************/
	public ArrayList<Point> getRandomTargetsLocations()
	
	{	ArrayList<Point> targets = new ArrayList<Point>();
		Random rand = new Random();

		int x1 = rand.nextInt(7);
		int y1 = rand.nextInt(7);
		Point target1=new Point(x1,y1);
		//We want to check that the new generated point is not an obstacle or 0,0, if it is, we will generate another one 
		boolean isAlsoObstacle = Arrays.stream(this.obstacles).anyMatch(target1::equals) || (x1 ==0 &&y1==0);
		while (isAlsoObstacle)
		{
			x1 = rand.nextInt(7);
			y1 = rand.nextInt(7);
			target1=new Point(x1,y1);
			isAlsoObstacle = Arrays.stream(this.obstacles).anyMatch(target1::equals)|| (x1 ==0 &&y1==0);
		}
		targets.add(target1);
		

		return  targets;
	}
	
	/*
	public void run() throws Exception {

		executor = new ControllerExecutor(new BasicJitController(), "out");
		m = ImageIO.read(new File("img/monkey.jpg"));

		Random rand = new Random();
		banana[0] = rand.nextInt(8);
		banana[1] = rand.nextInt(8);
		
		inputs.put("banana[0]", Integer.toString(banana[0]));
		inputs.put("banana[1]", Integer.toString(banana[1]));
		executor.initState(inputs);
		
		Map<String, String> sysValues = executor.getCurrOutputs();
		
		monkey[0] = Integer.parseInt(sysValues.get("monkey[0]"));
		monkey[1] = Integer.parseInt(sysValues.get("monkey[1]"));
		
		paint(this.getGraphics());
		Thread.sleep(1000);
		
		while (true) {
			
			if (monkey[0] == banana[0] & monkey[1] == banana[1]) {
				banana[0] = rand.nextInt(8);
				banana[1] = rand.nextInt(8);
			}
			
			inputs.put("banana[0]", Integer.toString(banana[0]));
			inputs.put("banana[1]", Integer.toString(banana[1]));
			
			executor.updateState(inputs);
			
			sysValues = executor.getCurrOutputs();
			
			monkey[0] = Integer.parseInt(sysValues.get("monkey[0]"));
			monkey[1] = Integer.parseInt(sysValues.get("monkey[1]"));
			
			paint(this.getGraphics());
			Thread.sleep(1000);
		}
	}
	*/
}
