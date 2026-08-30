package org.example.eventmanagementapi.building;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BuildingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuildingService buildingService;

    @Test
    @DisplayName("DELETE /api/buildings/{id} - Successfully soft delete building")
    void deleteBuilding_ShouldReturnNoContent() throws Exception {
        UUID buildingId = UUID.randomUUID();
        doNothing().when(buildingService).softDeleteBuilding(buildingId);

        mockMvc.perform(delete("/api/buildings/{id}", buildingId))
                .andExpect(status().isNoContent());
    }
}
