/*
 * Java Parallel Processing Framework.
 * Copyright (C) 2005-2006 Laurent Cohen.
 * lcohen@osp-chicago.com
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jppf.ui.options;

import static org.jppf.ui.utils.GuiUtils.addLayoutComp;
import java.awt.*;
import java.text.DecimalFormat;
import javax.swing.*;
import org.jppf.ui.utils.GuiUtils;

/**
 * Option class for numbers formatted using a pattern, as defined in class
 * {@link java.text.DecimalFormat}.
 * @author Laurent Cohen
 */
public class FormattedNumberOption extends AbstractOption
{
	/**
	 * The field used to edit the value.
	 */
	private JFormattedTextField field = null;
	/**
	 * Used to format the value according to the pattern specified in the constructor.
	 */
	private DecimalFormat format = null;
	/**
	 * Label associated with the formatted field.
	 */
	protected JLabel fieldLabel = null;
	/**
	 * The pattern that defines the field format.
	 */
	protected String pattern = null;

	/**
	 * Constructor provided as a convenience to facilitate the creation of
	 * option elements through reflexion.
	 */
	public FormattedNumberOption()
	{
	}

	/**
	 * Initialize this text option with the specified parameters.
	 * @param name this component's name.
	 * @param label the label displayed with the text field.
	 * @param tooltip the tooltip associated with the text field.
	 * @param value the initial value of this component.
	 * @param pattern the pattern defining the format of the number used as value.
	 * @see java.text.DecimalFormat
	 */
	public FormattedNumberOption(String name, String label, String tooltip, Number value, String pattern)
	{
		this.name = name;
		this.label = label;
		this.toolTipText = tooltip;
		this.value = value;
		format = new DecimalFormat(pattern);
		createUI();
	}

	/**
	 * Create the UI components for this option.
	 */
	public void createUI()
	{
		fieldLabel = new JLabel(label);
		field = createField();
		JPanel panel = new JPanel();
		GridBagLayout g = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 0;
		c.insets = new Insets(5, 5, 5, 5);
		panel.setLayout(g);
		if (toolTipText != null)
		{
			field.setToolTipText(toolTipText);
			fieldLabel.setToolTipText(toolTipText);
		}
		c.anchor = GridBagConstraints.LINE_START;
		addLayoutComp(panel, g, c, fieldLabel);
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 1.0;
		JComponent filler = GuiUtils.createFiller(1, 1);
		addLayoutComp(panel, g, c, filler);
		c.anchor = GridBagConstraints.LINE_END;
		addLayoutComp(panel, g, c, field);
		UIComponent = panel;
		setupValueChangeNotifications();
	}

	/**
	 * Create the text field that holds the value of this password option.
	 * @return a <code>JPasswordField</code> instance.
	 * @see org.jppf.ui.options.TextOption#createField()
	 */
	protected JFormattedTextField createField()
	{
		field = new JFormattedTextField(format);
		field.setValue(value);
		field.setPreferredSize(new Dimension(60, 20));
		field.setHorizontalAlignment(JTextField.RIGHT);
		return field;
	}

	/**
	 * Get the value of the edited number.
	 * @return the value of the formatted text field as a <code>Number</code>.
	 * @see org.jppf.ui.options.TextOption#getValue()
	 */
	public Object getValue()
	{
		JFormattedTextField tmp = (JFormattedTextField) field;
		return tmp.getValue();
	}

	/**
	 * Add a listener to the underlying text document, to receive and propagate change events.
	 * @see org.jppf.ui.options.AbstractOption#setupValueChangeNotifications()
	 */
	protected void setupValueChangeNotifications()
	{
	}

	/**
	 * Enable or disable this option.
	 * @param enabled true to enable this option, false to disable it.
	 * @see org.jppf.ui.options.Option#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		field.setEnabled(enabled);
		fieldLabel.setEnabled(enabled);
	}

	/**
	 * Get the pattern that defines the field format.
	 * @return the pattern as a string.
	 */
	public String getPattern()
	{
		return pattern;
	}

	/**
	 * Set the pattern that defines the field format.
	 * @param pattern the pattern as a string.
	 */
	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}
}
