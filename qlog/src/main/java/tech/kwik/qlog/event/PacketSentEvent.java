package tech.kwik.qlog.event;

import tech.kwik.core.packet.QuicPacket;

import java.time.Instant;

public class PacketSentEvent extends PacketEvent {

    public PacketSentEvent(long connectionHandle, byte[] cid, QuicPacket packet, Instant time) {
        super(connectionHandle, cid, packet, time);
    }

    public void accept(QLogEventProcessor processor) {
        processor.process(this);
    }

}