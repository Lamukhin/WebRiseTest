package com.lamukhin.WebRiseTest.service;

import com.lamukhin.WebRiseTest.dao.UserDao;
import com.lamukhin.WebRiseTest.dto.EntryUserDto;
import com.lamukhin.WebRiseTest.dto.FullUserInfoDto;
import com.lamukhin.WebRiseTest.dto.UpdateUserDataDto;
import com.lamukhin.WebRiseTest.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserDao userDao;

    public UUID saveNewUser(EntryUserDto newData) {
        try {
            if (userDao.findUserByEmail(newData.email()) != null) {
                throw new DuplicateKeyException("User with email " + newData.email() + " already exists.");
            }
            UUID newUserUuid = UUID.randomUUID();
            var newUser = new FullUserInfoDto(
                    newUserUuid,
                    newData.userName(),
                    newData.email(),
                    LocalDateTime.now(),
                    0
            );

            userDao.saveNewUser(newUser);
            return newUserUuid;
        } catch (DataAccessException ex) {
            log.error("Failed to save a new user: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public FullUserInfoDto getUserById(String id) {
        UUID userUuid = ServiceUtil.convertStringToUuid(id);
        try {
            return userDao.findUserById(userUuid);
        } catch (DataAccessException ex) {
            log.error("Failed to load user info: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public int updateUserById(String id, UpdateUserDataDto newData) {
        try {
            if (newData.newEmail() != null) {
                if (userDao.findUserByEmail(newData.email()) != null) {
                    throw new DuplicateKeyException("User with email " + newData.email() + " already exists.");
                }
            }
            UUID userUuid = ServiceUtil.convertStringToUuid(id);

            return userDao.updateUserById(userUuid, newData);
        } catch (DataAccessException ex) {
            log.error("Failed to update user info: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public int deleteUserById(String id) {
        UUID userUuid = ServiceUtil.convertStringToUuid(id);
        try {
            return userDao.deleteUserById(userUuid);
        } catch (DataAccessException ex) {
            log.error("Failed to update user info: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
