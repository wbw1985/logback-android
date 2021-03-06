/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LoggerNameUtilTest {


  @Test
  public void smoke0() {
    List<String> witnessList = new ArrayList<String>();
    witnessList.add("a");
    witnessList.add("b");
    witnessList.add("c");
    List<String> partList = computeNameParts("a.b.c");
    assertEquals(witnessList, partList);
  }

  @Test
  public void smoke1() {
    List<String> witnessList = new ArrayList<String>();
    witnessList.add("com");
    witnessList.add("foo");
    witnessList.add("Bar");
    List<String> partList = computeNameParts("com.foo.Bar");
    assertEquals(witnessList, partList);
  }

  @Test
  public void emptyStringShouldReturnAListContainingOneEmptyString() {
    List<String> witnessList = new ArrayList<String>();
    witnessList.add("");
    List<String> partList = computeNameParts("");
    assertEquals(witnessList, partList);
  }

  @Test
  public void dotAtLastPositionShouldReturnAListWithAnEmptyStringAsLastElement() {
    List<String> witnessList = new ArrayList<String>();
    witnessList.add("com");
    witnessList.add("foo");
    witnessList.add("");

    List<String> partList = computeNameParts("com.foo.");
    assertEquals(witnessList, partList);
  }

  @Test
  public void supportNestedClasses() {
    List<String> witnessList = new ArrayList<String>();
    witnessList.add("com");
    witnessList.add("foo");
    witnessList.add("Bar");
    witnessList.add("Nested");

    List<String> partList = computeNameParts("com.foo.Bar$Nested");
    assertEquals(witnessList, partList);
  }

  private List<String> computeNameParts(String loggerName) {
    List<String> partList = new ArrayList<String>();

    int fromIndex = 0;
    while(true) {
      int index = LoggerNameUtil.getSeparatorIndexOf(loggerName, fromIndex);
      if(index == -1) {
       partList.add(loggerName.substring(fromIndex));
       break;
      }
      partList.add(loggerName.substring(fromIndex, index));
      fromIndex = index+1;
    }
    return partList;
  }
}
