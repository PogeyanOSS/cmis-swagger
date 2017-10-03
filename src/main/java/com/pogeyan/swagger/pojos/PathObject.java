package com.pogeyan.swagger.pojos;

import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(false)
public class PathObject {
	private PathCommonObject post;
	private PathCommonObject get;
	private PathCommonObject put;
	private PathCommonObject delete;

	public PathObject() {
		super();
	}

	public PathObject(PathCommonObject post, PathCommonObject get, PathCommonObject put, PathCommonObject delete) {
		super();
		this.post = post;
		this.get = get;
		this.put = put;
		this.delete = delete;
	}

	public PathCommonObject getPost() {
		return post;
	}

	public void setPost(PathCommonObject post) {
		this.post = post;
	}

	public PathCommonObject getGet() {
		return get;
	}

	public void setGet(PathCommonObject get) {
		this.get = get;
	}

	public PathCommonObject getPut() {
		return put;
	}

	public void setPut(PathCommonObject put) {
		this.put = put;
	}

	public PathCommonObject getDelete() {
		return delete;
	}

	public void setDelete(PathCommonObject delete) {
		this.delete = delete;
	}

}
