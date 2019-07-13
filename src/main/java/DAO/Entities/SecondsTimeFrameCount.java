package DAO.Entities;

public class SecondsTimeFrameCount {
	private final TimeFrameEnum timeFrame;
	private int count = 0;
	private int seconds = 0;

	public SecondsTimeFrameCount(TimeFrameEnum timeFrame) {
		this.timeFrame = timeFrame;
	}

	public TimeFrameEnum getTimeFrame() {
		return timeFrame;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public int getMinutes() {
		return seconds / 60;
	}

	public int getHours() {
		return seconds / 3600;
	}

	public int getRemainingMinutes() {
		return getMinutes() % 60;
	}

}
