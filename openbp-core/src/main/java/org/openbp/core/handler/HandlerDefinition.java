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
package org.openbp.core.handler;

import java.util.List;

import org.openbp.common.ReflectException;
import org.openbp.common.ReflectUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.msgcontainer.StandardMsgContainer;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.string.StringUtil;
import org.openbp.common.string.TextUtil;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.MimeTypes;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.AssociationUtil;
import org.openbp.core.model.ModelLocationUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.ConfigurationBean;

/**
 * Definition of an event handler.
 *
 * @author Heiko Erhardt
 */
public class HandlerDefinition
	implements Cloneable, Copyable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Name of the handler class */
	private String handlerClassName;

	/** Name of the class containing the configuration for this handler */
	private String configurationClassName;

	/** Bean shell script to execute */
	private String script;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Owner of this handler */
	private ModelObject owner;

	/** Global instance of the handler class */
	private transient Object singleInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public HandlerDefinition()
	{
	}

	/**
	 * Creates a clone of this object.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained members failed
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		HandlerDefinition clone = (HandlerDefinition) super.clone();

		// Perform a deep copy
		clone.copyFrom(this, Copyable.COPY_DEEP);

		return clone;
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(final Object source, final int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;

		HandlerDefinition src = (HandlerDefinition) source;

		handlerClassName = src.handlerClassName;
		configurationClassName = src.configurationClassName;
		script = src.script;
		owner = src.owner;
		singleInstance = src.singleInstance;
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "handlerClassName");
	}

	/**
	 * Creates a new configuration bean of the type specified by the {@link #setConfigurationClassName} property.
	 *
	 * @return The new bean or null if no setting class name was given
	 */
	public ConfigurationBean createConfigurationBean()
	{
		if (configurationClassName == null)
			return null;

		ConfigurationBean configurationbean = null;
		try
		{
			ClassLoader cl = owner.getOwningModel().getClassLoader();
			configurationbean = (ConfigurationBean) ReflectUtil
				.instantiate(configurationClassName, cl, ConfigurationBean.class, "configuration bean");
		}
		catch (ReflectException e)
		{
			LogUtil.error(getClass(), "The configuration bean class of handler of $0 could not be instantiated.", owner.getQualifier(), e);
		}

		return configurationbean;
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * Instantiates the handler class if specified.
	 *
	 * Any errors will be logged to the message container of the model manager that loaded the object.
	 */
	public void instantiate()
	{
		singleInstance = null;

		// Create an instance in order to check if the class is available
		Object instance = createInstance();

		// Keep the instance if it is thread safe
		if (instance instanceof ThreadSafeHandler)
		{
			singleInstance = instance;
		}
	}

	/**
	 * Gets the instance of the handler class.
	 * @nowarn
	 */
	public Object obtainHandlerInstance()
	{
		if (singleInstance != null)
			return singleInstance;

		StandardMsgContainer msgContainer = owner.getModelMgr().getMsgContainer();
		msgContainer.clearMsgs();

		Object handler = createInstance();

		if (! msgContainer.isEmpty())
		{
			String msg = msgContainer.toString();
			LogUtil.error(getClass(), msg);
			throw new OpenBPException("HandlerExecutionFailed", msg);
		}

		return handler;
	}

	/**
	 * Instantiates the handler class if specified.
	 *
	 * Any errors will be logged to the message container of the model manager that loaded the object.
	 */
	private Object createInstance()
	{
		Object instance = null;
		if (handlerClassName != null)
		{
			try
			{
				// Use the model's class loader to load it
				ClassLoader cl = owner.getOwningModel().getClassLoader();
				instance = ReflectUtil.instantiate(handlerClassName, cl, null, "handler implementation");
			}
			catch (ReflectException e)
			{
				owner.getModelMgr().getMsgContainer().addMsg(owner, "Error instantiating handler $0.", new Object[]
				{
					owner.getQualifier(), e
				});
			}
			catch (Exception e)
			{
				// Catch any exceptions, so the server won't be confused by errors in handers
				owner.getModelMgr().getMsgContainer().addMsg(owner, "The handler class $0 could not be initialized.", new Object[]
				{
					handlerClassName, e
				});
			}
			catch (NoClassDefFoundError e)
			{
				// Catch any exceptions, so the server won't be confused by errors in handers
				owner.getModelMgr().getMsgContainer().addMsg(owner, "Error instantiating handler $0: No class definition found", new Object[]
				{
					owner.getQualifier(), e
				});
			}
		}
		return instance;
	}

	//////////////////////////////////////////////////
	// @@ Associations
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.getAssociations
	 * @nowarn
	 */
	public List addHandlerAssociations(List associations, String handlerTitle, int level)
	{
		String javaSource = null;

		if (handlerClassName != null)
		{
			javaSource = ModelLocationUtil.expandModelLocation(owner.getOwningModel(), ModelLocationUtil.DIR_SRC) + StringUtil.FOLDER_SEP
				+ handlerClassName.replace('.', StringUtil.FOLDER_SEP_CHAR) + ".java";
		}

		associations = AssociationUtil.addAssociation(associations, - 1, handlerTitle, javaSource, owner, new String[]
		{
			MimeTypes.JAVA_SOURCE_FILE, MimeTypes.SOURCE_FILE, MimeTypes.TEXT_FILE,
		}, level, "No handler class name has been specified.");

		return associations;
	}

	//////////////////////////////////////////////////
	// @@ Execution
	//////////////////////////////////////////////////

	/**
	 * Determines if this event type should be handled by this handler.
	 * @nowarn
	 */
	public boolean isDefined()
	{
		return handlerClassName != null || script != null;
	}

	/**
	 * Determines if a handler is defined.
	 *
	 * @param eventType Event type
	 * @nowarn
	 */
	public boolean canHandle(final String eventType)
	{
		return isDefined();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the handler class.
	 * The class must implement the org.openbp.server.handler.Handler interface.
	 * @nowarn
	 */
	public String getHandlerClassName()
	{
		return handlerClassName;
	}

	/**
	 * Sets the name of the handler class.
	 * The class must implement the org.openbp.server.handler.Handler interface.
	 * @nowarn
	 */
	public void setHandlerClassName(final String handlerClassName)
	{
		this.handlerClassName = handlerClassName;
	}

	/**
	 * Gets the name of the class containing the configuration for this handler.
	 * @nowarn
	 */
	public String getConfigurationClassName()
	{
		return configurationClassName;
	}

	/**
	 * Sets the name of the class containing the configuration for this handler.
	 * @nowarn
	 */
	public void setConfigurationClassName(final String configurationClassName)
	{
		this.configurationClassName = configurationClassName;
	}

	/**
	 * Gets the bean shell script to execute.
	 * @nowarn
	 */
	public String getScript()
	{
		return script;
	}

	/**
	 * Sets the bean shell script to execute.
	 * @nowarn
	 */
	public void setScript(final String script)
	{
		this.script = script;
	}

	/**
	 * Gets the escape representation of the bean shell script to execute.
	 * In the escape representation, a newline escape ("\n") has been added before any newline character.
	 * Tab characters are replaced by the tab escape ("\t").
	 * (we need this in order to remove spaces inserted by Castor when serializing
	 * multi-line text content)
	 * @nowarn
	 */
	public String getScriptEscape()
	{
		return TextUtil.encodeMultiLineString(script);
	}

	/**
	 * Sets the escape representation of the bean shell script to execute.
	 * In the escape representation, a newline escape ("\n") has been added before any newline character.
	 * Tab characters are replaced by the tab escape ("\t").
	 * (we need this in order to remove spaces inserted by Castor when serializing
	 * multi-line text content)
	 * @nowarn
	 */
	public void setScriptEscape(final String script)
	{
		this.script = TextUtil.decodeMultiLineString(script);
	}

	/**
	 * Gets the owner of this handler.
	 * @nowarn
	 */
	public ModelObject getOwner()
	{
		return owner;
	}

	/**
	 * Sets the owner of this handler.
	 * @nowarn
	 */
	public void setOwner(final ModelObject owner)
	{
		this.owner = owner;
	}
}
