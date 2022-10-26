package com.github.yihtserns.spring.remoting.jsonrpc

import org.javatuples.Quartet

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
        value.each { assert it.getClass() == String }
        return value
    }

    @Override
    Set<String> returnStringSetArg(Set<String> value) {
        value.each { assert it.getClass() == String }
        return value
    }

    @Override
    Collection<String> returnStringCollectionArg(Collection<String> value) {
        value.each { assert it.getClass() == String }
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
        value.each { assert it.getClass() == Boolean }
        return value
    }

    @Override
    List<Boolean> returnBooleanListArg(List<Boolean> value) {
        value.each { assert it.getClass() == Boolean }
        return value
    }

    @Override
    Set<Boolean> returnBooleanSetArg(Set<Boolean> value) {
        value.each { assert it.getClass() == Boolean }
        return value
    }

    @Override
    Collection<Boolean> returnBooleanCollectionArg(Collection<Boolean> value) {
        value.each { assert it.getClass() == Boolean }
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
        value.each { assert it.getClass() == Double }
        return value
    }

    @Override
    Set<Double> returnDoubleSetArg(Set<Double> value) {
        value.each { assert it.getClass() == Double }
        return value
    }

    @Override
    Collection<Double> returnDoubleCollectionArg(Collection<Double> value) {
        value.each { assert it.getClass() == Double }
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
        value.each { assert it.getClass() == Float }
        return value
    }

    @Override
    Set<Float> returnFloatSetArg(Set<Float> value) {
        value.each { assert it.getClass() == Float }
        return value
    }

    @Override
    Collection<Float> returnFloatCollectionArg(Collection<Float> value) {
        value.each { assert it.getClass() == Float }
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
        value.each { assert it.getClass() == BigDecimal }
        return value
    }

    @Override
    Set<BigDecimal> returnBigDecimalSetArg(Set<BigDecimal> value) {
        value.each { assert it.getClass() == BigDecimal }
        return value
    }

    @Override
    Collection<BigDecimal> returnBigDecimalCollectionArg(Collection<BigDecimal> value) {
        value.each { assert it.getClass() == BigDecimal }
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
        value.each { assert it.getDeclaringClass() == TimeUnit }
        return value
    }

    @Override
    Set<TimeUnit> returnEnumSetArg(Set<TimeUnit> value) {
        value.each { assert it.getDeclaringClass() == TimeUnit }
        return value
    }

    @Override
    Collection<TimeUnit> returnEnumCollectionArg(Collection<TimeUnit> value) {
        value.each { assert it.getDeclaringClass() == TimeUnit }
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
        value.each { assert it.getClass() == OffsetDateTime }
        return value
    }

    @Override
    Set<OffsetDateTime> returnOffsetDateTimeSetArg(Set<OffsetDateTime> value) {
        value.each { assert it.getClass() == OffsetDateTime }
        return value
    }

    @Override
    Collection<OffsetDateTime> returnOffsetDateTimeCollectionArg(Collection<OffsetDateTime> value) {
        value.each { assert it.getClass() == OffsetDateTime }
        return value
    }

    @Override
    Quartet<Integer, String, Double, OffsetDateTime> returnTupleArg(Quartet<Integer, String, Double, OffsetDateTime> value) {
        value.with {
            assert it.value0.getClass() == Integer
            assert it.value1.getClass() == String
            assert it.value2.getClass() == Double
            assert it.value3.getClass() == OffsetDateTime
        }
        return value
    }

    @Override
    Quartet<Integer, String, Double, OffsetDateTime>[] returnTupleArrayArg(Quartet<Integer, String, Double, OffsetDateTime>[] value) {
        value.each {
            it.with {
                assert it.value0.getClass() == Integer
                assert it.value1.getClass() == String
                assert it.value2.getClass() == Double
                assert it.value3.getClass() == OffsetDateTime
            }
        }
        return value
    }

    @Override
    List<Quartet<Integer, String, Double, OffsetDateTime>> returnTupleListArg(List<Quartet<Integer, String, Double, OffsetDateTime>> value) {
        value.each {
            it.with {
                assert it.value0.getClass() == Integer
                assert it.value1.getClass() == String
                assert it.value2.getClass() == Double
                assert it.value3.getClass() == OffsetDateTime
            }
        }
        return value
    }

    @Override
    Set<Quartet<Integer, String, Double, OffsetDateTime>> returnTupleSetArg(Set<Quartet<Integer, String, Double, OffsetDateTime>> value) {
        value.each {
            it.with {
                assert it.value0.getClass() == Integer
                assert it.value1.getClass() == String
                assert it.value2.getClass() == Double
                assert it.value3.getClass() == OffsetDateTime
            }
        }
        return value
    }

    @Override
    Collection<Quartet<Integer, String, Double, OffsetDateTime>> returnTupleCollectionArg(Collection<Quartet<Integer, String, Double, OffsetDateTime>> value) {
        value.each {
            it.with {
                assert it.value0.getClass() == Integer
                assert it.value1.getClass() == String
                assert it.value2.getClass() == Double
                assert it.value3.getClass() == OffsetDateTime
            }
        }
        return value
    }

    @Override
    Map<String, Integer> returnMapArg(Map<String, Integer> value) {
        value.each { k, v -> assert k.getClass() == String && v.getClass() == Integer }
        return value
    }

    @Override
    Map<String, Integer>[] returnMapArrayArg(Map<String, Integer>[] value) {
        value*.each { k, v -> assert k.getClass() == String && v.getClass() == Integer }
        return value
    }

    @Override
    List<Map<String, Integer>> returnMapListArg(List<Map<String, Integer>> value) {
        value*.each { k, v -> assert k.getClass() == String && v.getClass() == Integer }
        return value
    }

    @Override
    Set<Map<String, Integer>> returnMapSetArg(Set<Map<String, Integer>> value) {
        value*.each { k, v -> assert k.getClass() == String && v.getClass() == Integer }
        return value
    }

    @Override
    Collection<Map<String, Integer>> returnMapCollectionArg(Collection<Map<String, Integer>> value) {
        value*.each { k, v -> assert k.getClass() == String && v.getClass() == Integer }
        return value
    }

    @Override
    DataTypeObject returnObjectArg(DataTypeObject value) {
        value.stringListValue.each { assert it.getClass() == String }
        value.stringSetValue.each { assert it.getClass() == String }
        value.stringCollectionValue.each { assert it.getClass() == String }

        value.doubleListValue.each { assert it.getClass() == Double }
        value.doubleSetValue.each { assert it.getClass() == Double }
        value.doubleCollectionValue.each { assert it.getClass() == Double }

        value.floatListValue.each { assert it.getClass() == Float }
        value.floatSetValue.each { assert it.getClass() == Float }
        value.floatCollectionValue.each { assert it.getClass() == Float }

        value.bigDecimalListValue.each { assert it.getClass() == BigDecimal }
        value.bigDecimalSetValue.each { assert it.getClass() == BigDecimal }
        value.bigDecimalCollectionValue.each { assert it.getClass() == BigDecimal }

        value.enumListValue.each { assert it.getDeclaringClass() == TimeUnit }
        value.enumSetValue.each { assert it.getDeclaringClass() == TimeUnit }
        value.enumCollectionValue.each { assert it.getDeclaringClass() == TimeUnit }

        value.offsetDateTimeListValue.each { assert it.getClass() == OffsetDateTime }
        value.offsetDateTimeSetValue.each { assert it.getClass() == OffsetDateTime }
        value.offsetDateTimeCollectionValue.each { assert it.getClass() == OffsetDateTime }

        value.tupleValue?.with {
            assert it.value0.getClass() == Integer
            assert it.value1.getClass() == String
            assert it.value2.getClass() == Double
            assert it.value3.getClass() == OffsetDateTime
        }
        value.tupleArrayValue.with {
            it.each {
                it.with {
                    assert it.value0.getClass() == Integer
                    assert it.value1.getClass() == String
                    assert it.value2.getClass() == Double
                    assert it.value3.getClass() == OffsetDateTime
                }
            }
        }
        value.tupleListValue.each {
            it.with {
                assert it.value0.getClass() == Integer
                assert it.value1.getClass() == String
                assert it.value2.getClass() == Double
                assert it.value3.getClass() == OffsetDateTime
            }
        }
        value.tupleSetValue.each {
            it.with {
                assert it.value0.getClass() == Integer
                assert it.value1.getClass() == String
                assert it.value2.getClass() == Double
                assert it.value3.getClass() == OffsetDateTime
            }
        }
        value.tupleCollectionValue.each {
            it.with {
                assert it.value0.getClass() == Integer
                assert it.value1.getClass() == String
                assert it.value2.getClass() == Double
                assert it.value3.getClass() == OffsetDateTime
            }
        }

        value.mapValue.each { k, v -> assert k.getClass() == String && v.getClass() == Integer }
        value.mapArrayValue*.each { k, v -> assert k.getClass() == String && v.getClass() == Integer }
        value.mapListValue*.each { k, v -> assert k.getClass() == String && v.getClass() == Integer }
        value.mapSetValue*.each { k, v -> assert k.getClass() == String && v.getClass() == Integer }
        value.mapCollectionValue*.each { k, v -> assert k.getClass() == String && v.getClass() == Integer }

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
