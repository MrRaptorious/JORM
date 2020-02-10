package jormCore.Wrapping;

public class AssociationWrapper {

	private ClassWrapper associationPartnerClass;
	private FieldWrapper associationPartnerPrimaryKeyMember;
	private FieldWrapper associationPartner;
	private String associationName;

	public AssociationWrapper(ClassWrapper foreignType, String associationName) {
		this.associationPartnerClass = foreignType;
		associationPartnerPrimaryKeyMember = foreignType.getPrimaryKeyMember();

		// case there is a AssociationAnnotation
		if (associationName != null && !associationName.equals("")) {
			this.associationName = associationName;
			for (FieldWrapper fw : associationPartnerClass.getRelationWrapper()) {
				if (associationName.equals(fw.getForeigenKey().getAssociationName())) {
					associationPartner = fw;
					break;
				}
			}
		}
	}

	public boolean isAnonym() {
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
