// automatically generated by the FlatBuffers compiler, do not modify

package tau.tac.adx.parser.flatbuffers;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class AdxQueryPricing extends Table {
  public static AdxQueryPricing getRootAsAdxQueryPricing(ByteBuffer _bb) { return getRootAsAdxQueryPricing(_bb, new AdxQueryPricing()); }
  public static AdxQueryPricing getRootAsAdxQueryPricing(ByteBuffer _bb, AdxQueryPricing obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public AdxQueryPricing __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public AdxQuery adxQuery() { return adxQuery(new AdxQuery()); }
  public AdxQuery adxQuery(AdxQuery obj) { int o = __offset(4); return o != 0 ? obj.__init(__indirect(o + bb_pos), bb) : null; }
  public float reservePrice() { int o = __offset(6); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }

  public static int createAdxQueryPricing(FlatBufferBuilder builder,
      int adxQueryOffset,
      float reservePrice) {
    builder.startObject(2);
    AdxQueryPricing.addReservePrice(builder, reservePrice);
    AdxQueryPricing.addAdxQuery(builder, adxQueryOffset);
    return AdxQueryPricing.endAdxQueryPricing(builder);
  }

  public static void startAdxQueryPricing(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addAdxQuery(FlatBufferBuilder builder, int adxQueryOffset) { builder.addOffset(0, adxQueryOffset, 0); }
  public static void addReservePrice(FlatBufferBuilder builder, float reservePrice) { builder.addFloat(1, reservePrice, 0.0f); }
  public static int endAdxQueryPricing(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
