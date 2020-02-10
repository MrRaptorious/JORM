package jormCore;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jormCore.Annotaions.*;
import jormCore.Wrapping.AssociationWrapper;
import jormCore.Wrapping.FieldWrapper;
import jormCore.Wrapping.WrappingHandler;

/**
 * Represents the base class for all objects in the database
 */
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
		if (!os.isLoadingObjects()) {
			id = UUID.randomUUID();
			creationDate = new Date();
			lastChange = new Date();
			os.addCreatedObject(this);
		}
		this.objectSpace = os;

	}

	public UUID getID() {
		return id;
	}

	public Date getLastChange() {
		return lastChange;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Gets all fields with values from calling object
	 * 
	 * @return Map of all fields and values
	 */
	public Map<FieldWrapper, Object> getPersistentPropertiesWithValues() {
		List<FieldWrapper> wrappedFields = WrappingHandler.getWrappingHandler().getClassWrapper(this.getClass())
				.getWrappedFields();
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

	/**
	 * Gets the value of a member with the given name
	 * 
	 * @param memberName the name of the field from which to get the value
	 * @return the value of the given member
	 */
	public Object getMemberValue(String memberName) {
		try {
			Field f = this.getClass().getDeclaredField(memberName);
			f.setAccessible(true);
			return f.get(this);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {

			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets the value of a member with the given name (WILL NOT RESULT IN DB-UPDATE)
	 * 
	 * @param memberName the name of the field to which to set the value to
	 * @param value      the value to set
	 * @return the value of the given member
	 */
	public boolean setMemberValue(String memberName, Object value) {
		try {

			WrappingHandler.getWrappingHandler().getClassWrapper(this.getClass()).getFieldWrapper(memberName)
					.getOriginalField().set(this, value);
			return true;
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Sets the value of a member AND marks change to update in database
	 * 
	 * @param changedMember the name of the changed member
	 * @param newValue      the new value of the member
	 */
	protected void setPropertyValue(String changedMember, Object newValue) {
		objectSpace.addChangedObject(this, changedMember, newValue);
		this.setMemberValue(changedMember, newValue);
	}

	protected void setRelation(String memberName, PersistentObject value) {
		setMemberValue(memberName, value);

		AssociationWrapper aw = WrappingHandler.getWrappingHandler().getClassWrapper(this.getClass())
				.getFieldWrapper(memberName).getForeigenKey();

		if(aw != null && aw.getAssociationPartner() != null && aw.getAssociationPartner() != null)
		{
			value.setMemberValue(aw.getAssociationPartner().getOriginalField().getName(),this);
		}
	}
}