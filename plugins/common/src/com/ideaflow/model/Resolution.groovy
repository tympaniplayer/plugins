package com.ideaflow.model

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Resolution extends Event {

	String answer

	Resolution() {}

	Resolution(String answer) {
		this.answer = answer
	}

	String toString() {
		"Conflict: $created, $answer"
	}

}