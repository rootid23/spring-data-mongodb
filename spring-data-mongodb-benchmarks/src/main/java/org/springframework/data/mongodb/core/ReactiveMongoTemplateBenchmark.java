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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.microbenchmark.AbstractMicrobenchmark;

import com.mongodb.CursorType;
import com.mongodb.MongoNamespace;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.AggregatePublisher;
import com.mongodb.reactivestreams.client.DistinctPublisher;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.ListCollectionsPublisher;
import com.mongodb.reactivestreams.client.ListIndexesPublisher;
import com.mongodb.reactivestreams.client.MapReducePublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.Success;

/**
 * @author Mark Paluch
 */
@State(Scope.Benchmark)
public class ReactiveMongoTemplateBenchmark extends AbstractMicrobenchmark {

	private ReactiveMongoTemplate template;
	private Document documentWith2Properties;
	private final MockCollection collection = new MockCollection();
	private MockFindPublisher SINGLE_PUBLISHER;
	private MockFindPublisher TEN_PUBLISHER;

	@Setup
	public void setUp() {

		this.template = new ReactiveMongoTemplate(new MockReactiveMongoDatabaseFactory(new MockDatabase(collection)));
		this.template.getCollectionName(Customer.class);

		this.documentWith2Properties = new Document("firstname", "Dave").append("lastname", "Matthews");

		SINGLE_PUBLISHER = new MockFindPublisher(Flux.just(documentWith2Properties));
		TEN_PUBLISHER = new MockFindPublisher(Flux.just(documentWith2Properties, documentWith2Properties,
				documentWith2Properties, documentWith2Properties, documentWith2Properties, documentWith2Properties,
				documentWith2Properties, documentWith2Properties, documentWith2Properties, documentWith2Properties));

	}

	@Benchmark
	public Object readFindOne() {

		collection.setFindPublisher(SINGLE_PUBLISHER);
		return template.findAll(Customer.class).blockLast();
	}

	@Benchmark
	public Object readFindTen() {

		collection.setFindPublisher(TEN_PUBLISHER);
		return template.findAll(Customer.class).blockLast();
	}

	@RequiredArgsConstructor
	static class MockReactiveMongoDatabaseFactory implements ReactiveMongoDatabaseFactory {

		private static final MongoExceptionTranslator EXCEPTION_TRANSLATOR = new MongoExceptionTranslator();

		private final MongoDatabase database;

		@Override
		public MongoDatabase getMongoDatabase() throws DataAccessException {
			return database;
		}

		@Override
		public MongoDatabase getMongoDatabase(String dbName) throws DataAccessException {
			return database;
		}

		@Override
		public PersistenceExceptionTranslator getExceptionTranslator() {
			return EXCEPTION_TRANSLATOR;
		}
	}

	@RequiredArgsConstructor
	static class MockFindPublisher implements FindPublisher<Document> {

		private final Publisher<Document> delegate;

		@Override
		public Publisher<Document> first() {
			return this;
		}

		@Override
		public FindPublisher<Document> filter(Bson filter) {
			return this;
		}

		@Override
		public FindPublisher<Document> limit(int limit) {
			return this;
		}

		@Override
		public FindPublisher<Document> skip(int skip) {
			return this;
		}

		@Override
		public FindPublisher<Document> maxTime(long maxTime, TimeUnit timeUnit) {
			return this;
		}

		@Override
		public FindPublisher<Document> maxAwaitTime(long maxAwaitTime, TimeUnit timeUnit) {
			return this;
		}

		@Override
		public FindPublisher<Document> modifiers(Bson modifiers) {
			return this;
		}

		@Override
		public FindPublisher<Document> projection(Bson projection) {
			return this;
		}

		@Override
		public FindPublisher<Document> sort(Bson sort) {
			return this;
		}

		@Override
		public FindPublisher<Document> noCursorTimeout(boolean noCursorTimeout) {
			return this;
		}

		@Override
		public FindPublisher<Document> oplogReplay(boolean oplogReplay) {
			return this;
		}

		@Override
		public FindPublisher<Document> partial(boolean partial) {
			return this;
		}

		@Override
		public FindPublisher<Document> cursorType(CursorType cursorType) {
			return this;
		}

		@Override
		public FindPublisher<Document> collation(Collation collation) {
			return this;
		}

		@Override
		public void subscribe(Subscriber<? super Document> s) {
			delegate.subscribe(s);
		}
	}

	static class MockCollection implements MongoCollection<Document> {

		@Setter private FindPublisher<Document> findPublisher;

		@Override
		public MongoNamespace getNamespace() {
			return null;
		}

		@Override
		public Class<Document> getDocumentClass() {
			return null;
		}

		@Override
		public CodecRegistry getCodecRegistry() {
			return null;
		}

		@Override
		public ReadPreference getReadPreference() {
			return null;
		}

		@Override
		public WriteConcern getWriteConcern() {
			return null;
		}

		@Override
		public ReadConcern getReadConcern() {
			return null;
		}

		@Override
		public <NewTDocument> MongoCollection<NewTDocument> withDocumentClass(Class<NewTDocument> clazz) {
			return null;
		}

		@Override
		public MongoCollection<Document> withCodecRegistry(CodecRegistry codecRegistry) {
			return this;
		}

		@Override
		public MongoCollection<Document> withReadPreference(ReadPreference readPreference) {
			return this;
		}

		@Override
		public MongoCollection<Document> withWriteConcern(WriteConcern writeConcern) {
			return this;
		}

		@Override
		public MongoCollection<Document> withReadConcern(ReadConcern readConcern) {
			return this;
		}

		@Override
		public Publisher<Long> count() {
			return null;
		}

		@Override
		public Publisher<Long> count(Bson filter) {
			return null;
		}

		@Override
		public Publisher<Long> count(Bson filter, CountOptions options) {
			return null;
		}

		@Override
		public <TResult> DistinctPublisher<TResult> distinct(String fieldName, Class<TResult> tResultClass) {
			return null;
		}

		@Override
		public <TResult> DistinctPublisher<TResult> distinct(String fieldName, Bson filter, Class<TResult> tResultClass) {
			return null;
		}

		@Override
		public FindPublisher<Document> find() {
			return findPublisher;
		}

		@Override
		public <TResult> FindPublisher<TResult> find(Class<TResult> clazz) {
			return (FindPublisher) findPublisher;
		}

		@Override
		public FindPublisher<Document> find(Bson filter) {
			return findPublisher;
		}

		@Override
		public <TResult> FindPublisher<TResult> find(Bson filter, Class<TResult> clazz) {
			return (FindPublisher) findPublisher;
		}

		@Override
		public AggregatePublisher<Document> aggregate(List<? extends Bson> pipeline) {
			return null;
		}

		@Override
		public <TResult> AggregatePublisher<TResult> aggregate(List<? extends Bson> pipeline, Class<TResult> clazz) {
			return null;
		}

		@Override
		public MapReducePublisher<Document> mapReduce(String mapFunction, String reduceFunction) {
			return null;
		}

		@Override
		public <TResult> MapReducePublisher<TResult> mapReduce(String mapFunction, String reduceFunction,
				Class<TResult> clazz) {
			return null;
		}

		@Override
		public Publisher<BulkWriteResult> bulkWrite(List<? extends WriteModel<? extends Document>> requests) {
			return null;
		}

		@Override
		public Publisher<BulkWriteResult> bulkWrite(List<? extends WriteModel<? extends Document>> requests,
				BulkWriteOptions options) {
			return null;
		}

		@Override
		public Publisher<Success> insertOne(Document document) {
			return null;
		}

		@Override
		public Publisher<Success> insertOne(Document document, InsertOneOptions options) {
			return null;
		}

		@Override
		public Publisher<Success> insertMany(List<? extends Document> documents) {
			return null;
		}

		@Override
		public Publisher<Success> insertMany(List<? extends Document> documents, InsertManyOptions options) {
			return null;
		}

		@Override
		public Publisher<DeleteResult> deleteOne(Bson filter) {
			return null;
		}

		@Override
		public Publisher<DeleteResult> deleteOne(Bson filter, DeleteOptions options) {
			return null;
		}

		@Override
		public Publisher<DeleteResult> deleteMany(Bson filter) {
			return null;
		}

		@Override
		public Publisher<DeleteResult> deleteMany(Bson filter, DeleteOptions options) {
			return null;
		}

		@Override
		public Publisher<UpdateResult> replaceOne(Bson filter, Document replacement) {
			return null;
		}

		@Override
		public Publisher<UpdateResult> replaceOne(Bson filter, Document replacement, UpdateOptions options) {
			return null;
		}

		@Override
		public Publisher<UpdateResult> updateOne(Bson filter, Bson update) {
			return null;
		}

		@Override
		public Publisher<UpdateResult> updateOne(Bson filter, Bson update, UpdateOptions options) {
			return null;
		}

		@Override
		public Publisher<UpdateResult> updateMany(Bson filter, Bson update) {
			return null;
		}

		@Override
		public Publisher<UpdateResult> updateMany(Bson filter, Bson update, UpdateOptions options) {
			return null;
		}

		@Override
		public Publisher<Document> findOneAndDelete(Bson filter) {
			return null;
		}

		@Override
		public Publisher<Document> findOneAndDelete(Bson filter, FindOneAndDeleteOptions options) {
			return null;
		}

		@Override
		public Publisher<Document> findOneAndReplace(Bson filter, Document replacement) {
			return null;
		}

		@Override
		public Publisher<Document> findOneAndReplace(Bson filter, Document replacement, FindOneAndReplaceOptions options) {
			return null;
		}

		@Override
		public Publisher<Document> findOneAndUpdate(Bson filter, Bson update) {
			return null;
		}

		@Override
		public Publisher<Document> findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options) {
			return null;
		}

		@Override
		public Publisher<Success> drop() {
			return null;
		}

		@Override
		public Publisher<String> createIndex(Bson key) {
			return null;
		}

		@Override
		public Publisher<String> createIndex(Bson key, IndexOptions options) {
			return null;
		}

		@Override
		public Publisher<String> createIndexes(List<IndexModel> indexes) {
			return null;
		}

		@Override
		public ListIndexesPublisher<Document> listIndexes() {
			return null;
		}

		@Override
		public <TResult> ListIndexesPublisher<TResult> listIndexes(Class<TResult> clazz) {
			return null;
		}

		@Override
		public Publisher<Success> dropIndex(String indexName) {
			return null;
		}

		@Override
		public Publisher<Success> dropIndex(Bson keys) {
			return null;
		}

		@Override
		public Publisher<Success> dropIndexes() {
			return null;
		}

		@Override
		public Publisher<Success> renameCollection(MongoNamespace newCollectionNamespace) {
			return null;
		}

		@Override
		public Publisher<Success> renameCollection(MongoNamespace newCollectionNamespace, RenameCollectionOptions options) {
			return null;
		}
	}

	@RequiredArgsConstructor
	static class MockDatabase implements MongoDatabase {

		private final MongoCollection<Document> collection;

		@Override
		public String getName() {
			return null;
		}

		@Override
		public CodecRegistry getCodecRegistry() {
			return null;
		}

		@Override
		public ReadPreference getReadPreference() {
			return null;
		}

		@Override
		public WriteConcern getWriteConcern() {
			return null;
		}

		@Override
		public ReadConcern getReadConcern() {
			return null;
		}

		@Override
		public MongoDatabase withCodecRegistry(CodecRegistry codecRegistry) {
			return this;
		}

		@Override
		public MongoDatabase withReadPreference(ReadPreference readPreference) {
			return this;
		}

		@Override
		public MongoDatabase withWriteConcern(WriteConcern writeConcern) {
			return this;
		}

		@Override
		public MongoDatabase withReadConcern(ReadConcern readConcern) {
			return this;
		}

		@Override
		public MongoCollection<Document> getCollection(String collectionName) {
			return collection;
		}

		@Override
		public <TDocument> MongoCollection<TDocument> getCollection(String collectionName, Class<TDocument> clazz) {
			return null;
		}

		@Override
		public Publisher<Document> runCommand(Bson command) {
			return null;
		}

		@Override
		public Publisher<Document> runCommand(Bson command, ReadPreference readPreference) {
			return null;
		}

		@Override
		public <TResult> Publisher<TResult> runCommand(Bson command, Class<TResult> clazz) {
			return null;
		}

		@Override
		public <TResult> Publisher<TResult> runCommand(Bson command, ReadPreference readPreference, Class<TResult> clazz) {
			return null;
		}

		@Override
		public Publisher<Success> drop() {
			return null;
		}

		@Override
		public Publisher<String> listCollectionNames() {
			return null;
		}

		@Override
		public ListCollectionsPublisher<Document> listCollections() {
			return null;
		}

		@Override
		public <TResult> ListCollectionsPublisher<TResult> listCollections(Class<TResult> clazz) {
			return null;
		}

		@Override
		public Publisher<Success> createCollection(String collectionName) {
			return null;
		}

		@Override
		public Publisher<Success> createCollection(String collectionName, CreateCollectionOptions options) {
			return null;
		}

		@Override
		public Publisher<Success> createView(String viewName, String viewOn, List<? extends Bson> pipeline) {
			return null;
		}

		@Override
		public Publisher<Success> createView(String viewName, String viewOn, List<? extends Bson> pipeline,
				CreateViewOptions createViewOptions) {
			return null;
		}
	}

	@Getter
	@RequiredArgsConstructor
	static class Customer {

		private @Id ObjectId id;
		private final String firstname, lastname;
	}
}
