package com.tlcsdm.figma2json;

import javafx.application.Application;

/**
 * Main entry point that launches the JavaFX application.
 * This class is needed to avoid issues with JavaFX module loading.
 */
public class Main {
    public static void main(String[] args) {
        Application.launch(Figma2JsonApp.class, args);
    }
}
