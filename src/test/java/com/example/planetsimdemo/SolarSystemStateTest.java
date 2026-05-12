package com.example.planetsimdemo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import javafx.scene.paint.Color;

class SolarSystemStateTest {
    SolarSystem system = new SolarSystem();

    @Test
    void updateBody() {
        assertEquals(true, system.updateBody(
                "Earth",
                "Arth",
                "Planet",
                null,
                5.9722e24,
                6371,
                1.0,
                0.0167,
                0.0,
                0.0,
                0.0,
                0.0,
                Color.BLUE,
                1.0,
                "/textures/earth.jpg"
        ));
    }






}