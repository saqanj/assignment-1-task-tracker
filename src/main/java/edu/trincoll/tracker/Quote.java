package edu.trincoll.tracker;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base entity class for Quote object.
 */
public class Quote {
    
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    @JsonProperty("name") // <-- map to tests' expected key
    private String quoteName;

    @JsonProperty("description") // <-- map to tests' expected key
    private String quoteContent;

    private String author;

    private String source;

    private String category;

    private LocalDateTime createdAt;
    
    private boolean completed;
    
    // Constructor
    public Quote() {
        this.createdAt = LocalDateTime.now();
        this.completed = false;
    }

    public String getAuthor() { return author; }

    public void setAuthor(String author) { this.author = author; }

    public String getSource() { return source; }

    public void setSource(String source) { this.source = source; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getQuoteName() {
        return quoteName;
    }
    
    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
    }
    
    public String getQuoteContent() {
        return quoteContent;
    }
    
    public void setQuoteContent(String quoteContent) {
        this.quoteContent = quoteContent;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Quote quote = (Quote) o;
        return completed == quote.completed &&
               Objects.equals(id, quote.id) &&
               Objects.equals(quoteName, quote.quoteName) &&
               Objects.equals(quoteContent, quote.quoteContent) &&
                Objects.equals(author, quote.author) &&
                Objects.equals(source, quote.source) &&
                Objects.equals(category, quote.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quoteName, quoteContent, author, source, category, completed);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", quoteName='" + quoteName + '\'' +
                ", quoteContent='" + quoteContent + '\'' +
                ", quoteContent='" + author + '\'' +
                ", quoteContent='" + source + '\'' +
                ", quoteContent='" + category + '\'' +
                ", createdAt=" + createdAt +
                ", completed=" + completed +
                '}';
    }
}