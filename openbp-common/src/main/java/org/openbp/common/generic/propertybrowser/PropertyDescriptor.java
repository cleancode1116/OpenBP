/*
 *   Copyright 2007 skynamics AG
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.openbp.common.generic.propertybrowser;

import org.openbp.common.ReflectUtil;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.io.xml.XMLDriverException;

/**
 * Property description for the standard property browser.
 *
 * The 'name' member of the super class specifies the name of the property.
 * If no getter/setter methods are specified, the member name will be used to generate
 * the access method names (getName/isName and setName).
 *
 * The 'group' specification may be used to group properties in a tree table-like
 * representation.
 *
 * @author Heiko Erhardt
 */
public class PropertyDescriptor extends DisplayObjectImpl
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Group specification or null */
	private String group;

	/** Flag if the property is required */
	private boolean required;

	/** Flag if the property is read only */
	private boolean readOnly;

	/** Flag if the property is expanded, i\. e\. visible in the property tree by default */
	private boolean expanded;

	/** Validator class name */
	private String validatorClassName;

	/** Editor class name */
	private String editorClassName;

	/** Optional editor parameter string */
	private String editorParamString;

	/** Object descriptor describing a complex type property */
	private ObjectDescriptor complexProperty;

	/** Collection descriptor */
	private CollectionDescriptor collectionDescriptor;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Validator class */
	private Class validatorClass;

	/** Editor class */
	private Class editorClass;

	/** Optional editor parameters */
	private Object editorParam;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public PropertyDescriptor()
	{
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		PropertyDescriptor src = (PropertyDescriptor) source;

		group = src.group;
		required = src.required;
		readOnly = src.readOnly;
		expanded = src.expanded;
		validatorClassName = src.validatorClassName;
		editorClassName = src.editorClassName;
		editorParamString = src.editorParamString;
		validatorClass = src.validatorClass;
		editorClass = src.editorClass;
		editorParam = src.editorParam;

		complexProperty = src.complexProperty;

		if (src.collectionDescriptor != null)
		{
			collectionDescriptor = (CollectionDescriptor) src.collectionDescriptor.clone();
		}
		else
		{
			collectionDescriptor = null;
		}
	}

	/**
	 * Copies all attributes of a property descriptor that are not yet present in this descriptor.
	 *
	 * @param source Source descriptor
	 */
	protected void copyNonNull(PropertyDescriptor source)
	{
		if (getName() == null)
			setName(source.getName());
		if (getDisplayName() == null)
			setDisplayName(source.getDisplayName());
		if (getDescription() == null)
			setDescription(source.getDescription());

		if (source.required)
			required = true;
		if (source.readOnly)
			readOnly = true;
		if (source.expanded)
			expanded = true;

		if (group == null)
			group = source.group;
		if (validatorClassName == null)
			validatorClassName = source.validatorClassName;
		if (editorClassName == null)
			editorClassName = source.editorClassName;
		if (editorParamString == null)
			editorParamString = source.editorParamString;
		if (complexProperty == null)
			complexProperty = source.complexProperty;
		if (collectionDescriptor == null)
			collectionDescriptor = source.collectionDescriptor;
		if (validatorClass == null)
			validatorClass = source.validatorClass;
		if (editorClass == null)
			editorClass = source.editorClass;
		if (editorParam == null)
			editorParam = source.editorParam;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the group specification or null.
	 * @nowarn
	 */
	public String getGroup()
	{
		return group;
	}

	/**
	 * Sets the group specification or null.
	 * @nowarn
	 */
	public void setGroup(String group)
	{
		this.group = group;
	}

	/**
	 * Gets the flag if the property is required.
	 * @nowarn
	 */
	public boolean isRequired()
	{
		return required;
	}

	/**
	 * Determines if the flag if the property is required is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasRequired()
	{
		return required;
	}

	/**
	 * Sets the flag if the property is required.
	 * @nowarn
	 */
	public void setRequired(boolean required)
	{
		this.required = required;
	}

	/**
	 * Gets the flag if the property is read only.
	 * @nowarn
	 */
	public boolean isReadOnly()
	{
		return readOnly;
	}

	/**
	 * Determines if the flag if the property is read only is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasReadOnly()
	{
		return readOnly;
	}

	/**
	 * Sets the flag if the property is read only.
	 * @nowarn
	 */
	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	/**
	 * Gets the flag if the property is expanded, i\. e\. visible in the property tree by default.
	 * @nowarn
	 */
	public boolean isExpanded()
	{
		return expanded;
	}

	/**
	 * Sets the flag if the property is expanded, i\. e\. visible in the property tree by default.
	 * @nowarn
	 */
	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
	}

	/**
	 * Gets the validator class name.
	 * @return Specifies a class that implements the PropertyValidator interface
	 */
	public String getValidatorClassName()
	{
		return validatorClassName;
	}

	/**
	 * Sets the validator class name.
	 * @param validatorClassName Specifies a class that implements the PropertyValidator interface
	 */
	public void setValidatorClassName(String validatorClassName)
	{
		this.validatorClassName = validatorClassName;
	}

	/**
	 * Gets the editor class.
	 * @return Specifies a class that implements the PropertyEditor interface
	 */
	public String getEditorClassName()
	{
		return editorClassName;
	}

	/**
	 * Sets the editor class.
	 * @param editorClassName Specifies a class that implements the PropertyEditor interface
	 */
	public void setEditorClassName(String editorClassName)
	{
		this.editorClassName = editorClassName;
	}

	/**
	 * Gets the optional editor parameter string.
	 * @nowarn
	 */
	public String getEditorParamString()
	{
		return editorParamString;
	}

	/**
	 * Sets the optional editor parameter string.
	 * @nowarn
	 */
	public void setEditorParamString(String editorParamString)
	{
		this.editorParamString = editorParamString;
	}

	/**
	 * Gets the validator class.
	 * @return A class that implements the PropertyValidator interface
	 */
	public Class getValidatorClass()
	{
		return validatorClass;
	}

	/**
	 * Sets the validator class.
	 * @param validatorClass A class that implements the PropertyValidator interface
	 */
	public void setValidatorClass(Class validatorClass)
	{
		this.validatorClass = validatorClass;
	}

	/**
	 * Gets the editor class.
	 * @return A class that implements the PropertyEditor interface
	 */
	public Class getEditorClass()
	{
		return editorClass;
	}

	/**
	 * Sets the editor class.
	 * @param editorClass A class that implements the PropertyEditor interface
	 */
	public void setEditorClass(Class editorClass)
	{
		this.editorClass = editorClass;
	}

	/**
	 * Gets the optional editor parameters.
	 * @nowarn
	 */
	public Object getEditorParam()
	{
		return editorParam;
	}

	/**
	 * Sets the optional editor parameters.
	 * @nowarn
	 */
	public void setEditorParam(Object editorParam)
	{
		this.editorParam = editorParam;
	}

	/**
	 * Gets the type of the complex type property value.
	 * @nowarn
	 */
	public String getComplexPropertyType()
	{
		if (complexProperty != null)
			return complexProperty.getObjectClassName();
		return null;
	}

	/**
	 * Sets the type of the complex type property value.
	 * @nowarn
	 */
	public void setComplexPropertyType(String complexPropertyType)
	{
		complexProperty = null;
		if (complexPropertyType != null)
		{
			Class cls = ReflectUtil.loadClass(complexPropertyType);
			try
			{
				complexProperty = ObjectDescriptorMgr.getInstance().getDescriptor(cls, ObjectDescriptorMgr.ODM_THROW_ERROR);
			}
			catch (XMLDriverException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets the object descriptor describing a complex type property.
	 * @nowarn
	 */
	public ObjectDescriptor getComplexProperty()
	{
		return complexProperty;
	}

	/**
	 * Sets the object descriptor describing a complex type property.
	 * @nowarn
	 */
	public void setComplexProperty(ObjectDescriptor complexProperty)
	{
		this.complexProperty = complexProperty;
	}

	/**
	 * Gets the collection descriptor.
	 * @nowarn
	 */
	public CollectionDescriptor getCollectionDescriptor()
	{
		return collectionDescriptor;
	}

	/**
	 * Sets the collection descriptor.
	 * @nowarn
	 */
	public void setCollectionDescriptor(CollectionDescriptor collectionDescriptor)
	{
		this.collectionDescriptor = collectionDescriptor;
	}
}
