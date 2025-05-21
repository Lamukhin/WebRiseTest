package com.lamukhin.WebRiseTest;

import com.lamukhin.WebRiseTest.dao.UserDao;
import com.lamukhin.WebRiseTest.dto.EntryUserDto;
import com.lamukhin.WebRiseTest.dto.FullUserInfoDto;
import com.lamukhin.WebRiseTest.dto.UpdateUserDataDto;
import com.lamukhin.WebRiseTest.service.UserService;
import com.lamukhin.WebRiseTest.util.ServiceUtil;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    private final String testEmail = "test@example.com";
    private final String testUserName = "TestUser";
    private final String testIdString = "123e4567-e89b-12d3-a456-426614174000";
    private UUID testUuid;
    private EntryUserDto entryUserDto;
    private FullUserInfoDto existingUserInfoDto;
    private UpdateUserDataDto updateUserDataDto;
    private String testUuidString;
    private final String newTestEmail = "new@example.com";


    @BeforeEach
    void setUp() {
        testUuid = UUID.fromString(testIdString);
        testUuidString = testUuid.toString();
        entryUserDto = new EntryUserDto(testUserName, testEmail);
        existingUserInfoDto = new FullUserInfoDto(UUID.randomUUID(), "existingUser", "existing@example.com", LocalDateTime.now(), 0);
        updateUserDataDto = new UpdateUserDataDto(testUserName, newTestEmail, "updatedUserName", 0);
    }

    @Test
    void saveNewUser_whenEmailIsNotDuplicate_shouldSaveUserAndReturnUuid() {
        when(userDao.findUserByEmail(entryUserDto.email())).thenReturn(null);
        doNothing().when(userDao).saveNewUser(any(FullUserInfoDto.class));
        UUID resultUuid = userService.saveNewUser(entryUserDto);
        assertNotNull(resultUuid);
        ArgumentCaptor<FullUserInfoDto> userCaptor = ArgumentCaptor.forClass(FullUserInfoDto.class);
        verify(userDao, times(1)).saveNewUser(userCaptor.capture());
        FullUserInfoDto savedUser = userCaptor.getValue();
        assertEquals(entryUserDto.userName(), savedUser.userName());
        assertEquals(entryUserDto.email(), savedUser.email());
        assertNotNull(savedUser.id());
        assertNotNull(savedUser.registrationTime());
    }

    @Test
    void saveNewUser_whenEmailIsDuplicate_shouldThrowDuplicateKeyException() {
        when(userDao.findUserByEmail(entryUserDto.email())).thenReturn(existingUserInfoDto);
        DuplicateKeyException exception = assertThrows(DuplicateKeyException.class, () -> {
            userService.saveNewUser(entryUserDto);
        });
        assertEquals("User with email " + entryUserDto.email() + " already exists.", exception.getMessage());
        verify(userDao, never()).saveNewUser(any(FullUserInfoDto.class));
    }

    @Test
    void saveNewUser_whenFindByEmailThrowsDataAccessException_shouldThrowRuntimeException() {
        when(userDao.findUserByEmail(entryUserDto.email())).thenThrow(new DataAccessException("DB find error") {
        });
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.saveNewUser(entryUserDto);
        });
        assertTrue(exception.getCause() instanceof DataAccessException);
        assertEquals("DB find error", exception.getCause().getMessage());
        verify(userDao, never()).saveNewUser(any(FullUserInfoDto.class));
    }

    @Test
    void saveNewUser_whenSaveNewUserThrowsDataAccessException_shouldThrowRuntimeException() {
        when(userDao.findUserByEmail(entryUserDto.email())).thenReturn(null);
        doThrow(new DataAccessException("DB save error") {
        }).when(userDao).saveNewUser(any(FullUserInfoDto.class));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.saveNewUser(entryUserDto);
        });
        assertTrue(exception.getCause() instanceof DataAccessException);
        assertEquals("DB save error", exception.getCause().getMessage());
    }

    @Test
    void updateUserById_whenNewEmailProvidedAndNotDuplicate_shouldUpdateUser() {
        when(userDao.findUserByEmail(updateUserDataDto.email())).thenReturn(null);
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testUuidString)).thenReturn(testUuid);
            when(userDao.updateUserById(testUuid, updateUserDataDto)).thenReturn(1);
            int result = userService.updateUserById(testUuidString, updateUserDataDto);
            assertEquals(1, result);
            verify(userDao, times(1)).findUserByEmail(updateUserDataDto.email());
            verify(userDao, times(1)).updateUserById(testUuid, updateUserDataDto);
        }
    }

    @Test
    void updateUserById_whenNewEmailProvidedAndIsDuplicate_shouldThrowDuplicateKeyException() {
        when(userDao.findUserByEmail(updateUserDataDto.email())).thenReturn(existingUserInfoDto);
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testUuidString)).thenReturn(testUuid);
            DuplicateKeyException exception = assertThrows(DuplicateKeyException.class, () -> {
                userService.updateUserById(testUuidString, updateUserDataDto);
            });
            assertEquals("User with email " + updateUserDataDto.email() + " already exists.", exception.getMessage());
            verify(userDao, times(1)).findUserByEmail(updateUserDataDto.email());
            verify(userDao, never()).updateUserById(any(UUID.class), any(UpdateUserDataDto.class));
        }
    }

    @Test
    void updateUserById_whenFindByEmailThrowsDataAccessException_shouldThrowRuntimeException() {
        when(userDao.findUserByEmail(updateUserDataDto.email())).thenThrow(new DataAccessException("DB find error during update") {
        });
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testUuidString)).thenReturn(testUuid);
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.updateUserById(testUuidString, updateUserDataDto);
            });
            assertTrue(exception.getCause() instanceof DataAccessException);
            assertEquals("DB find error during update", exception.getCause().getMessage());
            verify(userDao, never()).updateUserById(any(UUID.class), any(UpdateUserDataDto.class));
        }
    }

    @Test
    void updateUserById_whenUpdateUserThrowsDataAccessException_shouldThrowRuntimeException() {
        when(userDao.findUserByEmail(updateUserDataDto.email())).thenReturn(null);
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testUuidString)).thenReturn(testUuid);
            when(userDao.updateUserById(testUuid, updateUserDataDto)).thenThrow(new DataAccessException("DB update error") {
            });
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.updateUserById(testUuidString, updateUserDataDto);
            });
            assertTrue(exception.getCause() instanceof DataAccessException);
            assertEquals("DB update error", exception.getCause().getMessage());
        }
    }

    @Test
    void updateUserById_whenConvertStringToUuidThrowsException_shouldPropagateException() {
        UpdateUserDataDto updateDtoNoNewEmail = new UpdateUserDataDto(testUserName, null, "updatedNick", 0);
        String invalidUuidString = "invalid-uuid";
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(invalidUuidString))
                    .thenThrow(new IllegalArgumentException("Invalid UUID format"));
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.updateUserById(invalidUuidString, updateDtoNoNewEmail);
            });
            assertEquals("Invalid UUID format", exception.getMessage());
            verify(userDao, never()).findUserByEmail(anyString());
            verify(userDao, never()).updateUserById(any(UUID.class), any(UpdateUserDataDto.class));
        }
    }

    @Test
    void getUserById_shouldReturnUser_whenFound() {
        FullUserInfoDto expectedUser = new FullUserInfoDto(testUuid, testUserName, testEmail, LocalDateTime.now(), 0);
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testIdString)).thenReturn(testUuid);
            when(userDao.findUserById(testUuid)).thenReturn(expectedUser);
            FullUserInfoDto actualUser = userService.getUserById(testIdString);
            assertNotNull(actualUser);
            assertEquals(expectedUser, actualUser);
            verify(userDao, times(1)).findUserById(testUuid);
        }
    }

    @Test
    void getUserById_shouldReturnNull_whenUserNotFound() {
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testIdString)).thenReturn(testUuid);
            when(userDao.findUserById(testUuid)).thenReturn(null);
            FullUserInfoDto actualUser = userService.getUserById(testIdString);
            assertNull(actualUser);
            verify(userDao, times(1)).findUserById(testUuid);
        }
    }

    @Test
    void getUserById_shouldThrowRuntimeException_whenDaoFails() {
        DataAccessException daoException = new DataAccessException("DAO find failed") {
        };
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testIdString)).thenReturn(testUuid);
            when(userDao.findUserById(testUuid)).thenThrow(daoException);
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.getUserById(testIdString);
            });
            assertEquals(daoException, exception.getCause());
        }
    }

    @Test
    void getUserById_shouldThrowException_whenIdIsInvalid() {
        String invalidId = "invalid-uuid-format";
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(invalidId))
                    .thenThrow(new IllegalArgumentException("Invalid UUID string"));
            assertThrows(IllegalArgumentException.class, () -> {
                userService.getUserById(invalidId);
            });
            verify(userDao, never()).findUserById(any(UUID.class));
        }
    }

    @Test
    void deleteUserById_shouldDeleteUser() {
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testIdString)).thenReturn(testUuid);
            when(userDao.deleteUserById(testUuid)).thenReturn(1);
            int deletedCount = userService.deleteUserById(testIdString);
            assertEquals(1, deletedCount);
            verify(userDao, times(1)).deleteUserById(testUuid);
        }
    }

    @Test
    void deleteUserById_shouldThrowRuntimeException_whenDaoFails() {
        DataAccessException daoException = new DataAccessException("DAO delete failed") {
        };
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.convertStringToUuid(testIdString)).thenReturn(testUuid);
            when(userDao.deleteUserById(testUuid)).thenThrow(daoException);
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.deleteUserById(testIdString);
            });
            assertEquals(daoException, exception.getCause());
        }
    }
}
