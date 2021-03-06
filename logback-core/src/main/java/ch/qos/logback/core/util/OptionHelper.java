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
package ch.qos.logback.core.util;

import java.lang.reflect.Constructor;
import java.util.Properties;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.android.SystemPropertiesProxy;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.subst.NodeToStringTransformer;

/**
 * @author Ceki Gulcu
 */
public class OptionHelper {

  public static Object instantiateByClassName(String className,
                                              Class superClass, Context context) throws IncompatibleClassException,
          DynamicClassLoadingException {
    ClassLoader classLoader = Loader.getClassLoaderOfObject(context);
    return instantiateByClassName(className, superClass, classLoader);
  }

  public static Object instantiateByClassNameAndParameter(String className,
                                                          Class superClass, Context context, Class type, Object param) throws IncompatibleClassException,
          DynamicClassLoadingException {
    ClassLoader classLoader = Loader.getClassLoaderOfObject(context);
    return instantiateByClassNameAndParameter(className, superClass, classLoader, type, param);
  }

  @SuppressWarnings("unchecked")
  public static Object instantiateByClassName(String className,
                                              Class superClass, ClassLoader classLoader)
          throws IncompatibleClassException, DynamicClassLoadingException {
    return instantiateByClassNameAndParameter(className, superClass, classLoader, null, null);
  }

  public static Object instantiateByClassNameAndParameter(String className,
                                                          Class superClass, ClassLoader classLoader, Class type, Object parameter)
          throws IncompatibleClassException, DynamicClassLoadingException {

    if (className == null) {
      throw new NullPointerException();
    }
    try {
      Class classObj = null;
      classObj = classLoader.loadClass(className);
      if (!superClass.isAssignableFrom(classObj)) {
        throw new IncompatibleClassException(superClass, classObj);
      }
      if (type == null) {
        return classObj.newInstance();
      } else {
        Constructor constructor = classObj.getConstructor(type);
        return constructor.newInstance(parameter);
      }
    } catch (IncompatibleClassException ice) {
      throw ice;
    } catch (Throwable t) {
      throw new DynamicClassLoadingException("Failed to instantiate type "
              + className, t);
    }
  }

  /**
   * Find the value corresponding to <code>key</code> in <code>props</code>.
   * Then perform variable substitution on the found value.
   */
  // public static String findAndSubst(String key, Properties props) {
  // String value = props.getProperty(key);
  //
  // if (value == null) {
  // return null;
  // }
  //
  // try {
  // return substVars(value, props);
  // } catch (IllegalArgumentException e) {
  // return value;
  // }
  // }
  final static String DELIM_START = "${";
  final static char DELIM_STOP = '}';
  final static String DELIM_DEFAULT = ":-";

  final static int DELIM_START_LEN = 2;
  final static int DELIM_STOP_LEN = 1;
  final static int DELIM_DEFAULT_LEN = 2;

  final static String _IS_UNDEFINED = "_IS_UNDEFINED";

  /**
   * @see #substVars(String, PropertyContainer, PropertyContainer)
   */
  public static String substVars(String val, PropertyContainer pc1) {
    return substVars(val, pc1, null);
  }

  /**
   * See  http://logback.qos.ch/manual/configuration.html#variableSubstitution
   */
  public static String substVars(String input, PropertyContainer pc0, PropertyContainer pc1) {
    try {
      String replacement = NodeToStringTransformer.substituteVariable(input, pc0, pc1);
      // for backward compatibility sake, perform one level of recursion
      if(replacement.contains(DELIM_START)) {
        replacement =  NodeToStringTransformer.substituteVariable(replacement, pc0, pc1);
      }
      return replacement;
    } catch (ScanException e) {
      throw new IllegalArgumentException("Failed to parse input [" + input + "]", e);
    }
  }

  public static String propertyLookup(String key, PropertyContainer pc1,
                                      PropertyContainer pc2) {
    String value = null;
    // first try the props passed as parameter
    value = pc1.getProperty(key);

    // then try  the pc2
    if (value == null && pc2 != null) {
      value = pc2.getProperty(key);
    }
    // then try in System properties
    if (value == null) {
      value = getSystemProperty(key, null);
    }
    if (value == null) {
      value = getEnv(key);
    }
    return value;
  }

  /**
   * Very similar to <code>System.getProperty</code> except that the
   * {@link SecurityException} is absorbed.
   *
   * @param key The key to search for.
   * @param def The default value to return.
   * @return the string value of the system property, or the default value if
   *         there is no property with that key.
   */
  public static String getSystemProperty(String key, String def) {
    try {
      return System.getProperty(key, def);
    } catch (SecurityException e) {
      return def;
    }
  }

  /**
   * Lookup a key from the environment.
   *
   * @param key
   * @return value corresponding to key from the OS environment
   */
  public static String getEnv(String key) {
    try {
      return System.getenv(key);
    } catch (SecurityException e) {
      return null;
    }
  }

  /**
   * Gets an Android system property
   *
   * @param key The key to search for
   * @return the string value of the system property
   */
  public static String getAndroidSystemProperty(String key) {
    try {
      return SystemPropertiesProxy.getInstance().get(key, null);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Very similar to <code>System.getProperty</code> except that the
   * {@link SecurityException} is absorbed. Also checks Android
   * system properties as a fallback.
   *
   * @param key The key to search for.
   * @return the string value of the system property.
   */
  public static String getSystemProperty(String key) {
    try {
      String prop = System.getProperty(key);
      return (prop == null) ? getAndroidSystemProperty(key) : prop;
    } catch (SecurityException e) {
      return null;
    }
  }

  public static void setSystemProperties(ContextAware contextAware, Properties props) {
    for (Object o : props.keySet()) {
      String key = (String) o;
      String value = props.getProperty(key);
      setSystemProperty(contextAware, key, value);
    }
  }

  public static void setSystemProperty(ContextAware contextAware, String key, String value) {
    try {
      System.setProperty(key, value);
    } catch (SecurityException e) {
      contextAware.addError("Failed to set system property [" + key + "]", e);
    }
  }

  /**
   * Very similar to {@link System#getProperties()} except that the
   * {@link SecurityException} is absorbed.
   *
   * @return the system properties
   */
  public static Properties getSystemProperties() {
    try {
      return System.getProperties();
    } catch (SecurityException e) {
      return new Properties();
    }
  }

  public static boolean isEmpty(String str) {
    return ((str == null) || CoreConstants.EMPTY_STRING.equals(str));
  }


}
