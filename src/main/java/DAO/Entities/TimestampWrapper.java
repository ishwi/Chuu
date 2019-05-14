package DAO.Entities;

public class TimestampWrapper<T> {
	private T wrapped;
	private int timestamp;

	public TimestampWrapper(T wrapped, int timestamp) {
		this.wrapped = wrapped;
		this.timestamp = timestamp;
	}

	public T getWrapped() {
		return wrapped;
	}

	public void setWrapped(T wrapped) {
		this.wrapped = wrapped;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
}
