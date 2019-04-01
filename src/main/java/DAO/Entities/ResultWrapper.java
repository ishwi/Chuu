package DAO.Entities;

import java.util.List;

public class ResultWrapper {
	private int rows;
	private List<Results> resultList;

	public ResultWrapper(int rows, List<Results> resultList) {
		this.rows = rows;
		this.resultList = resultList;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public List<Results> getResultList() {
		return resultList;
	}

	public void setResultList(List<Results> resultList) {
		this.resultList = resultList;
	}
}
