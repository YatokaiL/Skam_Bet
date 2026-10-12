package ru.skambet.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.skambet.SkamBetApplication;
import ru.skambet.bdd.support.TestDatabaseConfig;

/**
 * Starts the whole application on a random port once for all scenarios.
 */
@CucumberContextConfiguration
@SpringBootTest(classes = SkamBetApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}
