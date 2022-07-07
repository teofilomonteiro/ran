package io.ran.instancemapper;

import org.junit.Test;

import static org.junit.Assert.*;

public class InstanceMapperTest {
    InstanceMappingRegistry registry =  new InstanceMappingRegistry(new TestGenericFactory());

    @Test
    public void happyPath() {
        InstanceMapper mapper = new InstanceMapper(registry);
        MyFrom from = new MyFrom();
        from.setField1("field 1");
        MyTo to = new MyTo();
        registry.putIfAbsent(MyFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1());
        });
        mapper.map(from, to);

        assertEquals("field 1", to.getOutField1());
    }

    @Test
    public void usingSetterForFieldReference() {
        InstanceMapper mapper = new InstanceMapper(registry);
        MyFrom from = new MyFrom();
        from.setField1("field 1");
        MyTo to = new MyTo();
        registry.putIfAbsent(MyFrom.class, MyTo.class, MyTo::setOutField1, (f, t) -> {
            t.setOutField1(f.getField1());
        });
        mapper.map(from, to);

        assertEquals("field 1", to.getOutField1());
    }


    @Test
    public void putIfAbsent_onlyIfDoesntExist() {
        InstanceMapper mapper = new InstanceMapper(registry);
        MyFrom from = new MyFrom();
        from.setField1("field 1");
        MyTo to = new MyTo();
        registry.putIfAbsent(MyFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1()+" first");
        });
        registry.putIfAbsent(MyFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1()+" second");
        });
        mapper.map(from, to);

        assertEquals("field 1 first", to.getOutField1());
    }

    @Test
    public void put_overridesExisting() {
        InstanceMapper mapper = new InstanceMapper(registry);
        MyFrom from = new MyFrom();
        from.setField1("field 1");
        MyTo to = new MyTo();
        registry.putIfAbsent(MyFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1()+" first");
        });
        registry.put(MyFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1()+" second");
        });
        mapper.map(from, to);

        assertEquals("field 1 second", to.getOutField1());
    }

    @Test
    public void multipleFieldBindings() {
        InstanceMapper mapper = new InstanceMapper(registry);
        MyFrom from = new MyFrom();
        from.setField1("field 1");
        from.setField2(55);
        MyTo to = new MyTo();
        registry.putIfAbsent(MyFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1());
        });
        registry.putIfAbsent(MyFrom.class, MyTo.class, MyTo::getOutField2, (f, t) -> {
            t.setOutField2(String.valueOf(f.getField2()));
        });
        mapper.map(from, to);

        assertEquals("field 1", to.getOutField1());
        assertEquals("55", to.getOutField2());
    }


    @Test
    public void fieldRegisteredOnParentClass() {
        InstanceMapper mapper = new InstanceMapper(registry);
        MyFrom from = new MyFrom();
        from.setField1("field 1");
        MyTo to = new MyTo();
        registry.putIfAbsent(BaseFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1());
        });
        mapper.map(from, to);

        assertEquals("field 1", to.getOutField1());
    }

    @Test
    public void fieldOnClassTakesPrecedenceOverFieldOnParentClass() {
        InstanceMapper mapper = new InstanceMapper(registry);
        MyFrom from = new MyFrom();
        from.setField1("field 1");
        MyTo to = new MyTo();
        registry.putIfAbsent(BaseFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1()+" from parent class");
        });
        registry.putIfAbsent(MyFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1()+" from class");
        });
        mapper.map(from, to);

        assertEquals("field 1 from class", to.getOutField1());
    }

    @Test
    public void separateContext() {
        InstanceMapper mapper = new InstanceMapper(registry);
        MyFrom from = new MyFrom();
        from.setField1("field 1");
        MyTo to = new MyTo();
        registry.putIfAbsent(MyFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1()+" without context");
        });
        registry.putIfAbsent(MyContext.class,MyFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1()+ " with context");
        });
        mapper.map(MyContext.class, from, to);

        assertEquals("field 1 with context", to.getOutField1());
    }


    @Test
    public void separateContextWithoutBinding_usesMappingFromBaseContext() {
        InstanceMapper mapper = new InstanceMapper(registry);
        MyFrom from = new MyFrom();
        from.setField1("field 1");
        MyTo to = new MyTo();
        registry.putIfAbsent(MyFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1()+" in base context");
        });
        mapper.map(MyContext.class, from, to);

        assertEquals("field 1 in base context",to.getOutField1());
    }

    @Test
    public void separateContextWithoutBinding_usesMappingFromLocalBaseContext() {
        InstanceMapper mapper = new InstanceMapper(registry);
        MyFrom from = new MyFrom();
        from.setField1("field 1");
        MyTo to = new MyTo();
        registry.putIfAbsent(MyBaseContext.class,MyFrom.class, MyTo.class, MyTo::getOutField1, (f, t) -> {
            t.setOutField1(f.getField1()+" in my base context");
        });
        mapper.map(MyContext.class, from, to);

        assertEquals("field 1 in my base context",to.getOutField1());
    }

    public static class MyBaseContext {

    }

    public static class MyContext extends MyBaseContext {

    }

    public static class MyFrom extends BaseFrom {
        private int field2;

        public int getField2() {
            return field2;
        }

        public void setField2(int field2) {
            this.field2 = field2;
        }
    }

    public static class MyTo {
        private String outField1;
        private String outField2;

        public String getOutField1() {
            return outField1;
        }

        public void setOutField1(String outField1) {
            this.outField1 = outField1;
        }

        public String getOutField2() {
            return outField2;
        }

        public void setOutField2(String outField2) {
            this.outField2 = outField2;
        }
    }

    public static class BaseFrom {
        private String field1;

        public String getField1() {
            return field1;
        }

        public void setField1(String field1) {
            this.field1 = field1;
        }
    }

}
