package com.lamukhin.WebRiseTest;

import com.lamukhin.WebRiseTest.dao.SubscriptionDao;
import com.lamukhin.WebRiseTest.dto.EntrySubscriptionDto;
import com.lamukhin.WebRiseTest.dto.FullSubscriptionInfoDto;
import com.lamukhin.WebRiseTest.dto.FullUserInfoDto;
import com.lamukhin.WebRiseTest.dto.TopSubscription;
import com.lamukhin.WebRiseTest.exception.SubscriptionException;
import com.lamukhin.WebRiseTest.exception.UserNotFoundException;
import com.lamukhin.WebRiseTest.service.SubscriptionService;
import com.lamukhin.WebRiseTest.service.UserService;
import com.lamukhin.WebRiseTest.util.ServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionDao subscriptionDao;

    @Mock
    private UserService userService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private String testUserIdString;
    private UUID testUserUuid;
    private FullUserInfoDto testUserInfoDto;
    private EntrySubscriptionDto entrySubscriptionDto;
    private FullSubscriptionInfoDto existingSubscription;
    private FullSubscriptionInfoDto newSubscriptionToSave;
    private final LocalDateTime MOCKED_NOW = LocalDateTime.of(2025, 5, 21, 10, 0, 0);


    @BeforeEach
    void setUp() {
        testUserUuid = UUID.randomUUID();
        testUserIdString = testUserUuid.toString();
        testUserInfoDto = mock(FullUserInfoDto.class);
        entrySubscriptionDto = new EntrySubscriptionDto("Netflix", 30);
        existingSubscription = new FullSubscriptionInfoDto(1, testUserUuid, "Netflix",
                MOCKED_NOW.minusDays(15), MOCKED_NOW.plusDays(15));
        newSubscriptionToSave = new FullSubscriptionInfoDto(
                null,
                testUserUuid,
                entrySubscriptionDto.serviceName(),
                MOCKED_NOW,
                MOCKED_NOW.plusDays(entrySubscriptionDto.subscriptionDurationDays())
        );
    }

    @Test
    void addSubscriptionByUserId_whenUserNotFound_shouldThrowUserNotFoundException() {
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testUserIdString)).thenReturn(testUserUuid);
            when(userService.getUserById(testUserIdString)).thenReturn(null);
            assertThrows(UserNotFoundException.class, () -> {
                subscriptionService.addSubscriptionByUserId(testUserIdString, entrySubscriptionDto);
            });
            verify(subscriptionDao, never()).getSubscriptionByUserIdAndName(any(), any());
            verify(subscriptionDao, never()).addNewSubscriptionByUser(any(), any());
        }
    }

    @Test
    void addSubscriptionByUserId_whenNoExistingSubscription_shouldAddNewSubscription() {
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testUserIdString)).thenReturn(testUserUuid);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(MOCKED_NOW);
            when(userService.getUserById(testUserIdString)).thenReturn(testUserInfoDto);
            when(subscriptionDao.getSubscriptionByUserIdAndName(testUserUuid, entrySubscriptionDto.serviceName())).thenReturn(null);
            when(subscriptionDao.addNewSubscriptionByUser(eq(testUserInfoDto), any(FullSubscriptionInfoDto.class))).thenReturn(1);
            int result = subscriptionService.addSubscriptionByUserId(testUserIdString, entrySubscriptionDto);
            assertEquals(1, result);
            ArgumentCaptor<FullSubscriptionInfoDto> captor = ArgumentCaptor.forClass(FullSubscriptionInfoDto.class);
            verify(subscriptionDao).addNewSubscriptionByUser(eq(testUserInfoDto), captor.capture());
            FullSubscriptionInfoDto capturedSub = captor.getValue();
            assertEquals(testUserUuid, capturedSub.userId());
            assertEquals(entrySubscriptionDto.serviceName(), capturedSub.serviceName());
            assertEquals(MOCKED_NOW, capturedSub.startTime());
            assertEquals(MOCKED_NOW.plusDays(entrySubscriptionDto.subscriptionDurationDays()), capturedSub.endTime());
        }
    }

    @Test
    void addSubscriptionByUserId_whenExistingSubscriptionIsActive_shouldThrowSubscriptionException() {
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class)) {

            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testUserIdString)).thenReturn(testUserUuid);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(MOCKED_NOW);
            when(userService.getUserById(testUserIdString)).thenReturn(testUserInfoDto);
            FullSubscriptionInfoDto activeSub = new FullSubscriptionInfoDto(1, testUserUuid, entrySubscriptionDto.serviceName(),
                    MOCKED_NOW.minusDays(10), MOCKED_NOW.plusDays(5));
            when(subscriptionDao.getSubscriptionByUserIdAndName(testUserUuid, entrySubscriptionDto.serviceName())).thenReturn(activeSub);
            SubscriptionException ex = assertThrows(SubscriptionException.class, () -> {
                subscriptionService.addSubscriptionByUserId(testUserIdString, entrySubscriptionDto);
            });
            assertEquals("Subscription is not ended yet! It ends at " + activeSub.endTime(), ex.getMessage());
            verify(subscriptionDao, never()).addNewSubscriptionByUser(any(), any());
            verify(subscriptionDao, never()).updateSubscriptionByNameAndUserId(any(), any());
        }
    }

    @Test
    void addSubscriptionByUserId_whenExistingSubscriptionHasEnded_shouldUpdateSubscription() {
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testUserIdString)).thenReturn(testUserUuid);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(MOCKED_NOW);
            when(userService.getUserById(testUserIdString)).thenReturn(testUserInfoDto);
            FullSubscriptionInfoDto endedSub = new FullSubscriptionInfoDto(1, testUserUuid, entrySubscriptionDto.serviceName(),
                    MOCKED_NOW.minusDays(40), MOCKED_NOW.minusDays(10));
            when(subscriptionDao.getSubscriptionByUserIdAndName(testUserUuid, entrySubscriptionDto.serviceName())).thenReturn(endedSub);
            when(subscriptionDao.updateSubscriptionByNameAndUserId(eq(testUserUuid), any(FullSubscriptionInfoDto.class))).thenReturn(1);
            int result = subscriptionService.addSubscriptionByUserId(testUserIdString, entrySubscriptionDto);
            assertEquals(1, result);
            ArgumentCaptor<FullSubscriptionInfoDto> captor = ArgumentCaptor.forClass(FullSubscriptionInfoDto.class);
            verify(subscriptionDao).updateSubscriptionByNameAndUserId(eq(testUserUuid), captor.capture());
            FullSubscriptionInfoDto capturedSub = captor.getValue();
            assertEquals(testUserUuid, capturedSub.userId());
            assertEquals(entrySubscriptionDto.serviceName(), capturedSub.serviceName());
            assertEquals(MOCKED_NOW, capturedSub.startTime());
            assertEquals(MOCKED_NOW.plusDays(entrySubscriptionDto.subscriptionDurationDays()), capturedSub.endTime());
            verify(subscriptionDao, never()).addNewSubscriptionByUser(any(), any());
        }
    }

    @Test
    void getAllSubscriptionsByUserId_whenSubscriptionsExist_shouldReturnCollection() {
        List<FullSubscriptionInfoDto> subs = List.of(existingSubscription);
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testUserIdString)).thenReturn(testUserUuid);
            when(subscriptionDao.getAllSubscriptionsByUserId(testUserUuid)).thenReturn(subs);
            Collection<FullSubscriptionInfoDto> result = subscriptionService.getAllSubscriptionsByUserId(testUserIdString);
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.contains(existingSubscription));
            verify(subscriptionDao).getAllSubscriptionsByUserId(testUserUuid);
        }
    }

    @Test
    void getAllSubscriptionsByUserId_whenNoSubscriptions_shouldReturnEmptyCollection() {
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testUserIdString)).thenReturn(testUserUuid);
            when(subscriptionDao.getAllSubscriptionsByUserId(testUserUuid)).thenReturn(Collections.emptyList());
            Collection<FullSubscriptionInfoDto> result = subscriptionService.getAllSubscriptionsByUserId(testUserIdString);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(subscriptionDao).getAllSubscriptionsByUserId(testUserUuid);
        }
    }

    @Test
    void deleteSubscriptionByIdAndUserId_whenUserNotFound_shouldThrowUserNotFoundException() {
        when(userService.getUserById(testUserIdString)).thenReturn(null);
        int subIdToDelete = 123;
        assertThrows(UserNotFoundException.class, () -> {
            subscriptionService.deleteSubscriptionByIdAndUserId(testUserIdString, subIdToDelete);
        });
        verify(subscriptionDao, never()).deleteSubscriptionByIdAndUser(any(), anyInt());
    }

    @Test
    void deleteSubscriptionByIdAndUserId_whenSubscriptionDeleted_shouldReturnCount() {
        when(userService.getUserById(testUserIdString)).thenReturn(testUserInfoDto);
        int subIdToDelete = 123;
        when(subscriptionDao.deleteSubscriptionByIdAndUser(testUserInfoDto, subIdToDelete)).thenReturn(1);
        int result = subscriptionService.deleteSubscriptionByIdAndUserId(testUserIdString, subIdToDelete);
        assertEquals(1, result);
        verify(subscriptionDao).deleteSubscriptionByIdAndUser(testUserInfoDto, subIdToDelete);
    }

    @Test
    void getTopThreeSubscription_shouldReturnDataFromDao() {
        List<TopSubscription> topSubs = List.of(new TopSubscription("Netflix", 100), new TopSubscription("Spotify", 90));
        when(subscriptionDao.getTopThreeSubscription()).thenReturn(topSubs);
        Collection<TopSubscription> result = subscriptionService.getTopThreeSubscription();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(topSubs));
        verify(subscriptionDao).getTopThreeSubscription();
    }

    @Test
    void getTopThreeSubscription_whenDaoReturnsEmpty_shouldReturnEmpty() {
        when(subscriptionDao.getTopThreeSubscription()).thenReturn(Collections.emptyList());
        Collection<TopSubscription> result = subscriptionService.getTopThreeSubscription();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(subscriptionDao).getTopThreeSubscription();
    }
}
