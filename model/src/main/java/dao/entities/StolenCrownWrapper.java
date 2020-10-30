package dao.entities;

import java.util.List;

public class StolenCrownWrapper {
	private final long ogId;
	private final long quriedId;
	private final List<StolenCrown> list;

	public StolenCrownWrapper(long ogId, long quriedId, List<StolenCrown> list) {
		this.ogId = ogId;
		this.quriedId = quriedId;
		this.list = list;
	}

	public long getOgId() {
		return ogId;
	}

	public long getQuriedId() {
		return quriedId;
	}

	public List<StolenCrown> getList() {
		return list;
	}
}
