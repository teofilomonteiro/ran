/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2022-02-22
 */
package io.ran;

import javax.inject.Inject;

public class WithMethodInjection {
	@PrimaryKey
	private String id;
	private transient MyDep myDep;

	@Inject
	public void _inject(MyDep myDep) {

		this.myDep = myDep;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String morphed() {
		return myDep.morphId(id);
	}
}
