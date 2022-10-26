package com.github.yihtserns.spring.remoting.jsonrpc

import org.javatuples.Quartet

import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class DataTypeObject {

    String stringValue
    String[] stringArrayValue
    List<String> stringListValue
    List<String> stringSetValue
    Collection<String> stringCollectionValue

    boolean booleanValue
    Boolean booleanWrapperValue
    boolean[] booleanArrayValue
    Boolean[] booleanWrapperArrayValue
    List<Boolean> booleanListValue
    Set<Boolean> booleanSetValue
    Collection<Boolean> booleanCollectionValue

    double doubleValue
    Double doubleWrapperValue
    double[] doubleArrayValue
    Double[] doubleWrapperArrayValue
    List<Double> doubleListValue
    Set<Double> doubleSetValue
    Collection<Double> doubleCollectionValue

    float floatValue
    Float floatWrapperValue
    float[] floatArrayValue
    Float[] floatWrapperArrayValue
    List<Float> floatListValue
    Set<Float> floatSetValue
    Collection<Float> floatCollectionValue

    BigDecimal bigDecimalValue
    BigDecimal[] bigDecimalArrayValue
    List<BigDecimal> bigDecimalListValue
    Set<BigDecimal> bigDecimalSetValue
    Collection<BigDecimal> bigDecimalCollectionValue

    TimeUnit enumValue
    TimeUnit[] enumArrayValue
    List<TimeUnit> enumListValue
    Set<TimeUnit> enumSetValue
    Collection<TimeUnit> enumCollectionValue

    OffsetDateTime offsetDateTimeValue
    OffsetDateTime[] offsetDateTimeArrayValue
    List<OffsetDateTime> offsetDateTimeListValue
    Set<OffsetDateTime> offsetDateTimeSetValue
    Collection<OffsetDateTime> offsetDateTimeCollectionValue

    Quartet<Integer, String, Double, OffsetDateTime> tupleValue
    Quartet<Integer, String, Double, OffsetDateTime>[] tupleArrayValue
    List<Quartet<Integer, String, Double, OffsetDateTime>> tupleListValue
    Set<Quartet<Integer, String, Double, OffsetDateTime>> tupleSetValue
    Collection<Quartet<Integer, String, Double, OffsetDateTime>> tupleCollectionValue

    Map<String, Integer> mapValue
    Map<String, Integer>[] mapArrayValue
    List<Map<String, Integer>> mapListValue
    Set<Map<String, Integer>> mapSetValue
    Collection<Map<String, Integer>> mapCollectionValue

    DataTypeObject objectValue
}
