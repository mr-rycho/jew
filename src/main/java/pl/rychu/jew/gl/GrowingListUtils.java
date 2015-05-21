package pl.rychu.jew.gl;



public class GrowingListUtils {

	public static final int getPower(final int num) {
		if (num <= 0) {
			throw new IllegalArgumentException("bad size: "+num);
		}
		int power = 0;
		int n = num;
		while (true) {
			if ((n & 1) == 1) {
				if (n != 1) {
					throw new IllegalArgumentException("num must be power of 2");
				} else {
					return power;
				}
			}
			n >>>= 1;
			power++;
		}
	}

}
