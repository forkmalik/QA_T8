package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;

import org.junit.jupiter.api.*;
import org.sql2o.Connection;
import org.sql2o.data.Table;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class DbUserServiceTest {

    private static final String FIND_USER = "SELECT count(1) FROM pg_catalog.pg_user where usename = :username";
    private static final String FIND_SPACE = "SELECT count(1) FROM pg_catalog.pg_tablespace where spcname = :username;";
    private static final String FIND_DB = "SELECT count(1) from pg_catalog.pg_database join pg_authid on pg_database.datdba = pg_authid.oid  where rolname = :username;";
    private static final String FIND_PASS = "SELECT passwd FROM pg_catalog.pg_user where usename = :username";

    Connection con;
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

    @BeforeEach
    void setUp(){
        con = DBPool.getConnection().open();
    }

    @AfterEach
    void tearDown() {
        con.close();
    }

    @Test
    void initUser() {

        int user_count = con.createQuery(FIND_USER).addParameter("username", username).executeScalar(Integer.class);
        int ts_count = con.createQuery(FIND_SPACE).addParameter("username", username).executeScalar(Integer.class);
        int db_count = con.createQuery(FIND_DB).addParameter("username", username).executeScalar(Integer.class);
        assertEquals(1, user_count, "User created");
        assertEquals(1, ts_count, "tablespace created");
        assertEquals(1, db_count, "database");
    }

    @Test
    void dbUserPasswordReset() {
        DbUserService.dbUserPasswordReset(username,"test2");
        String pass = con.createQuery(FIND_PASS).addParameter("username", username).executeScalar(String.class);
        assertEquals(pass, "********");
    }

    @Test
    void dropUser() {
        String password = "test";
        User user = UserService.newDefaultUser();
        user.setLogin(StringUtils.randomAlphaString(8) + "@example.com");
        user.setPassword(password);
        UserService.save(user);

        String login = user.getLogin();
        String newUsername = user.getUsername();
        UserService.initUserStorage(newUsername);
        DbUserService.initUser(newUsername, password);

        User found = UserService.byLogin(login);
        assertEquals(found.getLogin(), login);

        assertThrows(Exception.class, () -> {DbUserService.dropUser(newUsername);});
    }
}