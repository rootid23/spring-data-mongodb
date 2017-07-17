/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mongodb.core;

import org.bson.Document;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.data.mongodb.microbenchmark.AbstractMicrobenchmark;

import com.mongodb.MongoClient;

/**
 * @author Mark Paluch
 */
@State(Scope.Benchmark)
public class IntegrativeMongoTemplateBenchmark extends AbstractMicrobenchmark {

	private MongoTemplate template;

	@Setup
	public void setUp() {

		this.template = new MongoTemplate(new MongoClient(), "IntegrativeMongoTemplateBenchmark");

		this.template.dropCollection("one_document");
		this.template.dropCollection("ten_documents");

		this.template.insert(new Document("firstname", "Dave").append("lastname", "Matthews"), "one_document");

		for (int i = 0; i < 10; i++) {
			this.template.insert(new Document("firstname", "Dave").append("lastname", "Matthews"), "ten_documents");
		}
	}

	@Benchmark
	public Object readFindOne() {
		return template.findAll(Document.class, "one_document");
	}

	@Benchmark
	public Object readFindTen() {
		return template.findAll(Document.class, "ten_documents");
	}
}
