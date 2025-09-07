package edu.trincoll.tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite for the Item API.
 * <p>
 * ALL TESTS MUST PASS for full credit.
 * Do not modify these tests - modify your code to make them pass.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Item Controller Tests")
class QuoteControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() throws Exception {
        // Clear any existing data before each test
        QuoteController.clearStore();
    }
    
    @Nested
    @DisplayName("GET /api/items")
    class GetAllItems {
        
        @Test
        @DisplayName("should return empty list when no items exist")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/items"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
        }
        
        @Test
        @DisplayName("should return all items when items exist")
        void shouldReturnAllItems() throws Exception {
            // Create a test item first
            Quote testQuote = new Quote();
            testQuote.setQuoteName("Test Item");
            testQuote.setQuoteContent("Test Description");
            
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testQuote)))
                    .andExpect(status().isCreated());
            
            // Now get all items
            mockMvc.perform(get("/api/items"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[?(@.name == 'Test Item')]").exists());
        }
    }
    
    @Nested
    @DisplayName("GET /api/items/{id}")
    class GetQuoteById {
        
        @Test
        @DisplayName("should return item when it exists")
        void shouldReturnItemWhenExists() throws Exception {
            // Create a test item first
            Quote testQuote = new Quote();
            testQuote.setQuoteName("Specific Item");
            testQuote.setQuoteContent("Specific Description");
            
            String response = mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testQuote)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Quote createdQuote = objectMapper.readValue(response, Quote.class);
            
            // Get the specific item
            mockMvc.perform(get("/api/items/{id}", createdQuote.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Specific Item"))
                    .andExpect(jsonPath("$.description").value("Specific Description"));
        }
        
        @Test
        @DisplayName("should return 404 when item doesn't exist")
        void shouldReturn404WhenItemDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/items/{id}", 999999))
                    .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("POST /api/items")
    class CreateQuote {
        
        @Test
        @DisplayName("should create new item with valid data")
        void shouldCreateNewItem() throws Exception {
            Quote newQuote = new Quote();
            newQuote.setQuoteName("New Item");
            newQuote.setQuoteContent("New Description");
            
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newQuote)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("New Item"))
                    .andExpect(jsonPath("$.description").value("New Description"));
        }
        
        @Test
        @DisplayName("should return 400 when name is missing")
        void shouldReturn400WhenNameMissing() throws Exception {
            String invalidJson = """
                    {"description":"No name provided"}""";
            
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameBlank() throws Exception {
            Quote invalidQuote = new Quote();
            invalidQuote.setQuoteName("");  // Blank name
            invalidQuote.setQuoteContent("Valid Description");
            
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidQuote)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("should not allow duplicate items with same name")
        void shouldNotAllowDuplicates() throws Exception {
            Quote firstQuote = new Quote();
            firstQuote.setQuoteName("Unique Name");
            firstQuote.setQuoteContent("First Description");
            
            // Create first item
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstQuote)))
                    .andExpect(status().isCreated());
            
            // Try to create duplicate
            Quote duplicateQuote = new Quote();
            duplicateQuote.setQuoteName("Unique Name");  // Same name
            duplicateQuote.setQuoteContent("Different Description");
            
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicateQuote)))
                    .andExpect(status().isConflict());  // 409 Conflict
        }
    }
    
    @Nested
    @DisplayName("PUT /api/items/{id}")
    class UpdateQuote {
        
        @Test
        @DisplayName("should update existing item")
        void shouldUpdateExistingItem() throws Exception {
            // Create initial item
            Quote initialQuote = new Quote();
            initialQuote.setQuoteName("Original Name");
            initialQuote.setQuoteContent("Original Description");
            
            String response = mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(initialQuote)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Quote createdQuote = objectMapper.readValue(response, Quote.class);
            
            // Update the item
            Quote updatedQuote = new Quote();
            updatedQuote.setQuoteName("Updated Name");
            updatedQuote.setQuoteContent("Updated Description");
            updatedQuote.setCompleted(true);
            
            mockMvc.perform(put("/api/items/{id}", createdQuote.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedQuote)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Name"))
                    .andExpect(jsonPath("$.description").value("Updated Description"))
                    .andExpect(jsonPath("$.completed").value(true));
        }
        
        @Test
        @DisplayName("should return 404 when updating non-existent item")
        void shouldReturn404WhenUpdatingNonExistent() throws Exception {
            Quote updateQuote = new Quote();
            updateQuote.setQuoteName("Update Name");
            updateQuote.setQuoteContent("Update Description");
            
            mockMvc.perform(put("/api/items/{id}", 999999)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateQuote)))
                    .andExpect(status().isNotFound());
        }
        
        @Test
        @DisplayName("should validate required fields on update")
        void shouldValidateRequiredFieldsOnUpdate() throws Exception {
            // Create initial item
            Quote initialQuote = new Quote();
            initialQuote.setQuoteName("Original Name");
            initialQuote.setQuoteContent("Original Description");
            
            String response = mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(initialQuote)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Quote createdQuote = objectMapper.readValue(response, Quote.class);
            
            // Try to update with invalid data
            String invalidUpdate = "{\"name\":\"\",\"description\":\"Valid Description\"}";
            
            mockMvc.perform(put("/api/items/{id}", createdQuote.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidUpdate))
                    .andExpect(status().isBadRequest());
        }
    }
    
    @Nested
    @DisplayName("DELETE /api/items/{id}")
    class DeleteQuote {
        
        @Test
        @DisplayName("should delete existing item")
        void shouldDeleteExistingItem() throws Exception {
            // Create item to delete
            Quote quoteToDelete = new Quote();
            quoteToDelete.setQuoteName("Delete Me");
            quoteToDelete.setQuoteContent("To Be Deleted");
            
            String response = mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(quoteToDelete)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Quote createdQuote = objectMapper.readValue(response, Quote.class);
            
            // Delete the item
            mockMvc.perform(delete("/api/items/{id}", createdQuote.getId()))
                    .andExpect(status().isNoContent());
            
            // Verify it's gone
            mockMvc.perform(get("/api/items/{id}", createdQuote.getId()))
                    .andExpect(status().isNotFound());
        }
        
        @Test
        @DisplayName("should return 404 when deleting non-existent item")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            mockMvc.perform(delete("/api/items/{id}", 999999))
                    .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("Bonus: Search Functionality")
    class SearchItems {
        
        @Test
        @DisplayName("BONUS: should search items by name")
        void shouldSearchItemsByName() throws Exception {
            // Create test items
            Quote quote1 = new Quote();
            quote1.setQuoteName("Apple");
            quote1.setQuoteContent("Red fruit");
            
            Quote quote2 = new Quote();
            quote2.setQuoteName("Banana");
            quote2.setQuoteContent("Yellow fruit");
            
            Quote quote3 = new Quote();
            quote3.setQuoteName("Application");
            quote3.setQuoteContent("Software");
            
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(quote1)))
                    .andExpect(status().isCreated());
            
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(quote2)))
                    .andExpect(status().isCreated());
            
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(quote3)))
                    .andExpect(status().isCreated());
            
            // Search for items containing "App"
            mockMvc.perform(get("/api/items/search")
                    .param("name", "App"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[?(@.name == 'Apple')]").exists())
                    .andExpect(jsonPath("$[?(@.name == 'Application')]").exists());
        }
    }
}