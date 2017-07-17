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

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import org.bson.Document;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.data.mongodb.microbenchmark.AbstractMicrobenchmark;

import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.connection.netty.NettyStreamFactoryFactory;
import com.mongodb.reactivestreams.client.MongoClients;

/**
 * @author Mark Paluch
 */
@State(Scope.Benchmark)
public class IntegrativeReactiveMongoTemplateBenchmark extends AbstractMicrobenchmark {

	private ReactiveMongoTemplate template;
	private final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("foo", true));

	@Setup
	public void setUp() {

		MongoClientSettings settings = MongoClientSettings.builder(MongoClients.create().getSettings())
				.streamFactoryFactory(NettyStreamFactoryFactory.builder().allocator(PooledByteBufAllocator.DEFAULT)
						.eventLoopGroup(eventLoopGroup).socketChannelClass(NioSocketChannel.class).build())
				.build();

		this.template = new ReactiveMongoTemplate(MongoClients.create(settings), "IntegrativeMongoTemplateBenchmark");

		this.template.dropCollection("one_document").block();
		this.template.dropCollection("ten_documents").block();

		this.template.insert(new Document("firstname", "Dave").append("lastname", "Matthews"), "one_document").block();

		for (int i = 0; i < 10; i++) {
			this.template.insert(new Document("firstname", "Dave").append("lastname", "Matthews"), "ten_documents").block();
		}
	}

	@Benchmark
	public Object readFindOne() {
		return template.findAll(Document.class, "one_document").blockLast();
	}

	@Benchmark
	public Object readFindTen() {
		return template.findAll(Document.class, "ten_documents").blockLast();
	}
}
