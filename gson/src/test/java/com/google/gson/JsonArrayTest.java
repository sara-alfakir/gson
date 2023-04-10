/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import com.google.common.testing.EqualsTester;
import com.google.gson.common.MoreAsserts;
import java.math.BigInteger;
import org.junit.Test;

/**
 * Tests handling of JSON arrays.
 *
 * @author Jesse Wilson
 */
public final class JsonArrayTest {

   private JsonArray array;

    @Before
    public void setUp() {
        array = new JsonArray();
    }

  @Test
  public void testEqualsOnEmptyArray() {
    MoreAsserts.assertEqualsAndHashCode(new JsonArray(), new JsonArray());
  }

  @Test
  public void testEqualsNonEmptyArray() {
    JsonArray b = new JsonArray();

    new EqualsTester().addEqualityGroup(a).testEquals();

    array.add(new JsonObject());
    assertThat(array.equals(b)).isFalse();
    assertThat(b.equals(array)).isFalse();

    b.add(new JsonObject());
    MoreAsserts.assertEqualsAndHashCode(array, b);

    array.add(new JsonObject());
    assertThat(array.equals(b)).isFalse();
    assertThat(b.equals(array)).isFalse();

    b.add(JsonNull.INSTANCE);
    assertThat(array.equals(b)).isFalse();
    assertThat(b.equals(a)).isFalse();
  }

  @Test
  public void testRemove() {
    try {
      array.remove(0);
      fail();
    } catch (IndexOutOfBoundsException expected) {}
    JsonPrimitive a = new JsonPrimitive("a");
    array.add(a);
    assertThat(array.remove(a)).isTrue();
    assertThat(array).doesNotContain(a);
    array.add(a);
    array.add(new JsonPrimitive("b"));
    assertThat(array.remove(1).getAsString()).isEqualTo("b");
    assertThat(array).hasSize(1);
    assertThat(array).contains(a);
  }

  @Test
  public void testSet() {
    try {
      array.set(0, new JsonPrimitive(1));
      fail();
    } catch (IndexOutOfBoundsException expected) {}
    JsonPrimitive a = new JsonPrimitive("a");
    array.add(a);

    JsonPrimitive b = new JsonPrimitive("b");
    JsonElement oldValue = array.set(0, b);
    assertThat(oldValue).isEqualTo(a);
    assertThat(array.get(0).getAsString()).isEqualTo("b");

    oldValue = array.set(0, null);
    assertThat(oldValue).isEqualTo(b);
    assertThat(array.get(0)).isEqualTo(JsonNull.INSTANCE);

    oldValue = array.set(0, new JsonPrimitive("c"));
    assertThat(oldValue).isEqualTo(JsonNull.INSTANCE);
    assertThat(array.get(0).getAsString()).isEqualTo("c");
    assertThat(array).hasSize(1);
  }

  @Test
  public void testDeepCopy() {
    JsonArray original = new JsonArray();
    JsonArray firstEntry = new JsonArray();
    original.add(firstEntry);

    JsonArray copy = original.deepCopy();
    original.add(new JsonPrimitive("y"));

    assertThat(copy).hasSize(1);
    firstEntry.add(new JsonPrimitive("z"));

    assertThat(original.get(0).getAsJsonArray()).hasSize(1);
    assertThat(copy.get(0).getAsJsonArray()).hasSize(0);
  }

  @Test
  public void testIsEmpty() {
    assertThat(array).isEmpty();

    JsonPrimitive a = new JsonPrimitive("a");
    array.add(a);
    assertThat(array).isNotEmpty();

    array.remove(0);
    assertThat(array).isEmpty();
  }

  @Test
  public void testFailedGetArrayValues() {
    
    array.add(JsonParser.parseString("{" + "\"key1\":\"value1\"," + "\"key2\":\"value2\"," + "\"key3\":\"value3\"," + "\"key4\":\"value4\"" + "}"));
    try {
      array.getAsBoolean();
      fail("expected getBoolean to fail");
    } catch (UnsupportedOperationException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("JsonObject");
    }
    try {
      array.get(-1);
      fail("expected get to fail");
    } catch (IndexOutOfBoundsException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("Index -1 out of bounds for length 1");
    }
    try {
      array.getAsString();
      fail("expected getString to fail");
    } catch (UnsupportedOperationException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("JsonObject");
    }

    array.remove(0);
    array.add("hello");
    try {
      array.getAsDouble();
      fail("expected getDouble to fail");
    } catch (NumberFormatException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("For input string: \"hello\"");
    }
    try {
      array.getAsInt();
      fail("expected getInt to fail");
    } catch (NumberFormatException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("For input string: \"hello\"");
    }
    try {
      array.get(0).getAsJsonArray();
      fail("expected getJSONArray to fail");
    } catch (IllegalStateException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("Not a JSON Array: \"hello\"");
    }
    try {
      array.getAsJsonObject();
      fail("expected getJSONObject to fail");
    } catch (IllegalStateException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo( "Not a JSON Object: [\"hello\"]");
    }
    try {
      array.getAsLong();
      fail("expected getLong to fail");
    } catch (NumberFormatException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("For input string: \"hello\"");
    }
  }

  @Test
  public void testGetAs_WrongArraySize() {
    
    try {
      array.getAsByte();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo("Array must have size 1, but has size 0");
    }

    array.add(true);
    array.add(false);
    try {
      array.getAsByte();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo("Array must have size 1, but has size 2");
    }
  }

  @Test
  public void testStringPrimitiveAddition() {
    

    array.add("Hello");
    array.add("Goodbye");
    array.add("Thank you");
    array.add((String) null);
    array.add("Yes");

    assertThat(array.toString()).isEqualTo("[\"Hello\",\"Goodbye\",\"Thank you\",null,\"Yes\"]");
  }

  @Test
  public void testIntegerPrimitiveAddition() {
  

    int x = 1;
    array.add(x);

    x = 2;
    array.add(x);

    x = -3;
    array.add(x);

    array.add((Integer) null);

    x = 4;
    array.add(x);

    x = 0;
    array.add(x);

    assertThat(array.toString()).isEqualTo("[1,2,-3,null,4,0]");
  }

  @Test
  public void testDoublePrimitiveAddition() {
    
    double x = 1.0;
    array.add(x);

    x = 2.13232;
    array.add(x);

    x = 0.121;
    array.add(x);

    array.add((Double) null);

    x = -0.00234;
    array.add(x);

    array.add((Double) null);

    assertThat(array.toString()).isEqualTo("[1.0,2.13232,0.121,null,-0.00234,null]");
  }

  @Test
  public void testBooleanPrimitiveAddition() {
    
    array.add(true);
    array.add(true);
    array.add(false);
    array.add(false);
    array.add((Boolean) null);
    array.add(true);

    assertThat(array.toString()).isEqualTo("[true,true,false,false,null,true]");
  }

  @Test
  public void testCharPrimitiveAddition() {
   
    array.add('a');
    array.add('e');
    array.add('i');
    array.add((char) 111);
    array.add((Character) null);
    array.add('u');
    array.add("and sometimes Y");

    assertThat(array.toString()).isEqualTo("[\"a\",\"e\",\"i\",\"o\",null,\"u\",\"and sometimes Y\"]");
  }

  @Test
  public void testMixedPrimitiveAddition() {
   
    array.add('a');
    array.add("apple");
    array.add(12121);
    array.add((char) 111);

    array.add((Boolean) null);
    assertThat(array.get(array.size() - 1)).isEqualTo(JsonNull.INSTANCE);

    array.add((Character) null);
    assertThat(array.get(array.size() - 1)).isEqualTo(JsonNull.INSTANCE);

    array.add(12.232);
    array.add(BigInteger.valueOf(2323));

    assertThat(array.toString()).isEqualTo("[\"a\",\"apple\",12121,\"o\",null,null,12.232,2323]");
  }

  @Test
  public void testNullPrimitiveAddition() {
   
    array.add((Character) null);
    array.add((Boolean) null);
    array.add((Integer) null);
    array.add((Double) null);
    array.add((Float) null);
    array.add((BigInteger) null);
    array.add((String) null);
    array.add((Boolean) null);
    array.add((Number) null);

    assertThat(array.toString()).isEqualTo("[null,null,null,null,null,null,null,null,null]");
    for (int i = 0; i < array.size(); i++) {
      // Verify that they are actually a JsonNull and not a Java null
      assertThat(array.get(i)).isEqualTo(JsonNull.INSTANCE);
    }
  }

  @Test
  public void testNullJsonElementAddition() {
 
    array.add((JsonElement) null);
    assertThat(array.get(0)).isEqualTo(JsonNull.INSTANCE);
  }

  @Test
  public void testSameAddition() {
   
    array.add('a');
    array.add('a');
    array.add(true);
    array.add(true);
    array.add(1212);
    array.add(1212);
    array.add(34.34);
    array.add(34.34);
    array.add((Boolean) null);
    array.add((Boolean) null);

    assertThat(array.toString()).isEqualTo("[\"a\",\"a\",true,true,1212,1212,34.34,34.34,null,null]");
  }
}
