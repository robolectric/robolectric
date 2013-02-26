package android.os;

public class TestVibrator extends Vibrator {

	@Override
	public void cancel() {
	}

	@Override
	public boolean hasVibrator() {
		return false;
	}

	@Override
	public void vibrate(long arg0) {
	}

	@Override
	public void vibrate(long[] arg0, int arg1) {
	}

}
