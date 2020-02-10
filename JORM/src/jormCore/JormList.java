package jormCore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import jormCore.Wrapping.WrappingHandler;

public class JormList<T extends PersistentObject> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;
	ObjectSpace objectSpace;
	PersistentObject owner;
	String relationName;

	public JormList(ObjectSpace os, PersistentObject owner, String relationName)
	{
		this.objectSpace = os;
		this.owner = owner;
		this.relationName = relationName;
	}
	
	@Override
	public boolean add(T o)
	{
		try {
			WrappingHandler.getWrappingHandler().getClassWrapper(o.getClass()).getRelationWrapper(relationName).getOriginalField().set(o, owner);
			return super.add(o);
		
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean remove(Object o) {
		
		boolean removed =  super.remove(o);
		
		if(removed)
		{
			// never can not be a T
			@SuppressWarnings("unchecked")
			T castedObejct = (T)o;
			
			try {
				// set reference to null
				WrappingHandler.getWrappingHandler().getClassWrapper(castedObejct.getClass())
				.getRelationWrapper(relationName).getOriginalField().set(castedObejct, null);
				return true;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				// re-add in case of fail to ensure consistency
				super.add(castedObejct);
				
				return false;
			}
		}
		
		return false;
	}
	
	@Override
	public T remove(int index) {
		
		T removedObject =  super.remove(index);
		
		if(removedObject != null)
		{
			try {
				WrappingHandler.getWrappingHandler().getClassWrapper(removedObject.getClass())
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
	/** Do not use will only return false
	 * @deprecated this method is not supported yet  
	 */
	public boolean removeIf(Predicate<? super T> filter) {
		return false;
	}
	
	public void load()
	{
		
	}
}