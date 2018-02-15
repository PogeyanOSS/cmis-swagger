/**
 * Copyright 2017 Pogeyan Technologies
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
