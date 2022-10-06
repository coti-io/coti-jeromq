package perf;

import zmq.Ctx;
import zmq.Msg;
import zmq.SocketBase;
import zmq.ZMQ;

public class RemotePUBThr {
    private RemotePUBThr() {
    }

    public static void main(String[] argv) {
        String connectTo;
        long messageCount;
        int messageSize;
        Ctx ctx;
        SocketBase pub;
        boolean rc;
        long i;
        Msg msg;

        if (argv.length != 3) {
            printf("usage: remote_thr <connect-to> <message-size> <message-count>\n");
            return;
        }
        connectTo = argv[0];
        messageSize = atoi(argv[1]);
        messageCount = atol(argv[2]);

        ctx = ZMQ.init(1);
        if (ctx == null) {
            printf("error in init");
            return;
        }

        pub = ZMQ.socket(ctx, ZMQ.ZMQ_PUB);
        if (pub == null) {
            printf("error in socket");
        }

        //  Add your socket options here.
        //  For example ZMQ_RATE, ZMQ_RECOVERY_IVL and ZMQ_MCAST_LOOP for PGM.
        ZMQ.setSocketOption(pub,ZMQ.ZMQ_SNDHWM, (int) messageCount);
        rc = ZMQ.bind(pub, connectTo);
        if (!rc) {
            printf("error in connect: %s\n");
            return;
        }
        printf("host: " + ZMQ.getSocketOptionExt(pub, ZMQ.ZMQ_LAST_ENDPOINT));

        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e) {
        }

        for (i = 0; i != messageCount; i++) {
            msg = ZMQ.msgInitWithSize(messageSize);
            if (msg == null) {
                printf("error in msg_init: %s\n");
                return;
            }

            rc = pub.send(msg,0);
            if (!rc) {
                printf("error in sendmsg: %s\n");
                return;
            }
        }

        ZMQ.close(pub);

        ZMQ.term(ctx);

    }

    private static int atoi(String string) {
        return Integer.valueOf(string);
    }

    private static long atol(String string) {
        return Long.valueOf(string);
    }

    private static void printf(String string) {
        System.out.println(string);
    }
}
