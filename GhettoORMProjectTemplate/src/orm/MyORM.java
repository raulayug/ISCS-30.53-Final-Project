package orm;

import java.util.HashMap;
import java.lang.annotation.Annotation;
import java.util.List;

import annotations.MappedClass;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

import annotations.Entity;
import annotations.Column;
import dao.BasicMapper;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;


public class MyORM {

    HashMap<Class, Class> entityToMapperMap = new HashMap<Class, Class>();

    public void init() throws Exception {
        // Scan all mappers -- @MappedClass
        scanMappers();

        // Scan all the entities -- @Entity
        scanEntities();

        // Create all entity tables
        createTables();
    }

    private void scanMappers() throws ClassNotFoundException {
        ScanResult results = new FastClasspathScanner("dao").scan();
        List<String> allResults = results.getNamesOfClassesWithAnnotation(MappedClass.class);
        for (String s : allResults) {
            Class c = Class.forName(s);
            // Check if the clazz has the @MappedClass annotation
            Annotation[] annotations = c.getAnnotations();
            boolean hasMappedClassAnnotation = false;
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().getSimpleName().equals("MappedClass")) {
                    hasMappedClassAnnotation = true;
                    break;
                }
            }
            if (!hasMappedClassAnnotation) {
                throw new RuntimeException("No @MappedClass");
            }
            // Map the clazz to the mapper class
            // entityToMapperMap.put(clazz, ); // Replace with the actual mapper class
        }
    }

    private void scanEntities() throws ClassNotFoundException {
        ScanResult results = new FastClasspathScanner("entity.package").scan();
        List<String> allResults = results.getNamesOfClassesWithAnnotation(Entity.class);
        for (String className : allResults) {
            Class<?> clazz = Class.forName(className);
            Field[] fields = clazz.getDeclaredFields();
            int idFieldCount = 0;
            for (Field field : fields) {
                if (field.isAnnotationPresent(Column.class)) {
                    Column columnAnnotation = field.getAnnotation(Column.class);
                    if (columnAnnotation.id()) {
                        idFieldCount++;
                    }
                }
            }
            if (idFieldCount != 1) {
                throw new RuntimeException("duplicate id=true");
            }
        }
    }

    public Object getMapper(Class<?> clazz) {
        DaoInvocationHandler invocationHandler = new DaoInvocationHandler(/* pass any required parameters */);
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, invocationHandler);
    }


    private void createTables() {
        for (Class<?> mapperClass : entityToMapperMap.values()) {
            try {
                BasicMapper mapper = (BasicMapper) mapperClass.newInstance();
                mapper.createTable();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}

