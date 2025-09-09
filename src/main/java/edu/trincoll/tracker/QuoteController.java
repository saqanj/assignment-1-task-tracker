package edu.trincoll.tracker;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * AI Collaboration Report:
 * - AI Tool Used: ChatGPT-4
 * - Most Helpful Prompt: "Create a Spring Boot REST controller for quotes with GET, POST, PUT, DELETE operations that validates required fields and prevents duplicate names"
 * - AI Mistake We Fixed: AI initially suggested using @Valid annotation for validation, but our tests expected manual validation logic, so we implemented custom validation checks in each method
 * - Time Saved: Approximately 2-3 hours on boilerplate CRUD operations and HTTP status code handling
 * - Team Members: Saqlain Anjum, Gabriela Scavenius, Noella Uwayisenga, Chris Burns.
 */
@RestController
@RequestMapping(value = "/api/items", produces = MediaType.APPLICATION_JSON_VALUE) // TODO: Rename to match your domain (e.g., /api/bookmarks, /api/recipes)
public class QuoteController {

    // Simple in-memory store (will be replaced by a database later)
    private static final Map<Long, Quote> STORE = new ConcurrentHashMap<>();
    private static final AtomicLong ID_SEQ = new AtomicLong(1);

    /**
     * GET /api/items
     * Returns all items in the system
     */
    @GetMapping
    public ResponseEntity<List<Quote>> getAll() {
        List<Quote> quotes = STORE.values()
                .stream()
                .sorted(Comparator.comparing(Quote::getId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(quotes);
    }

    /**
     * GET /api/items/{id}
     * Returns a specific item by ID
     * Return 404 if item doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<Quote> getById(@PathVariable Long id) {
        Quote quote = STORE.get(id);
        if (quote == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(quote);
    }

    /**
     * POST /api/items
     * Creates a new item
     * - Validate required fields (name)
     * - Reject duplicates by name (409 Conflict)
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Quote> create(@RequestBody Quote quote) {
        // Validate name
        if (quote.getQuoteName() == null || quote.getQuoteName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // Enforce uniqueness by name
        boolean duplicate = STORE.values().stream()
                .anyMatch(existing -> Objects.equals(existing.getQuoteName(), quote.getQuoteName()));
        if (duplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Assign new ID (ignore any provided id)
        long id = ID_SEQ.getAndIncrement();
        Quote toSave = new Quote();
        toSave.setId(id);
        toSave.setQuoteName(quote.getQuoteName());
        toSave.setQuoteContent(quote.getQuoteContent());
        toSave.setCompleted(quote.isCompleted());
        // Keep server-controlled createdAt from constructor; do not override from client

        STORE.put(id, toSave);
        return ResponseEntity.status(HttpStatus.CREATED).body(toSave);
    }

    /**
     * PUT /api/items/{id}
     * Updates an existing item
     * - Validate required fields (name)
     * - Return 404 if item doesn't exist
     * - Reject duplicates by name (409 Conflict) if changing to an existing name
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Quote> update(@PathVariable Long id, @RequestBody Quote update) {
        Quote existing = STORE.get(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (update.getQuoteName() == null || update.getQuoteName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // Prevent changing to a name that duplicates another item's name
        boolean duplicateName = STORE.values().stream()
                .anyMatch(other -> !Objects.equals(other.getId(), id)
                        && Objects.equals(other.getQuoteName(), update.getQuoteName()));
        if (duplicateName) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        existing.setQuoteName(update.getQuoteName());
        existing.setQuoteContent(update.getQuoteContent());
        existing.setCompleted(update.isCompleted());
        // Keep original createdAt (ignore client-sent value)

        return ResponseEntity.ok(existing);
    }

    /**
     * DELETE /api/items/{id}
     * Deletes an item
     * - Return 204 No Content on successful delete
     * - Return 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Quote removed = STORE.remove(id);
        if (removed == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/items/search?name=value
     * Searches items by name (case-insensitive contains)
     * BONUS endpoint
     */
    @GetMapping("/search")
    public ResponseEntity<List<Quote>> searchByName(@RequestParam("name") String name) {
        if (name == null) {
            return ResponseEntity.badRequest().build();
        }
        String query = name.toLowerCase(Locale.ROOT);
        List<Quote> results = STORE.values().stream()
                .filter(it -> it.getQuoteName() != null && it.getQuoteName().toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing(Quote::getId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    // Test helper method - only for testing purposes
    static void clearStore() {
        STORE.clear();
        ID_SEQ.set(1);
    }
}