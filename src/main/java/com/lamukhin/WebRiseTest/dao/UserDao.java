package com.lamukhin.WebRiseTest.dao;


import com.lamukhin.WebRiseTest.dto.FullUserInfoDto;
import com.lamukhin.WebRiseTest.dto.UpdateUserDataDto;
import lombok.RequiredArgsConstructor;
import org.jooq.Field;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.lamukhin.generated.tables.Users.USERS;

@Repository
@RequiredArgsConstructor
public class UserDao {

    private final DefaultDSLContext dslContext;

    public void saveNewUser(FullUserInfoDto newUser) {
        try {
            dslContext
                    .insertInto(USERS)
                    .columns(USERS.ID, USERS.USER_NAME, USERS.EMAIL, USERS.REGISTRATION_TIME, USERS.SUBSCRIPTION_AMOUNT)
                    .values(newUser.id(), newUser.userName(), newUser.email(), newUser.registrationTime(), newUser.subscriptionAmount())
                    .execute();
        } catch (Throwable e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public FullUserInfoDto findUserByEmail(String email) {
        try {
            return dslContext
                    .selectFrom(USERS)
                    .where(USERS.EMAIL.eq(email))
                    .fetchOneInto(FullUserInfoDto.class);
        } catch (Throwable e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public FullUserInfoDto findUserById(UUID id) {
        try {
            return dslContext
                    .selectFrom(USERS)
                    .where(USERS.ID.eq(id))
                    .fetchOneInto(FullUserInfoDto.class);
        } catch (Throwable e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public int updateUserById(UUID id, UpdateUserDataDto newData) {
        Map<Field<?>, Object> fieldsToUpdate = new HashMap<>();
        fieldsToUpdate.put(USERS.USER_NAME, newData.userName());
        fieldsToUpdate.put(USERS.SUBSCRIPTION_AMOUNT, newData.subscriptionAmount());
        if (newData.newEmail() != null) {
            fieldsToUpdate.put(USERS.EMAIL, newData.newEmail());
        }
        try {
            return dslContext
                    .update(USERS)
                    .set(fieldsToUpdate)
                    .where(USERS.ID.eq(id))
                    .execute();
        } catch (Throwable e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public int deleteUserById(UUID userUuid) {
        try {
            return dslContext
                    .deleteFrom(USERS)
                    .where(USERS.ID.eq(userUuid))
                    .execute();
        } catch (Throwable e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
