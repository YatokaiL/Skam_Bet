package ru.skambet.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.OBJECT_FACTORY_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Entry point: gradlew test
 * Single feature: gradlew test -Dcucumber.filter.tags=@bet
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "ru.skambet.bdd")
// serenity brings its own object factory, steps are created by spring (cucumber-spring)
@ConfigurationParameter(key = OBJECT_FACTORY_PROPERTY_NAME, value = "io.cucumber.spring.SpringFactory")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, io.cucumber.core.plugin.SerenityReporterParallel, "
                + "html:build/cucumber-reports/cucumber.html, json:build/cucumber-reports/cucumber.json")
public class RunCucumberTest {
}
