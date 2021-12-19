package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseServiceTest {

    static String username;

    @BeforeAll
    static void init(){
        Keys.loadParams(new File("db.conf"));

        String password = "test";
        User user = UserService.newDefaultUser();
        user.setLogin(StringUtils.randomAlphaString(8) + "@example.com");
        user.setPassword(password);
        UserService.save(user);

        username = user.getUsername();
        UserService.initUserStorage(username);
        DbUserService.initUser(username, password);

        DatabaseService.create(username, username, username);
        DatabaseService.activeDatabase(username,username);
    }

    @AfterAll
    static void release(){
        Database db = DatabaseService.byName(username);
        DatabaseService.drop(db);
    }

    @Test
    void forUser() {
        List<Database> databases = DatabaseService.forUser(username);
        assertEquals(username, databases.get(0).getName());
    }

    @Test
    void size() {
        assertNotNull(DatabaseService.size(username));
    }

}