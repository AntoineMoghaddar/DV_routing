//package my_protocol;
//
//import framework.*;
//
//import java.sql.SQLOutput;
//import java.util.HashMap;
//import java.util.Map;
//
////Fabio Allous s2345080;
////
//
///**
// * @version 12-03-2019
// *
// * Copyright University of Twente, 2013-2019
// *
// **************************************************************************
// *                            Copyright notice                            *
// *                                                                        *
// *             This file may ONLY be distributed UNMODIFIED.              *
// * In particular, a correct solution to the challenge must NOT be posted  *
// * in public places, to preserve the learning effect for future students. *
// **************************************************************************
// */
//public class MyRoutingProtocol implements IRoutingProtocol {
//    private LinkLayer linkLayer;
//
//    // You can use this data structure to store your routing table.
//    private HashMap<Integer, MyRoute> myRoutingTable = new HashMap<>();
//
//    @Override
//    public void init(LinkLayer linkLayer) {
//        this.linkLayer = linkLayer;
//    }
//
//
//    @Override
//    public void tick(PacketWithLinkCost[] packetsWithLinkCosts) {
//        // Get the address of this node
//        int myAddress = this.linkLayer.getOwnAddress();
//
//        System.out.println("tick; received " + packetsWithLinkCosts.length + " packets");
//        int i;
//
//        // first process the incoming packets; loop over them:
//        for (i = 0; i < packetsWithLinkCosts.length; i++) {
//            Packet packet = packetsWithLinkCosts[i].getPacket();
//            int neighbour = packet.getSourceAddress();             // from whom is the packet?
//            int linkcost = packetsWithLinkCosts[i].getLinkCost();  // what's the link cost from/to this neighbour?
//            int dest = packet.getDestinationAddress();
//            DataTable dt = packet.getDataTable();                  // other data contained in the packet
//
//            System.out.printf("received packet from %d with %d rows and %d columns of data%n", neighbour, dt.getNRows(), dt.getNColumns());
//
//            // you'll probably want to process the data, update your data structures (myRoutingTable) , etc....
//
//            // add neighbour to rountingtable if not yet in
//            if (!myRoutingTable.containsKey(neighbour)) {
//                MyRoute r = new MyRoute();
//                r.nextHop = neighbour;
//                r.cost = linkcost;
//                this.myRoutingTable.put(neighbour, r);
//            }
//
//
//            // set dtroutecost to 9999 to see later if we find a route in dt
//            int hopfound = 9999;
//            int dtroutecost = 9999;
//
//            // go through dt to find better or new routes
//            // find bestnexthop in dt
//            for (int index = 1; index <= 6; index++) {
//                int nexthop = 0;
//                int dthopcost = 0;
//                try {
//                    nexthop = dt.get(index, 0);;
//                    dthopcost = dt.get(index, 1) + linkcost;
//                } catch (IllegalArgumentException e) {
//                    System.out.println("Error neighbour does not have destination");
//                }
//                if (nexthop != myAddress) {
//                    if (dthopcost < dtroutecost) {
//                        dtroutecost = dthopcost;
//                        hopfound = neighbour;
//                    }
//                    // check if route found in dt (hopfound variable true)
//                    // check if routingtable already has tempdest as key
//                    // if it does, check if linkcost is lower than cost through neighbour found earlier (variable dtroutecost)
//                    if (hopfound != 9999) {
//                        if (myRoutingTable.containsKey(nexthop)) {
//                            int myroutecost = myRoutingTable.get(nexthop).cost;
//                            if (dtroutecost < myroutecost) {
//                                MyRoute newroute = new MyRoute();
//                                newroute.cost = dtroutecost;
//                                newroute.nextHop = neighbour;
//                                myRoutingTable.put(index, newroute);
//                            }
//                        } else {
//                            MyRoute newroute = new MyRoute();
//                            newroute.cost = dtroutecost;
//                            newroute.nextHop = neighbour;
//                            myRoutingTable.put(nexthop, newroute);
//                        }
//                    }
//                }
//            }
//        }
//
//        DataTable mydt = new DataTable(3);
//        for (Integer node : myRoutingTable.keySet()) {
//            Integer[] nodes = new Integer[3];
//            nodes[0] = node;
//            nodes[1] = myRoutingTable.get(node).cost;
//            nodes[2] = myRoutingTable.get(node).nextHop;
//            mydt.addRow(nodes);
//        }
//
//        // next, actually send out the packet, with our own address as the source address
//        // and 0 as the destination address: that's a broadcast to be received by all neighbours.
//        Packet pkt = new Packet(myAddress, 0, mydt);
//        this.linkLayer.transmit(pkt);
//
//    }
//
//    public Map<Integer, Integer> getForwardingTable() {
//        // This code extracts from your routing table the forwarding table.
//        // The result of this method is send to the server to validate and score your protocol.
//
//        // <Destination, NextHop>
//        HashMap<Integer, Integer> ft = new HashMap<>();
//
//        for (Map.Entry<Integer, MyRoute> entry : myRoutingTable.entrySet()) {
//            ft.put(entry.getKey(), entry.getValue().nextHop);
//        }
//
//        return ft;
//    }
//}
