package edu.sumdu.tss.elephant.helper;

import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class HmacTest {

    @Test
    void calculate() throws NoSuchAlgorithmException, InvalidKeyException {
        String result = Hmac.calculate("list/user", "AnotherOneUser");
        assertEquals("58fcfb369d6f6a9f082dc5dc93a77a96ca941b72fffe80326f7afe5245f117b60b4d606e8fb868852859c29debcba154"
                , result, "HmacTest");
    }
}