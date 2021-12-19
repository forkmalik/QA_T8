package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BackupServiceTest {

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
    void setUp() {
    }

    @Test
    void list() {
        String backup = StringUtils.randomAlphaString(10);
        BackupService.perform(username, username, backup);
        List<Backup> backups = BackupService.list(username);
        assertEquals(username,backups.get(0).getDatabase());
    }

    @Test
    void byName() {
        String backupName = StringUtils.randomAlphaString(10);
        BackupService.perform(username, username, backupName);
        Backup backup = BackupService.byName(username, backupName);
        assertNotNull(backup.getId());
        assertEquals(username,backup.getDatabase());
    }

    @Test
    void perform() {
        String backup = StringUtils.randomAlphaString(10);

        BackupService.perform(username, username, backup);
        var point = BackupService.byName(username, backup);
        assertTrue(point != null, "new point exist");
        String path = BackupService.filePath(username, username, backup);
        assertTrue(new File(path).exists(), "fileExist");
    }

    @Test
    void restore() {
        String backup = StringUtils.randomAlphaString(10);

        BackupService.perform(username, username, backup);
        BackupService.restore(username, username, backup);
    }
}
