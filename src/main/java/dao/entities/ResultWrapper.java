package dao.entities;

import java.util.List;

public class ResultWrapper<T> {
	private int rows;
	private List<T> resultList;

	public ResultWrapper(int rows, List<T> resultList) {
		this.rows = rows;
		this.resultList = resultList;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public List<T> getResultList() {
		return resultList;
	}

	public void setResultList(List<T> resultList) {
		this.resultList = resultList;
	}
}
