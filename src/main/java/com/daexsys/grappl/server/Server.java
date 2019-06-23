package com.daexsys.grappl.server;

import com.daexsys.grappl.GrapplGlobal;
import com.daexsys.grappl.web.WebServer;
import io.github.itzvgcgpmo.grappl4bungee.Main;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    public static ServerSocket trafficServer;
    public static ServerSocket messageServer;
    public static ServerSocket heartBeatServer;

    public static List<Host> hosts = new ArrayList<Host>();

    public static Map<String, Long> heartBeats = new HashMap<String, Long>();
    public static Map<String, Host> hostMap = new HashMap<String, Host>();
    public static Map<Integer, Host> portMap = new HashMap<Integer, Host>();

    public static Set<Integer> portsTaken = new HashSet<Integer>();

    public static void main(String[] args) {
        startServer();
    }

    public static int getPort(int choice) {
        if (!portsTaken.contains(choice)) {
            portsTaken.add(choice);
            return choice;
        } else return getPort((new Random().nextInt(5000)*2)+40000);
        // if choice not avail, try random even port between 40k and 50k
    }

    static {
        try {
            trafficServer = new ServerSocket(GrapplGlobal.INNER_TRANSIT);
            messageServer = new ServerSocket(GrapplGlobal.MESSAGE_PORT);
            heartBeatServer = new ServerSocket(GrapplGlobal.HEARTBEAT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startServer() {

        // Start the web server
        WebServer.main(null);
        log(Main.gr_start);
        final Thread heartbeatReception = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        final Socket heartBeatClient = heartBeatServer.accept();

                        final String server = heartBeatClient.getInetAddress().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    DataInputStream dataInputStream = new DataInputStream(heartBeatClient
                                                .getInputStream());
                                    while(true) {
                                        int time = dataInputStream.readInt();

                                        tickHost(server);

                                        try {
                                            Thread.sleep(50);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (IOException e) {
                                    hostMap.get(server).closeHost();
                                }
                            }
                        }).start();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        heartbeatReception.start();

        boolean isRunning = true;
        // Waiting for connections from hosts
        while(isRunning) {
            // Accept a host connection.
            try {
                final Socket hostSocket = messageServer.accept();
                Host host = new Host(hostSocket);

                // Getting of user login information will occur here
//                final DataInputStream inStream = new DataInputStream(hostSocket.getInputStream());

                host.start();
                addHost(host);
            } catch (Exception e) {
                e.printStackTrace();
                isRunning = false;
            }
        }

        log("Fatal error. Closing.");
    }

    public static void addHost(Host host) {
        String srvnm = host.getServerName();
        String motd = Main.srv_motd.replaceAll("%server%", srvnm);
        log(Main.srv_add.replaceAll("%server%", srvnm));
        InetSocketAddress sock = new InetSocketAddress(InetAddress.getLoopbackAddress(), host.getPortNumber());
        final ServerInfo info = ProxyServer.getInstance().constructServerInfo(srvnm, sock, motd, Main.srv_restr);
        ProxyServer.getInstance().getServers().put(srvnm, info);
        for (ListenerInfo listener: ProxyServer.getInstance().getConfigurationAdapter().getListeners()) {
            listener.getForcedHosts().put(srvnm, srvnm);
        }
        hosts.add(host);
        hostMap.put(host.getAddress(), host);
        portMap.put(host.getPortNumber(), host);
        heartBeats.put(host.getAddress(), System.currentTimeMillis());
    }

    public static void removeHost(Host host) {
        String srvnm = host.getServerName();
        log(Main.srv_del.replaceAll("%server%", srvnm));
        ProxyServer.getInstance().getServers().remove(srvnm);
        for (ListenerInfo listener: ProxyServer.getInstance().getConfigurationAdapter().getListeners()) {
            listener.getForcedHosts().remove(srvnm, srvnm);
        }
        hosts.remove(host);
        portsTaken.remove(host.getPortNumber());
    }

    public static Host getHost(int port) {
        return portMap.get(port);
    }

    public static void tickHost(String ip) {
        heartBeats.put(ip, System.currentTimeMillis());
    }

    public static long getHostTick(String ip) {
        if(heartBeats.containsKey(ip)) {
            return heartBeats.get(ip);
        } else return System.currentTimeMillis();
    }

    public static int connectedHosts() {
        return hosts.size();
    }

    public static void log(String log) {
        System.out.println(Main.log_pref+log);
    }
}
