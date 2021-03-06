package jormCore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import jormCore.criteria.ComparisonOperator;
import jormCore.criteria.WhereClause;
import jormCore.wrapping.FieldWrapper;

public class JormList<T extends PersistentObject> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;
	ObjectSpace objectSpace;
	PersistentObject owner;
	String relationName;
	FieldWrapper listMember;

	public JormList(ObjectSpace os, PersistentObject owner, String relationName) {
		this.objectSpace = os;
		this.owner = owner;
		this.relationName = relationName;
		listMember = objectSpace.getWrappingHandler().getClassWrapper(owner.getClass())
				.getWrappedAssociation(relationName);

		load();
	}

	/**
	 * Adds the PersistentObject o to the list
	 * @param o object of type T to add
	 * @return if object is added
	 */
	@Override
	public boolean add(T o) {
		String fieldName = objectSpace.getWrappingHandler().getClassWrapper(o.getClass())
				.getWrappedAssociation(relationName).getOriginalField().getName();
		o.setPropertyValue(fieldName, owner);
		return super.add(o);
	}

	@Override
	public boolean remove(Object o) {

		boolean removed = super.remove(o);

		if (removed) {
			// never can not be a T
			@SuppressWarnings("unchecked")
			T castedObject = (T) o;

			try {
				// set reference to null
				String fieldName = objectSpace.getWrappingHandler().getClassWrapper(castedObject.getClass())
						.getWrappedAssociation(relationName).getOriginalField().getName();
				castedObject.setPropertyValue(fieldName, null);

				return true;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				// re-add in case of fail to ensure consistency
				super.add(castedObject);

				return false;
			}
		}

		return false;
	}

	@Override
	public T remove(int index) {

		T removedObject = super.remove(index);

		if (removedObject != null) {
			try {
				objectSpace.getWrappingHandler().getClassWrapper(removedObject.getClass())
						.getRelationWrapper(relationName).getOriginalField().set(removedObject, null);
				return removedObject;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				// re-add in case of fail to ensure consistency
				super.add(removedObject);

				return null;
			}
		}

		return null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean allRemoved = true;

		for (Object object : c) {
			allRemoved = remove(object) && allRemoved;
		}

		return allRemoved;
	}

	@Override
	@Deprecated
	/**
	 * Do not use will only return false
	 * 
	 * @deprecated this method is not supported yet
	 */
	public boolean removeIf(Predicate<? super T> filter) {
		return false;
	}

	public void load() {

		var lm = listMember;
		var fk = lm.getForeignKey();
		var rt = fk.getReferencingType();

		Class<T> partnerClass = (Class<T>) listMember.getForeignKey().getReferencingType().getClassToWrap();
		WhereClause clause = new WhereClause(listMember.getForeignKey().getAssociationPartner().getName(),
				owner.getID(), ComparisonOperator.Equal);

		for (T obj : objectSpace.getObjects(partnerClass, clause)) {
			add(obj);
		}
	}
}