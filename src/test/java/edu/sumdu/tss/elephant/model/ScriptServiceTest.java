package edu.sumdu.tss.elephant.model;

import com.google.common.io.Resources;
import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.Pair;
import edu.sumdu.tss.elephant.helper.sql.ScriptReader;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScriptServiceTest {

    static String username;

    @BeforeEach
    void setUp() {
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

    @AfterEach
    void tearDown() {
        Database db = DatabaseService.byName(username);
        DatabaseService.drop(db);
    }

    @Test
    void testReader() throws SQLException, FileNotFoundException {

        Script iScript = new Script();
        iScript.setFilename(username);
        iScript.setDescription("aaa");
        iScript.setSize(1);
        URL file = Resources.getResource("migrations/20211105_add_description_to_scripts.sql");
        iScript.setPath(file.getPath());
        iScript.setDatabase(username);
        ScriptService.save(iScript);

        List<Script> scripts = ScriptService.list(username);

        Script script = scripts.get(0);
        var sr = new ScriptReader(new BufferedReader(new FileReader(script.getPath())));
        String line;
        String result;
        Connection connection = DBPool.getConnection().open().getJdbcConnection();
        Statement statement = connection.createStatement();
        while ((line = sr.readStatement()) != null) {
            try {
                statement.executeQuery(line);
                result = "ok";
            } catch (SQLException ex) {
                result =
                        ex.getSQLState() +
                                ex.getErrorCode() +
                                ex.getMessage();
            }
            assertNotEquals(result, "ok");
        }
    }
}