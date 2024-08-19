/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EcFormat;

import EcFormat.model.Line;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Source",
        id = "EcFormat.EcCommentFormat"
)
@ActionRegistration(
        displayName = "#CTL_EcCommentFormat"
)
@ActionReferences({
    @ActionReference(path = "Menu/Source", position = 50, separatorBefore = 0)
    ,
  @ActionReference(path = "Shortcuts", name = "OS-D")
})
@Messages("CTL_EcCommentFormat=Ec Format Comment")
public final class EcCommentFormat implements ActionListener {

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
                formatComment(editorComponent, document);
            } catch (Exception ex) {

            }
        });
    }

    private void formatComment(JTextComponent editorComponent, StyledDocument document) {
        try {
            int idxStartSelectedTxt = editorComponent.getSelectionStart();
            int idxEndSelectedTxt = editorComponent.getSelectionEnd();

            String wholeText = editorComponent.getText();

            String[] linesWholeText = wholeText.split("\r\n");
            int idxStart = 0;
            int idxEnd = 0;
            String textSelected = "";
            boolean hasStart = false;

            for (String lineWholeText : linesWholeText) {
                idxEnd += lineWholeText.length() + 1;
                if (!hasStart && idxStartSelectedTxt >= idxStart && idxStartSelectedTxt <= idxEnd) {
                    idxStartSelectedTxt = idxStart;
                    hasStart = true;
                }

                if (hasStart) {
                    textSelected += lineWholeText;
                }
                idxStart += lineWholeText.length() + 1;

                if (idxEndSelectedTxt < idxEnd) {
                    break;
                }

                if (hasStart) {
                    textSelected += "\r\n";
                }
            }

            boolean hasComment = false;
            int codeLength = 0;
            int idxCodeEnd = 0;
            int idxCommentStart = 0;

            List<Line> lines = new ArrayList<>();

            int idxStartText = idxStartSelectedTxt;
            String[] lineArr = textSelected.split("\r\n");

            for (String line : lineArr) {
                if (line.contains("//")) {
                    if (line.split("//")[0].trim().isEmpty()) {
                        idxStartText += line.length() + 1;
                        lines.add(new Line(false, idxCodeEnd, idxCommentStart, codeLength));
                        continue;
                    }

                    hasComment = true;
                    String codeOnly = line.split("//")[0].replaceAll("\\s+$", "");

                    codeLength = codeOnly.length();
                    idxCodeEnd = codeLength - 1 + idxStartText;
                    idxCommentStart = line.indexOf("//") + idxStartText;
                } else {
                    hasComment = false;
                    codeLength = line.replaceAll("\\s+$", "").length();
                    idxCodeEnd = 0;
                    idxCommentStart = 0;
                }

                idxStartText += line.length() + 1;

                lines.add(new Line(hasComment, idxCodeEnd, idxCommentStart, codeLength));
            }

            int maxLengthCode = lines.stream().filter(x -> x.isHasComment()).mapToInt(x -> x.getCodeLength()).max().getAsInt();
            int maxCommentLength = maxLengthCode + 8;

            for (int i = lines.size() - 1; i >= 0; i--) {
                if (lines.get(i).isHasComment()) {
                    int idxCodeEndf = lines.get(i).getIdxCodeEnd();
                    int idxCommentStartf = lines.get(i).getIdxCommentStart();
                    document.remove(idxCodeEndf + 1, idxCommentStartf - idxCodeEndf - 1);

                    int codeLengthf = lines.get(i).getCodeLength();
                    String space = "";
                    for (int j = 0; j < maxCommentLength - codeLengthf; j++) {
                        space += " ";
                    }
                    document.insertString(idxCodeEndf + 1, space, null);
                }
            }

        } catch (Exception ex) {

        }
    }
}
