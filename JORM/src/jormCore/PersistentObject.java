package jormCore;

//import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jormCore.Annotaions.*;
import jormCore.Wrapping.FieldWrapper;
import jormCore.Wrapping.WrappingHandler;

public class PersistentObject {
	private ObjectSpace objectSpace;
	
	@PrimaryKey
	@Persistent(name = "ID")
	private UUID id;
	@Persistent(name = "CREATIONDATE")
	private Date creationDate;
	@Persistent(name = "LASTCHANGE")
	private Date lastChange;
	@Persistent(name = "DELETED")
	private boolean isDeleted;

	public PersistentObject(ObjectSpace os) {
		id = UUID.randomUUID();
		creationDate = new Date();
		lastChange = new Date();
		
		this.objectSpace = os;
		os.SaveObject(this);
	}

	public UUID getID() {
		return id;
	}

	public Map<FieldWrapper, Object> getPersistentPropertiesWithValues() {
		List<FieldWrapper> wrappedFields = WrappingHandler.getWrappingHandler().getClassWrapper(this.getClass()).getWrappedFields();
		Map<FieldWrapper, Object> mapping = new HashMap<FieldWrapper, Object>();

		for (FieldWrapper fw : wrappedFields) {
			try {
				mapping.put(fw, fw.getOriginalField().get(this));
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