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

import reactor.core.publisher.Mono;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.client.result.UpdateResult;

/**
 * {@link ReactiveExecutableUpdateOperation} allows creation and execution of reactive MongoDB update / findAndModify
 * operations in a fluent API style. <br />
 * The starting {@literal domainType} is used for mapping the {@link Query} provided via {@code matching}, as well as
 * the {@link Update} via {@code apply} into the MongoDB specific representations. The collection to operate on is by
 * default derived from the initial {@literal domainType} and can be defined there via
 * {@link org.springframework.data.mongodb.core.mapping.Document}. Using {@code inCollection} allows to override the
 * collection name for the execution.
 *
 * <pre>
 *     <code>
 *         update(Jedi.class)
 *             .inCollection("star-wars")
 *             .matching(query(where("firstname").is("luke")))
 *             .apply(new Update().set("lastname", "skywalker"))
 *             .upsert();
 *     </code>
 * </pre>
 *
 * @author Mark Paluch
 * @since 2.0
 */
public interface ReactiveExecutableUpdateOperation {

	/**
	 * Start creating an update operation for the given {@literal domainType}.
	 *
	 * @param domainType must not be {@literal null}.
	 * @return new instance of {@link ReactiveUpdateOperation}.
	 * @throws IllegalArgumentException if domainType is {@literal null}.
	 */
	<T> ReactiveUpdateOperation<T> update(Class<T> domainType);

	/**
	 * Compose findAndModify execution by calling one of the terminating methods.
	 */
	interface TerminatingReactiveFindAndModifyOperation<T> {

		/**
		 * Find, modify and return the first matching document.
		 *
		 * @return {@link Mono#empty()} if nothing found.
		 */
		Mono<T> findAndModify();
	}

	/**
	 * Compose update execution by calling one of the terminating methods.
	 */
	interface TerminatingReactiveUpdateOperation<T>
			extends TerminatingReactiveFindAndModifyOperation<T>, ReactiveFindAndModifyWithOptions<T> {

		/**
		 * Update all matching documents in the collection.
		 *
		 * @return never {@literal null}.
		 */
		Mono<UpdateResult> all();

		/**
		 * Update the first document in the collection.
		 *
		 * @return never {@literal null}.
		 */
		Mono<UpdateResult> first();

		/**
		 * Creates a new document if no documents match the filter query or updates the matching ones.
		 *
		 * @return never {@literal null}.
		 */
		Mono<UpdateResult> upsert();
	}

	interface ReactiveUpdateOperation<T> extends ReactiveUpdateOperationWithCollection<T>,
			ReactiveUpdateOperationWithQuery<T>, ReactiveUpdateOperationWithUpdate<T> {}

	/**
	 * Declare the {@link Update} to apply.
	 */
	interface ReactiveUpdateOperationWithUpdate<T> {

		/**
		 * Set the {@link Update} to be applied.
		 *
		 * @param update must not be {@literal null}.
		 * @return new instance of {@link TerminatingReactiveUpdateOperation}.
		 * @throws IllegalArgumentException if update is {@literal null}.
		 */
		TerminatingReactiveUpdateOperation<T> apply(Update update);
	}

	/**
	 * Explicitly define the name of the collection to perform operation in (optional).
	 */
	interface ReactiveUpdateOperationWithCollection<T> {

		/**
		 * Explicitly set the name of the collection to perform the query on. <br />
		 * Skip this step to use the default collection derived from the domain type.
		 *
		 * @param collection must not be {@literal null} nor {@literal empty}.
		 * @return new instance of {@link ReactiveUpdateOperationWithCollection}.
		 * @throws IllegalArgumentException if collection is {@literal null}.
		 */
		ReactiveUpdateOperationWithQuery<T> inCollection(String collection);
	}

	/**
	 * Define a filter query for the {@link Update} (optional).
	 */
	interface ReactiveUpdateOperationWithQuery<T> extends ReactiveUpdateOperationWithUpdate<T> {

		/**
		 * Filter documents by given {@literal query}.
		 *
		 * @param query must not be {@literal null}.
		 * @return new instance of {@link ReactiveUpdateOperationWithQuery}.
		 * @throws IllegalArgumentException if query is {@literal null}.
		 */
		ReactiveUpdateOperationWithUpdate<T> matching(Query query);
	}

	/**
	 * Define {@link FindAndModifyOptions} (optional).
	 */
	interface ReactiveFindAndModifyWithOptions<T> {

		/**
		 * Explicitly define {@link FindAndModifyOptions} for the {@link Update}.
		 *
		 * @param options must not be {@literal null}.
		 * @return new instance of {@link ReactiveFindAndModifyWithOptions}.
		 * @throws IllegalArgumentException if options is {@literal null}.
		 */
		TerminatingReactiveFindAndModifyOperation<T> withOptions(FindAndModifyOptions options);
	}
}
