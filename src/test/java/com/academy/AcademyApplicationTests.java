package com.academy;

import com.academy.availability.AvailabilityIntegrationTests;
import com.academy.global_configurations.GlobalConfigurationTests;
import com.academy.services.ServiceIntegrationTests;
import com.academy.services.ServicePermissionsIntegrationTests;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;

@Suite
@SelectClasses({
		ServiceIntegrationTests.class,
		AvailabilityIntegrationTests.class,
		GlobalConfigurationTests.class,
		ServicePermissionsIntegrationTests.class,
})
@SpringBootTest
public class AcademyApplicationTests {
}