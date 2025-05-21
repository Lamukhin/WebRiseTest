package com.lamukhin.WebRiseTest.util;

import com.lamukhin.WebRiseTest.exception.IncorrectIdException;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public final class ServiceUtil {

    public static UUID convertStringToUuid(final String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException ex) {
            throw new IncorrectIdException();
        }
    }
}
