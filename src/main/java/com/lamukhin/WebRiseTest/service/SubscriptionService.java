package com.lamukhin.WebRiseTest.service;

import com.lamukhin.WebRiseTest.dao.SubscriptionDao;
import com.lamukhin.WebRiseTest.dto.EntrySubscriptionDto;
import com.lamukhin.WebRiseTest.dto.FullSubscriptionInfoDto;
import com.lamukhin.WebRiseTest.dto.FullUserInfoDto;
import com.lamukhin.WebRiseTest.dto.TopSubscription;
import com.lamukhin.WebRiseTest.exception.SubscriptionException;
import com.lamukhin.WebRiseTest.exception.UserNotFoundException;
import com.lamukhin.WebRiseTest.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionDao subscriptionDao;
    private final UserService userService;

    public int addSubscriptionByUserId(String userId, EntrySubscriptionDto subscription) {
        UUID userUuid = ServiceUtil.convertStringToUuid(userId);
        FullUserInfoDto foundUser = userService.getUserById(userId);
        if (foundUser == null) {
            throw new UserNotFoundException();
        }
        var newFullInfo = new FullSubscriptionInfoDto(
                null,
                userUuid,
                subscription.serviceName(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(subscription.subscriptionDurationDays())
        );

        FullSubscriptionInfoDto sub = subscriptionDao.getSubscriptionByUserIdAndName(userUuid, subscription.serviceName());
        if (sub != null) {
            if (sub.endTime().isAfter(LocalDateTime.now())) {
                throw new SubscriptionException("Subscription is not ended yet! It ends at " + sub.endTime());
            } else {
                try {
                    return subscriptionDao.updateSubscriptionByNameAndUserId(userUuid, newFullInfo);
                } catch (DataAccessException ex) {
                    log.error("Failed to update sub info: {}", ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
        }

        try {
            return subscriptionDao.addNewSubscriptionByUser(foundUser, newFullInfo);
        } catch (DataAccessException ex) {
            log.error("Failed to insert sub info: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public Collection<FullSubscriptionInfoDto> getAllSubscriptionsByUserId(String id) {
        UUID userUuid = ServiceUtil.convertStringToUuid(id);
        try {
            return subscriptionDao.getAllSubscriptionsByUserId(userUuid);
        } catch (DataAccessException ex) {
            log.error("Failed to get all subs: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public int deleteSubscriptionByIdAndUserId(String id, int subId) {
        FullUserInfoDto foundUser = userService.getUserById(id);
        if (foundUser == null) {
            throw new UserNotFoundException();
        }
        try {
            return subscriptionDao.deleteSubscriptionByIdAndUser(foundUser, subId);
        } catch (DataAccessException ex) {
            log.error("Failed to delete sub: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }

    }

    public Collection<TopSubscription> getTopThreeSubscription() {
        try {
            return subscriptionDao.getTopThreeSubscription();
        } catch (DataAccessException ex) {
            log.error("Failed to get top 3 subs: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
