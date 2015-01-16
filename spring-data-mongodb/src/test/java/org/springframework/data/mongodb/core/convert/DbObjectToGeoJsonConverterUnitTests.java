/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.data.mongodb.core.convert;

import static org.hamcrest.core.IsInstanceOf.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;

import org.hamcrest.core.IsEqual;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.data.mongodb.core.convert.GeoConverters.DbObjectToGeoJsonConverter;
import org.springframework.data.mongodb.core.geo.GeoJson;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * @author Christoph Strobl
 */
public class DbObjectToGeoJsonConverterUnitTests {

	public @Rule ExpectedException expectedException = ExpectedException.none();

	private final DBObject point;
	private final DBObject polygon;

	private DbObjectToGeoJsonConverter converter = DbObjectToGeoJsonConverter.INSTANCE;

	public DbObjectToGeoJsonConverterUnitTests() {

		BasicDBList pointCords = new BasicDBList();
		pointCords.add(100D);
		pointCords.add(50D);
		this.point = new BasicDBObjectBuilder().add("type", "Point").add("coordinates", pointCords).get();

		BasicDBList polygonCords = new BasicDBList();
		BasicDBList outerPolygonCords = new BasicDBList();
		outerPolygonCords.add(toCords(0, 0));
		outerPolygonCords.add(toCords(50, 50));
		outerPolygonCords.add(toCords(0, 100));
		outerPolygonCords.add(toCords(0, 0));
		polygonCords.add(outerPolygonCords);
		this.polygon = new BasicDBObjectBuilder().add("type", "Polygon").add("coordinates", polygonCords).get();
	}

	/**
	 * @see DATAMONGO-1137
	 */
	@Test
	public void shouldReturnNullWhenDbObjectIsNull() {
		assertThat(converter.convert(null), nullValue());
	}

	/**
	 * @see DATAMONGO-1137
	 */
	@Test
	public void shouldConvertDbObjectToPointCorrectly() {

		GeoJson<?> converted = converter.convert(point);

		assertThat(converted.getGeometry(), instanceOf(Point.class));
		assertThat(converted.getGeometry(), IsEqual.<Object> equalTo(new Point(100, 50)));
	}

	/**
	 * @see DATAMONGO-1137
	 */
	@Test
	public void shouldConvertDbObjectToPolygonCorrectly() {

		GeoJson<?> converted = converter.convert(polygon);

		assertThat(converted.getGeometry(), instanceOf(Polygon.class));
		assertThat(converted.getGeometry(),
				IsEqual.<Object> equalTo(new Polygon(new Point(0, 0), new Point(50, 50), new Point(0, 100), new Point(0, 0))));
	}

	/**
	 * @see DATAMONGO-1137
	 */
	@Test
	public void shouldThrowExceptionOnUnknownTypeParameter() {

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Unknown GeoJson type foo!");

		converter.convert(new BasicDBObjectBuilder().add("type", "foo").add("coordinates", new BasicDBList()).get());
	}

	/**
	 * @see DATAMONGO-1137
	 */
	@Test
	public void shouldThrowExceptionOnMissingTypeParameter() {

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("specify type");

		converter.convert(new BasicDBObjectBuilder().add("coordinates", new BasicDBList()).get());
	}

	private BasicDBList toCords(double x, double y) {

		BasicDBList pointCords = new BasicDBList();
		pointCords.add(x);
		pointCords.add(y);

		return pointCords;
	}

}
