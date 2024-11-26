package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
/**
 * Main application for the DrawNumber game.
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * Constructor for DrawNumberApp.
     * @param views the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        // Initialize the model using configuration values
        final Configuration.Builder builder = new Configuration.Builder();

        try (InputStream input = DrawNumberApp.class.getResourceAsStream("/config.yml");
             BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Remove leading and trailing whitespace
                if (line.startsWith("minimum:")) {
                    builder.setMin(Integer.parseInt(line.split(":")[1].trim()));
                } else if (line.startsWith("maximum:")) {
                    builder.setMax(Integer.parseInt(line.split(":")[1].trim()));
                } else if (line.startsWith("attempts:")) {
                    builder.setAttempts(Integer.parseInt(line.split(":")[1].trim()));
                }
            }
        } catch (IOException e) {
            displayError("Error reading configuration: " + e.getMessage());
        }

        // Create the model using the configured values
        this.model = new DrawNumberImpl(builder.build());
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * Displays an error message using a JOptionPane.
     * @param message the error message to display
     */
    public void displayError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Main method to start the application.
     * @param args ignored
     * @throws FileNotFoundException if the configuration file is not found
     */
    public static void main(final String... args) {
        // Creazione di più viste
        DrawNumberView view1 = new DrawNumberViewImpl(); // Prima vista
        DrawNumberView view2 = new DrawNumberViewImpl(); // Seconda vista
        DrawNumberView view3 = new DrawNumberViewImpl(); // Terza vista (aggiunta opzionale)

        // Passaggio delle viste al costruttore dell'applicazione
        @SuppressWarnings("unused")//used to run main class
        DrawNumberApp app = new DrawNumberApp(view1, view2, view3);

        // Le viste saranno gestite dal costruttore e attivate
    }


}
