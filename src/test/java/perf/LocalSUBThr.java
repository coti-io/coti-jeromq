package perf;

import zmq.Ctx;
import zmq.Msg;
import zmq.SocketBase;
import zmq.ZMQ;

public class LocalSUBThr {

    private LocalSUBThr() {
    }

    public static void main(String[] argv) {
        String bindTo;
        long messageCount;
        int messageSize;
        Ctx ctx;
        SocketBase sub;
        boolean rc;
        long i;
        Msg msg;
        long watch;
        long elapsed;
        long throughput;
        double megabits;

        if (argv.length != 3) {
            printf("usage: local_thr <bind-to> <message-size> <message-count>\n");
            return;
        }
        bindTo = argv[0];
        messageSize = atoi(argv[1]);
        messageCount = atol(argv[2]);

        ctx = ZMQ.init(1);
        if (ctx == null) {
            printf("error in init");
            return;
        }

        sub = ZMQ.socket(ctx, ZMQ.ZMQ_SUB);
        if (sub == null) {
            printf("error in socket");
        }

        //  Add your socket options here.
        //  For example ZMQ_RATE, ZMQ_RECOVERY_IVL and ZMQ_MCAST_LOOP for PGM.

        rc = ZMQ.connect(sub, bindTo);
        if (!rc) {
            printf("error in bind: %s\n");
            return;
        }

        ZMQ.setSocketOption(sub, ZMQ.ZMQ_SUBSCRIBE, new byte[0]);
        msg = sub.recv(0);
        watch = ZMQ.startStopwatch();

        for (i = 0; i != messageCount - 2; i++) {
            msg = sub.recv(0);
            if (msg == null) {
                printf("error in recvmsg: %s\n",msg.toString());
                return;
            }
            if (ZMQ.msgSize(msg) != messageSize) {
                printf("message of incorrect size received " + ZMQ.msgSize(msg));
                return;
            }
        }

        elapsed = ZMQ.stopStopwatch(watch);
        if (elapsed == 0) {
            elapsed = 1;
        }

        throughput = (long) (((double) messageCount / (double) elapsed) * 1000000L);
        megabits = (double) (throughput * messageSize * 8) / 1000000;

        printf("time elapsed: %.3f", (double) elapsed / 1000000L);
        printf("message size: %d [B]", (int) messageSize);
        printf("message count: %d", messageCount);
        printf("message throughput: %d [msg/s]", (int) throughput);
        printf("message throughput: %.3f [Mb/s]", (double) megabits);

        ZMQ.close(sub);

        ZMQ.term(ctx);
    }

    private static void printf(String str, Object... args) {
        // TODO Auto-generated method stub
        System.out.println(String.format(str, args));
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
