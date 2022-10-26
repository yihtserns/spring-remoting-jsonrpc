package com.github.yihtserns.spring.remoting.jsonrpc

import org.javatuples.Quartet

import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

interface CalcService {

    int subtractArray(int firstValue, int secondValue)

    int subtractObject(SubtractObject bean)

    String returnStringArg(String value)

    String[] returnStringArrayArg(String[] value)

    List<String> returnStringListArg(List<String> value)

    Set<String> returnStringSetArg(Set<String> value)

    Collection<String> returnStringCollectionArg(Collection<String> value)

    boolean returnBooleanArg(boolean value)

    Boolean returnBooleanWrapperArg(Boolean value)

    boolean[] returnBooleanArrayArg(boolean[] value)

    Boolean[] returnBooleanWrapperArrayArg(Boolean[] value)

    List<Boolean> returnBooleanListArg(List<Boolean> value)

    Set<Boolean> returnBooleanSetArg(Set<Boolean> value)

    Collection<Boolean> returnBooleanCollectionArg(Collection<Boolean> value)

    double returnDoubleArg(double value)

    Double returnDoubleWrapperArg(Double value)

    double[] returnDoubleArrayArg(double[] value)

    Double[] returnDoubleWrapperArrayArg(Double[] value)

    List<Double> returnDoubleListArg(List<Double> value)

    Set<Double> returnDoubleSetArg(Set<Double> value)

    Collection<Double> returnDoubleCollectionArg(Collection<Double> value)

    float returnFloatArg(float value)

    Float returnFloatWrapperArg(Float value)

    float[] returnFloatArrayArg(float[] value)

    Float[] returnFloatWrapperArrayArg(Float[] value)

    List<Float> returnFloatListArg(List<Float> value)

    Set<Float> returnFloatSetArg(Set<Float> value)

    Collection<Float> returnFloatCollectionArg(Collection<Float> value)

    BigDecimal returnBigDecimalArg(BigDecimal value)

    BigDecimal[] returnBigDecimalArrayArg(BigDecimal[] value)

    List<BigDecimal> returnBigDecimalListArg(List<BigDecimal> value)

    Set<BigDecimal> returnBigDecimalSetArg(Set<BigDecimal> value)

    Collection<BigDecimal> returnBigDecimalCollectionArg(Collection<BigDecimal> value)

    TimeUnit returnEnumArg(TimeUnit value)

    TimeUnit[] returnEnumArrayArg(TimeUnit[] value)

    List<TimeUnit> returnEnumListArg(List<TimeUnit> value)

    Set<TimeUnit> returnEnumSetArg(Set<TimeUnit> value)

    Collection<TimeUnit> returnEnumCollectionArg(Collection<TimeUnit> value)

    OffsetDateTime returnOffsetDateTimeArg(OffsetDateTime value)

    OffsetDateTime[] returnOffsetDateTimeArrayArg(OffsetDateTime[] value)

    List<OffsetDateTime> returnOffsetDateTimeListArg(List<OffsetDateTime> value)

    Set<OffsetDateTime> returnOffsetDateTimeSetArg(Set<OffsetDateTime> value)

    Collection<OffsetDateTime> returnOffsetDateTimeCollectionArg(Collection<OffsetDateTime> value)

    Quartet<Integer, String, Double, OffsetDateTime> returnTupleArg(Quartet<Integer, String, Double, OffsetDateTime> value)

    Quartet<Integer, String, Double, OffsetDateTime>[] returnTupleArrayArg(Quartet<Integer, String, Double, OffsetDateTime>[] value)

    List<Quartet<Integer, String, Double, OffsetDateTime>> returnTupleListArg(List<Quartet<Integer, String, Double, OffsetDateTime>> value)

    Set<Quartet<Integer, String, Double, OffsetDateTime>> returnTupleSetArg(Set<Quartet<Integer, String, Double, OffsetDateTime>> value)

    Collection<Quartet<Integer, String, Double, OffsetDateTime>> returnTupleCollectionArg(Collection<Quartet<Integer, String, Double, OffsetDateTime>> value)

    Map<String, Integer> returnMapArg(Map<String, Integer> value)

    Map<String, Integer>[] returnMapArrayArg(Map<String, Integer>[] value)

    List<Map<String, Integer>> returnMapListArg(List<Map<String, Integer>> value)

    Set<Map<String, Integer>> returnMapSetArg(Set<Map<String, Integer>> value)

    Collection<Map<String, Integer>> returnMapCollectionArg(Collection<Map<String, Integer>> value)

    DataTypeObject returnObjectArg(DataTypeObject value)

    void throwException()

    void throwError()

    void throwCustomApplicationException(int errorCode) throws CustomApplicationException

    void throwHandledIllegalArgumentException()
}
