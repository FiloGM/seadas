package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.general.GridBagConstraintsCustom;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/14/12
 * Time: 9:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genMainPanel {

    private JPanel jPanel;

    private L2genPrimaryIOFilesSelector primaryIOFilesSelector;
    private L2genParfilePanel parfilePanel;

    L2genMainPanel(L2genForm l2genForm, int tabIndex) {
        primaryIOFilesSelector = new L2genPrimaryIOFilesSelector(l2genForm.getL2genData());
        parfilePanel = new L2genParfilePanel(l2genForm, tabIndex);

        createJPanel();
    }


    private void createJPanel() {
        jPanel = new JPanel(new GridBagLayout());
        jPanel.add(primaryIOFilesSelector.getjPanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        jPanel.add(parfilePanel,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 3));
    }


    public L2genPrimaryIOFilesSelector getPrimaryIOFilesSelector() {
        return primaryIOFilesSelector;
    }

    public L2genParfilePanel getParfilePanel() {
        return parfilePanel;
    }

    public JPanel getjPanel() {
        return jPanel;
    }
}
