package com.lamukhin.WebRiseTest.dao;

import com.lamukhin.WebRiseTest.dto.FullSubscriptionInfoDto;
import com.lamukhin.WebRiseTest.dto.FullUserInfoDto;
import com.lamukhin.WebRiseTest.dto.TopSubscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

import static com.lamukhin.generated.tables.Subscriptions.SUBSCRIPTIONS;
import static com.lamukhin.generated.tables.Users.USERS;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SubscriptionDao {

    private final DefaultDSLContext dslContext;

    @Transactional
    public int addNewSubscriptionByUser(FullUserInfoDto user, FullSubscriptionInfoDto subscription) {
        try {
            dslContext
                    .update(USERS)
                    .set(USERS.SUBSCRIPTION_AMOUNT, user.subscriptionAmount() + 1)
                    .where(USERS.ID.eq(user.id()))
                    .execute();

            return dslContext
                    .insertInto(SUBSCRIPTIONS)
                    .columns(SUBSCRIPTIONS.USER_ID, SUBSCRIPTIONS.SERVICE_NAME, SUBSCRIPTIONS.START_TIME, SUBSCRIPTIONS.END_TIME)
                    .values(user.id(), subscription.serviceName(), subscription.startTime(), subscription.endTime())
                    .execute();
        } catch (Throwable e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public FullSubscriptionInfoDto getSubscriptionByUserIdAndName(UUID userUuid, String serviceName) {
        try {
            return dslContext
                    .selectFrom(SUBSCRIPTIONS)
                    .where(SUBSCRIPTIONS.USER_ID.eq(userUuid))
                    .and(SUBSCRIPTIONS.SERVICE_NAME.eq(serviceName))
                    .fetchOneInto(FullSubscriptionInfoDto.class);
        } catch (Throwable e) {
            throw new DataAccessException(e.getMessage());
        }
    }


    public int updateSubscriptionByNameAndUserId(UUID userUuid, FullSubscriptionInfoDto fullInfo) {
        try {
            return dslContext
                    .update(SUBSCRIPTIONS)
                    .set(SUBSCRIPTIONS.START_TIME, fullInfo.startTime())
                    .set(SUBSCRIPTIONS.END_TIME, fullInfo.endTime())
                    .where(SUBSCRIPTIONS.USER_ID.eq(userUuid))
                    .and(SUBSCRIPTIONS.SERVICE_NAME.eq(fullInfo.serviceName()))
                    .execute();
        } catch (Throwable e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public Collection<FullSubscriptionInfoDto> getAllSubscriptionsByUserId(UUID userUuid) {
        try {
            return dslContext
                    .selectFrom(SUBSCRIPTIONS)
                    .where(SUBSCRIPTIONS.USER_ID.eq(userUuid))
                    .fetchInto(FullSubscriptionInfoDto.class);
        } catch (Throwable e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Transactional
    public int deleteSubscriptionByIdAndUser(FullUserInfoDto user, int subId) {
        try {
            dslContext
                    .update(USERS)
                    .set(USERS.SUBSCRIPTION_AMOUNT, user.subscriptionAmount() - 1)
                    .where(USERS.ID.eq(user.id()))
                    .execute();

            return dslContext
                    .deleteFrom(SUBSCRIPTIONS)
                    .where(SUBSCRIPTIONS.USER_ID.eq(user.id()))
                    .and(SUBSCRIPTIONS.ID.eq(subId))
                    .execute();
        } catch (Throwable e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public Collection<TopSubscription> getTopThreeSubscription() {
        try {
            return dslContext
                    .select(SUBSCRIPTIONS.SERVICE_NAME, DSL.count(SUBSCRIPTIONS.USER_ID))
                    .from(SUBSCRIPTIONS)
                    .groupBy(SUBSCRIPTIONS.SERVICE_NAME)
                    .orderBy(DSL.count(SUBSCRIPTIONS.USER_ID).desc())
                    .limit(3)
                    .fetchInto(TopSubscription.class);
        } catch (Throwable e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
