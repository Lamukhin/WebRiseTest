package com.lamukhin.WebRiseTest.controller;


import com.lamukhin.WebRiseTest.dto.EntryUserDto;
import com.lamukhin.WebRiseTest.dto.ResponseToWeb;
import com.lamukhin.WebRiseTest.dto.UpdateUserDataDto;
import com.lamukhin.WebRiseTest.exception.IncorrectIdException;
import com.lamukhin.WebRiseTest.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody EntryUserDto newData) {
        try {
            var newUserUuid = userService.saveNewUser(newData);
            log.warn("Successfully saved data for \"{}\"", newData.userName());
            ResponseToWeb okResponse = new ResponseToWeb(newUserUuid.toString(), HttpStatus.OK.value());
            return ResponseEntity.status(HttpStatus.OK).body(okResponse);
        } catch (DuplicateKeyException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("User with this email already exists", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Something went wrong...", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } //92d90d3d-cb16-48cd-8796-87fb9a6da86f
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            var userInfoDto = userService.getUserById(id);
            if (userInfoDto != null) {
                log.warn("Successfully loaded data for \"{}\"", userInfoDto.userName());
                return ResponseEntity.status(HttpStatus.OK).body(userInfoDto);
            }
            log.warn("User with ID \"{}\" is not found", id);
            ResponseToWeb errorResponse = new ResponseToWeb("User is not found", HttpStatus.NOT_FOUND.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (IncorrectIdException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Incorrect user ID", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Something went wrong...", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserDataDto newData,
                                        @PathVariable String id) {
        try {
            int userRowsUpdated = userService.updateUserById(id, newData);
            if (userRowsUpdated == 1) {
                log.warn("Successfully updated data for \"{}\"", newData.userName());
                return new ResponseEntity<>(HttpStatus.OK);
            }
            ResponseToWeb errorResponse = new ResponseToWeb("User with this ID doesn't exist", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (DuplicateKeyException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("User with a new email already exists", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IncorrectIdException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Incorrect user ID", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Something went wrong...", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            int userRowDeleted = userService.deleteUserById(id);
            if (userRowDeleted == 1) {
                log.warn("Successfully deleted user with id \"{}\"", id);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            ResponseToWeb errorResponse = new ResponseToWeb("User with this ID doesn't exist", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IncorrectIdException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Incorrect user ID", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Something went wrong...", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
