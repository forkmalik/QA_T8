package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import io.javalin.http.Context;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sql2o.Connection;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class LogServiceTest {

    private static final String FIND_LOG = "SELECT message FROM LOGGER where ip = :ip";

    static String username;
    static Connection con;

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

        con = DBPool.getConnection().open();
    }

    @AfterAll
    static void release(){
        Database db = DatabaseService.byName(username);
        DatabaseService.drop(db);
    }

    @Test
    void push() {
        Context ctx = Mockito.mock(Context.class);
        when(ctx.ip()).thenReturn("123");
        LogService.push(ctx,username,"aaa");
        Log l = new Log();
        String message = con.createQuery(FIND_LOG).addParameter("ip","123").executeScalar(String.class);
        assertEquals(message,"aaa");
    }
}