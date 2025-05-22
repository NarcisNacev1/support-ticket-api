package com.example.support_ticket_api.controller;

import com.example.support_ticket_api.exception.ErrorResponse;
import com.example.support_ticket_api.exception.TicketNotFoundException;
import com.example.support_ticket_api.model.Comment;
import com.example.support_ticket_api.model.Feedback;
import com.example.support_ticket_api.model.Ticket;
import com.example.support_ticket_api.model.TicketStatus;
import com.example.support_ticket_api.service.TicketService;
import com.example.support_ticket_api.dto.CreateTicketRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<?> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        try {
            Ticket createdTicket = ticketService.createTicket(request);
            return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error while creating ticket"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable Long id) {
        try {
            return ticketService.getTicketById(id)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + id));
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error while retrieving ticket"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTickets() {
        try {
            List<Ticket> tickets = ticketService.getAllTickets();
            if (tickets.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(tickets);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error while retrieving tickets"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        try {
            ticketService.deleteTicket(id);
            return ResponseEntity.ok().body("Ticket deleted successfully");
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error while deleting ticket"));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTicket(
    @PathVariable Long id,
    @Valid @RequestBody Ticket ticketUpdates) {
        try {
            Ticket updatedTicket = ticketService.updateTicket(id, ticketUpdates);
            return ResponseEntity.ok(updatedTicket);
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error while updating ticket"));
        }
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<?> assignTicket(
    @PathVariable Long id,
    @RequestBody Map<String, Long> request) {
        try {
            Long agentId = request.get("assignedAgentId");
            Ticket updatedTicket = ticketService.assignAgent(id, agentId);
            return ResponseEntity.ok(updatedTicket);
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
    @PathVariable Long id,
    @Valid @RequestBody Comment comment) {
        try {
            Comment savedComment = ticketService.addComment(id, comment);
            return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/escalate")
    public ResponseEntity<?> escalatePriority(@PathVariable Long id) {
        try {
            Ticket updatedTicket = ticketService.escalatePriority(id);
            return ResponseEntity.ok(updatedTicket);
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
    @PathVariable Long id,
    @RequestBody Map<String, TicketStatus> request) {
        try {
            TicketStatus newStatus = request.get("status");
            Ticket updatedTicket = ticketService.updateStatus(id, newStatus);
            return ResponseEntity.ok(updatedTicket);
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> submitFeedback(
    @PathVariable Long id,
    @Valid @RequestBody Feedback feedback) {
        try {
            Feedback savedFeedback = ticketService.submitFeedback(id, feedback);
            return new ResponseEntity<>(savedFeedback, HttpStatus.CREATED);
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }


    @ControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
            List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Validation failed", errors));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
            String message = "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue();
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(message));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred: " + ex.getMessage()));
        }
    }
}