/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.codideep.app.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345;
    private static List<ClienteHandler> clientes = new ArrayList<>();
    private static Random random = new Random();
    private static String botonActual = "";
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static int puntosJugador1 = 0;
    private static int puntosJugador2 = 0;
    private static boolean clickBloqueado = false;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado...");
        
        while (clientes.size() < 2) {
            Socket socket = serverSocket.accept();
            ClienteHandler clienteHandler = new ClienteHandler(socket);
            clientes.add(clienteHandler);
            new Thread(clienteHandler).start();
        }

        scheduler.scheduleAtFixedRate(Server::pintarBoton, 0, 4, TimeUnit.SECONDS);
    }

    private static void pintarBoton() {
        char boton = (char) ('A' + random.nextInt(27));
        botonActual = String.valueOf(boton);
        clickBloqueado = false;
        enviarMensajeATodos("PINTAR_BOTON " + botonActual);
    }

    private static void enviarMensajeATodos(String mensaje) {
        for (ClienteHandler cliente : clientes) {
            cliente.enviarMensaje(mensaje);
        }
    }

    private static void procesarClick(String cliente, String boton) {
        if (clickBloqueado) {
            // Si el click está bloqueado, ignorar el click
            return;
        }
        
        if (boton.equals(botonActual)) {
            clickBloqueado = true; // Bloquear clicks después de uno válido
            if (cliente.equals("Cliente1")) {
                puntosJugador1++;
                enviarMensajeATodos("ACTUALIZAR_PUNTUACION JUGADOR1 " + puntosJugador1);
            } else {
                puntosJugador2++;
                enviarMensajeATodos("ACTUALIZAR_PUNTUACION JUGADOR2 " + puntosJugador2);
            }
            if (puntosJugador1 == 4) {
                enviarMensajeACliente("GANASTE", "Cliente1");
                enviarMensajeACliente("PERDISTE", "Cliente2");
                scheduler.shutdown();
            } else if (puntosJugador2 == 4) {
                enviarMensajeACliente("GANASTE", "Cliente2");
                enviarMensajeACliente("PERDISTE", "Cliente1");
                scheduler.shutdown();
            }
        }
    }

    private static void enviarMensajeACliente(String mensaje, String cliente) {
        for (ClienteHandler ch : clientes) {
            if (ch.getNombre().equals(cliente)) {
                ch.enviarMensaje(mensaje);
                break;
            }
        }
    }

    private static void mostrarClientesConectados() {
        System.out.println("Clientes conectados:");
        for (ClienteHandler cliente : clientes) {
            System.out.println(" - " + cliente.getNombre());
        }
    }

    private static class ClienteHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String nombre;

        public ClienteHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                nombre = in.readLine();
                mostrarClientesConectados(); // Mostrar los clientes conectados al conectar un nuevo cliente
                
                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    String[] parts = mensaje.split(" ");
                    if (parts[0].equals("BUTTON_CLICKED")) {
                        procesarClick(nombre, parts[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void enviarMensaje(String mensaje) {
            out.println(mensaje);
        }

        public String getNombre() {
            return nombre;
        }
    }
}
