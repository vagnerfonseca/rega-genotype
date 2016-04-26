package rega.genotype.ui.admin.file_editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rega.genotype.AbstractSequence;
import rega.genotype.AlignmentAnalyses;
import rega.genotype.AlignmentAnalyses.Cluster;
import rega.genotype.AlignmentAnalyses.Taxus;
import rega.genotype.config.ToolManifest;
import rega.genotype.config.Config.ToolConfig;
import rega.genotype.ui.framework.widgets.FormTemplate;
import rega.genotype.ui.framework.widgets.ObjectListComboBox;
import rega.genotype.utils.Settings;
import eu.webtoolkit.jwt.SelectionBehavior;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Signal.Listener;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WDialog.DialogCode;
import eu.webtoolkit.jwt.WFormWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WStandardItem;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WTableView;

/**
 * Editing of cluster from blast.xml file.
 * 
 * @author michael
 */
public class ClusterForm extends FormTemplate{

	private WLineEdit idLE = new WLineEdit();
	private WLineEdit nameLE = new WLineEdit();
	private WLineEdit descriptionLE = new WLineEdit();
	private WTableView taxuesTable = new WTableView();
	private WPushButton addSequenceB = new WPushButton("Add sequences");
	private WPushButton removeSequenceB = new WPushButton("Remove sequences");

	private List<AbstractSequence> addedSequences = new ArrayList<AbstractSequence>();
	private List<String> removedSequenceNames = new ArrayList<String>();

	private Signal1<WDialog.DialogCode> done = new Signal1<WDialog.DialogCode>();
	
	private Cluster cluster;

	public ClusterForm(final Cluster c, final AlignmentAnalyses alignmentAnalyses,
			ToolManifest manifest) {
		super(tr("admin.cluster-form"));
		this.cluster = c == null ? new Cluster() : c;

		WPushButton okB = new WPushButton("OK");
		WPushButton cancelB = new WPushButton("Cancel");
		
		// read 

		setValue(idLE, cluster.getId());
		setValue(nameLE, cluster.getName());
		setValue(descriptionLE, cluster.getDescription());

		Set<String> toolIds = new HashSet<String>();
		for(ToolConfig tool : Settings.getInstance().getConfig().getTools())
			toolIds.add(tool.getId()); 
		ArrayList<String> toolIdsList = new ArrayList<String>(toolIds);
		toolIdsList.add(0, "(Empty)");
		final ObjectListComboBox<String> toolIdCB = new ObjectListComboBox<String>(new ArrayList<String>(toolIds)) {
			@Override
			protected WString render(String t) {
				return new WString(t);
			}
		};
		if (cluster.getToolId() == null)
			if (manifest == null)
				toolIdCB.setCurrentIndex(0);
			else
				setValue(toolIdCB, manifest.getId());
		setValue(toolIdCB, cluster.getToolId());

		// taxus model

		final WStandardItemModel taxuesModel = new WStandardItemModel(0, 1);
		taxuesModel.setHeaderData(0, "Taxuses");
		for (Taxus t: cluster.getTaxa()) {
			taxuesModel.appendRow(new WStandardItem(t.getId()));
		}
		taxuesTable.setModel(taxuesModel);
		taxuesTable.setSelectionMode(SelectionMode.ExtendedSelection);
		taxuesTable.setSelectionBehavior(SelectionBehavior.SelectRows);
		taxuesTable.setHeight(new WLength(100));
		
		// bind

		bindWidget("id", idLE);
		bindWidget("name", nameLE);
		bindWidget("description", descriptionLE);
		bindWidget("tool-id", toolIdCB);
		bindWidget("fasta", taxuesTable);
		bindWidget("add-sequence", addSequenceB);
		bindWidget("remove-sequence", removeSequenceB);
		bindWidget("ok", okB);
		bindWidget("cancel", cancelB);

		initInfoFields();

		addSequenceB.clicked().addListener(addSequenceB, new Listener() {
			public void trigger() {
				final FastaFileEditorDialog d = new FastaFileEditorDialog(
						cluster, alignmentAnalyses);

				d.finished().addListener(d, new Signal1.Listener<WDialog.DialogCode>() {
					public void trigger(DialogCode arg) {
						if(arg == DialogCode.Accepted){
							addedSequences.addAll(d.getSelectedSequences());
							// add to taxuesModel
							for (AbstractSequence s: d.getSelectedSequences())
								taxuesModel.appendRow(new WStandardItem(s.getName()));
						}
					}
				});
			}
		});

		removeSequenceB.clicked().addListener(addSequenceB, new Listener() {
			public void trigger() {
				WModelIndex[] indexs = taxuesTable.getSelectedIndexes().toArray(
						new WModelIndex[taxuesTable.getSelectedIndexes().size()]);
				for (int i = indexs.length -1; i >= 0; --i){
					int row = indexs[i].getRow();
					removedSequenceNames.add(taxuesModel.getItem(row).getText().toString());
					taxuesModel.removeRow(row);
				}
			}
		});

		okB.clicked().addListener(okB, new Listener() {
			public void trigger() {
				cluster.setName(nameLE.getText());
				cluster.setDescription(descriptionLE.getText());
				cluster.setId(idLE.getText());
				cluster.setTooId(toolIdCB.getCurrentObject());

				for(AbstractSequence s: addedSequences){
					alignmentAnalyses.getAlignment().addSequence(s);
					cluster.addTaxus(s.getName());
				}

				for(String sequenceName: removedSequenceNames){
					alignmentAnalyses.getAlignment().removeSequence(sequenceName);
					cluster.removeTaxus(sequenceName);
				}
				done.trigger(DialogCode.Accepted);
			}
		});

		cancelB.clicked().addListener(cancelB, new Listener() {
			public void trigger() {
				done.trigger(DialogCode.Rejected);
			}				
		});
	}

	private void setValue(WFormWidget w, String value) {
		if (value != null)
			w.setValueText(value);
	}

	public Signal1<WDialog.DialogCode> done() {
		return done;
	}

	public Cluster getCluster() {
		return cluster;
	}
}