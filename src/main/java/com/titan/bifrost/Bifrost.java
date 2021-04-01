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

                ZMQ.Poller items = context.createPoller(2);
                items.register(frontend, ZMQ.Poller.POLLIN);
                items.register(backend, ZMQ.Poller.POLLIN);

                boolean more = false;
                byte[] message;

                while (!Thread.currentThread().isInterrupted()) {
                    items.poll();

                    if (items.pollin(0)) {
                        do {
                            message = frontend.recv(0);
                            log.info("(1 of 4) Received a message from Loki");
                            more = frontend.hasReceiveMore();
                            backend.send(message, more ? ZMQ.SNDMORE : 0);
                            log.info("(2 of 4) Sent message to Thor");
                        } while (more);
                    }

                    if (items.pollin(1)) {
                        do {
                            log.info("(3 of 4) Received message from Thor");
                            message = backend.recv(0);
                            more = backend.hasReceiveMore();
                            frontend.send(message, more ? ZMQ.SNDMORE : 0);
                            log.info("(4 of 4) Sent message back to Loki");
                        } while (more);
                    }
                }
            }
        }
    }
}
