package com.lamukhin.WebRiseTest.controller;

import com.lamukhin.WebRiseTest.dto.EntrySubscriptionDto;
import com.lamukhin.WebRiseTest.dto.FullSubscriptionInfoDto;
import com.lamukhin.WebRiseTest.dto.ResponseToWeb;
import com.lamukhin.WebRiseTest.dto.TopSubscription;
import com.lamukhin.WebRiseTest.exception.IncorrectIdException;
import com.lamukhin.WebRiseTest.exception.SubscriptionException;
import com.lamukhin.WebRiseTest.exception.UserNotFoundException;
import com.lamukhin.WebRiseTest.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/users/{id}/subscriptions")
    public ResponseEntity<?> addSubscriptionToUserById(@PathVariable String id,
                                                       @RequestBody EntrySubscriptionDto subscription) {
        try {
            subscriptionService.addSubscriptionByUserId(id, subscription);
            log.warn("Successfully added subscription \"{}\"\" for user with ID \"{}\"", subscription.serviceName(), id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IncorrectIdException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Incorrect user ID", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (UserNotFoundException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("User is not found", HttpStatus.NOT_FOUND.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (SubscriptionException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Something went wrong...", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/users/{id}/subscriptions")
    public ResponseEntity<?> getAllSubscriptionsByUserId(@PathVariable String id) {
        try {
            Collection<FullSubscriptionInfoDto> allSubs = subscriptionService.getAllSubscriptionsByUserId(id);
            return ResponseEntity.status(HttpStatus.OK).body(allSubs);
        } catch (IncorrectIdException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Incorrect user ID", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Something went wrong...", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/users/{id}/subscriptions/{sub_id}")
    public ResponseEntity<?> deleteSubscriptionById(@PathVariable String id,
                                                    @PathVariable int sub_id) {
        try {
            int amountOfDeletedSubs = subscriptionService.deleteSubscriptionByIdAndUserId(id, sub_id);
            if (amountOfDeletedSubs == 1) {
                log.warn("Successfully deleted subscription with ID \"{}\"\" for user with ID \"{}\"", sub_id, id);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            ResponseToWeb errorResponse = new ResponseToWeb("This user doesn't have subscription with ID " + sub_id, HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IncorrectIdException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Incorrect user ID", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (UserNotFoundException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("User is not found", HttpStatus.NOT_FOUND.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Something went wrong...", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/subscriptions/top")
    public ResponseEntity<?> getTopSubscriptions() {
        try {
            Collection<TopSubscription> topSubs = subscriptionService.getTopThreeSubscription();
            return ResponseEntity.status(HttpStatus.OK).body(topSubs);
        } catch (RuntimeException ex) {
            ResponseToWeb errorResponse = new ResponseToWeb("Something went wrong...", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
