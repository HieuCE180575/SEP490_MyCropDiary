package com.mycropdiary.api.landplot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycropdiary.api.common.exception.GlobalExceptionHandler;
import com.mycropdiary.api.landplot.dto.LandPlotResponse;
import com.mycropdiary.api.landplot.entity.AreaUnit;
import com.mycropdiary.api.landplot.entity.LandPlotStatus;
import com.mycropdiary.api.landplot.service.LandPlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LandPlotControllerTest {

    @Mock
    private LandPlotService service;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new LandPlotController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReturns201AndStandardResponse() throws Exception {
        LandPlotResponse response = sampleResponse();
        when(service.create(eq(7L), any())).thenReturn(response);

        String requestBody = """
                {
                  "plotCode": "PLOT-001",
                  "name": "North Plot",
                  "areaValue": 1250.500,
                  "areaUnit": "M2",
                  "address": "Can Tho",
                  "latitude": 10.0300000,
                  "longitude": 105.7800000,
                  "soilType": "Alluvial",
                  "waterSource": "Canal",
                  "notes": "Vegetable production"
                }
                """;

        mockMvc.perform(post("/land-plots")
                        .header("X-Demo-User-Id", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageCode").value("MSG-LAND-PLOT-001"))
                .andExpect(jsonPath("$.data.id").value(11))
                .andExpect(jsonPath("$.data.plotCode").value("PLOT-001"));
    }

    @Test
    void createRejectsInvalidPayloadBeforeCallingService() throws Exception {
        String requestBody = """
                {
                  "plotCode": " ",
                  "name": "",
                  "areaValue": 0,
                  "areaUnit": null,
                  "latitude": 91,
                  "longitude": -181
                }
                """;

        mockMvc.perform(post("/land-plots")
                        .header("X-Demo-User-Id", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.messageCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.plotCode").exists())
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.areaValue").exists())
                .andExpect(jsonPath("$.data.areaUnit").exists());
    }

    private LandPlotResponse sampleResponse() {
        Instant now = Instant.parse("2026-08-27T10:00:00Z");
        return new LandPlotResponse(
                11L,
                "PLOT-001",
                "North Plot",
                new BigDecimal("1250.500"),
                AreaUnit.M2,
                "Can Tho",
                new BigDecimal("10.0300000"),
                new BigDecimal("105.7800000"),
                "Alluvial",
                "Canal",
                "Vegetable production",
                LandPlotStatus.ACTIVE,
                null,
                now,
                now
        );
    }
}
