package jormCore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jormCore.Annotaions.*;

public class PersistentObject {
	@PrimaryKey
	@Persistent
	private int id;
	@Persistent
	private Date creationDate;
	@Persistent
	private Date LastChange;

	public int getID() {
		return id;
	}

	public List<Field> getPersistentProperties() {
		return getPersistentProperties(this.getClass());
	}
	
	public static List<Field> getPersistentProperties(Class<?> persistentClass) {
		List<Field> members = new ArrayList<Field>();
		
		while (persistentClass != null) {
			for (Field field : persistentClass.getDeclaredFields()) {
				if (persistentClass.isAnnotationPresent(jormCore.Annotaions.Persistent.class)
						|| field.isAnnotationPresent(jormCore.Annotaions.Persistent.class)) {
					field.setAccessible(true);
					members.add(field);
				}
			}

			persistentClass = persistentClass.getSuperclass();
		}

		return members;
	}
	
	public static Field getPrimaryKey(Class<?> persistentClass) {
		while (persistentClass != null) {
			for (Field field : persistentClass.getDeclaredFields()) {
				if ((persistentClass.isAnnotationPresent(jormCore.Annotaions.Persistent.class)
						|| field.isAnnotationPresent(jormCore.Annotaions.Persistent.class))
						&& field.isAnnotationPresent(jormCore.Annotaions.PrimaryKey.class)) {
					field.setAccessible(true);
					return field;
				}
			}

			persistentClass = persistentClass.getSuperclass();
		}

		return null;
	}

	public Map<Field, Object> getPersistentPropertiesWithValues() {
		List<Field> fields = getPersistentProperties();
		Map<Field, Object> mapping = new HashMap<Field, Object>();

		for (Field f : fields) {
			try {
				mapping.put(f, f.get(this));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return mapping;
	}

	public Object getMemberValue(String memberName) {
		try {
			return this.getClass().getDeclaredField(memberName).get(this);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			return null;
		}
	}

	public boolean setMemberValue(String memberName, Object value) {
		try {
			this.getClass().getDeclaredField(memberName).set(this, value);
			return true;
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			return false;
		}
	}
}