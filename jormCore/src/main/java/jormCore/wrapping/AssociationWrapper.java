package jormCore.wrapping;

public class AssociationWrapper {

	private final ClassWrapper associationPartnerClass;
	private final FieldWrapper associationPartnerPrimaryKeyMember;
	private FieldWrapper associationPartner;
	private String associationName;

	public AssociationWrapper(ClassWrapper foreignType, String associationName) {
		this.associationPartnerClass = foreignType;
		associationPartnerPrimaryKeyMember = foreignType.getPrimaryKeyMember();

		// case there is a AssociationAnnotation
		if (associationName != null && !associationName.equals("")) {
			this.associationName = associationName;
			associationPartner = associationPartnerClass.getWrappedAssociation(associationName);
		}
	}

	public boolean isAnonymous() {
		return associationPartner == null;
	}

	public FieldWrapper getAssociationPartner() {
		return associationPartner;
	}

	public String getAssociationName() {
		return associationName;
	}

	public ClassWrapper getReferencingType() {
		return associationPartnerClass;
	}

	

	public String getReferencingPrimaryKeyName() {
		return associationPartnerPrimaryKeyMember.getName();
	}
}
