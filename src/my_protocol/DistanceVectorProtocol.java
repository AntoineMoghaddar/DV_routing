package my_protocol;

import framework.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Antoine Moghaddar, Fabio Allous
 * This code is based upon code from the submission of last year.
 */
public class DistanceVectorProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;

    //The potential Forwarding Table we will use
    private final ConcurrentHashMap<Integer, Map<Integer, MyRoute>> pots = new ConcurrentHashMap<>();

    //The forwarding table we will use
    private final HashMap<Integer, MyRoute> fots = new HashMap<>();

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
    }

    private void processDataTable(PacketWithLinkCost packet) {
        DataTable dataTable = packet.getPacket().getDataTable();

        int currentRow = 0;

        while (currentRow < dataTable.getNRows()) {
            if (dataTable.get(currentRow, 2) < 6) {   //6 = Node Count
                Map<Integer, MyRoute> routes;
                //if the potential Ft doesnt contain the node, create new hashMap and add the node to the routes
                if (!pots.containsKey(dataTable.get(currentRow, 0))) {
                    routes = new HashMap<>();
                    // Add the new routes to the potential forwarding table
                    pots.put(dataTable.get(currentRow, 0), routes);

                } else {
                    routes = pots.get(dataTable.get(currentRow, 0));
                }
                //create a newRoute and add this to the datatable of the potential forwarding table
                MyRoute newRoute = new MyRoute(

                        dataTable.get(currentRow, 0),
                        packet.getPacket().getSourceAddress(),
                        dataTable.get(currentRow, 1) + packet.getLinkCost(),
                        (dataTable.get(currentRow, 2) + 1));
                // Add new route (with new destination, nextHop, cost,and amount of hops) to routes hashmap
                routes.put(packet.getPacket().getSourceAddress(), newRoute);
            }
            currentRow++;
        }

    }

    private void reset() {
        // After every iteration, we reset because the topology also changes.
        fots.clear();
        pots.clear();
    }

    @Override
    public void tick(PacketWithLinkCost[] packets) {
        reset();

        //Loop through packets and check validity link cost
        Arrays.stream(packets).filter(packet -> packet.getLinkCost() != -1).forEach(this::processDataTable);

        // Add the node itself to the table so it also contains that
        MyRoute r = new MyRoute(this.linkLayer.getOwnAddress(), this.linkLayer.getOwnAddress(), 0, 1);
        fots.put(this.linkLayer.getOwnAddress(), r);

        constructFT();  // we construct the Forwarding Table
        addRowToDT();  // we add the row to the Data Table

    }

    //based on the pots, decide on which pots are the most ideal (lowest in cost) and update the fots accordingly
    private void constructFT() {
        pots.forEach((key, value) -> {
            value.forEach((key1, value1) -> { // We loop in the inner map of the for
                System.out.println("Own address: " + this.linkLayer.getOwnAddress() + "\nCurrent Key: " + key);
                if (fots.containsKey(key)) {
                    if (fots.get(key).getCost() > value1.getCost()) {
                        fots.put(key, value1);
                    }
                } else { // Since the entry is not in the table, we will just put it in the FT
                    fots.put(key, value1);
                }
            });
        });
    }

    // Add the currently calculated row to the datatable and update the status of the hops
    //After Forwarding table has constructed, packet is generated and sent over the linklayer
    private void addRowToDT() {
        IntStream.rangeClosed(1, 6) //6 = Node Count
                .filter(j -> j != this.linkLayer.getOwnAddress()).forEach(j -> {
            DataTable dt = new DataTable(3);
            fots.forEach((key, value) -> {
                if (value.getNextHop() != j) {
                    Integer[] row = {
                            value.getDestination(),
                            value.getCost(),
                            value.getHops()
                    };
                    dt.addRow(row); ///Construct new data table for presentation to server
                }
            });
            Packet pkt = new Packet(this.linkLayer.getOwnAddress(), j, dt); //We send the data to all the nodes one by one instead of sending to destination 0
            this.linkLayer.transmit(pkt);
        });
    }

    // Makes the result presentable to the server
    @Override
    public HashMap<Integer, Integer> getForwardingTable() {
        return fots.values().stream().collect(Collectors.toMap(MyRoute::getDestination, MyRoute::getNextHop, (a, b) -> b, HashMap::new));
    }
}
