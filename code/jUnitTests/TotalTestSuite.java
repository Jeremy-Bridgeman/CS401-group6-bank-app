package jUnitTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

// runs all real test classes only
@Suite
@SelectClasses({
    AccountValidatorTest.class,
    AddressTest.class,
    ATMJTest.class,
    CheckingAccountValidatorTest.class,
    CreditAccountValidatorTest.class,
    CustomerTest.class,
    LogTest.class,
    LoggerTest.class,
    ManagerTest.class,
    PersonTest.class,
    SavingsAccountValidatorTest.class,
    TellerTest.class
})
public class TotalTestSuite {
}