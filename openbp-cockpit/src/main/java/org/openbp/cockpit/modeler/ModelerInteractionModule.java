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
package org.openbp.cockpit.modeler;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;

import org.openbp.cockpit.modeler.custommodelobjectconfigurator.CustomModelObjectConfiguratorSupport;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.generic.Colorizable;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.figures.process.ParamConnection;
import org.openbp.cockpit.modeler.figures.process.ParamFigure;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.cockpit.modeler.paramvaluewizard.ParamValueWizard;
import org.openbp.cockpit.modeler.paramvaluewizard.ParamVisibilityHelper;
import org.openbp.cockpit.modeler.util.ModelerFlavors;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.clipboard.ClipboardMgr;
import org.openbp.jaspira.plugin.ExternalInteractionModule;
import org.openbp.jaspira.plugin.Plugin;

import CH.ifa.draw.framework.Figure;

/**
 * Event module of the {@link Modeler} class.
 *
 * @author Heiko Erhardt
 */
public class ModelerInteractionModule extends ExternalInteractionModule
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Modeler we are associated with */
	private Modeler modeler;

	/** Configurator support helper */
	private CustomModelObjectConfiguratorSupport configuratorSupport;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param modeler Modeler we are associated with
	 */
	public ModelerInteractionModule(Plugin modeler)
	{
		super(modeler);

		this.modeler = (Modeler) modeler;
		configuratorSupport = new CustomModelObjectConfiguratorSupport((Modeler) modeler);
	}

	/**
	 * Gets the module priority.
	 * We are high priority.
	 *
	 * @return The priority. 0 is lowest, 100 is highest.
	 */
	public int getPriority()
	{
		return 5;
	}

	/**
	 * Standard event handler that is called when a popup menu is to be shown.
	 * Adds the popup menu entries for relations of the popup initiator and the current selection.
	 * Also adds the popup menu entries for various modeler figures.
	 *
	 * @event global.interaction.popup
	 * @param ie Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode popup(InteractionEvent ie)
	{
		if (!modeler.getPluginComponent().isShowing())
		{
			return EVENT_IGNORED;
		}

		final WorkspaceDrawingView workspaceView = modeler.getDrawingView();

		if (ie.getSourcePlugin() == modeler)
		{
			DataFlavor [] flavor = ie.getTransferDataFlavors();

			JaspiraAction group = new JaspiraAction("popup", null, null, null, null, 100, JaspiraAction.TYPE_GROUP);

			for (int i = 0; i < flavor.length; i++)
			{
				if (flavor [i].equals(ClientFlavors.MODEL_OBJECT))
				{
					ModelObject mo = (ModelObject) ie.getSafeTransferData(flavor [i]);
					configuratorSupport.addConfiguratorMenuOptions(mo, group);
				}

				if (flavor [i].equals(ModelerFlavors.FIGURE))
				{
					Figure figure = (Figure) ie.getSafeTransferData(flavor [i]);
					workspaceView.singleSelect(figure);
				}

				if (flavor [i].equals(ModelerFlavors.PARAM_FIGURE))
				{
					final ParamFigure paramFigure = (ParamFigure) ie.getSafeTransferData(flavor [i]);
					final NodeParam nodeParam = paramFigure.getNodeParam();
					SocketFigure socketFigure = (SocketFigure) paramFigure.getParent();

					// Add the 'Parameter visibility' sub menu for all parameters of the socket if appropriate
					ParamVisibilityHelper.addParamMenu(ie, modeler, socketFigure);

					// 'Parameter Value Wizard' popup
					final NodeFigure nodeFigure = (NodeFigure) socketFigure.getParent();
					if (ParamValueWizard.isParameterValueWizardApplyable(modeler, nodeFigure, nodeParam.getSocket().getName()))
					{
						JaspiraAction wizardGroup = new JaspiraAction("popupwizard", null, null, null, null, 2, JaspiraAction.TYPE_GROUP);

						wizardGroup.addMenuChild(new JaspiraAction(modeler, "modeler.edit.paramvaluewizard")
						{
							public void actionPerformed(ActionEvent e)
							{
								// Display parameter value wizard if appropriate
								ParamValueWizard.displayParameterValueWizard(modeler, nodeFigure, nodeParam.getSocket().getName(), nodeParam.getName());
							}
						});

						ie.add(wizardGroup);
					}
				}

				if (flavor [i].equals(ModelerFlavors.SOCKET_FIGURE))
				{
					final SocketFigure socketFigure = (SocketFigure) ie.getSafeTransferData(flavor [i]);
					final NodeSocket socket = socketFigure.getNodeSocket();

					// Add the 'Parameter visibility' sub menu if appropriate
					ParamVisibilityHelper.addParamMenu(ie, modeler, socketFigure);

					// 'Parameter Value Wizard' popup
					final NodeFigure nodeFigure = (NodeFigure) socketFigure.getParent();
					if (ParamValueWizard.isParameterValueWizardApplyable(modeler, nodeFigure, socket.getName()))
					{
						JaspiraAction wizardGroup = new JaspiraAction("popupwizard", null, null, null, null, 2, JaspiraAction.TYPE_GROUP);

						wizardGroup.addMenuChild(new JaspiraAction(modeler, "modeler.edit.paramvaluewizard")
						{
							public void actionPerformed(ActionEvent e)
							{
								// Display parameter value wizard if appropriate
								ParamValueWizard.displayParameterValueWizard(modeler, nodeFigure, socket.getName(), null);
							}
						});

						ie.add(wizardGroup);
					}

					/* Currently unused, rathr leads to confusion
					JaspiraAction socketToNodeGroup = new JaspiraAction("popupsockettonode", null, null, null, null, 2, JaspiraAction.TYPE_GROUP);

					// 'Create initial node' popup
					socketToNodeGroup.addMenuChild(new JaspiraAction(modeler, "modeler.edit.createentryfromsocket")
					{
						public void actionPerformed(ActionEvent e)
						{
							modeler.startUndo("Create Initial Node");

							if (drawing.getProcess().getNodeByName(socket.getName()) != null)
							{
								// A node with this name already exists; issue a warning.
								// The createFinalNodeFromSocket method will ensure that the new node
								// has a unique name.
								String msg = modeler.getPluginResourceCollection().getRequiredString("messages.nodealreadyexists");
								JMsgBox.show(null, msg, JMsgBox.ICON_INFO);
							}

							// Create a node from the socket and add it to the process
							NodeFigure nodeFigure = ModelerUtil.createInitialNodeFromSocket(socketFigure);

							modeler.endUndo();
						}
					});

					if (socket.isExitSocket())
					{
						// 'Create final node' popup
						socketToNodeGroup.addMenuChild(new JaspiraAction(modeler, "modeler.edit.createexitfromsocket")
						{
							public void actionPerformed(ActionEvent e)
							{
								modeler.startUndo("Create Final Node");

								if (drawing.getProcess().getNodeByName(socket.getName()) != null)
								{
									// A node with this name already exists; issue a warning.
									// The createFinalNodeFromSocket method will ensure that the new node
									// has a unique name.
									String msg = modeler.getPluginResourceCollection().getRequiredString("messages.nodealreadyexists");
									JMsgBox.show(null, msg, JMsgBox.ICON_INFO);
								}

								// Create a node from the socket and add it to the process
								NodeFigure nodeFigure = ModelerUtil.createFinalNodeFromSocket(socketFigure);

								modeler.endUndo();
							}
						});
					}

					ie.add(socketToNodeGroup);
					 */
				}

				if (flavor [i].equals(ModelerFlavors.NODE_FIGURE))
				{
					final NodeFigure nodeFigure = (NodeFigure) ie.getSafeTransferData(flavor [i]);

					group.addMenuChild(new JaspiraAction(modeler, "modeler.edit.mirrororientation")
					{
						public void actionPerformed(ActionEvent e)
						{
							modeler.startUndo("Flip Orientation");

							nodeFigure.flipOrientation();
							workspaceView.checkDamage();

							modeler.endUndo();
						}
					});

					group.addMenuChild(new JaspiraAction(modeler, "modeler.edit.rotateorientationcw")
					{
						public void actionPerformed(ActionEvent e)
						{
							modeler.startUndo("Rotate Orientation");

							nodeFigure.changeOrientation(NodeFigure.ROTATE_CW);
							workspaceView.checkDamage();

							modeler.endUndo();
						}
					});

					group.addMenuChild(new JaspiraAction(modeler, "modeler.edit.rotateorientationccw")
					{
						public void actionPerformed(ActionEvent e)
						{
							modeler.startUndo("Rotate Orientation");

							nodeFigure.changeOrientation(NodeFigure.ROTATE_CCW);
							workspaceView.checkDamage();

							modeler.endUndo();
						}
					});
				}

				if (flavor [i].equals(ModelerFlavors.COLORIZABLE))
				{
					final Colorizable col = (Colorizable) ie.getSafeTransferData(flavor [i]);

					// 'Reset color' popup
					if (col.getFillColor() != null && !col.getFillColor().equals(col.getDefaultFillColor()))
					{
						group.addMenuChild(new JaspiraAction(modeler, "modeler.edit.resetcolor")
						{
							public void actionPerformed(ActionEvent e)
							{
								modeler.startUndo("Reset Color");

								// TODO Feature 6: Reset subordinate element color, too
								col.setFillColor(col.getDefaultFillColor());
								workspaceView.checkDamage();

								modeler.endUndo();
							}
						});
					}
				}

				if (flavor [i].equals(ModelerFlavors.PARAM_CONNECTION_FIGURE))
				{
					final ParamConnection connection = (ParamConnection) ie.getSafeTransferData(flavor [i]);

					// 'Lock/unlock orientation' popup
					JaspiraAction lockAction = new JaspiraAction(modeler, "modeler.edit.lockorientation")
					{
						public void actionPerformed(ActionEvent e)
						{
							modeler.startUndo("Lock Orientation");

							connection.toggleOrientationLock();

							modeler.endUndo();
						}
					};
					lockAction.setSelected(connection.isOrientationLocked());
					group.addMenuChild(lockAction);

					// 'Flip orientation' popup
					group.addMenuChild(new JaspiraAction(modeler, "modeler.edit.fliporientation")
					{
						public void actionPerformed(ActionEvent e)
						{
							modeler.startUndo("Flip Orientation");

							connection.flipOrientation();

							modeler.endUndo();
						}
					});
				}
			}

			ie.add(group);

			boolean copyEnabled = modeler.canCopy();
			boolean cutEnabled = modeler.canCut();
			boolean deleteEnabled = modeler.canDelete();
			boolean pasteEnabled = modeler.canPaste(ClipboardMgr.getInstance().getCurrentEntry());
			if (copyEnabled || deleteEnabled || cutEnabled || pasteEnabled)
			{
				JaspiraAction copyPasteGroup = new JaspiraAction("copypaste", null, null, null, null, 2, JaspiraAction.TYPE_GROUP);

				JaspiraAction ja;

				ja = new JaspiraAction(modeler, "modeler.edit.copy")
				{
					public void actionPerformed(ActionEvent e)
					{
						// Forward to the clipboard plugin
						modeler.fireEvent(new JaspiraActionEvent(modeler, "global.clipboard.copy", Plugin.LEVEL_APPLICATION));
					}
				};
				ja.setEnabled(copyEnabled);
				copyPasteGroup.addMenuChild(ja);

				ja = new JaspiraAction(modeler, "modeler.edit.cut")
				{
					public void actionPerformed(ActionEvent e)
					{
						// Forward to the clipboard plugin
						modeler.fireEvent(new JaspiraActionEvent(modeler, "global.clipboard.cut", Plugin.LEVEL_APPLICATION));
					}
				};
				ja.setEnabled(cutEnabled);
				copyPasteGroup.addMenuChild(ja);

				ja = new JaspiraAction(modeler, "modeler.edit.paste")
				{
					public void actionPerformed(ActionEvent e)
					{
						// Forward to the clipboard plugin
						modeler.fireEvent(new JaspiraActionEvent(modeler, "global.clipboard.paste", Plugin.LEVEL_APPLICATION));
					}
				};
				ja.setEnabled(pasteEnabled);
				copyPasteGroup.addMenuChild(ja);

				ie.add(copyPasteGroup);

				ja = new JaspiraAction(modeler, "modeler.edit.delete")
				{
					public void actionPerformed(ActionEvent e)
					{
						// Forward to the clipboard plugin
						modeler.fireEvent(new JaspiraActionEvent(modeler, "global.clipboard.delete", Plugin.LEVEL_APPLICATION));
					}
				};
				ja.setEnabled(deleteEnabled);
				ie.add(ja);
			}
		}

		return EVENT_HANDLED;
	}
}
