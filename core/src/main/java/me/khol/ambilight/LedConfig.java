package me.khol.ambilight;

public class LedConfig {

	private int ledsWidth;
	private int ledsHeight;
	private int[][] leds;

	public LedConfig() {
		ledsWidth = 14;
		ledsHeight = 8;
		leds = new int[][] {		// bottom centered WITH leg
			{4,7}, {3,7}, {2,7}, {1,7}, {0,7},	// bottom left
			{0,6}, {0,5}, {0,4}, {0,3}, {0,2}, {0,1},			// left
			{0,0}, {1,0}, {2,0}, {3,0}, {4,0}, {5,0}, {6,0}, 	// top left
			{7,0}, {8,0}, {9,0}, {10,0}, {11,0}, {12,0}, {13,0},// top right
			{13,1},	{13,2},	{13,3},	{13,4},	{13,5},	{13,6},		// right
			{13,7}, {12,7}, {11,7}, {10,7}, {9,7},	// bottom right
			{8,7}, {7,7}, {6,7}, {5,7}	// leg
		};	// 40 leds
	}

	public int getLedCount() {
		return leds.length;
	}

	public int getLedsWidth() {
		return ledsWidth;
	}

	public int getLedsHeight() {
		return ledsHeight;
	}

	public int[][] getLeds() {
		return leds;
	}

	public int[] getLed(int index) {
		return leds[index];
	}
}
