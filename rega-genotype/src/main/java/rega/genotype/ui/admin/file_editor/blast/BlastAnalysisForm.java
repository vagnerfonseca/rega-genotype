package rega.genotype.ui.admin.file_editor.blast;

import java.util.Arrays;
import java.util.List;

import rega.genotype.AlignmentAnalyses.Cluster;
import rega.genotype.BlastAnalysis;
import rega.genotype.SequenceAlignment;
import rega.genotype.ui.framework.widgets.FormTemplate;
import rega.genotype.ui.framework.widgets.ObjectListComboBox;
import rega.genotype.utils.Utils;
import eu.webtoolkit.jwt.WComboBox;
import eu.webtoolkit.jwt.WDoubleValidator;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WString;

/**
 * Edit analysis area of blast.xml
 * 
 * @author michael
 */
public class BlastAnalysisForm extends FormTemplate{
	private WLineEdit optionLE = new WLineEdit();
	private WComboBox sequenceTypeCB;
	private WLineEdit absCutOffLE = new WLineEdit();
	private WLineEdit absEValueLE = new WLineEdit();
	private WLineEdit relativeCutOffLE = new WLineEdit();
	private WLineEdit relativeEValueLE = new WLineEdit();

	private BlastAnalysis analysis;

	enum SequenceType {DNA, AA}

	public BlastAnalysisForm(BlastAnalysis analysis) {
		super(tr("admin.analisysis-template"));
		
		refresh(analysis);

		// validators
		absCutOffLE.setValidator(new WDoubleValidator());
		absEValueLE.setValidator(new WDoubleValidator());
		relativeCutOffLE.setValidator(new WDoubleValidator());
		relativeEValueLE.setValidator(new WDoubleValidator());

		// bind
		bindWidget("option", optionLE);
		bindWidget("sequence-type", sequenceTypeCB);
		bindWidget("absolute-cut-off", absCutOffLE);
		bindWidget("absolute-evalue", absEValueLE);
		bindWidget("relative-cut-off", relativeCutOffLE);
		bindWidget("relative-evalue", relativeEValueLE);

		init();
	}

	public void refresh(BlastAnalysis analysis) {
		this.analysis = analysis;

		List<SequenceType> list = Arrays.asList(SequenceType.values());
		sequenceTypeCB = new ObjectListComboBox<SequenceType>(list) {
			@Override
			protected WString render(SequenceType t) {
				if (t == SequenceType.DNA)
					return new WString("blastn (DNA)");
				else
					return new WString("blastx (AA)");
			}
		};

		// set values 
		optionLE.setText(Utils.nullToEmpty(analysis.getBlastOptions()));
		setValue(absCutOffLE, analysis.getAbsCutoff());
		setValue(absEValueLE, analysis.getAbsMaxEValue());
		setValue(relativeCutOffLE, analysis.getRelativeCutoff());
		setValue(relativeEValueLE, analysis.getRelativeMaxEValue());

		if (analysis.getOwner().getAlignment() != null)
			switch (analysis.getOwner().getAlignment().getSequenceType()) {
			case SequenceAlignment.SEQUENCE_ANY:
			case SequenceAlignment.SEQUENCE_DNA:
				sequenceTypeCB.setCurrentIndex(0);
				break;
			case SequenceAlignment.SEQUENCE_AA:
				sequenceTypeCB.setCurrentIndex(1);
			}
		else
			sequenceTypeCB.setCurrentIndex(0);
	}
	
	public boolean save() {
		if (validate()) {
			String identify = "";
			for(Cluster cluster: analysis.getOwner().getAllClusters()){
				if (!identify.isEmpty())
					identify += ",";
				identify += cluster.getId();
			}
			analysis.setId(identify);
			analysis.setBlastOptions(optionLE.getText());
			int type = sequenceTypeCB.getCurrentIndex() + 1;
			analysis.getOwner().getAlignment().setSequenceType(type);
			analysis.setAbsCutoff(doubleValue(absCutOffLE));
			analysis.setAbsMaxEValue(doubleValue(absEValueLE));
			analysis.setRelativeCutoff(doubleValue(relativeCutOffLE));
			analysis.setRelativeMaxEValue(doubleValue(relativeEValueLE));

			return true;
		} else 
			return false;
	}

	private Double doubleValue(WLineEdit le) {
		String txt = le.getText();
		if (txt == null || txt.isEmpty())
			return null;
		return txt == null ? null : Double.valueOf(txt);
	}
}