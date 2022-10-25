package com.github.yihtserns.spring.remoting.jsonrpc

import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class CalcServiceImpl implements CalcService {

    @Override
    int subtractArray(int firstValue, int secondValue) {
        return firstValue - secondValue;
    }

    @Override
    int subtractObject(SubtractObject bean) {
        return bean.firstValue - bean.secondValue
    }

    @Override
    String returnStringArg(String value) {
        return value
    }

    @Override
    String[] returnStringArrayArg(String[] value) {
        return value
    }

    @Override
    List<String> returnStringListArg(List<String> value) {
        value.each { assert it instanceof String }
        return value
    }

    @Override
    Set<String> returnStringSetArg(Set<String> value) {
        value.each { assert it instanceof String }
        return value
    }

    @Override
    Collection<String> returnStringCollectionArg(Collection<String> value) {
        value.each { assert it instanceof String }
        return value
    }

    @Override
    boolean returnBooleanArg(boolean value) {
        return value
    }

    @Override
    Boolean returnBooleanWrapperArg(Boolean value) {
        return value
    }

    @Override
    boolean[] returnBooleanArrayArg(boolean[] value) {
        return value
    }

    @Override
    Boolean[] returnBooleanWrapperArrayArg(Boolean[] value) {
        value.each { assert it instanceof Boolean }
        return value
    }

    @Override
    List<Boolean> returnBooleanListArg(List<Boolean> value) {
        value.each { assert it instanceof Boolean }
        return value
    }

    @Override
    Set<Boolean> returnBooleanSetArg(Set<Boolean> value) {
        value.each { assert it instanceof Boolean }
        return value
    }

    @Override
    Collection<Boolean> returnBooleanCollectionArg(Collection<Boolean> value) {
        value.each { assert it instanceof Boolean }
        return value
    }

    @Override
    double returnDoubleArg(double value) {
        return value
    }

    @Override
    Double returnDoubleWrapperArg(Double value) {
        return value
    }

    @Override
    double[] returnDoubleArrayArg(double[] value) {
        return value
    }

    @Override
    Double[] returnDoubleWrapperArrayArg(Double[] value) {
        return value
    }

    @Override
    List<Double> returnDoubleListArg(List<Double> value) {
        value.each { assert it instanceof Double }
        return value
    }

    @Override
    Set<Double> returnDoubleSetArg(Set<Double> value) {
        value.each { assert it instanceof Double }
        return value
    }

    @Override
    Collection<Double> returnDoubleCollectionArg(Collection<Double> value) {
        value.each { assert it instanceof Double }
        return value
    }

    @Override
    float returnFloatArg(float value) {
        return value
    }

    @Override
    Float returnFloatWrapperArg(Float value) {
        return value
    }

    @Override
    float[] returnFloatArrayArg(float[] value) {
        return value
    }

    @Override
    Float[] returnFloatWrapperArrayArg(Float[] value) {
        return value
    }

    @Override
    List<Float> returnFloatListArg(List<Float> value) {
        value.each { assert it instanceof Float }
        return value
    }

    @Override
    Set<Float> returnFloatSetArg(Set<Float> value) {
        value.each { assert it instanceof Float }
        return value
    }

    @Override
    Collection<Float> returnFloatCollectionArg(Collection<Float> value) {
        value.each { assert it instanceof Float }
        return value
    }

    @Override
    BigDecimal returnBigDecimalArg(BigDecimal value) {
        return value
    }

    @Override
    BigDecimal[] returnBigDecimalArrayArg(BigDecimal[] value) {
        return value
    }

    @Override
    List<BigDecimal> returnBigDecimalListArg(List<BigDecimal> value) {
        value.each { assert it instanceof BigDecimal }
        return value
    }

    @Override
    Set<BigDecimal> returnBigDecimalSetArg(Set<BigDecimal> value) {
        value.each { assert it instanceof BigDecimal }
        return value
    }

    @Override
    Collection<BigDecimal> returnBigDecimalCollectionArg(Collection<BigDecimal> value) {
        value.each { assert it instanceof BigDecimal }
        return value
    }

    @Override
    TimeUnit returnEnumArg(TimeUnit value) {
        return value
    }

    @Override
    TimeUnit[] returnEnumArrayArg(TimeUnit[] value) {
        return value
    }

    @Override
    List<TimeUnit> returnEnumListArg(List<TimeUnit> value) {
        value.each { assert it instanceof TimeUnit }
        return value
    }

    @Override
    Set<TimeUnit> returnEnumSetArg(Set<TimeUnit> value) {
        value.each { assert it instanceof TimeUnit }
        return value
    }

    @Override
    Collection<TimeUnit> returnEnumCollectionArg(Collection<TimeUnit> value) {
        value.each { assert it instanceof TimeUnit }
        return value
    }

    @Override
    OffsetDateTime returnOffsetDateTimeArg(OffsetDateTime value) {
        return value
    }

    @Override
    OffsetDateTime[] returnOffsetDateTimeArrayArg(OffsetDateTime[] value) {
        return value
    }

    @Override
    List<OffsetDateTime> returnOffsetDateTimeListArg(List<OffsetDateTime> value) {
        value.each { assert it instanceof OffsetDateTime }
        return value
    }

    @Override
    Set<OffsetDateTime> returnOffsetDateTimeSetArg(Set<OffsetDateTime> value) {
        value.each { assert it instanceof OffsetDateTime }
        return value
    }

    @Override
    Collection<OffsetDateTime> returnOffsetDateTimeCollectionArg(Collection<OffsetDateTime> value) {
        value.each { assert it instanceof OffsetDateTime }
        return value
    }

    @Override
    Map<String, Integer> returnMapArg(Map<String, Integer> value) {
        value.each { k, v -> assert k instanceof String && v instanceof Integer }
        return value
    }

    @Override
    Map<String, Integer>[] returnMapArrayArg(Map<String, Integer>[] value) {
        value*.each { k, v -> assert k instanceof String && v instanceof Integer }
        return value
    }

    @Override
    List<Map<String, Integer>> returnMapListArg(List<Map<String, Integer>> value) {
        value*.each { k, v -> assert k instanceof String && v instanceof Integer }
        return value
    }

    @Override
    Set<Map<String, Integer>> returnMapSetArg(Set<Map<String, Integer>> value) {
        value*.each { k, v -> assert k instanceof String && v instanceof Integer }
        return value
    }

    @Override
    Collection<Map<String, Integer>> returnMapCollectionArg(Collection<Map<String, Integer>> value) {
        value*.each { k, v -> assert k instanceof String && v instanceof Integer }
        return value
    }

    @Override
    DataTypeObject returnObjectArg(DataTypeObject value) {
        value.stringListValue.each { assert it instanceof String }
        value.stringSetValue.each { assert it instanceof String }
        value.stringCollectionValue.each { assert it instanceof String }

        value.doubleListValue.each { assert it instanceof Double }
        value.doubleSetValue.each { assert it instanceof Double }
        value.doubleCollectionValue.each { assert it instanceof Double }

        value.floatListValue.each { assert it instanceof Float }
        value.floatSetValue.each { assert it instanceof Float }
        value.floatCollectionValue.each { assert it instanceof Float }

        value.bigDecimalListValue.each { assert it instanceof BigDecimal }
        value.bigDecimalSetValue.each { assert it instanceof BigDecimal }
        value.bigDecimalCollectionValue.each { assert it instanceof BigDecimal }

        value.enumListValue.each { assert it instanceof TimeUnit }
        value.enumSetValue.each { assert it instanceof TimeUnit }
        value.enumCollectionValue.each { assert it instanceof TimeUnit }

        value.mapValue.each { k, v -> assert k instanceof String && v instanceof Integer }
        value.mapArrayValue*.each { k, v -> assert k instanceof String && v instanceof Integer }
        value.mapListValue*.each { k, v -> assert k instanceof String && v instanceof Integer }
        value.mapSetValue*.each { k, v -> assert k instanceof String && v instanceof Integer }
        value.mapCollectionValue*.each { k, v -> assert k instanceof String && v instanceof Integer }

        return value
    }

    @Override
    void throwException() {
        throw new RuntimeException("Simulated Exception!")
    }

    @Override
    void throwError() {
        throw new Error("Simulated Error!")
    }

    @Override
    void throwCustomApplicationException(int errorCode) throws CustomApplicationException {
        throw new CustomApplicationException(errorCode)
    }

    @Override
    void throwHandledIllegalArgumentException() {
        throw new IllegalArgumentException("Handled")
    }
}