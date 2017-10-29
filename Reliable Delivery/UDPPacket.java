import java.nio.ByteBuffer;

public class UDPPacket
{
  public byte[] getPacketBytes()
  {
    byte[] packetBytes = new byte[data.length + 13]; //4 = header length
    ByteBuffer buffer = ByteBuffer.wrap(packetBytes);

    buffer.putShort(seqNum);
    buffer.putLong(timeSent);
    buffer.put(isAck);
    buffer.put(endOfFile);
    buffer.put(acked);
    buffer.put(data);
    return packetBytes;
  }

  public static UDPPacket getPacketFields(byte[] packetBytes, int length)
  {
    byte[] data = new byte[length - 13];
    ByteBuffer packet = ByteBuffer.wrap(packetBytes);

    short seqNum = packet.getShort();
    long timeSent = packet.getLong();
    byte isAck = packet.get();
    byte endOfFile = packet.get();
    byte acked = packet.get();
    packet.get(data);
    return new UDPPacket(data, seqNum, isAck, endOfFile, timeSent, acked);
  }

  public byte[] data;
  public short seqNum;
  public byte isAck;
  public byte endOfFile;
  public long timeSent;
  public byte acked;

  public UDPPacket(byte[] data, short seqNum, byte isAck, byte endOfFile, long timeSent, byte acked)
  {
    this.data = data;
    this.seqNum = seqNum;
    this.isAck = isAck;
    this.endOfFile = endOfFile;
    this.timeSent = timeSent;
    this.acked = acked;
  }

}
