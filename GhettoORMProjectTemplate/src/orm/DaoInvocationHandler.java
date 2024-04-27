package orm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.HashMap;

import realdb.GhettoJdbcBlackBox;

import annotations.*;
import java.util.Arrays;
import java.util.Comparator;
import java.lang.annotation.Annotation;
import java.util.StringJoiner;

public class DaoInvocationHandler implements InvocationHandler {

	static GhettoJdbcBlackBox jdbc;
	
	public DaoInvocationHandler() {
		// TODO Auto-generated constructor stub
		
		if (jdbc==null)
		{
			jdbc = new GhettoJdbcBlackBox();
			jdbc.init("com.mysql.cj.jdbc.Driver", 				// DO NOT CHANGE
					  "jdbc:mysql://localhost/jdbcblackbox",    // change jdbcblackbox to the DB name you wish to use
					  "root", 									// USER NAME
					  "");										// PASSWORD
		}
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable 
	{
		
		// determine method annotation type and call the appropriate method
			// @CreateTable
			// @Save
			// @Delete
			// @Select
	    // If method has none of the specified annotations, return null
		if (method.getAnnotation(CreateTable.class) != null) 
		{
	        createTable(method);
	    } 
		else if (method.getAnnotation(Delete.class) != null) 
	    {
	        if (args == null || args.length == 0) 
	        {
	            throw new IllegalArgumentException("Delete method requires an object parameter");
	        }
	        delete(method, args[0]);
	    } 
		else if (method.getAnnotation(Save.class) != null) 
	    {
	        if (args == null || args.length == 0) 
	        {
	            throw new IllegalArgumentException("Save method requires an object parameter");
	        }
	        save(method, args[0]);
	    } 
		else if (method.getAnnotation(Select.class) != null) 
		{
	        return select(method, args);
	    }
	    return null;
	}
	
	
	// HELPER METHOD: when putting in field values into SQL, strings are in quotes otherwise they go in as is
	private String getValueAsSql(Object o) throws Exception
	{
		if (o.getClass()==String.class)
		{
			return "\""+o+"\"";
		}
		else
		{
			return String.valueOf(o);
		}		
	}
	
	
	// handles @CreateTable
	private void createTable(Method method)
	{
		
// 		SAMPLE SQL 		
//	    CREATE TABLE REGISTRATION (id INTEGER not NULL AUTO_INCREMENT,
//												first VARCHAR(255), 
//												last VARCHAR(255), age INTEGER, PRIMARY KEY ( id ))
		
// 		Using the @MappedClass annotation from method
		// get the required class 		
		// use reflection to check all the fields for @Column
		// use the @Column attributed to generate the required sql statment
		
		
		
// 		Run the sql
		// jdbc.runSQL(SQL STRING);
		
		// Extract the @MappedClass annotation from the method
	    MappedClass mappedClassAnnotation = method.getAnnotation(MappedClass.class);
	    if (mappedClassAnnotation == null) 
	    {
	        throw new IllegalArgumentException("Method does not have a @MappedClass annotation");
	    }

	    // Get the class specified in the @MappedClass annotation
	    Class<?> entityClass = mappedClassAnnotation.clazz();

	    // Retrieve the table name from the class or annotation (you can decide the priority)
	    String tableName = entityClass.getSimpleName(); // For example, assuming table name same as class name

	    // Start building the CREATE TABLE SQL statement
	    StringBuilder sqlBuilder = new StringBuilder("CREATE TABLE ");
	    sqlBuilder.append(tableName).append(" (");

	    // Use reflection to inspect the fields for @Column annotations
	    Field[] fields = entityClass.getDeclaredFields();
	    boolean firstColumn = true;
	    for (Field field : fields) 
	    {
	        Column columnAnnotation = field.getAnnotation(Column.class);
	        if (columnAnnotation != null) 
	        {
	            if (!firstColumn) 
	            {
	                sqlBuilder.append(", ");
	            }
	            sqlBuilder.append(columnAnnotation.name()).append(" ").append(columnAnnotation.sqlType());
	            if (columnAnnotation.id()) 
	            {
	                sqlBuilder.append(" PRIMARY KEY");
	            }
	            firstColumn = false;
	        }
	    }

	    // Finish the SQL statement
	    sqlBuilder.append(")");

	    // Execute the SQL statement using JDBC
	    String createTableSQL = sqlBuilder.toString();
	    jdbc.runSQL(createTableSQL);
	}
	
	// handles @Delete
	private void delete(Method method, Object o) throws Exception
	{
// 		SAMPLE SQL		
//  	DELETE FROM REGISTRATION WHERE ID=1
		
		
// 		Using the @MappedClass annotation from method
		// get the required class 		
		// use reflection to check all the fields for @Column
		// find which field is the primary key
		// for the Object o parameter, get the value of the field and use this as the primary value 
		// for the WHERE clause
				// if the primary key field value is null, throw a RuntimeException("no pk value")
		
		
		// run the sql
//		jdbc.runSQL(SQL STRING);
		
		// Extract the @MappedClass annotation from the method
	    MappedClass mappedClassAnnotation = method.getAnnotation(MappedClass.class);
	    if (mappedClassAnnotation == null) 
	    {
	        throw new IllegalArgumentException("Method does not have a @MappedClass annotation");
	    }

	    // Get the class specified in the @MappedClass annotation
	    Class<?> entityClass = mappedClassAnnotation.clazz();

	    // Get the fields of the class
	    Field[] fields = entityClass.getDeclaredFields();

	    // Find the primary key field
	    Field primaryKeyField = null;
	    for (Field field : fields) 
	    {
	        Column columnAnnotation = field.getAnnotation(Column.class);
	        if (columnAnnotation != null && columnAnnotation.id()) 
	        {
	            primaryKeyField = field;
	            break;
	        }
	    }

	    // Throw exception if primary key field is not found
	    if (primaryKeyField == null) 
	    {
	        throw new RuntimeException("No primary key field found");
	    }

	    // Get the value of the primary key field for the object
	    primaryKeyField.setAccessible(true); // Enable access to private fields
	    Object primaryKeyValue = primaryKeyField.get(o);

	    // Throw exception if primary key value is null
	    if (primaryKeyValue == null) 
	    {
	        throw new RuntimeException("Primary key value is null");
	    }

	    // Construct the DELETE SQL statement
	    StringBuilder sqlBuilder = new StringBuilder("DELETE FROM ");
	    sqlBuilder.append(entityClass.getSimpleName()).append(" WHERE ")
	              .append(primaryKeyField.getName()).append("=").append(primaryKeyValue);

	    // Execute the SQL statement using JDBC
	    String deleteSQL = sqlBuilder.toString();
	    jdbc.runSQL(deleteSQL);
	}
	
	// handles @Save
	private void save(Method method, Object o) throws Exception
	{
// 		Using the @MappedClass annotation from method
		// get the required class 		
		// use reflection to check all the fields for @Column
		// find which field is the primary key
		// for the Object o parameter, get the value of the field
			// if the field is null run the insert(Object o, Class entityClass, String tableName) method
			// if the field is not null run the update(Object o, Class entityClass, String tableName) method

		// Extract the @MappedClass annotation from the method
	    MappedClass mappedClassAnnotation = method.getDeclaringClass().getAnnotation(MappedClass.class);
	    
	    System.out.println("Method Class " + method.getClass());
	    System.out.println("Mapped Class Annotation " + mappedClassAnnotation);

	    // Get the class specified in the @MappedClass annotation
	    Class<?> entityClass = mappedClassAnnotation.clazz();

	    // Get the fields of the class
	    Field[] fields = entityClass.getDeclaredFields();

	    // Find the primary key field
	    Field primaryKeyField = null;
	    for (Field field : fields) 
	    {
	        Column columnAnnotation = field.getAnnotation(Column.class);
	        if (columnAnnotation != null && columnAnnotation.id()) 
	        {
	            primaryKeyField = field;
	            break;
	        }
	    }

	    // Get the value of the primary key field for the object
	    primaryKeyField.setAccessible(true); // Enable access to private fields
	    Object primaryKeyValue = primaryKeyField.get(o);

	    // Determine whether to insert or update based on the value of the primary key field
	    if (primaryKeyValue == null) 
	    {
	        // Primary key value is null, insert a new record
	        insert(o, entityClass, entityClass.getSimpleName());
	    } 
	    else 
	    {
	        // Primary key value is not null, update the existing record
	        update(o, entityClass, entityClass.getSimpleName());
	    }
	}

	private void insert(Object o, Class entityClass, String tableName) throws Exception 
	{
		
		
// 		SAMPLE SQL		
//		INSERT INTO table_name (column1, column2, column3, ...)
//		VALUES (value1, value2, value3, ...)	


//		HINT: columnX comes from the entityClass, valueX comes from o 
		
		
// 		run sql		
//		jdbc.runSQL(SQL STRING);
		
		// Get the fields of the class
	    Field[] fields = entityClass.getDeclaredFields();

	    // Construct the list of column names and values for the INSERT statement
	    StringJoiner columnNames = new StringJoiner(", ");
	    StringJoiner columnValues = new StringJoiner(", ");

	    for (Field field : fields) 
	    {
	    	field.setAccessible(true); // Enable access to private fields
	    	
	        System.out.println("field: " + field);
	        System.out.println("field name: " + field.getName());
	        System.out.println("field value: " + field.get(o));

	        // Get the value of the field from the object
	        Object value = field.get(o);

	        // Get the column name from the field's name
	        String columnName = field.getName();

	        // Add the column name and value to the respective joiners
	        columnNames.add(columnName);
	        if (columnName != "id")
	        	columnValues.add(getValueAsSql(value));
	    }

	    // Construct the INSERT SQL statement
	    String insertSQL = String.format("INSERT INTO %s (%s) VALUES (%s)",
	                                     tableName, columnNames.toString(), columnValues.toString());

	    // Execute the SQL statement using JDBC
	    jdbc.runSQL(insertSQL);
	}

	private void update(Object o, Class entityClass, String tableName) throws IllegalAccessException, Exception {

//		SAMPLE SQL		
//		UPDATE table_name
//		SET column1 = value1, column2 = value2, ...
//		WHERE condition;
		
//		HINT: columnX comes from the entityClass, valueX comes from o 		
		
//		run sql
//		jdbc.runSQL(SQL STRING);
		
		// Get the fields of the class
	    Field[] fields = entityClass.getDeclaredFields();

	    // Construct the list of column names and values for the INSERT statement
	    StringJoiner columnNames = new StringJoiner(", ");
	    StringJoiner columnValues = new StringJoiner(", ");

	    for (Field field : fields) 
	    {
	        field.setAccessible(true); // Enable access to private fields

	        // Get the value of the field from the object
	        Object value = field.get(o);

	        // Get the column name from the field's name
	        String columnName = field.getName();

	        // Add the column name and value to the respective joiners
	        columnNames.add(columnName);
	        columnValues.add(getValueAsSql(value));
	    }

	    // Construct the INSERT SQL statement
	    String insertSQL = String.format("INSERT INTO %s (%s) VALUES (%s)",
	                                     tableName, columnNames.toString(), columnValues.toString());

	    // Execute the SQL statement using JDBC
	    jdbc.runSQL(insertSQL);
	}

		
	// handles @Select
	private Object select(Method method, Object[] args) throws Exception
	{
		// same style as lab
		
// PART I		
// 		Using the @MappedClass annotation from method
//		get the required class
//		Use this class to extra all the column information (this is the replacement for @Results/@Result)		
//		generate the SELECT QUERY		

// PART II
		
//		this will pull actual values from the DB		
//		List<HashMap<String, Object>> results = jdbc.runSQLQuery(SQL QUERY);
		
		// Extract the @MappedClass annotation from the method
	    MappedClass mappedClassAnnotation = method.getAnnotation(MappedClass.class);
	    if (mappedClassAnnotation == null) 
	    {
	        throw new IllegalArgumentException("Method does not have a @MappedClass annotation");
	    }

	    // Get the class specified in the @MappedClass annotation
	    Class<?> entityClass = mappedClassAnnotation.clazz();

	    // Generate the SELECT query
	    String selectSQL = generateSelectSQL(entityClass);

	    // Execute the SELECT query using JDBC
	    List<HashMap<String, Object>> results = jdbc.runSQLQuery(selectSQL);


		
		// process list based on getReturnType
		if (method.getReturnType()==List.class)
		{
			List returnValue = new ArrayList();
			
			// create an instance for each entry in results based on mapped class
			// map the values to the corresponding fields in the object
			// DO NOT HARD CODE THE TYPE and FIELDS USE REFLECTION
			for (HashMap<String, Object> row : results) 
			{
	            Object instance = mapRowToInstance(row, entityClass);
	            returnValue.add(instance);
	        }
			return returnValue;
		}
		else
		{
			// if not a list return type
			
			// if the results.size() == 0 return null
			// if the results.size() >1 throw new RuntimeException("More than one object matches")
			// if the results.size() == 1
				// create one instance based on mapped class
				// map the values to the corresponding fields in the object
				// DO NOT HARD CODE THE TYPE and FIELDS USE REFLECTION
			if (results.size() == 0) 
			{
	            return null;
	        } 
			else if (results.size() > 1) 
	        {
	            throw new RuntimeException("More than one object matches the query");
	        } 
	        else 
	        {
	            return mapRowToInstance(results.get(0), entityClass);
	        }
		}
	}
	
	// Helper method to generate the SELECT query
	private String generateSelectSQL(Class entityClass) {
	    // Construct the SELECT query based on the table name
	    return "SELECT * FROM " + entityClass.getSimpleName();
	}

	// Helper method to map a row of data to an instance of the specified class
	private Object mapRowToInstance(HashMap<String, Object> row, Class entityClass) throws Exception {
	    Constructor<?> constructor = entityClass.getConstructor();
	    Object instance = constructor.newInstance();
	    Field[] fields = entityClass.getDeclaredFields();
	    for (Field field : fields) {
	        field.setAccessible(true);
	        Object value = row.get(field.getName());
	        field.set(instance, value);
	    }
	    return instance;
	}
	
}
