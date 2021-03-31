package com.titan.bifrost;

import lombok.extern.java.Log;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;import lombok.extern.java.Log;

@Log
public class Bifrost implements Runnable {

    public Bifrost() {}

    @Override
    public void run() {
        while (true) {
            try (ZContext context = new ZContext()) {
                ZMQ.Socket frontend = context.createSocket(SocketType.ROUTER);
                ZMQ.Socket backend = context.createSocket(SocketType.DEALER);
                frontend.bind("tcp://*:5559");
                backend.bind("tcp://*:5560");

                log.info("Started Bifrost...");

                //  Initialize poll set
                ZMQ.Poller items = context.createPoller(2);
                items.register(frontend, ZMQ.Poller.POLLIN);
                items.register(backend, ZMQ.Poller.POLLIN);

                boolean more = false;
                byte[] message;

                //  Switch messages between sockets
                while (!Thread.currentThread().isInterrupted()) {
                    //  poll and memorize multipart detection
                    items.poll();

                    if (items.pollin(0)) {
                        while (true) {
                            // receive message
                            message = frontend.recv(0);
                            more = frontend.hasReceiveMore();

                            // Broker it
                            backend.send(message, more ? ZMQ.SNDMORE : 0);
                            if (!more) {
                                break;
                            }
                        }
                    }

                    if (items.pollin(1)) {
                        while (true) {
                            // receive message
                            message = backend.recv(0);
                            more = backend.hasReceiveMore();
                            // Broker it
                            frontend.send(message, more ? ZMQ.SNDMORE : 0);
                            if (!more) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
