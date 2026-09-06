package com.example.algospeclab.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.algospeclab.web.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;

@WebMvcTest(AlgorithmController.class)
@Import(GlobalExceptionHandler.class)
class AlgorithmControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void sortReturnsAscendingOrder() throws Exception {
        mockMvc.perform(post("/api/algorithms/sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"values\":[3,1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.values[0]").value(1))
                .andExpect(jsonPath("$.values[2]").value(3));
    }

    @Test
    void sortRejectsMissingValues() throws Exception {
        mockMvc.perform(post("/api/algorithms/sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fibonacciReturnsExpectedValue() throws Exception {
        mockMvc.perform(get("/api/algorithms/fibonacci/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.n").value(10))
                .andExpect(jsonPath("$.value").value(55));
    }

    @Test
    void fibonacciRejectsNegative() throws Exception {
        mockMvc.perform(get("/api/algorithms/fibonacci/-1"))
                .andExpect(status().isBadRequest());
    }
}
