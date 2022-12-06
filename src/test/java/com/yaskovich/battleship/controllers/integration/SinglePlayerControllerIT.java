package com.yaskovich.battleship.controllers.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaskovich.battleship.api.controllers.SinglePlayerController;
import com.yaskovich.battleship.models.GameModelUI;
import com.yaskovich.battleship.models.PreparingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class SinglePlayerControllerIT {

    public static final String SINGLE_PLAYER_ENDPOINT = "/single_player";
    @Autowired
    private SinglePlayerController singlePlayerController;
    @Autowired
    protected ObjectMapper objectMapper;
    protected MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = standaloneSetup(singlePlayerController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnGameModelUITest() throws Exception {
        String expectedName = "Name";
        PreparingModel preparingModel = new PreparingModel(null, expectedName);
        String modelJson = objectMapper.writeValueAsString(preparingModel);
        MockHttpServletResponse response =
                mockMvc.perform(post(SINGLE_PLAYER_ENDPOINT+"/random_battlefield")
                        .contentType(MediaType.APPLICATION_JSON)
                                .content(modelJson)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse();
        assertNotNull(response);

        GameModelUI newModel = objectMapper.readValue(response.getContentAsString(), GameModelUI.class);
        assertNotNull(newModel);
        assertNotNull(newModel.getPlayerModel());
        assertNotNull(newModel.getEnemyModel());
        assertEquals(newModel.getPlayerModel().getPlayerName(), expectedName);
        assertEquals(newModel.getPlayerModel().getSizeOfShips(), 10);
        assertEquals(newModel.getEnemyModel().getPlayerName(), "Bot");
        assertEquals(newModel.getActivePlayer(), newModel.getPlayerModel().getPlayerId());

        preparingModel.setPlayerId(newModel.getPlayerModel().getPlayerId());
        modelJson = objectMapper.writeValueAsString(preparingModel);
        response =
                mockMvc.perform(post(SINGLE_PLAYER_ENDPOINT+"/random_battlefield")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(modelJson)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse();
        assertNotNull(response);

        GameModelUI changedModel = objectMapper.readValue(response.getContentAsString(), GameModelUI.class);
        assertNotNull(changedModel);
        assertNotNull(changedModel.getPlayerModel());
        assertNotNull(changedModel.getEnemyModel());

        assertEquals(newModel.getGameId(), changedModel.getGameId());
        assertEquals(newModel.getEnemyModel(), changedModel.getEnemyModel());
        assertEquals(newModel.getPlayerModel().getPlayerId(), changedModel.getPlayerModel().getPlayerId());
        assertEquals(newModel.getPlayerModel().getPlayerName(), changedModel.getPlayerModel().getPlayerName());
        assertNotEquals(newModel.getPlayerModel().getBattleField(), changedModel.getPlayerModel().getBattleField());
    }

    @Test
    void shouldDeleteGameModelTest() throws Exception {
        String expectedName = "Name";
        PreparingModel preparingModel = new PreparingModel(null, expectedName);
        String modelJson = objectMapper.writeValueAsString(preparingModel);
        MockHttpServletResponse response =
                mockMvc.perform(post(SINGLE_PLAYER_ENDPOINT+"/random_battlefield")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(modelJson)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse();
        assertNotNull(response);

        GameModelUI model = objectMapper.readValue(response.getContentAsString(), GameModelUI.class);

        UUID gameId = model.getGameId();
        response =
                mockMvc.perform(delete(SINGLE_PLAYER_ENDPOINT+"/game/"+gameId)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse();
        assertNotNull(response);
    }
}