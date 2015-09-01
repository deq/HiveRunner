package com.klarna.hiverunner.config;


import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * HiveRunner runtime configuration.
 *
 * Configure with System properties via mvn like
 * <pre>
 * {@literal <}plugin>
 *      &lt;groupId>org.apache.maven.plugins&lt;/groupId>
 *      &lt;artifactId>maven-surefire-plugin&lt;/artifactId>
 *      &lt;version>2.17&lt;/version>
 *      &lt;configuration>
 *          ...
 *          &lt;systemProperties>
 *              &lt;hiveExecutionEngine>tez&lt;/hiveExecutionEngine>
 *              &lt;enableTimeout>false&lt;/enableTimeout>
 *              &lt;timeoutSeconds>30&lt;/timeoutSeconds>
 *              &lt;timeoutRetries>2&lt;/timeoutRetries>
 *          &lt;/systemProperties>
 *      &lt;/configuration>
 * &lt;/plugin>
 * </pre>
 *
 * Properties may be overridden per test class by annotating a <b>static</b> HiveRunnerConfig field like:
 * <pre>
 *      &#064;HiveRunnerSetup
 *      public final static HiveRunnerConfig config = new HiveRunnerConfig(){{
 *          setTimeoutEnabled(true);
 *          setTimeoutSeconds(15);
 *          setTimeoutRetries(2);
 *      }};
 * </pre>
 * See {@link com.klarna.hiverunner.DisabledTimeoutTest}
 *
 */
public class HiveRunnerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiveRunnerConfig.class);


    /**
     * What execution engine hive should use. Available are 'mr' (map reduce) and 'tez'
     *
     * Defaults to 'mr'
     */
    public static final String HIVE_EXECUTION_ENGINE_PROPERTY_NAME = "hiveExecutionEngine";
    public static final String MAP_REDUCE = "mr";
    public static final String TEZ = "tez";
    public static final String HIVE_EXECUTION_ENGINE_DEFAULT = MAP_REDUCE;

    /**
     * Enable timeout. Some versions of tez has proven to not always terminate. By enabling timeout,
     * HiveRunner will kill the current query and attempt to retry the test case a configurable number of times.
     *
     * Defaults to disabled
     */
    public static final String ENABLE_TIMEOUT_PROPERTY_NAME = "enableTimeout";
    public static final boolean ENABLE_TIMEOUT_DEFAULT = false;

    /**
     * Seconds to wait for a query to terminate before triggering the timeout.
     *
     * Defaults to 30 seconds
     */
    public static final String TIMEOUT_SECONDS_PROPERTY_NAME = "timeoutSeconds";
    public static final int TIMEOUT_SECONDS_DEFAULT = 30;

    /**
     * Number of retries for a test case that keep timing out.
     *
     * Defaults to 2 retries
     */
    public static final String TIMEOUT_RETRIES_PROPERTY_NAME = "timeoutRetries";
    public static final int TIMEOUT_RETRIES_DEFAULT = 2;


    private Map<String, Object> config = new HashMap<>();

    public HiveRunnerConfig() {
        config.put(HIVE_EXECUTION_ENGINE_PROPERTY_NAME, load(HIVE_EXECUTION_ENGINE_PROPERTY_NAME,
                HIVE_EXECUTION_ENGINE_DEFAULT, Arrays.asList(MAP_REDUCE, TEZ)));

        config.put(ENABLE_TIMEOUT_PROPERTY_NAME, load(ENABLE_TIMEOUT_PROPERTY_NAME, ENABLE_TIMEOUT_DEFAULT));
        config.put(TIMEOUT_RETRIES_PROPERTY_NAME, load(TIMEOUT_RETRIES_PROPERTY_NAME, TIMEOUT_RETRIES_DEFAULT));
        config.put(TIMEOUT_SECONDS_PROPERTY_NAME, load(TIMEOUT_SECONDS_PROPERTY_NAME, TIMEOUT_SECONDS_DEFAULT));
    }

    public boolean isTimeoutEnabled() {
        return getBoolean(ENABLE_TIMEOUT_PROPERTY_NAME);
    }

    public int getTimeoutRetries() {
        return getInteger(TIMEOUT_RETRIES_PROPERTY_NAME);
    }

    public int getTimeoutSeconds() {
        return getInteger(TIMEOUT_SECONDS_PROPERTY_NAME);
    }

    public String getHiveExecutionEngine() {
        return getString(HIVE_EXECUTION_ENGINE_PROPERTY_NAME);
    }

    public void setTimeoutEnabled(boolean isEnabled) {
        config.put(ENABLE_TIMEOUT_PROPERTY_NAME, isEnabled);
    }

    public void setTimeoutRetries(int retries) {
        config.put(TIMEOUT_RETRIES_PROPERTY_NAME, retries);
    }

    public void setTimeoutSeconds(int timeout) {
        config.put(TIMEOUT_SECONDS_PROPERTY_NAME, timeout);
    }

    public void setHiveExecutionEngine(String executionEngine) {
        config.put(HIVE_EXECUTION_ENGINE_PROPERTY_NAME, executionEngine);
    }

    /**
     * Copy values from the inserted config to this config. Note that if properties has not been explicitly set,
     * the defaults will apply.
     */
    public void override(HiveRunnerConfig hiveRunnerConfig) {
        this.config.putAll(hiveRunnerConfig.config);
    }

    private String load(String property, String defaultValue,
                        List<String> validValues) {
        String value = load(property, defaultValue);
        Preconditions.checkArgument(validValues.contains(value),
                "Invalid value of system property '" + property + "': Only values '" + validValues + "' are allowed");
        return value;
    }

    private static boolean load(String property, boolean defaultValue) {
        String value = System.getProperty(property);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private static String load(String property, String defaultValue) {
        String value = System.getProperty(property);
        return value == null ? defaultValue : value;
    }

    private static int load(String property, int defaultValue) {
        String value = System.getProperty(property);
        return value == null ? defaultValue : Integer.parseInt(value);
    }


    private boolean getBoolean(String key) {
        return (boolean) config.get(key);
    }


    private int getInteger(String key) {
        return (int) config.get(key);
    }

    private String getString(String key) {
        return (String) config.get(key);
    }
}