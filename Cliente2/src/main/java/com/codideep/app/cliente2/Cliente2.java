/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.codideep.app.cliente2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Cliente2 {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private main gameFrame;
    private boolean isButtonEnabled = true;

    public Cliente2(main gameFrame) {
        this.gameFrame = gameFrame;
        try {
            socket = new Socket("localhost", 12345); // Cambiar "localhost" por la IP del servidor si es necesario
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("Cliente2");
            new Thread(new Listener()).start();
        } catch (IOException ex) {
            Logger.getLogger(Cliente2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendMessage(String message) {
        if (isButtonEnabled) {
            out.println(message);
            isButtonEnabled = false;
        }
    }

    private class Listener implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    handleServerMessage(message);
                }
            } catch (IOException ex) {
                Logger.getLogger(Cliente2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void handleServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = message.split(" ");
            String command = parts[0];

            switch (command) {
                case "ACTUALIZAR_PUNTUACION":
                    String jugador = parts[1];
                    String puntuacion = parts[2];
                    if (jugador.equals("JUGADOR1")) {
                        gameFrame.updateScore1(puntuacion);
                    } else if (jugador.equals("JUGADOR2")) {
                        gameFrame.updateScore2(puntuacion);
                    }
                    break;
                case "PINTAR_BOTON":
                    gameFrame.paintButton(parts[1]);
                    isButtonEnabled = true;
                    break;
               case "GANASTE":
                    JOptionPane.showMessageDialog(gameFrame, "¡Ganaste!");
                    System.exit(0);
                    break;
                case "PERDISTE":
                    JOptionPane.showMessageDialog(gameFrame, "Perdiste.");
                    System.exit(0);
                    break;
            }
        });
    }
}
