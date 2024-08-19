/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EcFormat;

import EcFormat.model.Line;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Source",
        id = "EcFormat.EcFormat"
)
@ActionRegistration(
        displayName = "#CTL_EcFormat"
)
@ActionReferences({
    @ActionReference(path = "Menu/Source", position = 200, separatorBefore = 150, separatorAfter = 250)
    ,
  @ActionReference(path = "Shortcuts", name = "DOS-F")
})
@Messages("CTL_EcFormat=EC Format")
public final class EcFormat implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextComponent editorComponent = EditorRegistry.lastFocusedComponent();
        StyledDocument document = (StyledDocument) editorComponent.getDocument();
        AtomicLockDocument atomicLockDocument = LineDocumentUtils.asRequired(document, AtomicLockDocument.class);
        if (atomicLockDocument == null) {
            return;
        }
        atomicLockDocument.runAtomic(() -> {
            try {
                addBr(document);

                formatDefault(document);

                removeBr(document);

                switchComma(document);

                fixElseIf(document);

            } catch (Exception ex) {
                System.out.println(ex);
            }
        });
    }

    private void fixElseIf(StyledDocument document) throws Exception {
        String sbs = document.getText(0, document.getLength());
        List<Line> lines = Line.stringToLine(sbs);

        for (int i = lines.size() - 1; i >= 0; i--) {
            Line line = lines.get(i);
            if (line.getContent().trim().startsWith("} else")) {
                Line lineAbove = lines.get(i - 1);
                if (lineAbove.getContent().trim().startsWith("//")) {
                    document.remove(lineAbove.getIdxStartChar() - 4, 4);
                }
            }
        }
    }

    private void addBr(StyledDocument document) throws Exception {
        String sbs = document.getText(0, document.getLength());
        List<Line> lines = Line.stringToLine(sbs);

        for (int i = lines.size() - 1; i >= 0; i--) {
            Line line = lines.get(i);
            if (line.getContent().trim().startsWith("* ")) {
                document.insertString(line.getIdxEnd(), "</br>", null);
            }
        }
    }

    private void removeBr(StyledDocument document) throws Exception {
        String sbs = document.getText(0, document.getLength());
        while (sbs.contains("</br>")) {
            int idx = sbs.indexOf("</br>");
            document.remove(idx, 5);
            sbs = document.getText(0, document.getLength());
        }
    }

    private void switchComma(StyledDocument document) throws Exception {
        String sbs = document.getText(0, document.getLength());
        List<Line> lines = Line.stringToLine(sbs);
        for (int i = lines.size() - 1; i >= 0; i--) {
            Line line = lines.get(i);
            if (line.getContent().trim().isEmpty()) {
                continue;
            }
            if (sbs.charAt(line.getIdxEnd() - 1) == ',') {
                document.insertString(lines.get(i + 1).getIdxStartChar(), ", ", null);
                document.remove(line.getIdxEnd() - 1, 1);
            }
        }
    }

    private void formatDefault(StyledDocument document) throws Exception {
        Reformat reformat = Reformat.get(document);
        reformat.lock();
        try {
            reformat.reformat(0, document.getLength());
        } finally {
            reformat.unlock();
        }
    }
}
