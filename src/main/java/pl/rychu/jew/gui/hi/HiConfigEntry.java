package pl.rychu.jew.gui.hi;



public class HiConfigEntry {

	private final String regexp;
	private final int colorB;
	private final int colorF;

	public HiConfigEntry(final String regexp, final int colorB, final int colorF) {
		this.regexp = regexp;
		this.colorB = colorB;
		this.colorF = colorF;
	}

	public String getRegexp() {
		return regexp;
	}

	public int getColorB() {
		return colorB;
	}

	public int getColorF() {
		return colorF;
	}

}
