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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.microbenchmark.AbstractMicrobenchmark;

import com.mongodb.*;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

/**
 * @author Mark Paluch
 */
@State(Scope.Benchmark)
public class MongoTemplateBenchmark extends AbstractMicrobenchmark {

	private MongoTemplate template;
	private Document documentWith2Properties;
	private final MockCollection collection = new MockCollection();
	private MockFindIterable<Document> SINGLE_ITERABLE;
	private MockFindIterable<Document> TEN_ITERABLE;

	@Setup
	public void setUp() {

		this.template = new MongoTemplate(new MockMongoDatabaseFactory(new MockDatabase(collection)));
		this.template.getCollectionName(Customer.class);

		this.documentWith2Properties = new Document("firstname", "Dave").append("lastname", "Matthews");

		SINGLE_ITERABLE = new MockFindIterable<>(Collections.singletonList(documentWith2Properties));

		List<Document> tenDocuments = Stream.generate(() -> documentWith2Properties).limit(10).collect(Collectors.toList());

		TEN_ITERABLE = new MockFindIterable<>(tenDocuments);
	}

	@Benchmark
	public Object readFindOne() {
		collection.setFindIterable(SINGLE_ITERABLE);
		return template.findAll(Customer.class);
	}

	@Benchmark
	public Object readFindTen() {
		collection.setFindIterable(TEN_ITERABLE);
		return template.findAll(Customer.class);
	}

	@RequiredArgsConstructor
	static class MockMongoDatabaseFactory implements MongoDbFactory {

		private static final MongoExceptionTranslator EXCEPTION_TRANSLATOR = new MongoExceptionTranslator();

		private final MongoDatabase database;

		@Override
		public MongoDatabase getDb() throws DataAccessException {
			return database;
		}

		@Override
		public MongoDatabase getDb(String dbName) throws DataAccessException {
			return database;
		}

		@Override
		public PersistenceExceptionTranslator getExceptionTranslator() {
			return EXCEPTION_TRANSLATOR;
		}

		@Override
		public DB getLegacyDb() {
			return null;
		}
	}

	@RequiredArgsConstructor
	static class MockFindIterable<TResult> implements FindIterable<TResult> {

		private final Collection<TResult> result;

		@Override
		public FindIterable<TResult> filter(Bson filter) {
			return this;
		}

		@Override
		public FindIterable<TResult> limit(int limit) {
			return this;
		}

		@Override
		public FindIterable<TResult> skip(int skip) {
			return this;
		}

		@Override
		public FindIterable<TResult> maxTime(long maxTime, TimeUnit timeUnit) {
			return this;
		}

		@Override
		public FindIterable<TResult> maxAwaitTime(long maxAwaitTime, TimeUnit timeUnit) {
			return this;
		}

		@Override
		public FindIterable<TResult> modifiers(Bson modifiers) {
			return this;
		}

		@Override
		public FindIterable<TResult> projection(Bson projection) {
			return this;
		}

		@Override
		public FindIterable<TResult> sort(Bson sort) {
			return this;
		}

		@Override
		public FindIterable<TResult> noCursorTimeout(boolean noCursorTimeout) {
			return this;
		}

		@Override
		public FindIterable<TResult> oplogReplay(boolean oplogReplay) {
			return this;
		}

		@Override
		public FindIterable<TResult> partial(boolean partial) {
			return this;
		}

		@Override
		public FindIterable<TResult> cursorType(CursorType cursorType) {
			return this;
		}

		@Override
		public FindIterable<TResult> batchSize(int batchSize) {
			return this;
		}

		@Override
		public FindIterable<TResult> collation(Collation collation) {
			return this;
		}

		@Override
		public MongoCursor<TResult> iterator() {
			return new MockCursor<>(result.iterator());
		}

		@Override
		public TResult first() {
			return iterator().next();
		}

		@Override
		public <U> MongoIterable<U> map(Function<TResult, U> mapper) {
			return null;
		}

		@Override
		public void forEach(Block<? super TResult> block) {
			for (TResult tResult : result) {
				block.apply(tResult);
			}
		}

		@Override
		public <A extends Collection<? super TResult>> A into(A target) {
			return null;
		}
	}

	@RequiredArgsConstructor
	static class MockCursor<TResult> implements MongoCursor<TResult> {

		private final Iterator<TResult> delegate;

		@Override
		public void close() {

		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public TResult next() {
			return delegate.next();
		}

		@Override
		public TResult tryNext() {
			return null;
		}

		@Override
		public ServerCursor getServerCursor() {
			return null;
		}

		@Override
		public ServerAddress getServerAddress() {
			return null;
		}
	}

	static class MockCollection implements MongoCollection<Document> {

		@Setter private FindIterable<Document> findIterable;

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
		public long count() {
			return 0;
		}

		@Override
		public long count(Bson filter) {
			return 0;
		}

		@Override
		public long count(Bson filter, CountOptions options) {
			return 0;
		}

		@Override
		public <TResult> DistinctIterable<TResult> distinct(String fieldName, Class<TResult> tResultClass) {
			return null;
		}

		@Override
		public <TResult> DistinctIterable<TResult> distinct(String fieldName, Bson filter, Class<TResult> tResultClass) {
			return null;
		}

		@Override
		public FindIterable<Document> find() {
			return findIterable;
		}

		@Override
		public <TResult> FindIterable<TResult> find(Class<TResult> tResultClass) {
			return (FindIterable) findIterable;
		}

		@Override
		public FindIterable<Document> find(Bson filter) {
			return findIterable;
		}

		@Override
		public <TResult> FindIterable<TResult> find(Bson filter, Class<TResult> tResultClass) {
			return (FindIterable) findIterable;
		}

		@Override
		public AggregateIterable<Document> aggregate(List<? extends Bson> pipeline) {
			return null;
		}

		@Override
		public <TResult> AggregateIterable<TResult> aggregate(List<? extends Bson> pipeline, Class<TResult> tResultClass) {
			return null;
		}

		@Override
		public MapReduceIterable<Document> mapReduce(String mapFunction, String reduceFunction) {
			return null;
		}

		@Override
		public <TResult> MapReduceIterable<TResult> mapReduce(String mapFunction, String reduceFunction,
				Class<TResult> tResultClass) {
			return null;
		}

		@Override
		public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends Document>> requests) {
			return null;
		}

		@Override
		public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends Document>> requests,
				BulkWriteOptions options) {
			return null;
		}

		@Override
		public void insertOne(Document document) {

		}

		@Override
		public void insertOne(Document document, InsertOneOptions options) {

		}

		@Override
		public void insertMany(List<? extends Document> documents) {

		}

		@Override
		public void insertMany(List<? extends Document> documents, InsertManyOptions options) {

		}

		@Override
		public DeleteResult deleteOne(Bson filter) {
			return null;
		}

		@Override
		public DeleteResult deleteOne(Bson filter, DeleteOptions options) {
			return null;
		}

		@Override
		public DeleteResult deleteMany(Bson filter) {
			return null;
		}

		@Override
		public DeleteResult deleteMany(Bson filter, DeleteOptions options) {
			return null;
		}

		@Override
		public UpdateResult replaceOne(Bson filter, Document replacement) {
			return null;
		}

		@Override
		public UpdateResult replaceOne(Bson filter, Document replacement, UpdateOptions updateOptions) {
			return null;
		}

		@Override
		public UpdateResult updateOne(Bson filter, Bson update) {
			return null;
		}

		@Override
		public UpdateResult updateOne(Bson filter, Bson update, UpdateOptions updateOptions) {
			return null;
		}

		@Override
		public UpdateResult updateMany(Bson filter, Bson update) {
			return null;
		}

		@Override
		public UpdateResult updateMany(Bson filter, Bson update, UpdateOptions updateOptions) {
			return null;
		}

		@Override
		public Document findOneAndDelete(Bson filter) {
			return null;
		}

		@Override
		public Document findOneAndDelete(Bson filter, FindOneAndDeleteOptions options) {
			return null;
		}

		@Override
		public Document findOneAndReplace(Bson filter, Document replacement) {
			return null;
		}

		@Override
		public Document findOneAndReplace(Bson filter, Document replacement, FindOneAndReplaceOptions options) {
			return null;
		}

		@Override
		public Document findOneAndUpdate(Bson filter, Bson update) {
			return null;
		}

		@Override
		public Document findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options) {
			return null;
		}

		@Override
		public void drop() {

		}

		@Override
		public String createIndex(Bson keys) {
			return null;
		}

		@Override
		public String createIndex(Bson keys, IndexOptions indexOptions) {
			return null;
		}

		@Override
		public List<String> createIndexes(List<IndexModel> indexes) {
			return null;
		}

		@Override
		public ListIndexesIterable<Document> listIndexes() {
			return null;
		}

		@Override
		public <TResult> ListIndexesIterable<TResult> listIndexes(Class<TResult> tResultClass) {
			return null;
		}

		@Override
		public void dropIndex(String indexName) {

		}

		@Override
		public void dropIndex(Bson keys) {

		}

		@Override
		public void dropIndexes() {

		}

		@Override
		public void renameCollection(MongoNamespace newCollectionNamespace) {

		}

		@Override
		public void renameCollection(MongoNamespace newCollectionNamespace,
				RenameCollectionOptions renameCollectionOptions) {

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
		public com.mongodb.client.MongoCollection<Document> getCollection(String collectionName) {
			return collection;
		}

		@Override
		public <TDocument> com.mongodb.client.MongoCollection<TDocument> getCollection(String collectionName,
				Class<TDocument> tDocumentClass) {
			return (MongoCollection) collection;
		}

		@Override
		public Document runCommand(Bson command) {
			return null;
		}

		@Override
		public Document runCommand(Bson command, ReadPreference readPreference) {
			return null;
		}

		@Override
		public <TResult> TResult runCommand(Bson command, Class<TResult> tResultClass) {
			return null;
		}

		@Override
		public <TResult> TResult runCommand(Bson command, ReadPreference readPreference, Class<TResult> tResultClass) {
			return null;
		}

		@Override
		public void drop() {

		}

		@Override
		public MongoIterable<String> listCollectionNames() {
			return null;
		}

		@Override
		public ListCollectionsIterable<Document> listCollections() {
			return null;
		}

		@Override
		public <TResult> ListCollectionsIterable<TResult> listCollections(Class<TResult> tResultClass) {
			return null;
		}

		@Override
		public void createCollection(String collectionName) {

		}

		@Override
		public void createCollection(String collectionName, CreateCollectionOptions createCollectionOptions) {

		}

		@Override
		public void createView(String viewName, String viewOn, List<? extends Bson> pipeline) {

		}

		@Override
		public void createView(String viewName, String viewOn, List<? extends Bson> pipeline,
				CreateViewOptions createViewOptions) {

		}
	}

	@Getter
	@RequiredArgsConstructor
	static class Customer {

		private @Id ObjectId id;
		private final String firstname, lastname;
	}
}
