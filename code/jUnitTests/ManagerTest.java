package jUnitTests;

import bankapp.Customer;
import bankapp.Teller;
import bankapp.Manager;
import bankapp.Address;
import bankapp.Person;
import bankapp.Account;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ManagerTest {

    @Test
    void managerInheritsTellerFields() {
        Address address = new Address(10, "2B", "Main St", "Springfield", "IL", "62704");
        Manager manager = new Manager("Bob", "Manager", address, 42);

        assertEquals("Bob Manager", manager.getName());
        assertEquals(42, manager.getRegisterNumber());
        assertEquals(address, manager.getAddress());
    }

    @Test
    void viewLogsReturnsServerLogsWhenAuthorized() {
        Manager manager = new Manager("Bob", "Manager",
                new Address(1, null, "Oak", "Town", "IL", "11111"), 1);

        AuthorizedLogServer server = new AuthorizedLogServer();

        Object result = manager.viewLogs(server);

        assertNotNull(result);
        assertTrue(result instanceof List<?>);
        assertEquals(2, ((List<?>) result).size());
        assertEquals("log-1", ((List<?>) result).get(0));
        assertEquals("log-2", ((List<?>) result).get(1));
    }

    @Test
    void viewLogsThrowsWhenAccessDenied() {
        Manager manager = new Manager("Bob", "Manager",
                new Address(1, null, "Oak", "Town", "IL", "11111"), 1);

        AccessDeniedServer server = new AccessDeniedServer();

        SecurityException ex = assertThrows(SecurityException.class, () -> manager.viewLogs(server));
        assertEquals("manager is not authorized to view logs", ex.getMessage());
    }

    @Test
    void viewLogsFallsBackWhenValidationMethodMissing() {
        Manager manager = new Manager("Bob", "Manager",
                new Address(1, null, "Oak", "Town", "IL", "11111"), 1);

        NoValidationMethodServer server = new NoValidationMethodServer();

        Object result = manager.viewLogs(server);

        assertNotNull(result);
        assertTrue(result instanceof List<?>);
        assertEquals(1, ((List<?>) result).size());
        assertEquals("only-log", ((List<?>) result).get(0));
    }

    @Test
    void viewLogsThrowsWhenGetLogsMissing() {
        Manager manager = new Manager("Bob", "Manager",
                new Address(1, null, "Oak", "Town", "IL", "11111"), 1);

        MissingGetLogsServer server = new MissingGetLogsServer();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> manager.viewLogs(server));
        assertTrue(ex.getMessage().contains("failed retrieving logs from server"));
    }

    // ---------- test doubles ----------

    public static class AuthorizedLogServer {
        public boolean validateLogAccess(Teller teller) {
            return true;
        }

        public List<String> getLogs() {
            List<String> logs = new ArrayList<>();
            logs.add("log-1");
            logs.add("log-2");
            return logs;
        }
    }

    public static class AccessDeniedServer {
        public boolean validateLogAccess(Teller teller) {
            return false;
        }

        public List<String> getLogs() {
            List<String> logs = new ArrayList<>();
            logs.add("should-not-be-returned");
            return logs;
        }
    }

    public static class NoValidationMethodServer {
        public List<String> getLogs() {
            List<String> logs = new ArrayList<>();
            logs.add("only-log");
            return logs;
        }
    }

    public static class MissingGetLogsServer {
        public boolean validateLogAccess(Teller teller) {
            return true;
        }
    }
}