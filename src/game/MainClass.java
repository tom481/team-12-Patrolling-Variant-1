package game;

public class MainClass {
	public static void main(String args[]) throws Exception {

		int x = 5;
		int y = 5;
		int num_robots = 1;

		Point[] obstacles = new Point[5];
		obstacles[0] = new Point(1, 1);
		obstacles[1] = new Point(2, 1);
		obstacles[2] = new Point(0, 3);
		obstacles[3] = new Point(3, 3);
		obstacles[4] = new Point(3, 4);
//		obstacles[5] = new Point(2, 5);
//		obstacles[6] = new Point(2, 6);
//		obstacles[7] = new Point(5, 1);
//		obstacles[8] = new Point(5, 2);
//		obstacles[9] = new Point(5, 3);
//		obstacles[10] = new Point(5, 4);
//		obstacles[11] = new Point(5, 5);
//		obstacles[12] = new Point(5, 6);
//		obstacles[13] = new Point(5, 7);

		Point[] goals = new Point[num_robots];

		goals[0] = new Point(3, 4);

		ControlPanel cp;
		String path = "out//";

		System.out.println("Running the system");
		cp = new ControlPanel(x, y, num_robots, obstacles, goals, path);
		cp.init();

	}
}
