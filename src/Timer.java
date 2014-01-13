
public class Timer {
	static long startMS;
	
	public static void start() {
		startMS = System.currentTimeMillis();
	}
	
	public static int stop() {
		long endMS = System.currentTimeMillis();
		long durMS = endMS - startMS;
		int durS = (int) (durMS / 1000);
		
		String seconds = Integer.toString((int) (durS % 60));
	    String minutes = Integer.toString((int) ((durS % 3600) / 60));
	    String hours = Integer.toString((int) (durS / 3600));

	    if (seconds.length() < 2) {
	      seconds = "0" + seconds;
	    }

	    if (minutes.length() < 2) {
	      minutes = "0" + minutes;
	    }

	    if (hours.length() < 2) {
	      hours = "0" + hours;
	    }

	    System.out.println("Time elapsed: " +  hours + ":" + minutes + ":" + seconds);
		
		return durS;
	}
}
