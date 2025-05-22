package com.example.support_ticket_api.service;

import com.example.support_ticket_api.dto.CreateTicketRequest;
import com.example.support_ticket_api.exception.TicketNotFoundException;
import com.example.support_ticket_api.model.*;
import com.example.support_ticket_api.repository.CommentRepository;
import com.example.support_ticket_api.repository.FeedbackRepository;
import com.example.support_ticket_api.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;

    public Ticket createTicket(CreateTicketRequest request) {
    Ticket ticket = new Ticket();
    ticket.setTitle(request.getTitle());
    ticket.setDescription(request.getDescription());
    ticket.setPriority(request.getPriority());

    if (request.getCategory() == null || request.getCategory().isBlank()) {
        ticket.setCategory("unknown");
    } else {
        ticket.setCategory(request.getCategory());
    }

    ticket.setStatus(TicketStatus.OPEN);
    ticket.setAssignedAgentId(null);

    return ticketRepository.save(ticket);
}

     public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
     }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket updateTicket(Long id, Ticket ticketUpdates) {
    return ticketRepository.findById(id)
            .map(existingTicket -> {
                if (ticketUpdates.getTitle() != null) {
                    existingTicket.setTitle(ticketUpdates.getTitle());
                }
                if (ticketUpdates.getDescription() != null) {
                    existingTicket.setDescription(ticketUpdates.getDescription());
                }
                if (ticketUpdates.getPriority() != null) {
                    existingTicket.setPriority(ticketUpdates.getPriority());
                }
                if (ticketUpdates.getCategory() != null) {
                    existingTicket.setCategory(ticketUpdates.getCategory());
                }
                return ticketRepository.save(existingTicket);
            })
            .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + id));
        }

    public Ticket assignAgent(Long ticketId, Long agentId) {
    return ticketRepository.findById(ticketId)
        .map(ticket -> {
            if (agentId == null) {
                throw new IllegalArgumentException("Agent ID is required");
            }
            ticket.setAssignedAgentId(agentId);
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            return ticketRepository.save(ticket);
        })
        .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));
    }

    public void deleteTicket(Long id) {
        ticketRepository.findById(id)
            .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + id));
        ticketRepository.deleteById(id);
    }

    public Comment addComment(Long ticketId, Comment comment) {
    Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));
        comment.setTicket(ticket);
        return commentRepository.save(comment);
    }

    public Ticket escalatePriority(Long ticketId) {
    return ticketRepository.findById(ticketId)
        .map(ticket -> {
            TicketPriority current = ticket.getPriority();
            TicketPriority[] priorities = TicketPriority.values();
            if (current.ordinal() < priorities.length - 1) {
                ticket.setPriority(priorities[current.ordinal() + 1]);
            }
            return ticketRepository.save(ticket);
        })
        .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));
    }

    public Ticket updateStatus(Long ticketId, TicketStatus newStatus) {
    return ticketRepository.findById(ticketId)
        .map(ticket -> {
            if (ticket.getStatus() == TicketStatus.CLOSED) {
                throw new IllegalStateException("Closed tickets cannot be modified");
            }
            ticket.setStatus(newStatus);
            return ticketRepository.save(ticket);
        })
        .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));
    }

    public Feedback submitFeedback(Long ticketId, Feedback feedback) {
    Ticket ticket = ticketRepository.findById(ticketId)
        .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));

        if (ticket.getStatus() != TicketStatus.CLOSED) {
            throw new IllegalStateException("Feedback only allowed on closed tickets");
        }

        feedback.setTicket(ticket);
        return feedbackRepository.save(feedback);
    }
}