package niv.burning.impl;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

public abstract class DynamicField {

    private static final ImmutableMap<Class<?>, Function<Field, ? extends DynamicField>> MAP;

    static {
        MAP = ImmutableMap.<Class<?>, Function<Field, ? extends DynamicField>>builderWithExpectedSize(8)
                .put(int.class, IntegerField::new).put(Integer.class, IntegerField::new)
                .put(long.class, LongField::new).put(Long.class, LongField::new)
                .put(float.class, FloatField::new).put(Float.class, FloatField::new)
                .put(double.class, DoubleField::new).put(Double.class, DoubleField::new)
                .build();
    }

    protected final Field field;

    private DynamicField(Field field) {
        this.field = field;
    }

    final String getName() {
        return this.field.getName();
    }

    abstract Double get(Object target);

    abstract void set(Object target, Double value);

    static final Optional<DynamicField> of(Field field) {
        return Optional.ofNullable(MAP.getOrDefault(field.getType(), null))
                .map(constructor -> constructor.apply(field));
    }

    private static final class IntegerField extends DynamicField {
        private IntegerField(Field field) {
            super(field);
        }

        @Override
        Double get(Object target) {
            try {
                return Double.valueOf(field.getInt(target));
            } catch (NullPointerException ex) {
                return 0d;
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        @SuppressWarnings("java:S3011")
        void set(Object target, Double value) {
            try {
                field.setInt(target, value.intValue());
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private static final class LongField extends DynamicField {
        private LongField(Field field) {
            super(field);
        }

        @Override
        Double get(Object target) {
            try {
                return Double.valueOf(field.getLong(target));
            } catch (NullPointerException ex) {
                return 0d;
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        @SuppressWarnings("java:S3011")
        void set(Object target, Double value) {
            try {
                field.setLong(target, value.longValue());
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private static final class FloatField extends DynamicField {
        private FloatField(Field field) {
            super(field);
        }

        @Override
        Double get(Object target) {
            try {
                return Double.valueOf(field.getFloat(target));
            } catch (NullPointerException ex) {
                return 0d;
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        @SuppressWarnings("java:S3011")
        void set(Object target, Double value) {
            try {
                field.setFloat(target, value.floatValue());
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private static final class DoubleField extends DynamicField {
        private DoubleField(Field field) {
            super(field);
        }

        @Override
        Double get(Object target) {
            try {
                return Double.valueOf(field.getDouble(target));
            } catch (NullPointerException ex) {
                return 0d;
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        @SuppressWarnings("java:S3011")
        void set(Object target, Double value) {
            try {
                field.setDouble(target, value.doubleValue());
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
