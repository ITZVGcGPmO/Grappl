package com.daexsys.grappl.server;

import io.github.itzvgcgpmo.grappl4bungee.Main;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A host represents the connection between the server and an open Grappl client.
 * It is connected to a service-server, which runs on the assigned port on the GrapplServer.
 * ExClients can connect to that service-server. When they do, a message is sent to the Grappl client
 * associated with that connection, and it will open a traffic-client to the traffic-server.
 *
 * Each connection (ExClient-GrapplServer-Client) spawns two threads on the GrapplServer;
 * one is for reading data, one is for writing data.
 *
 * An additional thread is maintained to handle the host loop.
 */
public class Host {

    // The address (IP) of the Grappl client associated with this connection.
    private String address;

    // The message socket open to the associated Grappl client.
    private Socket messageSocket;

    // Whether or not the ExServer is open.
    private boolean open = false;

    // The port number the ExServer is running on.
    private int portNumber;

    // The bungeecord server name (and forced_host entry)
    private String srvnm;

    // The number of ExClients currently connected to the ExServer. This variable is not always accurate. (Fix?)
    private int clientCount = 0;

    // The time the host opened
    private long timeOpened;

    // The traffic server
    private ServerSocket trafficSocket;

    /**
     * Construct a host object.
     * @param socket the message-socket associated with this host
     */
    public Host(final Socket socket) {
        this.messageSocket = socket;
        this.address = socket.getInetAddress().toString();

        timeOpened = System.currentTimeMillis();

        // Display debug message for VPS-user to see.
        log(Main.hst_conn.replaceAll("%host%", socket.getInetAddress().toString()));
    }

    /**
     * Initializes the host.
     *
     * Activities this involves:
     * - Getting the ExServer's port (either randomized or attached to the User object)
     * - Creating the ExServer.
     * - Initializing the host thread. This waits for ExClient connections.
     * - Passing messages to the GrClient.
     */
    public void start() {
        try {
            final Host thisHost = this;
            final DataInputStream inStream = new DataInputStream(messageSocket.getInputStream());
            portNumber = Server.getPort(inStream.readInt());
            trafficSocket = new ServerSocket(portNumber+1);

            // Tell the host what port they're running on.
            final PrintStream printStream = new PrintStream(messageSocket.getOutputStream());
            printStream.println(getPortNumber());

            srvnm = inStream.readLine();
            if (!(0 < srvnm.length() && srvnm.length() < 64)) {
                srvnm = Main.df_srvnm.replaceAll("%port%", getPortNumber()+"");
            }
            log(srvnm);

            final ServerSocket serviceServer = new ServerSocket(getPortNumber());
            log(Main.hst_hosting.replaceAll("%port%", getPortNumber()+"").replaceAll("%host%", messageSocket.getInetAddress().toString()));

            open = true;

            /**
             * This thread loops over and over attempting to connect ExClients to the Host until something in the loop
             * throws an exception, in which case the thread prints an error message, cleans up, and disconnects
             * the host.
             */
            Thread hostServer = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            // Receive external client connection
                            final Socket local = serviceServer.accept();

                            // Display message that a client is attempting to connect
//                            log(getPortNumber()+": EX-CL:"+local.getInetAddress()+" ATTEMPT -> H:"+
//                                    messageSocket.getInetAddress());

                            // Inform host that something is coming
                            printStream.println(getPortNumber());

                            // If host is still open
                            if(System.currentTimeMillis() < getTick() + 2000) {
                                // Attempt to launch connection
                                launchNewConnection(local);
                            } else {
                                thisHost.closeHost();
                            }
                        }
                    } catch (Exception e) {
                        log(getPortNumber()+": CONNECTION FAILED; SHUTDOWN HOST");

                        try {
                            serviceServer.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        closeHost();
                        e.printStackTrace();
                    }
                }
            });
            hostServer.start();

        } catch (Exception e) {
            e.printStackTrace();
            open = false;
        }
    }

    /**
     * If the ExServer is currently open, close the host.
     */
    public void closeHost() {
        if(open) {
            open = false;

            log(Main.hst_close.replaceAll("%port%", getPortNumber()+"").replaceAll("%host%", address));

            try {
                messageSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Server.removeHost(this);
        }
    }

    public static void log(String log) {
        System.out.println("[GrapplServer] " + log);
    }

    /**
     * Launches a new 'connections'.
     *
     * A connection is the bridge between an ExClient and a GrClient.
     * @param local the socket connecting the ExServer to the ExClient
     */
    public void launchNewConnection(final Socket local) {
        // Increment the number of clients connected. May or may not decrement, ever. Probably doesn't.
        clientCount++;

        // Create the actual connection thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Get traffic socket.
                    final Socket remote = trafficSocket.accept();

//                    log(getPortNumber()+": EX-CL:"+local.getInetAddress()+" CONNECTED -> H:"+
//                            messageSocket.getInetAddress());
                    Thread localToRemote = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] buffer = new byte[4096];
                            int size;

                            try {
                                while ((size = local.getInputStream().read(buffer)) != -1) {
                                    remote.getOutputStream().write(buffer, 0, size);

                                    try {
                                        Thread.sleep(5);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                try {
                                    local.close();
                                    remote.close();
                                    clientCount--;
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }

                            try {
                                local.close();
                                remote.close();
                                clientCount--;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    localToRemote.start();

                    final Thread remoteToLocal = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] buffer = new byte[4096];
                            int size;

                            try {
                                while ((size = remote.getInputStream().read(buffer)) != -1) {
                                    local.getOutputStream().write(buffer, 0, size);

                                    try {
                                        Thread.sleep(5);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                try {
                                    local.close();
                                    remote.close();
                                    clientCount--;
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }

                            try {
                                local.close();
                                remote.close();
                                clientCount--;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    remoteToLocal.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public long getTimeOpened() {
        return timeOpened;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getServerName() {
        return srvnm;
    }

    public boolean isOpen() {
        return open;
    }

    public int connectionCount() {
        return clientCount;
    }

    public long getTick() {
        return Server.getHostTick(address);
    }

    public String getAddress() {
        return address;
    }
}
