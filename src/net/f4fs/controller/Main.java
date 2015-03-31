package net.f4fs.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.f4fs.bootstrapserver.BootstrapServerAccess;
import net.f4fs.config.Config;
import net.f4fs.fspeer.FSPeer;
import net.f4fs.util.DhtOperationsCommand;
import net.f4fs.util.IpAddressJsonParser;
import net.f4fs.util.ShutdownHookThread;

import org.json.simple.parser.ParseException;


public class Main {

    public static void main(String[] args) {
        BootstrapServerAccess boostrapServerAccess = new BootstrapServerAccess();
        FSPeer fsPeer = new FSPeer();

        // 2 ways to get the ip address
        String myIp = fsPeer.findLocalIp();
        //String myIp = fsPeer.findExternalIp();

        List<Map<String, String>> ipList = new ArrayList<>();
        try {
            ipList = IpAddressJsonParser.parse(boostrapServerAccess.getIpAddressList());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int nrOfIpAddresses = ipList.size();
        boostrapServerAccess.postIpPortPair(myIp, Config.DEFAULT.getPort());

        try {
            if (nrOfIpAddresses == 0) {
                fsPeer.startAsBootstrapPeer(myIp, Config.DEFAULT.getPort());
            } else {
                boolean success = false;
                int counter = 0;

                while (!success && (counter < nrOfIpAddresses)) {
                    success = fsPeer.startPeer(myIp,
                                               Config.DEFAULT.getPort(),
                                               ipList.get(0).get("address"),
                                               Integer.parseInt(ipList.get(0).get("port")));
                    counter++;
                }

                if (success) {
                    System.out.println("[Peer@" + myIp + "]: Bootstrap successfull");
                } else {
                    System.out.println("[Peer@" + myIp + "]: No connection possible");
                }
            }

//            // for testing on own pc
//            String myIp = "172.20.10.5";
//            int myPort = 4000;
//            // fsPeer.startAsBootstrapPeer(myIp, myPort);
//            fsPeer.startPeer(myIp, myIp, myPort, myPort);
            
            // Add shutdown hook so that IP address gets removed from server when 
            // user does not terminate program correctly on 
            Runtime.getRuntime().addShutdownHook(new ShutdownHookThread(myIp, Config.DEFAULT.getPort()));

            DhtOperationsCommand.readAndProcess(fsPeer);

            boostrapServerAccess.removeIpPortPair(myIp, Config.DEFAULT.getPort());
            fsPeer.shutdown();
        } catch (Exception pEx) {
            pEx.printStackTrace();
        }
    }

}
