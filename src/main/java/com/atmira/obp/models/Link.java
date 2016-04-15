package com.atmira.obp.models;

public class Link {

	private HRef self;
	private HRef detail;
	
	public HRef getSelf() {
		return self;
	}

	public void setSelf(HRef self) {
		this.self = self;
	}

	public HRef getDetail() {
		return detail;
	}

	public void setDetail(HRef detail) {
		this.detail = detail;
	}

	public static class HRef {
		private String href;

		public String getHref() {
			return href;
		}

		public void setHref(String href) {
			this.href = href;
		}
		
	}
}
