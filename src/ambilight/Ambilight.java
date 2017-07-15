package ambilight;


public interface Ambilight {

	void init(int ledsWidth, int ledsHeight, int leds[][]);

	byte[][] getScreenSegmentsColors();


	default void benchmarkOnce() {
		long start = System.currentTimeMillis();
		getScreenSegmentsColors();
		long end = System.currentTimeMillis();

		long duration = end - start;

		System.out.println("Time spent: " + duration + " ms.");
	}

	default void benchmarkRepeatedly(int ms) {
		long start = System.currentTimeMillis();
		int counter = 0;
		while (true) {
			long current = System.currentTimeMillis();
			if (current - start > ms) {
				break;
			}
			getScreenSegmentsColors();
			counter++;
		}
		System.out.println("During " + ms + "ms, called " + counter + " times.");
	}

}
